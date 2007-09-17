/***************************************************************************
 * Copyright (c) 2004 - 2007 Eike Stepper, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 *    Simon McDuff - EMF invalidation notifications,
 *                   IFeatureAnalyzer,
 *                   LoadRevisionCollectionChunk
 **************************************************************************/
package org.eclipse.emf.internal.cdo;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.cdo.CDOState;
import org.eclipse.emf.cdo.CDOView;
import org.eclipse.emf.cdo.CDOViewEvent;
import org.eclipse.emf.cdo.CDOViewResourcesEvent;
import org.eclipse.emf.cdo.analyzer.CDOFeatureAnalyzer;
import org.eclipse.emf.cdo.eresource.CDOResource;
import org.eclipse.emf.cdo.eresource.EresourceFactory;
import org.eclipse.emf.cdo.eresource.impl.CDOResourceImpl;
import org.eclipse.emf.cdo.internal.protocol.model.CDOClassImpl;
import org.eclipse.emf.cdo.internal.protocol.revision.CDOIDProvider;
import org.eclipse.emf.cdo.internal.protocol.revision.CDORevisionImpl;
import org.eclipse.emf.cdo.protocol.CDOID;
import org.eclipse.emf.cdo.protocol.CDOIDTyped;
import org.eclipse.emf.cdo.protocol.model.CDOClass;
import org.eclipse.emf.cdo.protocol.model.CDOClassRef;
import org.eclipse.emf.cdo.protocol.revision.CDORevisionResolver;
import org.eclipse.emf.cdo.protocol.util.TransportException;
import org.eclipse.emf.cdo.util.CDOUtil;
import org.eclipse.emf.cdo.util.ReadOnlyException;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.internal.cdo.bundle.OM;
import org.eclipse.emf.internal.cdo.protocol.ResourcePathRequest;
import org.eclipse.emf.internal.cdo.util.FSMUtil;
import org.eclipse.emf.internal.cdo.util.ModelUtil;
import org.eclipse.net4j.internal.util.om.trace.ContextTracer;
import org.eclipse.net4j.signal.IFailOverStrategy;
import org.eclipse.net4j.util.ImplementationError;

/**
 * @author Eike Stepper
 * @author Simon McDuff
 */
public class CDOViewImpl extends org.eclipse.net4j.internal.util.event.Notifier implements CDOView, CDOIDProvider,
    Adapter.Internal
{
  private static final ContextTracer TRACER = new ContextTracer(OM.DEBUG_VIEW, CDOViewImpl.class);

  private int viewID;

  private CDOSessionImpl session;

  private ResourceSet resourceSet;

  private boolean enableInvalidationNotifications;

  private int loadRevisionCollectionChunkSize;

  private CDOFeatureAnalyzer featureAnalyzer = CDOFeatureAnalyzer.NOOP;

  private Map<CDOID, InternalCDOObject> objects = new HashMap<CDOID, InternalCDOObject>();

  private CDOStore store = new CDOStore(this);

  private CDOID lastLookupID;

  private InternalCDOObject lastLookupObject;

  public CDOViewImpl(int id, CDOSessionImpl session)
  {
    this.viewID = id;
    this.session = session;
    enableInvalidationNotifications = OM.PREF_ENABLE_INVALIDATION_NOTIFICATIONS.getValue();
    loadRevisionCollectionChunkSize = OM.PREF_LOAD_REVISION_COLLECTION_CHUNK_SIZE.getValue();
  }

  public int getViewID()
  {
    return viewID;
  }

  public Type getViewType()
  {
    return Type.READONLY;
  }

  public ResourceSet getResourceSet()
  {
    return resourceSet;
  }

  public CDOSessionImpl getSession()
  {
    return session;
  }

  public CDOStore getStore()
  {
    return store;
  }

  public boolean isDirty()
  {
    return false;
  }

  public boolean isEnableInvalidationNotifications()
  {
    return enableInvalidationNotifications;
  }

  public void setEnableInvalidationNotifications(boolean on)
  {
    enableInvalidationNotifications = on;
  }

  public int getLoadRevisionCollectionChunkSize()
  {
    return loadRevisionCollectionChunkSize;
  }

  public void setLoadRevisionCollectionChunkSize(int loadRevisionCollectionChunkSize)
  {
    this.loadRevisionCollectionChunkSize = loadRevisionCollectionChunkSize;
  }

  public CDOFeatureAnalyzer getFeatureAnalyzer()
  {
    return featureAnalyzer;
  }

  public void setFeatureAnalyzer(CDOFeatureAnalyzer featureAnalyzer)
  {
    this.featureAnalyzer = featureAnalyzer == null ? CDOFeatureAnalyzer.NOOP : featureAnalyzer;
  }

  public CDOTransactionImpl toTransaction()
  {
    if (this instanceof CDOTransactionImpl)
    {
      return (CDOTransactionImpl)this;
    }

    throw new ReadOnlyException("CDO view is read only: " + this);
  }

  public CDOResource createResource(String path)
  {
    URI createURI = CDOUtil.createResourceURI(path);
    return (CDOResource)getResourceSet().createResource(createURI);
  }

  public CDOResource getResource(String path)
  {
    URI uri = CDOUtil.createResourceURI(path);
    return (CDOResource)getResourceSet().getResource(uri, true);
  }

  public CDOResourceImpl getResource(CDOID resourceID)
  {
    if (resourceID == null || resourceID == CDOID.NULL)
    {
      throw new IllegalArgumentException("resourceID == null || resourceID == CDOID.NULL");
    }

    ResourceSet resourceSet = getResourceSet();
    EList<Resource> resources = resourceSet.getResources();
    for (Resource resource : resources)
    {
      if (resource instanceof CDOResourceImpl)
      {
        CDOResourceImpl cdoResource = (CDOResourceImpl)resource;
        if (resourceID.equals(cdoResource.cdoID()))
        {
          return cdoResource;
        }
      }
    }

    try
    {
      IFailOverStrategy failOverStrategy = session.getFailOverStrategy();
      ResourcePathRequest request = new ResourcePathRequest(session.getChannel(), resourceID);
      String path = failOverStrategy.send(request);

      CDOResourceImpl resource = (CDOResourceImpl)EresourceFactory.eINSTANCE.createCDOResource();
      resource.setPath(path);

      InternalCDOObject resourceObject = resource;
      resourceObject.cdoInternalSetID(resourceID);
      resourceObject.cdoInternalSetView(this);
      resourceObject.cdoInternalSetResource(resource);
      resourceObject.cdoInternalSetState(CDOState.PROXY);

      resourceSet.getResources().add(resource);
      return resource;
    }
    catch (RuntimeException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new TransportException(ex);
    }
  }

  public InternalCDOObject newInstance(EClass eClass)
  {
    EObject eObject = EcoreUtil.create(eClass);
    return FSMUtil.adapt(eObject, this);
  }

  public InternalCDOObject newInstance(CDOClass cdoClass)
  {
    EClass eClass = ModelUtil.getEClass((CDOClassImpl)cdoClass, session.getPackageRegistry());
    if (eClass == null)
    {
      throw new IllegalStateException("No EClass for " + cdoClass);
    }

    return newInstance(eClass);
  }

  public CDORevisionImpl getRevision(CDOID id)
  {
    CDORevisionResolver revisionManager = session.getRevisionManager();
    return (CDORevisionImpl)revisionManager.getRevision(id, session.getReferenceChunkSize());
  }

  public InternalCDOObject getObject(CDOID id)
  {
    return getObject(id, false);
  }

  public InternalCDOObject getObject(CDOID id, boolean loadOnDemand)
  {
    if (id.equals(lastLookupID))
    {
      return lastLookupObject;
    }

    lastLookupID = id;
    lastLookupObject = objects.get(id);
    if (lastLookupObject == null)
    {
      if (id.isMeta())
      {
        lastLookupObject = createMetaObject(id);
      }
      else
      {
        if (loadOnDemand)
        {
          lastLookupObject = createObject(id);
        }
        else
        {
          lastLookupObject = createProxy(id);
        }
      }

      registerObject(lastLookupObject);
    }

    return lastLookupObject;
  }

  public boolean isObjectRegistered(CDOID id)
  {
    return objects.containsKey(id);
  }

  /**
   * @return Never <code>null</code>
   */
  private InternalCDOObject createMetaObject(CDOID id)
  {
    if (TRACER.isEnabled())
    {
      TRACER.trace("Creating meta object for " + id);
    }

    InternalEObject metaInstance = session.lookupMetaInstance(id);
    if (metaInstance == null)
    {
      throw new ImplementationError("No metaInstance for " + id);
    }

    return new CDOMetaImpl(this, metaInstance, id);
  }

  /**
   * @return Never <code>null</code>
   */
  private InternalCDOObject createObject(CDOID id)
  {
    if (TRACER.isEnabled())
    {
      TRACER.trace("Creating object for " + id);
    }

    CDORevisionImpl revision = getRevision(id);
    CDOClassImpl cdoClass = revision.getCDOClass();
    InternalCDOObject object = newInstance(cdoClass);
    if (object instanceof CDOResourceImpl)
    {
      object.cdoInternalSetResource((CDOResourceImpl)object);
    }
    else
    {
      CDOID resourceID = revision.getResourceID();
      if (!resourceID.isNull())
      {
        CDOResourceImpl resource = getResource(resourceID);
        object.cdoInternalSetResource(resource);
      }
    }

    object.cdoInternalSetView(this);
    object.cdoInternalSetRevision(revision);
    object.cdoInternalSetID(revision.getID());
    object.cdoInternalSetState(CDOState.CLEAN);
    object.cdoInternalPostLoad();
    return object;
  }

  private InternalCDOObject createProxy(CDOID id)
  {
    if (TRACER.isEnabled())
    {
      TRACER.format("Creating proxy for " + id);
    }

    CDOClassImpl cdoClass = getObjectType(id);
    InternalCDOObject object = newInstance(cdoClass);
    if (object instanceof CDOResourceImpl)
    {
      object.cdoInternalSetResource((CDOResourceImpl)object);
    }

    object.cdoInternalSetView(this);
    object.cdoInternalSetID(id);
    object.cdoInternalSetState(CDOState.PROXY);
    return object;
  }

  private CDOClassImpl getObjectType(CDOID id)
  {
    CDOClassImpl type = session.getObjectType(id);
    if (type != null)
    {
      return type;
    }

    if (id instanceof CDOIDTyped)
    {
      CDOIDTyped typed = (CDOIDTyped)id;
      CDOClassRef typeRef = typed.getType();
      type = (CDOClassImpl)typeRef.resolve(session.getPackageManager());
      session.registerObjectType(id, type);
      return type;
    }

    return session.requestObjectType(id);
  }

  public CDOID provideCDOID(Object idOrObject)
  {
    Object shouldBeCDOID = convertObjectToID(idOrObject);
    if (shouldBeCDOID instanceof CDOID)
    {
      CDOID id = (CDOID)shouldBeCDOID;
      if (TRACER.isEnabled() && id != idOrObject)
      {
        TRACER.format("Converted dangling reference: {0} --> {1}", idOrObject, id);
      }

      return id;
    }

    throw new ImplementationError("Unable to provideCDOID: " + idOrObject.getClass().getName());
  }

  public Object convertObjectToID(Object potentialObject)
  {
    if (potentialObject instanceof CDOID)
    {
      return potentialObject;
    }

    if (potentialObject == null)
    {
      throw new ImplementationError();
    }

    if (potentialObject instanceof InternalEObject && !(potentialObject instanceof InternalCDOObject))
    {
      InternalEObject eObject = (InternalEObject)potentialObject;
      InternalCDOObject adapter = FSMUtil.adapt(eObject, this);
      if (adapter != null)
      {
        potentialObject = adapter;
      }
    }

    if (potentialObject instanceof InternalCDOObject)
    {
      InternalCDOObject object = (InternalCDOObject)potentialObject;
      if (object.cdoView() == this)
      {
        return object.cdoID();
      }
    }

    if (TRACER.isEnabled())
    {
      TRACER.format("Dangling reference: {0}", potentialObject);
    }

    return potentialObject;
  }

  public Object convertIDToObject(Object potentialID)
  {
    if (potentialID instanceof CDOID)
    {
      if (potentialID == CDOID.NULL)
      {
        return null;
      }

      CDOID id = (CDOID)potentialID;
      InternalCDOObject result = getObject(id, true);
      if (result == null)
      {
        throw new ImplementationError("ID not registered: " + id);
      }

      return result.cdoInternalInstance();
    }

    return potentialID;
  }

  public void registerObject(InternalCDOObject object)
  {
    if (TRACER.isEnabled())
    {
      TRACER.format("Registering {0}", object);
    }

    InternalCDOObject old = objects.put(object.cdoID(), object);
    if (old != null)
    {
      throw new IllegalStateException("Duplicate ID: " + object);
    }
  }

  public void deregisterObject(InternalCDOObject object)
  {
    if (TRACER.isEnabled())
    {
      TRACER.format("Deregistering {0}", object);
    }

    InternalCDOObject old = objects.remove(object.cdoID());
    if (old == null)
    {
      throw new IllegalStateException("Unknown ID: " + object);
    }
  }

  public void remapObject(CDOID oldID)
  {
    InternalCDOObject object = objects.remove(oldID);
    CDOID newID = object.cdoID();
    objects.put(newID, object);
    if (TRACER.isEnabled())
    {
      TRACER.format("Remapping {0} --> {1}", oldID, newID);
    }
  }

  // public final class HistoryEntryImpl implements HistoryEntry, Comparable
  // {
  // private String resourcePath;
  //
  // private HistoryEntryImpl(String resourcePath)
  // {
  // this.resourcePath = resourcePath;
  // }
  //
  // public CDOView getView()
  // {
  // return CDOViewImpl.this;
  // }
  //
  // public String getResourcePath()
  // {
  // return resourcePath;
  // }
  //
  // public int compareTo(Object o)
  // {
  // HistoryEntry that = (HistoryEntry)o;
  // return resourcePath.compareTo(that.getResourcePath());
  // }
  //
  // @Override
  // public String toString()
  // {
  // return resourcePath;
  // }
  // }

  /**
   * Turns registered objects into proxies and synchronously delivers invalidation events to registered event listeners.
   * <p>
   * <b>Implementation note:</b> This implementation guarantees that exceptions from listener code don't propagate up
   * to the caller of this method. Runtime exceptions from the implementation of the {@link CDOStateMachine} are
   * propagated to the caller of this method but this should not happen in the absence of implementation errors.
   * 
   * @param timeStamp
   *          The point in time when the newly committed revision have been created.
   * @param dirtyOIDs
   *          A set of the object IDs to be invalidated. <b>Implementation note:</b> This implementation expects the
   *          dirtyOIDs set to be unmodifiable. It does not wrap the set (again).
   */
  public void notifyInvalidation(long timeStamp, Set<CDOID> dirtyOIDs)
  {
    List<InternalCDOObject> dirtyObjects = enableInvalidationNotifications ? new ArrayList<InternalCDOObject>() : null;
    for (CDOID dirtyOID : dirtyOIDs)
    {
      InternalCDOObject dirtyObject = objects.get(dirtyOID);
      if (dirtyObject != null)
      {
        CDOStateMachine.INSTANCE.invalidate(dirtyObject, timeStamp);
        if (dirtyObjects != null && dirtyObject.eNotificationRequired())
        {
          dirtyObjects.add(dirtyObject);
        }
      }
    }

    if (dirtyObjects != null)
    {
      for (InternalCDOObject dirtyObject : dirtyObjects)
      {
        CDOInvalidationNotificationImpl notification = new CDOInvalidationNotificationImpl(dirtyObject);
        dirtyObject.eNotify(notification);
      }
    }
  }

  public void close()
  {
    session.viewDetached(this);
  }

  @Override
  public String toString()
  {
    return MessageFormat.format("CDOView({0})", viewID);
  }

  public boolean isAdapterForType(Object type)
  {
    return type instanceof ResourceSet;
  }

  public Notifier getTarget()
  {
    return resourceSet;
  }

  public void setTarget(Notifier newTarget)
  {
    ResourceSet resourceSet = (ResourceSet)newTarget;
    if (TRACER.isEnabled())
    {
      TRACER.trace("Attaching CDO view to " + resourceSet);
    }

    this.resourceSet = resourceSet;
    for (Resource resource : resourceSet.getResources())
    {
      if (resource instanceof CDOResourceImpl)
      {
        CDOResourceImpl cdoResource = (CDOResourceImpl)resource;
        notifyAdd(cdoResource);
      }
    }
  }

  public void unsetTarget(Notifier oldTarget)
  {
    ResourceSet resourceSet = (ResourceSet)oldTarget;
    for (Resource resource : resourceSet.getResources())
    {
      if (resource instanceof CDOResourceImpl)
      {
        CDOResourceImpl cdoResource = (CDOResourceImpl)resource;
        notifyRemove(cdoResource);
      }
    }

    if (TRACER.isEnabled())
    {
      TRACER.trace("Detaching CDO view from " + resourceSet);
    }

    if (resourceSet == oldTarget)
    {
      setTarget(null);
    }
  }

  public void notifyChanged(Notification msg)
  {
    try
    {
      switch (msg.getEventType())
      {
      case Notification.ADD:
        notifyAdd(msg);
        break;

      case Notification.ADD_MANY:
        notifyAddMany(msg);
        break;

      case Notification.REMOVE:
        notifyRemove(msg);
        break;

      case Notification.REMOVE_MANY:
        notifyRemoveMany(msg);
        break;
      }
    }
    catch (RuntimeException ex)
    {
      OM.LOG.error(ex);
    }
  }

  private void notifyAdd(Notification msg)
  {
    if (msg.getNewValue() instanceof CDOResourceImpl)
    {
      notifyAdd((CDOResourceImpl)msg.getNewValue());
    }
  }

  private void notifyAddMany(Notification msg)
  {
    EList<Resource> newResources = (EList<Resource>)msg.getNewValue();
    EList<Resource> oldResources = (EList<Resource>)msg.getOldValue();
    for (Resource newResource : newResources)
    {
      if (newResource instanceof CDOResourceImpl)
      {
        if (!oldResources.contains(newResource))
        {
          notifyAdd((CDOResourceImpl)newResource);
        }
      }
    }
  }

  private void notifyAdd(CDOResourceImpl cdoResource)
  {
    try
    {
      CDOStateMachine.INSTANCE.attach(cdoResource, cdoResource, this);
      fireEvent(new ResourcesEvent(cdoResource.getPath(), ResourcesEvent.Kind.ADDED));
    }
    catch (RuntimeException ex)
    {
      try
      {
        ((InternalCDOObject)cdoResource).cdoInternalSetState(CDOState.NEW);
        resourceSet.getResources().remove(cdoResource);
      }
      catch (RuntimeException ignore)
      {
      }

      throw ex;
    }
  }

  private void notifyRemove(Notification msg)
  {
    if (msg.getOldValue() instanceof CDOResourceImpl)
    {
      notifyRemove((CDOResourceImpl)msg.getOldValue());
    }
  }

  private void notifyRemoveMany(Notification msg)
  {
    EList<Resource> newResources = (EList<Resource>)msg.getNewValue();
    EList<Resource> oldResources = (EList<Resource>)msg.getOldValue();
    for (Resource oldResource : oldResources)
    {
      if (oldResource instanceof CDOResourceImpl)
      {
        if (!newResources.contains(oldResource))
        {
          // TODO Optimize event notification with IContainerEvent
          notifyRemove((CDOResourceImpl)oldResource);
        }
      }
    }
  }

  private void notifyRemove(CDOResourceImpl cdoResource)
  {
    CDOStateMachine.INSTANCE.detach(cdoResource, cdoResource, this);
    fireEvent(new ResourcesEvent(cdoResource.getPath(), ResourcesEvent.Kind.REMOVED));
  }

  protected void checkWritable()
  {
    if (getViewType() != Type.TRANSACTION)
    {
      throw new IllegalStateException("CDO view is read only");
    }
  }

  /**
   * @author Eike Stepper
   */
  protected abstract class Event extends org.eclipse.net4j.internal.util.event.Event implements CDOViewEvent
  {
    private static final long serialVersionUID = 1L;

    public Event()
    {
      super(CDOViewImpl.this);
    }

    public CDOViewImpl getView()
    {
      return CDOViewImpl.this;
    }
  }

  /**
   * @author Eike Stepper
   */
  private final class ResourcesEvent extends Event implements CDOViewResourcesEvent
  {
    private static final long serialVersionUID = 1L;

    private String resourcePath;

    private Kind kind;

    public ResourcesEvent(String resourcePath, Kind kind)
    {
      this.resourcePath = resourcePath;
      this.kind = kind;
    }

    public String getResourcePath()
    {
      return resourcePath;
    }

    public Kind getKind()
    {
      return kind;
    }

    @Override
    public String toString()
    {
      return MessageFormat.format("CDOViewResourcesEvent[{0}, {1}, {2}]", getView(), resourcePath, kind);
    }
  }
}

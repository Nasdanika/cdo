/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.internal.admin;

import org.eclipse.emf.cdo.common.admin.CDOAdmin;
import org.eclipse.emf.cdo.common.admin.CDOAdminRepository;
import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.common.id.CDOID.ObjectType;
import org.eclipse.emf.cdo.common.id.CDOIDUtil;
import org.eclipse.emf.cdo.common.util.RepositoryStateChangedEvent;
import org.eclipse.emf.cdo.common.util.RepositoryTypeChangedEvent;

import org.eclipse.net4j.util.event.Notifier;
import org.eclipse.net4j.util.io.ExtendedDataInputStream;
import org.eclipse.net4j.util.om.monitor.NotifyingMonitor;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Eike Stepper
 */
public class CDOAdminClientRepository extends Notifier implements CDOAdminRepository
{
  private CDOAdminClientImpl admin;

  private String name;

  private String uuid;

  private Type type;

  private State state;

  private String storeType;

  private Set<ObjectType> objectIDTypes;

  private long creationTime;

  private CDOID rootResourceID;

  private boolean supportingAudits;

  private boolean supportingBranches;

  private boolean supportingEcore;

  private boolean ensuringReferentialIntegrity;

  private IDGenerationLocation idGenerationLocation;

  public CDOAdminClientRepository(CDOAdminClientImpl admin, ExtendedDataInputStream in) throws IOException
  {
    this.admin = admin;
    name = in.readString();
    uuid = in.readString();
    type = in.readEnum(Type.class);
    state = in.readEnum(State.class);
    storeType = in.readString();

    Set<CDOID.ObjectType> objectIDTypes = new HashSet<ObjectType>();
    int types = in.readInt();
    for (int i = 0; i < types; i++)
    {
      CDOID.ObjectType objectIDType = in.readEnum(CDOID.ObjectType.class);
      objectIDTypes.add(objectIDType);
    }

    creationTime = in.readLong();
    rootResourceID = CDOIDUtil.read(in);
    supportingAudits = in.readBoolean();
    supportingBranches = in.readBoolean();
    supportingEcore = in.readBoolean();
    ensuringReferentialIntegrity = in.readBoolean();
    idGenerationLocation = in.readEnum(IDGenerationLocation.class);
  }

  public CDOAdmin getAdmin()
  {
    return admin;
  }

  public String getName()
  {
    return name;
  }

  public String getUUID()
  {
    return uuid;
  }

  public Type getType()
  {
    return type;
  }

  public State getState()
  {
    return state;
  }

  public String getStoreType()
  {
    return storeType;
  }

  public Set<ObjectType> getObjectIDTypes()
  {
    return objectIDTypes;
  }

  public long getCreationTime()
  {
    return creationTime;
  }

  public CDOID getRootResourceID()
  {
    return rootResourceID;
  }

  public boolean isSupportingAudits()
  {
    return supportingAudits;
  }

  public boolean isSupportingBranches()
  {
    return supportingBranches;
  }

  public boolean isSupportingEcore()
  {
    return supportingEcore;
  }

  public boolean isEnsuringReferentialIntegrity()
  {
    return ensuringReferentialIntegrity;
  }

  public IDGenerationLocation getIDGenerationLocation()
  {
    return idGenerationLocation;
  }

  public long getTimeStamp() throws UnsupportedOperationException
  {
    throw new UnsupportedOperationException();
  }

  public boolean delete(String type)
  {
    return admin.deleteRepository(this, type);
  }

  @Override
  public String toString()
  {
    return name;
  }

  public void typeChanged(Type oldType, Type newType)
  {
    type = newType;
    fireEvent(new RepositoryTypeChangedEvent(this, oldType, newType));
  }

  public void stateChanged(State oldState, State newState)
  {
    state = newState;
    fireEvent(new RepositoryStateChangedEvent(this, oldState, newState));
  }

  public void replicationProgressed(double totalWork, double work)
  {
    fireEvent(new NotifyingMonitor.ProgressEvent(this, totalWork, work));
  }
}

/**
 * Copyright (c) 2004 - 2010 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Simon McDuff - initial API and implementation
 *    Eike Stepper - maintenance
 */
package org.eclipse.emf.cdo.internal.common.revision.delta;

import org.eclipse.emf.cdo.common.protocol.CDODataInput;
import org.eclipse.emf.cdo.common.protocol.CDODataOutput;
import org.eclipse.emf.cdo.common.revision.CDOReferenceAdjuster;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.delta.CDOFeatureDelta;
import org.eclipse.emf.cdo.common.revision.delta.CDOFeatureDeltaVisitor;
import org.eclipse.emf.cdo.common.revision.delta.CDOListFeatureDelta;
import org.eclipse.emf.cdo.common.revision.delta.CDORemoveFeatureDelta;

import org.eclipse.net4j.util.ObjectUtil;
import org.eclipse.net4j.util.collection.Pair;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.FeatureMapUtil;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Simon McDuff
 */
public class CDOListFeatureDeltaImpl extends CDOFeatureDeltaImpl implements CDOListFeatureDelta
{
  private List<CDOFeatureDelta> featureDeltas = new ArrayList<CDOFeatureDelta>();

  private transient int[] cachedIndices;

  private transient ListTargetAdding[] cachedSources;

  private transient List<CDOFeatureDelta> unprocessedFeatureDeltas;

  public CDOListFeatureDeltaImpl(EStructuralFeature feature)
  {
    super(feature);
  }

  public CDOListFeatureDeltaImpl(CDODataInput in, EClass eClass) throws IOException
  {
    super(in, eClass);
    int size = in.readInt();
    for (int i = 0; i < size; i++)
    {
      featureDeltas.add(in.readCDOFeatureDelta(eClass));
    }
  }

  public CDOListFeatureDelta copy()
  {
    CDOListFeatureDeltaImpl result = new CDOListFeatureDeltaImpl(getFeature());

    Map<CDOFeatureDelta, CDOFeatureDelta> map = null;
    if (cachedSources != null || unprocessedFeatureDeltas != null)
    {
      map = new HashMap<CDOFeatureDelta, CDOFeatureDelta>();
    }

    for (CDOFeatureDelta delta : featureDeltas)
    {
      CDOFeatureDelta newDelta = delta.copy();
      result.featureDeltas.add(newDelta);
      if (map != null)
      {
        map.put(delta, newDelta);
      }
    }

    if (cachedIndices != null)
    {
      result.cachedIndices = copyOf(cachedIndices, cachedIndices.length);
    }

    if (cachedSources != null)
    {
      int length = cachedSources.length;
      result.cachedSources = new ListTargetAdding[length];
      for (int i = 0; i < length; i++)
      {
        ListTargetAdding oldElement = cachedSources[i];
        CDOFeatureDelta newElement = map.get(oldElement);
        if (newElement instanceof ListTargetAdding)
        {
          result.cachedSources[i] = (ListTargetAdding)newElement;
        }
      }
    }

    if (unprocessedFeatureDeltas != null)
    {
      int size = unprocessedFeatureDeltas.size();
      result.unprocessedFeatureDeltas = new ArrayList<CDOFeatureDelta>(size);
      for (CDOFeatureDelta oldDelta : unprocessedFeatureDeltas)
      {
        CDOFeatureDelta newDelta = map.get(oldDelta);
        if (newDelta != null)
        {
          result.unprocessedFeatureDeltas.add(newDelta);
        }
      }
    }

    return result;
  }

  @Override
  public void write(CDODataOutput out, EClass eClass) throws IOException
  {
    super.write(out, eClass);
    out.writeInt(featureDeltas.size());
    for (CDOFeatureDelta featureDelta : featureDeltas)
    {
      out.writeCDOFeatureDelta(eClass, featureDelta);
    }
  }

  public Type getType()
  {
    return Type.LIST;
  }

  public List<CDOFeatureDelta> getListChanges()
  {
    return featureDeltas;
  }

  /**
   * Returns the number of indices as the first element of the array.
   * 
   * @return never <code>null</code>.
   */
  public Pair<ListTargetAdding[], int[]> reconstructAddedIndices()
  {
    reconstructAddedIndicesWithNoCopy();
    return new Pair<ListTargetAdding[], int[]>(copyOf(cachedSources, cachedSources.length, cachedSources.getClass()),
        copyOf(cachedIndices, cachedIndices.length));
  }

  private void reconstructAddedIndicesWithNoCopy()
  {
    if (cachedIndices == null || unprocessedFeatureDeltas != null)
    {
      if (cachedIndices == null)
      {
        cachedIndices = new int[1 + featureDeltas.size()];
      }
      else if (cachedIndices.length <= 1 + featureDeltas.size())
      {
        int newCapacity = Math.max(10, cachedIndices.length * 3 / 2 + 1);
        int[] newElements = new int[newCapacity];
        System.arraycopy(cachedIndices, 0, newElements, 0, cachedIndices.length);
        cachedIndices = newElements;
      }

      if (cachedSources == null)
      {
        cachedSources = new ListTargetAdding[1 + featureDeltas.size()];
      }
      else if (cachedSources.length <= 1 + featureDeltas.size())
      {
        int newCapacity = Math.max(10, cachedSources.length * 3 / 2 + 1);
        ListTargetAdding[] newElements = new ListTargetAdding[newCapacity];
        System.arraycopy(cachedSources, 0, newElements, 0, cachedSources.length);
        cachedSources = newElements;
      }

      List<CDOFeatureDelta> featureDeltasToBeProcess = unprocessedFeatureDeltas == null ? featureDeltas
          : unprocessedFeatureDeltas;

      for (CDOFeatureDelta featureDelta : featureDeltasToBeProcess)
      {
        if (featureDelta instanceof ListIndexAffecting)
        {
          ListIndexAffecting affecting = (ListIndexAffecting)featureDelta;
          affecting.affectIndices(cachedSources, cachedIndices);
        }

        if (featureDelta instanceof ListTargetAdding)
        {
          cachedIndices[++cachedIndices[0]] = ((ListTargetAdding)featureDelta).getIndex();
          cachedSources[cachedIndices[0]] = (ListTargetAdding)featureDelta;
        }
      }

      unprocessedFeatureDeltas = null;
    }
  }

  private void cleanupWithNewDelta(CDOFeatureDelta featureDelta)
  {
    EStructuralFeature feature = getFeature();
    if ((feature instanceof EReference || FeatureMapUtil.isFeatureMap(feature))
        && featureDelta instanceof CDORemoveFeatureDelta)
    {
      int indexToRemove = ((CDORemoveFeatureDelta)featureDelta).getIndex();
      reconstructAddedIndicesWithNoCopy();

      for (int i = 1; i <= cachedIndices[0]; i++)
      {
        int index = cachedIndices[i];
        if (indexToRemove == index)
        {
          cachedSources[i].clear();
          break;
        }
      }
    }

    if (cachedIndices != null)
    {
      if (unprocessedFeatureDeltas == null)
      {
        unprocessedFeatureDeltas = new ArrayList<CDOFeatureDelta>();
      }

      unprocessedFeatureDeltas.add(featureDelta);
    }
  }

  public void add(CDOFeatureDelta featureDelta)
  {
    cleanupWithNewDelta(featureDelta);
    featureDeltas.add(featureDelta);
  }

  public void apply(CDORevision revision)
  {
    for (CDOFeatureDelta featureDelta : featureDeltas)
    {
      ((CDOFeatureDeltaImpl)featureDelta).apply(revision);
    }
  }

  @Override
  public void adjustReferences(CDOReferenceAdjuster adjuster)
  {
    for (CDOFeatureDelta featureDelta : featureDeltas)
    {
      ((CDOFeatureDeltaImpl)featureDelta).adjustReferences(adjuster);
    }
  }

  public void accept(CDOFeatureDeltaVisitor visitor)
  {
    visitor.visit(this);
  }

  @Override
  public int hashCode()
  {
    return super.hashCode() ^ ObjectUtil.hashCode(featureDeltas);
  }

  @Override
  public boolean equals(Object obj)
  {
    if (!super.equals(obj))
    {
      return false;
    }

    CDOListFeatureDelta that = (CDOListFeatureDelta)obj;
    return ObjectUtil.equals(featureDeltas, that.getListChanges());
  }

  @Override
  protected String toStringAdditional()
  {
    return "list=" + featureDeltas; //$NON-NLS-1$
  }

  /**
   * Copied from JAVA 1.6 {@link Arrays Arrays.copyOf}.
   */
  private static <T, U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType)
  {
    @SuppressWarnings("unchecked")
    T[] copy = (Object)newType == (Object)Object[].class ? (T[])new Object[newLength] : (T[])Array.newInstance(newType
        .getComponentType(), newLength);
    System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
    return copy;
  }

  /**
   * Copied from JAVA 1.6 {@link Arrays Arrays.copyOf}.
   */
  private static int[] copyOf(int[] original, int newLength)
  {
    int[] copy = new int[newLength];
    System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));
    return copy;
  }
}

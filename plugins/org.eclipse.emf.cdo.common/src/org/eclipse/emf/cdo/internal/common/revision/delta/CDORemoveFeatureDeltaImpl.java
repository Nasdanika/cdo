/***************************************************************************
 * Copyright (c) 2004 - 2008 Eike Stepper, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Simon McDuff - initial API and implementation
 *    Eike Stepper - maintenance
 **************************************************************************/
package org.eclipse.emf.cdo.internal.common.revision.delta;

import org.eclipse.emf.cdo.common.io.CDODataInput;
import org.eclipse.emf.cdo.common.io.CDODataOutput;
import org.eclipse.emf.cdo.common.model.CDOClass;
import org.eclipse.emf.cdo.common.model.CDOFeature;
import org.eclipse.emf.cdo.common.revision.CDOReferenceAdjuster;
import org.eclipse.emf.cdo.common.revision.CDORevision;
import org.eclipse.emf.cdo.common.revision.delta.CDOFeatureDeltaVisitor;
import org.eclipse.emf.cdo.common.revision.delta.CDORemoveFeatureDelta;
import org.eclipse.emf.cdo.spi.common.revision.InternalCDORevision;

import java.io.IOException;

/**
 * @author Simon McDuff
 */
public class CDORemoveFeatureDeltaImpl extends CDOFeatureDeltaImpl implements CDORemoveFeatureDelta,
    IListIndexAffecting
{
  private int index;

  public CDORemoveFeatureDeltaImpl(CDOFeature feature, int index)
  {
    super(feature);
    this.index = index;
  }

  public CDORemoveFeatureDeltaImpl(CDODataInput in, CDOClass cdoClass) throws IOException
  {
    super(in, cdoClass);
    index = in.readInt();
  }

  @Override
  public void write(CDODataOutput out, CDOClass cdoClass) throws IOException
  {
    super.write(out, cdoClass);
    out.writeInt(index);
  }

  public int getIndex()
  {
    return index;
  }

  public Type getType()
  {
    return Type.REMOVE;
  }

  public void apply(CDORevision revision)
  {
    ((InternalCDORevision)revision).getList(getFeature()).remove(index);
  }

  public void accept(CDOFeatureDeltaVisitor visitor)
  {
    visitor.visit(this);
  }

  public void affectIndices(IListTargetAdding sources[], int[] indices)
  {
    int index = getIndex();
    for (int i = 1; i <= indices[0]; i++)
    {
      if (indices[i] > index)
      {
        --indices[i];
      }
      else if (indices[i] == index)
      {
        int rest = indices[0]-- - i;
        if (rest > 0)
        {
          System.arraycopy(indices, i + 1, indices, i, rest);
          System.arraycopy(sources, i + 1, sources, i, rest);
          --i;
        }
      }
    }
  }

  @Override
  public void adjustReferences(CDOReferenceAdjuster idMappings)
  {
    // do Nothing
  }
}

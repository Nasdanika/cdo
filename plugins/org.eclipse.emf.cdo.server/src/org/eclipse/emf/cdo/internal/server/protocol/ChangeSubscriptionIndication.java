/**
 * Copyright (c) 2004 - 2009 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Simon McDuff - initial API and implementation
 *    Eike Stepper - maintenance
 */
package org.eclipse.emf.cdo.internal.server.protocol;

import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.common.io.CDODataInput;
import org.eclipse.emf.cdo.common.io.CDODataOutput;
import org.eclipse.emf.cdo.common.protocol.CDOProtocolConstants;
import org.eclipse.emf.cdo.internal.server.View;
import org.eclipse.emf.cdo.internal.server.bundle.OM;

import org.eclipse.net4j.util.om.trace.ContextTracer;

import java.io.IOException;

/**
 * @author Simon McDuff
 */
public class ChangeSubscriptionIndication extends CDOReadIndication
{
  private static final ContextTracer TRACER = new ContextTracer(OM.DEBUG_PROTOCOL, ChangeSubscriptionIndication.class);

  public ChangeSubscriptionIndication(CDOServerProtocol protocol)
  {
    super(protocol, CDOProtocolConstants.SIGNAL_CHANGE_SUBSCRIPTION);
  }

  @Override
  protected void indicating(CDODataInput in) throws IOException
  {
    boolean subscribeMode = true;

    int viewID = in.readInt();
    boolean clear = in.readBoolean();
    int size = in.readInt();
    if (size <= 0)
    {
      subscribeMode = false;
      size = -size;
    }

    View view = (View)getSession().getView(viewID);
    if (clear)
    {
      if (TRACER.isEnabled())
      {
        TRACER.trace("Clear subscription"); //$NON-NLS-1$
      }

      view.clearChangeSubscription();
    }

    for (int i = 0; i < size; i++)
    {
      CDOID id = in.readCDOID();
      if (subscribeMode)
      {
        view.subscribe(id);
      }
      else
      {
        view.unsubscribe(id);
      }
    }
  }

  @Override
  protected void responding(CDODataOutput out) throws IOException
  {
    out.writeBoolean(true);
  }
}

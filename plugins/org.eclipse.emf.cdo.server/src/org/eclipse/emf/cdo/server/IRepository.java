/***************************************************************************
 * Copyright (c) 2004-2007 Eike Stepper, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 **************************************************************************/
package org.eclipse.emf.cdo.server;

import org.eclipse.emf.cdo.protocol.CDOID;
import org.eclipse.emf.cdo.protocol.model.CDOClassRef;
import org.eclipse.emf.cdo.protocol.model.CDOPackageManager;

/**
 * @author Eike Stepper
 */
public interface IRepository
{
  public String getName();

  public IStore getStore();

  public String getUUID();

  public CDOPackageManager getPackageManager();

  public ISessionManager getSessionManager();

  public IResourceManager getResourceManager();

  public IRevisionManager getRevisionManager();

  public CDOClassRef getObjectType(CDOID id);
}

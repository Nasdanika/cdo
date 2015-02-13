/*
 * Copyright (c) 2009-2015 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.explorer.ui.handlers;

import org.eclipse.emf.cdo.explorer.repositories.CDORepository;
import org.eclipse.emf.cdo.explorer.ui.repositories.CDORepositoriesView;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Eike Stepper
 */
public class RepositoryConnectHandler extends AbstractRepositoryHandler
{
  public RepositoryConnectHandler()
  {
    super(null, false);
  }

  @Override
  protected void doExecute(ExecutionEvent event, IProgressMonitor monitor) throws Exception
  {
    CDORepositoriesView view = null;

    IWorkbenchPart part = HandlerUtil.getActivePart(event);
    if (part instanceof CDORepositoriesView)
    {
      view = (CDORepositoriesView)part;
    }

    for (CDORepository repository : elements)
    {
      if (view != null)
      {
        view.connectRepository(repository);
      }
      else
      {
        repository.connect();
      }
    }
  }
}

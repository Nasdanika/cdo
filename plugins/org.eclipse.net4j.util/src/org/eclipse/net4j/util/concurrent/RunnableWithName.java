/*
 * Copyright (c) 2015, 2016 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.net4j.util.concurrent;

import org.eclipse.net4j.util.StringUtil;
import org.eclipse.net4j.util.om.OMPlatform;

/**
 * @author Eike Stepper
 * @since 3.6
 */
public abstract class RunnableWithName implements Runnable
{
  private static final boolean DISABLE_RUNNABLE_NAMES = OMPlatform.INSTANCE.isProperty("org.eclipse.net4j.util.concurrent.DISABLE_RUNNABLE_NAMES");

  public final void run()
  {
    if (DISABLE_RUNNABLE_NAMES)
    {
      doRun();
      return;
    }

    Thread thread = null;
    String oldName = null;

    String name = getName();
    if (!StringUtil.isEmpty(name))
    {
      thread = Thread.currentThread();
      oldName = thread.getName();
      if (name.equals(oldName))
      {
        thread = null;
        oldName = null;
      }
      else
      {
        thread.setName(name);
      }
    }

    try
    {
      doRun();
    }
    finally
    {
      if (thread != null)
      {
        thread.setName(oldName);
      }
    }
  }

  public abstract String getName();

  protected abstract void doRun();
}

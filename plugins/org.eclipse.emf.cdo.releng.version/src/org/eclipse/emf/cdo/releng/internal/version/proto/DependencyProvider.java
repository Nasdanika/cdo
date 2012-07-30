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
package org.eclipse.emf.cdo.releng.internal.version.proto;

/**
 * @author Eike Stepper
 */
public abstract class DependencyProvider
{
  private BuildContext buildContext;

  public DependencyProvider()
  {
  }

  public BuildContext getBuildContext()
  {
    return buildContext;
  }

  public void setBuildContext(BuildContext buildContext)
  {
    this.buildContext = buildContext;
  }

  public abstract boolean dependenciesMayHaveChanged();
}
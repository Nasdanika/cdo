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
package org.eclipse.emf.cdo.explorer.ui.checkouts;

import org.eclipse.emf.cdo.explorer.checkouts.CDOCheckout;
import org.eclipse.emf.cdo.explorer.ui.BaseHandler;

import org.eclipse.jface.viewers.ISelection;

/**
 * @author Eike Stepper
 */
public abstract class CheckoutHandler extends BaseHandler<CDOCheckout>
{
  private final Boolean open;

  public CheckoutHandler(Boolean multi, Boolean open)
  {
    super(CDOCheckout.class, multi);
    this.open = open;
  }

  @Override
  protected boolean updateSelection(ISelection selection)
  {
    boolean result = super.updateSelection(selection);

    if (result && open != null)
    {
      for (CDOCheckout checkout : elements)
      {
        if (open != checkout.isOpen())
        {
          return false;
        }
      }
    }

    return result;
  }
}

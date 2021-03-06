/*
 * Copyright (c) 2008-2013, 2015 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.tests.mango;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc --> The <b>Factory</b> for the model. It provides a create method for each non-abstract class of
 * the model. <!-- end-user-doc -->
 * @see org.eclipse.emf.cdo.tests.mango.MangoPackage
 * @generated
 */
public interface MangoFactory extends EFactory
{
  /**
   * The singleton instance of the factory.
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * @generated
   */
  MangoFactory eINSTANCE = org.eclipse.emf.cdo.tests.mango.impl.MangoFactoryImpl.init();

  /**
   * Returns a new object of class '<em>Value List</em>'.
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * @return a new object of class '<em>Value List</em>'.
   * @generated
   */
  MangoValueList createMangoValueList();

  /**
   * Returns a new object of class '<em>Value</em>'.
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * @return a new object of class '<em>Value</em>'.
   * @generated
   */
  MangoValue createMangoValue();

  /**
   * Returns a new object of class '<em>Parameter</em>'.
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * @return a new object of class '<em>Parameter</em>'.
   * @generated
   */
  MangoParameter createMangoParameter();

  /**
   * Returns the package supported by this factory.
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * @return the package supported by this factory.
   * @generated
   */
  MangoPackage getMangoPackage();

} // MangoFactory

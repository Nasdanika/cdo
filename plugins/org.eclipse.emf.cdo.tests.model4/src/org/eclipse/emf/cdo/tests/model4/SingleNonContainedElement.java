/*
 * Copyright (c) 2008, 2009, 2011-2013, 2015 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.tests.model4;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Single Non Contained Element</b></em>'. <!--
 * end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.emf.cdo.tests.model4.SingleNonContainedElement#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.emf.cdo.tests.model4.SingleNonContainedElement#getParent <em>Parent</em>}</li>
 * </ul>
 *
 * @see org.eclipse.emf.cdo.tests.model4.model4Package#getSingleNonContainedElement()
 * @model
 * @generated
 */
public interface SingleNonContainedElement extends EObject
{
  /**
   * Returns the value of the '<em><b>Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Name</em>' attribute isn't clear, there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Name</em>' attribute.
   * @see #setName(String)
   * @see org.eclipse.emf.cdo.tests.model4.model4Package#getSingleNonContainedElement_Name()
   * @model
   * @generated
   */
  String getName();

  /**
   * Sets the value of the '{@link org.eclipse.emf.cdo.tests.model4.SingleNonContainedElement#getName <em>Name</em>}' attribute.
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * @param value the new value of the '<em>Name</em>' attribute.
   * @see #getName()
   * @generated
   */
  void setName(String value);

  /**
   * Returns the value of the '<em><b>Parent</b></em>' reference.
   * It is bidirectional and its opposite is '{@link org.eclipse.emf.cdo.tests.model4.RefSingleNonContained#getElement <em>Element</em>}'.
   * <!-- begin-user-doc
   * -->
   * <p>
   * If the meaning of the '<em>Parent</em>' reference isn't clear, there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Parent</em>' reference.
   * @see #setParent(RefSingleNonContained)
   * @see org.eclipse.emf.cdo.tests.model4.model4Package#getSingleNonContainedElement_Parent()
   * @see org.eclipse.emf.cdo.tests.model4.RefSingleNonContained#getElement
   * @model opposite="element"
   * @generated
   */
  RefSingleNonContained getParent();

  /**
   * Sets the value of the '{@link org.eclipse.emf.cdo.tests.model4.SingleNonContainedElement#getParent <em>Parent</em>}' reference.
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * @param value the new value of the '<em>Parent</em>' reference.
   * @see #getParent()
   * @generated
   */
  void setParent(RefSingleNonContained value);

} // SingleNonContainedElement

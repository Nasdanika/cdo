/*
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.eclipse.emf.cdo.releng.doc.article;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Body</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are supported:
 * <ul>
 * <li>{@link org.eclipse.emf.cdo.releng.doc.article.Body#getCategory <em>Category</em>}</li>
 * </ul>
 * </p>
 * 
 * @see org.eclipse.emf.cdo.releng.doc.article.ArticlePackage#getBody()
 * @model abstract="true"
 * @generated
 */
public interface Body extends StructuralElement, BodyElementContainer
{
  /**
   * Returns the value of the '<em><b>Category</b></em>' reference. <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Category</em>' reference isn't clear, there really should be more of a description
   * here...
   * </p>
   * <!-- end-user-doc -->
   * 
   * @return the value of the '<em>Category</em>' reference.
   * @see org.eclipse.emf.cdo.releng.doc.article.ArticlePackage#getBody_Category()
   * @model resolveProxies="false" transient="true" changeable="false" volatile="true" derived="true"
   * @generated
   */
  Category getCategory();

} // Body

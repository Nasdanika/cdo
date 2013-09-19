/**
 */
package org.eclipse.emf.cdo.expressions.impl;

import org.eclipse.emf.cdo.expressions.EvaluationContext;
import org.eclipse.emf.cdo.expressions.ExpressionsPackage;
import org.eclipse.emf.cdo.expressions.StaticAccess;

import org.eclipse.net4j.util.WrappedException;

import org.eclipse.emf.ecore.EClass;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Static Access</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * </p>
 *
 * @generated
 */
public class StaticAccessImpl extends AccessImpl implements StaticAccess
{
  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected StaticAccessImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected EClass eStaticClass()
  {
    return ExpressionsPackage.Literals.STATIC_ACCESS;
  }

  @Override
  protected Object evaluate(EvaluationContext context, String name)
  {
    try
    {
      int fieldStart = name.lastIndexOf('.');
      if (fieldStart == -1)
      {
        throw new IllegalArgumentException("Field name missing: " + name);
      }

      String className = name.substring(0, fieldStart);
      Class<?> c = context.getClass(className);

      String fieldName = name.substring(fieldStart + 1);
      Field field = c.getField(fieldName);
      if (!Modifier.isStatic(field.getModifiers()))
      {
        throw new IllegalStateException("Field is not static: " + name);
      }

      return field.get(null);
    }
    catch (Exception ex)
    {
      throw WrappedException.wrap(ex);
    }
  }

} // StaticAccessImpl

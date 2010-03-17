/**
 * Copyright (c) 2004 - 2010 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Simon McDuff - initial API and implementation
 *    Ibrahim Sallam - code refactoring for CDO 3.0
 */
package org.eclipse.emf.cdo.server.internal.objectivity.schema;

import org.eclipse.emf.cdo.server.internal.objectivity.bundle.OM;
import org.eclipse.emf.cdo.server.internal.objectivity.db.ObjySchema;

import org.eclipse.net4j.util.om.trace.ContextTracer;

/**
 * @author Simon McDuff
 */
public class ooArrayListLong extends ooArrayList<Long>
{
  private static final ContextTracer TRACER_DEBUG = new ContextTracer(OM.DEBUG, ooArrayListLong.class);

  static public String ArrayName = "oo_ooArrayListLong";

  public static void buildSchema()
  {
    d_Module top_mod = ObjySchema.getTopModule();
    if (top_mod.resolve_class(ooArrayListLong.ArrayName) == null)
    {

      if (TRACER_DEBUG.isEnabled())
      {
        TRACER_DEBUG.trace("Schema not found for ooArrayListId. Adding ooArrayListId");
      }

      boolean inProcess = top_mod.proposed_classes().hasNext();

      // Proposed_Class A = new Proposed_Class(ooArrayListLong.ArrayName);
      Proposed_Class A = top_mod.propose_new_class(ooArrayListLong.ArrayName);

      A.add_base_class(com.objy.as.app.d_Module.LAST, com.objy.as.app.d_Access_Kind.d_PUBLIC, "ooObj");

      A.add_basic_attribute(com.objy.as.app.d_Module.LAST, d_Access_Kind.d_PUBLIC, // Access kind
          ooArrayList.sizeName, // Attribute name
          1, // # elements in fixed-size array
          ooBaseType.ooINT32 // Type of numeric data
          ); // Default value

      A.add_varray_attribute(com.objy.as.app.d_Module.LAST, d_Access_Kind.d_PUBLIC, // Access kind
          ooArrayList.arrayName, // Attribute name
          1, ooBaseType.ooINT64);

      // top_mod.propose_new_class(A);

      if (!inProcess)
      {
        top_mod.activate_proposals(true, true);
      }

      if (TRACER_DEBUG.isEnabled())
      {
        TRACER_DEBUG.trace("SCHEMA changed : ooArrayListId added");
      }
    }
  }

  public ooArrayListLong(Class_Object classObject)
  {
    super(classObject);
  }

  @Override
  protected void setValue(long index, Long newValue)
  {
    getVArray().set_numeric(index, new Numeric_Value(newValue.longValue()));
  }

  @Override
  protected Long getValue(long index)
  {
    return getVArray().get_numeric(index).longValue();
  }

}

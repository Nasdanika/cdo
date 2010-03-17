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
 * EMF Classes in Objectivity are enhanced with this base class. This class is use for the revision data as a base for
 * other classes created.
 * 
 * @author ibrahim
 */
public class ooBase
{
  private static final ContextTracer TRACER_DEBUG = new ContextTracer(OM.DEBUG, ooBase.class);

  static public String ClassName = "oo_ooBase";

  static public String Attribute_containerId = "oo_containerId";

  static public String Attribute_containerFeatureId = "oo_containerFeatureId";

  static public String Attribute_resourceId = "oo_resourceId";

  static public String Attribute_version = "oo_version";

  public static final String Attribute_revisedTime = "oo_revisedTime";

  public static final String Attribute_creationTime = "oo_creationTime";

  public static final String Attribute_revisions = "oo_revisions";

  public static final String Attribute_base = "oo_base";

  public static final String Attribute_lastRevision = "oo_lastRevision";

  public static void buildSchema()
  {
    d_Module top_mod = ObjySchema.getTopModule();
    if (top_mod.resolve_class(ooBase.ClassName) == null && top_mod.resolve_proposed_class(ooBase.ClassName) == null)
    {
      if (TRACER_DEBUG.isEnabled())
      {
        TRACER_DEBUG.trace("Schema not found for ooBase. Adding it.");
      }

      boolean inProcess = top_mod.proposed_classes().hasNext();

      Proposed_Class propClass = top_mod.propose_new_class(ooBase.ClassName);

      propClass.add_base_class(com.objy.as.app.d_Module.LAST, com.objy.as.app.d_Access_Kind.d_PUBLIC, "ooObj");

      propClass.add_bidirectional_relationship(d_Module.LAST, d_Access_Kind.d_PUBLIC, ooBase.Attribute_revisions,
          ooBase.ClassName, false, false, true, Rel_Copy.DELETE, Rel_Versioning.COPY,
          Rel_Propagation.LOCK_YES_DELETE_YES, ooBase.Attribute_base, false);

      // propClass.add_bidirectional_relationship(position, visibility,
      // name, destinationClassName, isInline, isShort, isToMany,
      // copyMode, versioning, propagation, inverseName, inverseIsToMany)

      propClass.add_bidirectional_relationship(d_Module.LAST, d_Access_Kind.d_PUBLIC, ooBase.Attribute_base,
          ooBase.ClassName, false, false, false, Rel_Copy.DELETE, Rel_Versioning.COPY,
          Rel_Propagation.LOCK_YES_DELETE_YES, ooBase.Attribute_revisions, true);

      propClass.add_unidirectional_relationship(d_Module.LAST, d_Access_Kind.d_PUBLIC, ooBase.Attribute_lastRevision,
          ooBase.ClassName, true, false, false, Rel_Copy.DELETE, Rel_Versioning.COPY,
          Rel_Propagation.LOCK_YES_DELETE_YES);

      propClass.add_basic_attribute(com.objy.as.app.d_Module.LAST, d_Access_Kind.d_PUBLIC, // Access kind
          ooBase.Attribute_containerFeatureId, // Attribute name
          1, // # elements in fixed-size array
          ooBaseType.ooINT32 // Type of numeric data
          ); // Default value

      propClass.add_ref_attribute(com.objy.as.app.d_Module.LAST, // Access kind
          d_Access_Kind.d_PUBLIC, // Access kind
          ooBase.Attribute_containerId, // Attribute name
          1, // # elements in fixed-size array
          "ooObj", false); // Default value // Default value

      propClass.add_ref_attribute(com.objy.as.app.d_Module.LAST, // Access kind
          d_Access_Kind.d_PUBLIC, // Access kind
          ooBase.Attribute_resourceId, // Attribute name
          1, // # elements in fixed-size array
          "ooObj", false); // Default value // Default value

      propClass.add_basic_attribute(com.objy.as.app.d_Module.LAST, d_Access_Kind.d_PUBLIC, // Access kind
          ooBase.Attribute_version, // Attribute name
          1, // # elements in fixed-size array
          ooBaseType.ooINT32 // Type of numeric data
          ); // Default value

      propClass.add_basic_attribute(com.objy.as.app.d_Module.LAST, d_Access_Kind.d_PUBLIC, // Access kind
          ooBase.Attribute_creationTime, // Attribute name
          1, // # elements in fixed-size array
          ooBaseType.ooINT64 // Type of numeric data
          ); // Default value

      propClass.add_basic_attribute(com.objy.as.app.d_Module.LAST, d_Access_Kind.d_PUBLIC, // Access kind
          ooBase.Attribute_revisedTime, // Attribute name
          1, // # elements in fixed-size array
          ooBaseType.ooINT64 // Type of numeric data
          ); // Default value

      if (!inProcess)
      {
        top_mod.activate_proposals(true, true);
      }

      if (TRACER_DEBUG.isEnabled())
      {
        TRACER_DEBUG.trace("SCHEMA changed : ooBaseClass added");
      }
    }

  }

}

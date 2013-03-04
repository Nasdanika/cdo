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
package org.eclipse.net4j.internal.db.ddl;

import org.eclipse.net4j.db.DBType;
import org.eclipse.net4j.db.ddl.IDBField;
import org.eclipse.net4j.db.ddl.IDBSchema;
import org.eclipse.net4j.spi.db.DBSchemaElement;

/**
 * @author Eike Stepper
 */
public class DBField extends DBSchemaElement implements IDBField
{
  public static final int DEFAULT_DECIMAL_PRECISION = 5;

  public static final int DEFAULT_SCALE = 0;

  public static final int DEFAULT_CHAR_LENGTH = 1;

  public static final int DEFAULT_VARCHAR_LENGTH = 255;

  private static final long serialVersionUID = 1L;

  private DBTable table;

  private DBType type;

  private int precision;

  private int scale;

  private boolean notNull;

  private int position;

  public DBField(DBTable table, String name, DBType type, int precision, int scale, boolean notNull, int position)
  {
    super(name);
    this.table = table;
    this.type = type;
    this.precision = precision;
    this.scale = scale;
    this.notNull = notNull;
    this.position = position;
  }

  /**
   * Constructor for deserialization.
   */
  protected DBField()
  {
  }

  public IDBSchema getSchema()
  {
    return table.getSchema();
  }

  public DBTable getTable()
  {
    return table;
  }

  public DBType getType()
  {
    return type;
  }

  public void setType(DBType type)
  {
    table.getSchema().assertUnlocked();
    this.type = type;
  }

  public int getPrecision()
  {
    if (precision == DEFAULT)
    {
      switch (type)
      {
      case CHAR:
        return DEFAULT_CHAR_LENGTH;

      case VARCHAR:
      case VARBINARY:
        return DEFAULT_VARCHAR_LENGTH;

      case DECIMAL:
      case NUMERIC:
        return DEFAULT_DECIMAL_PRECISION;
      }
    }

    return precision;
  }

  public void setPrecision(int precision)
  {
    table.getSchema().assertUnlocked();
    this.precision = precision;
  }

  public int getScale()
  {
    if (scale == DEFAULT)
    {
      return DEFAULT_SCALE;
    }

    return scale;
  }

  public void setScale(int scale)
  {
    table.getSchema().assertUnlocked();
    this.scale = scale;
  }

  public boolean isNotNull()
  {
    return notNull;
  }

  public void setNotNull(boolean notNull)
  {
    table.getSchema().assertUnlocked();
    this.notNull = notNull;
  }

  public int getPosition()
  {
    return position;
  }

  public void setPosition(int position)
  {
    table.getSchema().assertUnlocked();
    this.position = position;
  }

  public String getFullName()
  {
    return table.getName() + "." + getName(); //$NON-NLS-1$
  }

  public void remove()
  {
    table.removeField(this);
  }

  public String formatPrecision()
  {
    int precision = getPrecision();
    if (precision > 0)
    {
      return "(" + precision + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    return ""; //$NON-NLS-1$
  }

  public String formatPrecisionAndScale()
  {
    if (scale == DEFAULT)
    {
      return "(" + getPrecision() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    return "(" + getPrecision() + ", " + scale + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}

/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the program BioNetView.
 *
 * Copyright (C) 2013-2016 by the University of California, San Diego.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package edu.ucsd.sbrg.escher.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Andreas Dr&auml;ger
 */
public abstract class AbstractEscherBase implements EscherBase {

  /**
   *
   */
  private Map<String, Object> userObjects;


  /**
   *
   */
  public AbstractEscherBase() {
    super();
    userObjects = new HashMap<String, Object>();
  }


  /**
   * @param base
   */
  public AbstractEscherBase(AbstractEscherBase base) {
    this();
  }


  /* (non-Javadoc)
   * @see java.lang.Object#clone()
   */
  @Override
  public abstract AbstractEscherBase clone();


  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    AbstractEscherBase other = (AbstractEscherBase) obj;
    if (userObjects == null) {
      if (other.userObjects != null) {
        return false;
      }
    } else if (!userObjects.equals(other.userObjects)) {
      return false;
    }
    return true;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.EscherBase#getUserObject(java.lang.String)
   */
  @Override
  public Object getUserObject(String key) {
    return userObjects.get(key);
  }


  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result + ((userObjects == null) ? 0 : userObjects.hashCode());
    return result;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.EscherBase#putUserObject(java.lang.String, T)
   */
  @Override
  public <T> Object putUserObject(String key, T value) {
    return userObjects.put(key, value);
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.EscherBase#userObjectEntrySet()
   */
  @Override
  public Set<Map.Entry<String, Object>> userObjectEntrySet() {
    return userObjects.entrySet();
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.EscherBase#userObjectKeys()
   */
  @Override
  public Set<String> userObjectKeys() {
    return userObjects.keySet();
  }
}

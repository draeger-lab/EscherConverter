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

import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Andreas Dr&auml;ger
 *
 */
public interface EscherBase extends Cloneable {

  /**
   * 
   * @param key
   * @param value
   * @return
   */
  public abstract <T> Object putUserObject(String key, T value);

  /**
   * 
   * @param key
   * @return
   */
  public abstract Object getUserObject(String key);

  /**
   * 
   * @return
   */
  public abstract Set<String> userObjectKeys();

  /**
   * 
   * @return
   */
  public abstract Set<Map.Entry<String, Object>> userObjectEntrySet();

}
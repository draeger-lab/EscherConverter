/* ---------------------------------------------------------------------
 * This file is part of the program EscherConverter.
 *
 * Copyright (C) 2013-2017 by the University of California, San Diego.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package edu.ucsd.sbrg.escher.model;

import java.util.Map;
import java.util.Set;

/**
 * @author Andreas Dr&auml;ger
 */
public interface EscherBase extends Cloneable {

  /**
   * @param key
   * @param value
   * @return
   */
  <T> Object putUserObject(String key, T value);

  /**
   * @param key
   * @return
   */
  Object getUserObject(String key);

  /**
   * @return
   */
  Set<String> userObjectKeys();

  /**
   * @return
   */
  Set<Map.Entry<String, Object>> userObjectEntrySet();
}

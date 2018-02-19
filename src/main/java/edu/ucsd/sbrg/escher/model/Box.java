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

/**
 * @author Andreas Dr&auml;ger
 */
public interface Box extends EscherBase, Position {

  /**
   * @param height the height to set
   */
  void setHeight(Double height);

  /**
   * @return the height
   */
  Double getHeight();

  /**
   * @param width the width to set
   */
  void setWidth(Double width);

  /**
   * @return the width
   */
  Double getWidth();

  /**
   * @return {@code true} if the requested property is not {@code null}.
   */
  boolean isSetHeight();

  /**
   * @return {@code true} if the requested property is not {@code null}.
   */
  boolean isSetWidth();

}

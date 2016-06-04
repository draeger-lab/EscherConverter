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

/**
 * 
 * @author Andreas Dr&auml;ger
 *
 */
public class Point extends AbstractPosition {

  /**
   * 
   */
  public Point() {
    super();
  }

  /**
   * 
   * @param x
   * @param y
   */
  public Point(Double x, Double y) {
    super(x, y);
  }

  /**
   * 
   * @param point
   */
  public Point(Point point) {
    this();
    setX(point.getX());
    setY(point.getY());
  }

  /**
   * 
   * @param p
   */
  public Point add(Point p) {
    setX(getX() + p.getX());
    setY(getY() + p.getY());
    return this;
  }

  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.AbstractEscherBase#clone()
   */
  @Override
  public Point clone() {
    return new Point(this);
  }

  /**
   * 
   * @param scale
   */
  public Point scale(double scale) {
    setX(scale * getX());
    setY(scale * getY());
    return this;
  }

}

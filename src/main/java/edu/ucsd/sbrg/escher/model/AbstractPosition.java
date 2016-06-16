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
package edu.ucsd.sbrg.escher.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Andreas Dr&auml;ger
 */
public abstract class AbstractPosition extends AbstractEscherBase
    implements Position {

  /**
   *
   */
  protected Double x;
  /**
   *
   */
  protected Double y;


  /**
   *
   */
  public AbstractPosition() {
    super();
    x = y = null;
  }


  /**
   * @param position
   */
  public AbstractPosition(AbstractPosition position) {
    super(position);
    if (position.isSetX()) {
      setX(position.getX().doubleValue());
    }
    if (position.isSetY()) {
      setY(position.getY().doubleValue());
    }
  }


  /**
   * @param x
   * @param y
   */
  public AbstractPosition(Double x, Double y) {
    this();
    setX(x);
    setY(y);
  }


  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    AbstractPosition other = (AbstractPosition) obj;
    if (x == null) {
      if (other.x != null) {
        return false;
      }
    } else if (!x.equals(other.x)) {
      return false;
    }
    if (y == null) {
      if (other.y != null) {
        return false;
      }
    } else if (!y.equals(other.y)) {
      return false;
    }
    return true;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.Position#getX()
   */
  @Override
  @JsonProperty("x")
  public Double getX() {
    return x;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.Position#getY()
   */
  @Override
  @JsonProperty("y")
  public Double getY() {
    return y;
  }


  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((x == null) ? 0 : x.hashCode());
    result = prime * result + ((y == null) ? 0 : y.hashCode());
    return result;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.Position#isSetX()
   */
  @Override
  public boolean isSetX() {
    return x != null;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.Position#isSetY()
   */
  @Override
  public boolean isSetY() {
    return y != null;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.Position#setX(java.lang.Double)
   */
  @Override
  @JsonProperty("x")
  public void setX(Double x) {
    this.x = x;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.Position#setY(java.lang.Double)
   */
  @Override
  @JsonProperty("y")
  public void setY(Double y) {
    this.y = y;
  }


  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append(" [x=");
    builder.append(x);
    builder.append(", y=");
    builder.append(y);
    builder.append("]");
    return builder.toString();
  }
}

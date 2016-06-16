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
public abstract class AbstractBox extends AbstractPosition implements Box {

  /**
   *
   */
  protected Double height;
  /**
   *
   */
  protected Double width;


  /**
   *
   */
  public AbstractBox() {
    super();
    x = y = width = height = null;
  }


  /**
   * @param box
   */
  public AbstractBox(AbstractBox box) {
    super(box);
    if (box.isSetHeight()) {
      setHeight(box.getHeight().doubleValue());
    }
    if (box.isSetWidth()) {
      setWidth(box.getWidth().doubleValue());
    }
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
    AbstractBox other = (AbstractBox) obj;
    if (height == null) {
      if (other.height != null) {
        return false;
      }
    } else if (!height.equals(other.height)) {
      return false;
    }
    if (width == null) {
      if (other.width != null) {
        return false;
      }
    } else if (!width.equals(other.width)) {
      return false;
    }
    return true;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.Box#getHeight()
   */
  @Override
  @JsonProperty("height")
  public Double getHeight() {
    return height;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.Box#getWidth()
   */
  @Override
  @JsonProperty("width")
  public Double getWidth() {
    return width;
  }


  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((height == null) ? 0 : height.hashCode());
    result = prime * result + ((width == null) ? 0 : width.hashCode());
    return result;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.Box#isSetHeight()
   */
  @Override
  public boolean isSetHeight() {
    return height != null;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.Box#isSetWidth()
   */
  @Override
  public boolean isSetWidth() {
    return width != null;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.Box#setHeight(java.lang.Double)
   */
  @Override
  @JsonProperty("height")
  public void setHeight(Double height) {
    this.height = height;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.Box#setWidth(java.lang.Double)
   */
  @Override
  @JsonProperty("width")
  public void setWidth(Double width) {
    this.width = width;
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
    builder.append(", width=");
    builder.append(width);
    builder.append(", height=");
    builder.append(height);
    builder.append("]");
    return builder.toString();
  }
}

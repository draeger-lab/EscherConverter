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

import edu.ucsd.sbrg.escher.models.interfaces.Element;

/**
 * @author Andreas Dr&auml;ger
 */
public class Segment extends AbstractEscherBase implements Element {

  /**
   *
   */
  private Point  b1;
  /**
   *
   */
  private Point  b2;
  /**
   *
   */
  private String fromNodeId;
  /**
   *
   */
  private String id;
  /**
   *
   */
  private String toNodeId;


  /**
   *
   */
  public Segment() {
    id = fromNodeId = toNodeId = null;
    b1 = b2 = null;
  }


  /**
   * @param segment
   */
  public Segment(Segment segment) {
    super(segment);
    if (segment.isSetBasePoint1()) {
      setBasePoint1(segment.getBasePoint1().clone());
    }
    if (segment.isSetBasePoint2()) {
      setBasePoint2(segment.getBasePoint2().clone());
    }
    if (segment.isSetFromNodeId()) {
      setFromNodeId(segment.getFromNodeId());
    }
    if (segment.isSetId()) {
      setId(segment.getId());
    }
    if (segment.isSetToNodeId()) {
      setToNodeId(segment.getToNodeId());
    }
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.AbstractEscherBase#clone()
   */
  @Override
  public Segment clone() {
    return new Segment(this);
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
    Segment other = (Segment) obj;
    if (b1 == null) {
      if (other.b1 != null) {
        return false;
      }
    } else if (!b1.equals(other.b1)) {
      return false;
    }
    if (b2 == null) {
      if (other.b2 != null) {
        return false;
      }
    } else if (!b2.equals(other.b2)) {
      return false;
    }
    if (fromNodeId == null) {
      if (other.fromNodeId != null) {
        return false;
      }
    } else if (!fromNodeId.equals(other.fromNodeId)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (toNodeId == null) {
      if (other.toNodeId != null) {
        return false;
      }
    } else if (!toNodeId.equals(other.toNodeId)) {
      return false;
    }
    return true;
  }


  /**
   * @return the b1
   */
  public Point getBasePoint1() {
    return b1;
  }


  /**
   * @return the b2
   */
  public Point getBasePoint2() {
    return b2;
  }


  /**
   * @return the fromNodeId
   */
  public String getFromNodeId() {
    return fromNodeId;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.interfaces.Element#getId()
   */
  @Override
  public String getId() {
    return id;
  }


  /**
   * @return the toNodeId
   */
  public String getToNodeId() {
    return toNodeId;
  }


  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((b1 == null) ? 0 : b1.hashCode());
    result = prime * result + ((b2 == null) ? 0 : b2.hashCode());
    result =
        prime * result + ((fromNodeId == null) ? 0 : fromNodeId.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((toNodeId == null) ? 0 : toNodeId.hashCode());
    return result;
  }


  /**
   * @return {@code true} if the requested property is not {@code null}.
   */
  public boolean isSetBasePoint1() {
    return b1 != null;
  }


  /**
   * @return {@code true} if the requested property is not {@code null}.
   */
  public boolean isSetBasePoint2() {
    return b2 != null;
  }


  /**
   * @return {@code true} if the requested property is not {@code null}.
   */
  public boolean isSetFromNodeId() {
    return fromNodeId != null;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.interfaces.Element#isSetId()
   */
  @Override
  public boolean isSetId() {
    return id != null;
  }


  /**
   * @return {@code true} if the requested property is not {@code null}.
   */
  public boolean isSetToNodeId() {
    return toNodeId != null;
  }


  /**
   * @return
   */
  public Point removeBasePoint1() {
    Point point = getBasePoint1();
    setBasePoint1(null);
    return point;
  }


  /**
   * @return
   */
  public Point removeBasePoint2() {
    Point point = getBasePoint2();
    setBasePoint2(null);
    return point;
  }


  /**
   * @param b1 the b1 to set
   */
  public void setBasePoint1(Point b1) {
    this.b1 = b1;
  }


  /**
   * @param b2 the b2 to set
   */
  public void setBasePoint2(Point b2) {
    this.b2 = b2;
  }


  /**
   * @param fromNodeId the fromNodeId to set
   */
  public void setFromNodeId(String fromNodeId) {
    this.fromNodeId = fromNodeId;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.interfaces.Element#setId(java.lang.String)
   */
  @Override
  public void setId(String id) {
    this.id = id;
  }


  /**
   * @param toNodeId the toNodeId to set
   */
  public void setToNodeId(String toNodeId) {
    this.toNodeId = toNodeId;
  }


  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append(" [id=");
    builder.append(id);
    builder.append(", fromNodeId=");
    builder.append(fromNodeId);
    builder.append(", toNodeId=");
    builder.append(toNodeId);
    builder.append(", b1=");
    builder.append(b1);
    builder.append(", b2=");
    builder.append(b2);
    builder.append("]");
    return builder.toString();
  }


  /**
   * @return
   */
  public String unsetFromNodeId() {
    String fromString = getFromNodeId();
    setFromNodeId(null);
    return fromString;
  }


  /**
   * @return
   */
  public String unsetToNodeId() {
    String toString = getToNodeId();
    setToNodeId(null);
    return toString;
  }
}

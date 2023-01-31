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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Andreas Dr&auml;ger
 */
public class Annotation extends AbstractEscherBase {

  private String sbo;

  public Annotation() {
  }

  public Annotation(String sbo) {
    this();
    setSBO(sbo);
  }

  public Annotation(Annotation annotation) {
    super(annotation);
  }

  @Override
  public Annotation clone() {
    return new Annotation(this);
  }

  @JsonProperty("sbo")
  public String getSBO() {
    return sbo;
  }

  @JsonProperty("sbo")
  public void setSBO(String sbo) {
    this.sbo = sbo;
  }

  public boolean isSetSBO() {
    return sbo != null;
  }

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
    Annotation other = (Annotation) obj;
    if (sbo == null) {
      if (other.sbo != null) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 89;
    int result = super.hashCode();
    result = prime * result + ((sbo == null) ? 0 : sbo.hashCode());
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append(" [sbo=");
    builder.append(getSBO());
    builder.append("]");
    return builder.toString();
  }

}

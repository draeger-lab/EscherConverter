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

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.ucsd.sbrg.escher.models.interfaces.Element;

/**
 * @author Andreas Dr&auml;ger
 */
public class Gene extends AbstractEscherBase implements Element {

  /**
   * The BiGG id of the gene.
   */
  private String biggId;
  /**
   * The name of the gene
   */
  private String name;


  /**
   *
   */
  public Gene() {
  }


  /**
   * @param gene
   */
  public Gene(Gene gene) {
    super(gene);
    if (gene.isSetId()) {
      setId(gene.getId());
    }
    if (gene.isSetName()) {
      setName(gene.getName());
    }
  }


  public Gene(String biggId, String name) {
    this();
    setId(biggId);
    setName(name);
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.AbstractEscherBase#clone()
   */
  @Override
  public Gene clone() {
    return new Gene(this);
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
    Gene other = (Gene) obj;
    if (biggId == null) {
      if (other.biggId != null) {
        return false;
      }
    } else if (!biggId.equals(other.biggId)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.interfaces.Element#getId()
   */
  @Override
  public String getId() {
    return biggId;
  }


  /**
   * @return the name
   */
  public String getName() {
    return name;
  }


  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((biggId == null) ? 0 : biggId.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.interfaces.Element#isSetId()
   */
  @Override
  public boolean isSetId() {
    return biggId != null;
  }


  /**
   * @return
   */
  public boolean isSetName() {
    return name != null;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.interfaces.Element#setId(java.lang.String)
   */
  @Override
  @JsonProperty("bigg_id")
  public void setId(String id) {
    biggId = id;
  }


  /**
   * @param name the name to set
   */
  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }


  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append(" [biggId=");
    builder.append(biggId);
    builder.append(", name=");
    builder.append(name);
    builder.append("]");
    return builder.toString();
  }
}

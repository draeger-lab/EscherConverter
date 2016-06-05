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
 * @author Andreas Dr&auml;ger
 *
 */
public class Metabolite extends AbstractEscherBase implements Element {

  /**
   * The BiGG id of the metabolite.
   */
  private String biggId;

  /**
   * The stoichiometric coefficient of this metabolite.
   */
  private Double coefficient;

  /**
   * A reference to the node that represents this metabolite in the map.
   */
  private String nodeRefId;

  /**
   * 
   */
  public Metabolite() {
    coefficient = null;
    biggId = null;
  }

  /**
   * 
   * @param metabolite
   */
  public Metabolite(Metabolite metabolite) {
    super(metabolite);
    if (metabolite.isSetId()) {
      setId(metabolite.getId());
    }
    if (metabolite.isSetCoefficient()) {
      setCoefficient(metabolite.getCoefficient());
    }
    if (metabolite.isSetNodeRefId()) {
      setNodeRefId(metabolite.getNodeRefId());
    }
  }

  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.AbstractEscherBase#clone()
   */
  @Override
  public Metabolite clone() {
    return new Metabolite(this);
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
    Metabolite other = (Metabolite) obj;
    if (biggId == null) {
      if (other.biggId != null) {
        return false;
      }
    } else if (!biggId.equals(other.biggId)) {
      return false;
    }
    if (coefficient == null) {
      if (other.coefficient != null) {
        return false;
      }
    } else if (!coefficient.equals(other.coefficient)) {
      return false;
    }
    if (nodeRefId == null) {
      if (other.nodeRefId != null) {
        return false;
      }
    } else if (!nodeRefId.equals(other.nodeRefId)) {
      return false;
    }
    return true;
  }

  /**
   * @return the coefficient
   */
  public Double getCoefficient() {
    return coefficient;
  }

  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.Element#getId()
   */
  @Override
  public String getId() {
    return biggId;
  }

  /**
   * @return the nodeRefId
   */
  public String getNodeRefId() {
    return nodeRefId;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((biggId == null) ? 0 : biggId.hashCode());
    result = prime * result + ((coefficient == null) ? 0 : coefficient.hashCode());
    result = prime * result + ((nodeRefId == null) ? 0 : nodeRefId.hashCode());
    return result;
  }

  /**
   * 
   * @return {@code true} if the requested property is not {@code null}.
   */
  public boolean isSetCoefficient() {
    return coefficient != null;
  }

  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.Element#isSetId()
   */
  @Override
  public boolean isSetId() {
    return biggId != null;
  }

  /**
   * 
   * @return
   */
  public boolean isSetNodeRefId() {
    return nodeRefId != null;
  }

  /**
   * @param coefficient the coefficient to set
   */
  public void setCoefficient(Double coefficient) {
    this.coefficient = coefficient;
  }

  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.Element#setId(java.lang.String)
   */
  @Override
  public void setId(String id) {
    biggId = id;
  }

  /**
   * @param nodeRefId the nodeRefId to set
   */
  public void setNodeRefId(String nodeRefId) {
    this.nodeRefId = nodeRefId;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append(" [coefficient=");
    builder.append(coefficient);
    builder.append(", id=");
    builder.append(biggId);
    builder.append(", nodeRefId=");
    builder.append(nodeRefId);
    builder.append("]");
    return builder.toString();
  }

}

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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;

import org.sbml.jsbml.util.ResourceManager;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.zbit.util.Utils;

/**
 * @author Andreas Dr&auml;ger
 */
public class Node extends AbstractBox implements Element {

  /**
   * The kinds of node.
   *
   * @author Andreas Dr&auml;ger
   */
  public enum Type {

    /**
     *
     */
    @JsonProperty("exchange")
    exchange,

    /**
     *
     */
    @JsonProperty("metabolite")
    metabolite,

    /**
     *
     */
    @JsonProperty("midmarker")
    midmarker,

    /**
     *
     */
    @JsonProperty("multimarker")
    multimarker
  }


  /**
   * Localization support.
   */
  private static final transient ResourceBundle bundle = ResourceManager.getBundle("edu.ucsd.sbrg.escher.Messages");
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(Node.class.getName());
  /**
   * This node's BiGG id. Can be {@code null}.
   */
  private String                    biggId;
  /**
   * The identifier of the compartment in which this node is located. This id
   * must be taken from this node's BiGG id and must therefore not be conflict
   * to the {@link #biggId}.
   */
  private String                    compartment;
  /**
   * Mapping from reaction id to segment id
   */
  private Map<String, List<String>> connectedSegments;
  /**
   * The identifier of this node.
   */
  private String                    id;
  /**
   * Decides if this node is a primary node, i.e., a main metabolite, or a currency metabolite.
   */
  private Boolean                   isPrimary;
  /**
   * Coordinates of a text label for this node. This is the bottom-left corner.
   */
  private Double                    labelX, labelY;
  /**
   * A human-readable name for this node.
   */
  private String name;
  /**
   * The kind of nodes
   */
  private Type   nodeType;


  /**
   *
   */
  public Node() {
    super();
    id = name = biggId = null;
    x = y = labelX = labelY = null;
    nodeType = null;
  }


  /**
   * @param node
   */
  public Node(Node node) {
    super(node);
    if (node.isSetBiggId()) {
      setBiggId(node.getBiggId());
    }
    for (Map.Entry<String, List<String>> entry : node.getConnectedSegments()) {
      for (String segment : entry.getValue()) {
        addConnectedSegment(entry.getKey(), segment);
      }
    }
    if (node.isSetId()) {
      setId(node.getId());
    }
    if (node.isSetPrimary()) {
      setPrimary(node.isPrimary().booleanValue());
    }
    if (node.isSetLabelX()) {
      setLabelX(node.getLabelX().doubleValue());
    }
    if (node.isSetLabelY()) {
      setLabelY(node.getLabelY().doubleValue());
    }
    if (node.isSetName()) {
      setName(node.getName());
    }
    if (node.isSetType()) {
      setType(node.getType());
    }
  }


  /**
   * @param reactionId
   * @param segment
   */
  public void addConnectedSegment(String reactionId, Segment segment) {
    initConnectedSegments();
    addConnectedSegment(reactionId, segment.getId());
  }


  /**
   * @param reactionId
   * @param segmentId
   */
  public void addConnectedSegment(String reactionId, String segmentId) {
    List<String> listOfSegments = connectedSegments.get(reactionId);
    if (listOfSegments == null) {
      listOfSegments = new ArrayList<String>();
    }
    if (!listOfSegments.contains(segmentId)) {
      listOfSegments.add(segmentId);
      connectedSegments.put(reactionId, listOfSegments);
    }
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.AbstractEscherBase#clone()
   */
  @Override
  public Node clone() {
    return new Node(this);
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
    Node other = (Node) obj;
    if (biggId == null) {
      if (other.biggId != null) {
        return false;
      }
    } else if (!biggId.equals(other.biggId)) {
      return false;
    }
    if (compartment == null) {
      if (other.compartment != null) {
        return false;
      }
    } else if (!compartment.equals(other.compartment)) {
      return false;
    }
    if (connectedSegments == null) {
      if (other.connectedSegments != null) {
        return false;
      }
    } else if (!connectedSegments.equals(other.connectedSegments)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (isPrimary == null) {
      if (other.isPrimary != null) {
        return false;
      }
    } else if (!isPrimary.equals(other.isPrimary)) {
      return false;
    }
    if (labelX == null) {
      if (other.labelX != null) {
        return false;
      }
    } else if (!labelX.equals(other.labelX)) {
      return false;
    }
    if (labelY == null) {
      if (other.labelY != null) {
        return false;
      }
    } else if (!labelY.equals(other.labelY)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return nodeType == other.nodeType;
  }


  /**
   * @return the biggId
   */
  @JsonProperty("bigg_id")
  public String getBiggId() {
    return biggId;
  }


  /**
   * @return the compartment
   */
  public String getCompartment() {
    return compartment;
  }


  /**
   * @return
   */
  public Set<Entry<String, List<String>>> getConnectedSegments() {
    initConnectedSegments();
    return connectedSegments.entrySet();
  }


  /**
   * @return the connected_segments
   */
  public List<String> getConnectedSegments(String reactionId) {
    initConnectedSegments();
    return connectedSegments.get(reactionId);
  }


  /* (non-Javadoc)
   * @see edu.ucsd.BioNetView.escher.Element#getId()
   */
  @Override
  public String getId() {
    return id;
  }


  /**
   * @return the labelX
   */
  @JsonProperty("label_x")
  public Double getLabelX() {
    return labelX;
  }


  /**
   * @return the labelY
   */
  @JsonProperty("label_y")
  public Double getLabelY() {
    return labelY;
  }


  /**
   * @return the name
   */
  @JsonProperty("name")
  public String getName() {
    return name;
  }


  /**
   * @return the nodeType
   */
  @JsonProperty("node_type")
  public Type getType() {
    return nodeType;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.BioNetView.escher.Box#getX()
   */
  @Override
  public Double getX() {
    return x;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.BioNetView.escher.Box#getY()
   */
  @Override
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
    result = prime * result + ((biggId == null) ? 0 : biggId.hashCode());
    result =
        prime * result + ((compartment == null) ? 0 : compartment.hashCode());
    result =
        prime * result + ((connectedSegments == null) ? 0 :
          connectedSegments.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((isPrimary == null) ? 0 : isPrimary.hashCode());
    result = prime * result + ((labelX == null) ? 0 : labelX.hashCode());
    result = prime * result + ((labelY == null) ? 0 : labelY.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
    return result;
  }


  /**
   * Creates an empty {@link Map} data structure for the {@link #connectedSegments}
   * if it does not yet exist in order to avoid {@link NullPointerException}s.
   */
  private void initConnectedSegments() {
    if (!isSetConnectedSegments()) {
      connectedSegments = new HashMap<String, List<String>>();
    }
  }


  /**
   * @return
   */
  public boolean isMetabolite() {
    return isSetType() && getType().equals(Type.metabolite);
  }


  /**
   * @return
   */
  public boolean isMidmarker() {
    return isSetType() && getType().equals(Type.midmarker);
  }


  /**
   * @return
   */
  public boolean isMultimarker() {
    return isSetType() && getType().equals(Type.multimarker);
  }


  /**
   * @return the node_is_primary
   */
  @JsonProperty("node_is_primary")
  public Boolean isPrimary() {
    return isPrimary;
  }


  /**
   * @return
   */
  public boolean isSetBiggId() {
    return (biggId != null) && (biggId.trim().length() > 0);
  }


  /**
   * @return
   */
  public boolean isSetCompartment() {
    return (compartment != null) && (compartment.length() > 0);
  }


  /**
   * @return {@code true} if the requested property is not {@code null}.
   */
  public boolean isSetConnectedSegments() {
    return connectedSegments != null;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.BioNetView.escher.Element#isSetId()
   */
  @Override
  public boolean isSetId() {
    return id != null;
  }


  /**
   * @return {@code true} if the requested property is not {@code null}.
   */
  public boolean isSetIsPrimary() {
    return isPrimary != null;
  }


  /**
   * @return {@code true} if the requested property is not {@code null}.
   */
  public boolean isSetLabelX() {
    return labelX != null;
  }


  /**
   * @return {@code true} if the requested property is not {@code null}.
   */
  public boolean isSetLabelY() {
    return labelY != null;
  }


  /**
   * @return {@code true} if the requested property is not {@code null}.
   */
  public boolean isSetName() {
    return (name != null) && (name.trim().length() > 0);
  }


  /**
   * @return
   */
  public boolean isSetPrimary() {
    return isPrimary != null;
  }


  /**
   * @return {@code true} if the requested property is not {@code null}.
   */
  public boolean isSetType() {
    return nodeType != null;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.BioNetView.escher.Box#isSetX()
   */
  @Override
  public boolean isSetX() {
    return x != null;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.BioNetView.escher.Box#isSetY()
   */
  @Override
  public boolean isSetY() {
    return y != null;
  }


  /**
   * @param biggId the biggId to set. Parameter is allowed to be {@code null}.
   *               If the parameter is not {@code null}, this method will attempt to
   *               extract the compartment code from the given BiGG id.
   */
  @JsonProperty("bigg_id")
  public void setBiggId(String biggId) {
    this.biggId = biggId;
    if (biggId != null) {
      /* ID structure: [prefix]_[abbreviation]_[compartment code]_[tissue code]
       * prefix and tissue code can be absent; abbreviation and compartment code
       * are mandatory!
       * Prefix must be either R or M.
       */
      String id = biggId;
      if (id.matches("[RrMm]_.*")) {
        id = id.substring(2);
      }
      String idParts[] = id.replace("__", "-").split("_");
      if (idParts.length > 1) {
        compartment = idParts[1];
        if ((idParts.length > 2) &&
            ((compartment.length() > 1) || Utils.isNumber(compartment, true) ||
                Character.isUpperCase(compartment.charAt(0)))) {
          int i = 2;
          while (Utils.isNumber(idParts[i], true)) {
            i++;
          }
          compartment = idParts[i];
        }
      } else {
        logger.warning(MessageFormat
          .format(bundle.getString("Node.invalidCompartmentId"), biggId));
      }
    } else {
      compartment = null;
    }
  }


  /**
   * @param connectedSegments the connected segments to set
   */
  public void setConnectedSegments(
    Map<String, List<String>> connectedSegments) {
    this.connectedSegments = connectedSegments;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.Element#setId(java.lang.String)
   */
  @Override
  public void setId(String id) {
    this.id = id;
  }


  /**
   * @param labelX the labelX to set
   */
  @JsonProperty("label_x")
  public void setLabelX(Double labelX) {
    this.labelX = labelX;
  }


  /**
   * @param labelY the labelY to set
   */
  @JsonProperty("label_y")
  public void setLabelY(Double labelY) {
    this.labelY = labelY;
  }


  /**
   * @param name the name to set
   */
  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }


  /**
   * @param isPrimary the node_is_primary to set
   */
  @JsonProperty("node_is_primary")
  public void setPrimary(Boolean isPrimary) {
    this.isPrimary = isPrimary;
  }


  /**
   * @param nodeType the nodeType to set
   */
  @JsonProperty("node_type")
  public void setType(Type nodeType) {
    this.nodeType = nodeType;
  }


  /* /(non-Javadoc)
   * @see edu.ucsd.BioNetView.escher.Box#setX(java.lang.Double)
   */
  @Override
  public void setX(Double x) {
    this.x = x;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.BioNetView.escher.Box#setY(java.lang.Double)
   */
  @Override
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
    builder.append(" [");
    builder.append("id=");
    builder.append(id);
    builder.append(", biggId=");
    builder.append(biggId);
    builder.append(", compartment=");
    builder.append(compartment);
    builder.append(", name=");
    builder.append(name);
    builder.append(", nodeType=");
    builder.append(nodeType);
    builder.append(", x=");
    builder.append(x);
    builder.append(", y=");
    builder.append(y);
    builder.append(", isPrimary=");
    builder.append(isPrimary);
    builder.append(", labelX=");
    builder.append(labelX);
    builder.append(", labelY=");
    builder.append(labelY);
    builder.append(", connectedSegments=");
    builder.append(connectedSegments);
    builder.append("]");
    return builder.toString();
  }
}

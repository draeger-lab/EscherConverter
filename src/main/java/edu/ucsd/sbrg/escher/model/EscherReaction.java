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
import org.sbml.jsbml.util.ResourceManager;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Represents all properties of a reaction in an Escher map.
 *
 * @author Andreas Dr&auml;ger
 */
public class EscherReaction extends AbstractEscherBase implements Element {

  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(EscherReaction.class.getName());
  /**
   * Localization support.
   */
  public static final  ResourceBundle bundle = ResourceManager.getBundle("edu.ucsd.sbrg.escher.Messages");
  /**
   * The reaction's BiGG id.
   */
  private String            biggId;
  /**
   *
   */
  private String            geneReactionRule;
  /**
   *
   */
  private List<Gene>        geneList;
  /**
   * Gene BiGG id to gene.
   */
  private Map<String, Gene> genes;
  /**
   * The identifier of this reaction.
   */
  private String            id;
  /**
   * The x and y coordinates for the label of this reaction (the bottom left corner of the label).
   */
  private Double            labelX, labelY;
  /**
   *
   */
  private List<Metabolite>        metaboliteList;
  /**
   * BiGG id to metabolites.
   */
  private Map<String, Metabolite> metabolites;
  /**
   *
   */
  private Node                    midmarker;
  /**
   * A human-readable name of this reaction.
   */
  private String                  name;
  /**
   * Set of node identifiers that participate in this reaction.
   */
  private Set<String>             nodes;
  /**
   * Flag if this reaction is reversible ({@code true}) or not ({@code false}).
   */
  private Boolean                 reversibility;
  /**
   * Mapping from the segment identifier to the {@link Segment}, for all
   * connecting arcs in this reaction.
   */
  private Map<String, Segment>    segments;


  /**
   * Creates a new reaction object where all properties are initialized with
   * {@code null} values.
   */
  public EscherReaction() {
    id = name = biggId = null;
    reversibility = null;
    labelX = labelY = null;
    segments = new HashMap<String, Segment>();
    metabolites = new HashMap<String, Metabolite>();
    nodes = new HashSet<String>();
    metaboliteList = new ArrayList<>();
    geneList = new ArrayList<>();
  }


  /**
   * @param escherReaction
   */
  public EscherReaction(EscherReaction escherReaction) {
    super(escherReaction);
    if (escherReaction.isSetBiggId()) {
      setBiggId(escherReaction.getBiggId());
    }
    if (escherReaction.isSetGeneReactionRule()) {
      setGeneReactionRule(escherReaction.getGeneReactionRule());
    }
    if (escherReaction.getGeneCount() > 0) {
      for (Map.Entry<String, Gene> entry : escherReaction.getGenes().entrySet()) {
        addGene(entry.getValue().clone());
      }
    }
    if (escherReaction.isSetId()) {
      setId(escherReaction.getId());
    }
    if (escherReaction.isSetLabelX()) {
      setLabelX(escherReaction.getLabelX().doubleValue());
    }
    if (escherReaction.isSetLabelY()) {
      setLabelY(escherReaction.getLabelY().doubleValue());
    }
    if (escherReaction.getMetaboliteCount() > 0) {
      for (Map.Entry<String, Metabolite> entry : escherReaction.getMetabolites().entrySet()) {
        addMetabolite(entry.getValue().clone());
      }
    }
    if (escherReaction.isSetMidmarker()) {
      setMidmarker(escherReaction.getMidmarker().clone());
    }
    if (escherReaction.isSetName()) {
      setName(escherReaction.getName());
    }
    if (escherReaction.isSetReversibility()) {
      setReversibility(escherReaction.getReversibility().booleanValue());
    }
    if (escherReaction.getSegmentCount() > 0) {
      for (Map.Entry<String, Segment> entry : escherReaction.segments()) {
        addSegment(entry.getValue().clone());
      }
    }
  }


  /**
   * @param gene
   */
  public void addGene(Gene gene) {
    if (genes == null) {
      genes = new HashMap<String, Gene>();
    }
    genes.put(gene.getId(), gene);
  }


  /**
   * @param metabolite
   */
  public void addMetabolite(Metabolite metabolite) {
    if (metabolite != null) {
      if (metabolites == null) {
        metabolites = new HashMap<String, Metabolite>();
      }
      metabolites.put(metabolite.getId(), metabolite);
      metaboliteList.add(metabolite);
    } else {
      logger.warning(MessageFormat.format(
        bundle.getString("EscherReaction.skippingNullElement"),
        Metabolite.class.getSimpleName(),
        isSetBiggId() ? getBiggId() : getId()));
    }
  }


  /**
   * @param segment
   */
  public void addSegment(Segment segment) {
    if (segment != null) {
      if (segments == null) {
        segments = new HashMap<String, Segment>();
      }
      segments.put(segment.getId(), segment);
      if (nodes == null) {
        nodes = new HashSet<String>();
      }
      if (segment.isSetFromNodeId()) {
        nodes.add(segment.getFromNodeId());
      }
      if (segment.isSetToNodeId()) {
        nodes.add(segment.getToNodeId());
      }
    } else {
      logger.warning(MessageFormat.format(
        bundle.getString("EscherReaction.skippingNullElement"),
        Segment.class.getSimpleName(),
        isSetBiggId() ? getBiggId() : getId()));
    }
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.AbstractEscherBase#clone()
   */
  @Override
  public EscherReaction clone() {
    return new EscherReaction(this);
  }


  /**
   * @param nodeId
   * @return
   * @see Set#contains(Object)
   */
  public boolean contains(String nodeId) {
    return nodes.contains(nodeId);
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
    EscherReaction other = (EscherReaction) obj;
    if (biggId == null) {
      if (other.biggId != null) {
        return false;
      }
    } else if (!biggId.equals(other.biggId)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
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
    if (metabolites == null) {
      if (other.metabolites != null) {
        return false;
      }
    } else if (!metabolites.equals(other.metabolites)) {
      return false;
    }
    if (midmarker == null) {
      if (other.midmarker != null) {
        return false;
      }
    } else if (!midmarker.equals(other.midmarker)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    if (nodes == null) {
      if (other.nodes != null) {
        return false;
      }
    } else if (!nodes.equals(other.nodes)) {
      return false;
    }
    if (reversibility == null) {
      if (other.reversibility != null) {
        return false;
      }
    } else if (!reversibility.equals(other.reversibility)) {
      return false;
    }
    if (segments == null) {
      if (other.segments != null) {
        return false;
      }
    } else if (!segments.equals(other.segments)) {
      return false;
    }
    return true;
  }


  /**
   * @return
   */
  public Node findMidmarker(EscherMap map) {
    for (String nodeId : nodes) {
      Node node = map.getNode(nodeId);
      if (node.getType() == Node.Type.midmarker) {
        return node;
      }
    }
    return null;
  }


  /**
   * @return the biggId
   */
  @JsonProperty("bigg_id")
  public String getBiggId() {
    return biggId;
  }


  /**
   * @return
   */
  public int getGeneCount() {
    return genes != null ? genes.size() : 0;
  }


  /**
   * @return the geneReactionRule
   */
  @JsonProperty("gene_reaction_rule")
  public String getGeneReactionRule() {
    return geneReactionRule;
  }


  /**
   * @return
   */
  public Map<String, Gene> getGenes() {
    return genes;
  }


  @JsonProperty("genes")
  public List<Gene> getGeneList() {
    return geneList;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.Element#getId()
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
   * @param biggId
   * @return
   */
  public Metabolite getMetabolite(String biggId) {
    return metabolites.get(biggId);
  }


  /**
   * @return
   */
  public int getMetaboliteCount() {
    return metabolites != null ? metabolites.size() : 0;
  }


  /**
   * Gives you the mapping between BiGG ids and the metabolites with that
   * BiGG id that participate in this reaction.
   *
   * @return the metabolites
   */
  public Map<String, Metabolite> getMetabolites() {
    return metabolites;
  }


  @JsonProperty("metabolites")
  public List<Metabolite> getMetaboliteList() {
    return new ArrayList<>(metabolites.values());
  }


  /**
   * @return the midmarker
   */
  public Node getMidmarker() {
    return midmarker;
  }


  /**
   * @return the name
   */
  @JsonProperty("name")
  public String getName() {
    return name;
  }


  /**
   * @return the reversibility
   */
  @JsonProperty("reversibility")
  public Boolean getReversibility() {
    return reversibility;
  }


  @JsonProperty("segments")
  public Map<String, Segment> getSegments() {
    return segments;
  }


  /**
   * @param id
   * @return
   */
  public Segment getSegment(String id) {
    return segments.get(id);
  }


  /**
   * @return
   */
  public int getSegmentCount() {
    return segments.size();
  }


  public Set<String> getNodes() {
    return nodes;
  }


  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((biggId == null) ? 0 : biggId.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((labelX == null) ? 0 : labelX.hashCode());
    result = prime * result + ((labelY == null) ? 0 : labelY.hashCode());
    result = prime * result + ((metabolites == null) ? 0 : metabolites.hashCode());
    result = prime * result + ((midmarker == null) ? 0 : midmarker.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
    result = prime * result + ((reversibility == null) ? 0 : reversibility.hashCode());
    result = prime * result + ((segments == null) ? 0 : segments.hashCode());
    return result;
  }


  /**
   * @param nodes
   * @return
   */
  public Set<Node> intersect(Set<Node> nodes) {
    Set<Node> intersection = new HashSet<Node>();
    for (Node node : nodes) {
      if (this.nodes.contains(node.getId())) {
        intersection.add(node);
      }
    }
    return intersection;
  }


  /**
   * @return {@code true} if the requested property is not {@code null}.
   */
  public boolean isSetBiggId() {
    return biggId != null;
  }


  /**
   * @return
   */
  public boolean isSetGeneReactionRule() {
    return geneReactionRule != null;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.Element#isSetId()
   */
  @Override
  public boolean isSetId() {
    return id != null;
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
   * @return
   */
  public boolean isSetMidmarker() {
    return midmarker != null;
  }


  /**
   * @return {@code true} if the requested property is not {@code null}.
   */
  public boolean isSetName() {
    return (name != null) && (name.trim().length() > 0);
  }


  /**
   * @return {@code true} if the requested property is not {@code null}.
   */
  public boolean isSetReversibility() {
    return reversibility != null;
  }


  /**
   * @param key
   * @return
   */
  public Segment removeSegment(String key) {
    return segments.remove(key);
  }


  /**
   * @return
   */
  public Set<Map.Entry<String, Segment>> segments() {
    return segments.entrySet();
  }


  /**
   * @param biggId the biggId to set
   */
  @JsonProperty("bigg_id")
  public void setBiggId(String biggId) {
    this.biggId = biggId;
  }


  /**
   * @param geneReactionRule the geneReactionRule to set
   */
  @JsonProperty("gene_reaction_rule")
  public void setGeneReactionRule(String geneReactionRule) {
    this.geneReactionRule = geneReactionRule;
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
   * @param midmarker the midmarker to set
   */
  public void setMidmarker(Node midmarker) {
    this.midmarker = midmarker;
  }


  /**
   * @param name the name to set
   */
  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }


  /**
   * @param reversibility the reversibility to set
   */
  @JsonProperty("reversibility")
  public void setReversibility(Boolean reversibility) {
    this.reversibility = reversibility;
  }


  @JsonProperty("genes")
  public void setGeneList(List<Gene> geneList) {
    this.geneList = geneList;
    if (genes == null) {
      genes = new HashMap<>();
    }
    geneList.forEach((g) -> genes.put(g.getId(), g));
  }


  @JsonProperty("metabolites")
  public void setMetaboliteList(List<Metabolite> metaboliteList) {
    this.metaboliteList = metaboliteList;
    if (metabolites == null) {
      metabolites = new HashMap<>();
    }
    metaboliteList.forEach((m) -> metabolites.put(m.getId(), m));
  }


  @JsonProperty("segments")
  public void setSegments(Map<String, Segment> segments) {
    this.segments = segments;
    if (nodes == null) {
      nodes = new HashSet<>();
    }
    segments.forEach((k, v) -> {
      v.setId(k);
      nodes.add(v.getFromNodeId());
      nodes.add(v.getToNodeId());
    });
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
    builder.append(", name=");
    builder.append(name);
    builder.append(", biggId=");
    builder.append(biggId);
    builder.append(", genes=");
    builder.append(genes);
    builder.append(", gene_reaction_rule=");
    builder.append(geneReactionRule);
    builder.append(", reversibility=");
    builder.append(reversibility);
    builder.append(", labelX=");
    builder.append(labelX);
    builder.append(", labelY=");
    builder.append(labelY);
    builder.append(", segments=");
    builder.append(segments);
    builder.append(", metabolites=");
    builder.append(metabolites);
    builder.append("]");
    return builder.toString();
  }

}

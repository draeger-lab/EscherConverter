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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Andreas Dr&auml;ger
 */
public class EscherMap extends AbstractEscherBase {

  private Map<String, Set<String>>       bigg2nodes;
  private Map<String, Set<String>>       bigg2reactions;
  private Canvas                         canvas;
  private Map<String, EscherCompartment> compartments;
  private String                         description;
  private String                         id;
  private String                         name;
  private Map<String, Node>              nodes;
  private Map<String, EscherReaction>    reactions;
  private String                         schema;
  private Map<String, TextLabel>         textLabels;
  private String                         url;


  /**
   *
   */
  public EscherMap() {
    canvas = null;
    nodes = new HashMap<String, Node>();
    reactions = new HashMap<String, EscherReaction>();
    textLabels = new HashMap<String, TextLabel>();
    bigg2nodes = new HashMap<String, Set<String>>();
    bigg2reactions = new HashMap<String, Set<String>>();
    compartments = new HashMap<String, EscherCompartment>();
  }


  /**
   * @param escherMap
   */
  public EscherMap(EscherMap escherMap) {
    super(escherMap);
    if (escherMap.isSetCanvas()) {
      setCanvas(escherMap.getCanvas().clone());
    }
    if (escherMap.isSetDescription()) {
      setDescription(escherMap.getDescription());
    }
    if (escherMap.isSetId()) {
      setId(escherMap.getId());
    }
    if (escherMap.isSetName()) {
      setName(escherMap.getName());
    }
    if (escherMap.getNodesCount() > 0) {
      for (Map.Entry<String, Node> entry : escherMap.nodes()) {
        addNode(entry.getValue().clone());
      }
    }
    if (escherMap.getReactionCount() > 0) {
      for (Map.Entry<String, EscherReaction> entry : escherMap.reactions()) {
        addReaction(entry.getValue().clone());
      }
    }
    if (escherMap.isSetSchema()) {
      setSchema(escherMap.getSchema());
    }
    if (escherMap.getTextLabelCount() > 0) {
      for (Map.Entry<String, TextLabel> entry : escherMap.textLabels()) {
        addTextLabel(entry.getValue().clone());
      }
    }
    if (escherMap.isSetURL()) {
      setURL(escherMap.getURL());
    }
  }


  /**
   * @param compartment
   */
  public void addCompartment(EscherCompartment compartment) {
    compartments.put(compartment.getId(), compartment);
  }


  /**
   * @param node
   */
  public void addNode(Node node) {
    nodes.put(node.getId(), node);
    if (node.isSetBiggId()) {
      if (!bigg2nodes.containsKey(node.getBiggId())) {
        Set<String> nodeSet = new HashSet<String>();
        nodeSet.add(node.getId());
        bigg2nodes.put(node.getBiggId(), nodeSet);
      } else {
        bigg2nodes.get(node.getBiggId()).add(node.getId());
      }
    }
  }


  /**
   * @param reaction
   */
  public void addReaction(EscherReaction reaction) {
    reactions.put(reaction.getId(), reaction);
    if (reaction.isSetBiggId()) {
      if (!bigg2reactions.containsKey(reaction.getBiggId())) {
        Set<String> reactionSet = new HashSet<String>();
        reactionSet.add(reaction.getId());
        bigg2reactions.put(reaction.getBiggId(), reactionSet);
      } else {
        bigg2reactions.get(reaction.getBiggId()).add(reaction.getId());
      }
    }
  }


  /**
   * @param textLabel
   */
  public void addTextLabel(TextLabel textLabel) {
    textLabels.put(textLabel.getId(), textLabel);
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.AbstractEscherBase#clone()
   */
  @Override
  public EscherMap clone() {
    return new EscherMap(this);
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
    EscherMap other = (EscherMap) obj;
    if (bigg2nodes == null) {
      if (other.bigg2nodes != null) {
        return false;
      }
    } else if (!bigg2nodes.equals(other.bigg2nodes)) {
      return false;
    }
    if (bigg2reactions == null) {
      if (other.bigg2reactions != null) {
        return false;
      }
    } else if (!bigg2reactions.equals(other.bigg2reactions)) {
      return false;
    }
    if (canvas == null) {
      if (other.canvas != null) {
        return false;
      }
    } else if (!canvas.equals(other.canvas)) {
      return false;
    }
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (nodes == null) {
      if (other.nodes != null) {
        return false;
      }
    } else if (!nodes.equals(other.nodes)) {
      return false;
    }
    if (reactions == null) {
      if (other.reactions != null) {
        return false;
      }
    } else if (!reactions.equals(other.reactions)) {
      return false;
    }
    if (textLabels == null) {
      if (other.textLabels != null) {
        return false;
      }
    } else if (!textLabels.equals(other.textLabels)) {
      return false;
    }
    return true;
  }


  /**
   * @return
   */
  public Canvas getCanvas() {
    return canvas;
  }


  /**
   * @param id
   * @return
   */
  public EscherCompartment getCompartment(String id) {
    return compartments.get(id);
  }


  /**
   * @return
   */
  public int getCompartmentCount() {
    return compartments.size();
  }


  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }


  /**
   * @return the id
   */
  public String getId() {
    return id;
  }


  /**
   * @return the name
   */
  public String getName() {
    return name;
  }


  /**
   * @param id
   * @return
   */
  public Node getNode(String id) {
    return nodes.get(id);
  }


  /**
   * @param biggId
   * @return
   */
  public Set<Node> getNodes(String biggId) {
    Set<String> nodeIds = bigg2nodes.get(biggId);
    Set<Node> set = new HashSet<Node>();
    if (nodeIds != null) {
      for (String id : nodeIds) {
        set.add(getNode(id));
      }
    }
    return set;
  }


  /**
   * @return
   */
  public int getNodesCount() {
    return nodes.size();
  }


  /**
   * @param id
   * @return
   */
  public EscherReaction getReaction(String id) {
    return reactions.get(id);
  }


  /**
   * @return
   */
  public int getReactionCount() {
    return reactions.size();
  }


  /**
   * @param biggId
   * @return
   */
  public Set<EscherReaction> getReactions(String biggId) {
    Set<String> reactionIds = bigg2reactions.get(biggId);
    Set<EscherReaction> set = new HashSet<EscherReaction>();
    if (reactionIds != null) {
      for (String id : reactionIds) {
        set.add(getReaction(id));
      }
    }
    return set;
  }


  /**
   * @return the schema
   */
  public String getSchema() {
    return schema;
  }


  /**
   * @param id
   * @return
   */
  public TextLabel getTextLabel(String id) {
    return textLabels.get(id);
  }


  /**
   * @return
   */
  public int getTextLabelCount() {
    return textLabels.size();
  }


  /**
   * @return the url
   */
  public String getURL() {
    return url;
  }


  public Map<String, Set<String>> getBigg2nodes() {
    return bigg2nodes;
  }


  public Map<String, Set<String>> getBigg2reactions() {
    return bigg2reactions;
  }


  /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result =
        prime * result + ((bigg2nodes == null) ? 0 : bigg2nodes.hashCode());
    result =
        prime * result + ((bigg2reactions == null) ? 0 :
            bigg2reactions.hashCode());
    result = prime * result + ((canvas == null) ? 0 : canvas.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
    result = prime * result + ((reactions == null) ? 0 : reactions.hashCode());
    result =
        prime * result + ((textLabels == null) ? 0 : textLabels.hashCode());
    return result;
  }


  /**
   * @return
   */
  public boolean isSetCanvas() {
    return canvas != null;
  }


  /**
   * @return
   */
  public boolean isSetDescription() {
    return (description != null) && (description.length() > 0);
  }


  /**
   * @return
   */
  public boolean isSetId() {
    return (id != null) && (id.length() > 0);
  }


  /**
   * @return
   */
  public boolean isSetName() {
    return name != null;
  }


  /**
   * @return
   */
  public boolean isSetSchema() {
    return schema != null;
  }


  /**
   * @return
   */
  public boolean isSetURL() {
    return url != null;
  }


  /**
   * @return
   */
  public Set<Entry<String, Node>> nodes() {
    return nodes.entrySet();
  }


  /**
   * @return
   */
  public Set<Entry<String, EscherReaction>> reactions() {
    return reactions.entrySet();
  }


  /**
   * @param canvas
   */
  @JsonProperty("canvas")
  public void setCanvas(Canvas canvas) {
    this.canvas = canvas;
  }


  /**
   * @param description the description to set
   */
  @JsonProperty("map_description")
  public void setDescription(String description) {
    this.description = description;
  }


  /**
   * @param id the id to set
   */
  @JsonProperty("map_id")
  public void setId(String id) {
    this.id = id;
  }


  /**
   * @param name the name to set
   */
  @JsonProperty("map_name")
  public void setName(String name) {
    this.name = name;
  }


  /**
   * @param schema the schema to set
   */
  @JsonProperty("schema")
  public void setSchema(String schema) {
    this.schema = schema;
  }


  /**
   * @param url the url to set
   */
  @JsonProperty("homepage")
  public void setURL(String url) {
    this.url = url;
  }


  @JsonProperty("nodes")
  public void setNodes(Map<String, Node> nodes) {
    nodes.forEach((k, v) -> v.setId(k));
    this.nodes = nodes;
  }


  @JsonProperty("reactions")
  public void setReactions(Map<String, EscherReaction> reactions) {
    reactions.forEach((k, v) -> v.setId(k));
    this.reactions = reactions;
  }


  @JsonProperty("text_labels")
  public void setTextLabels(Map<String, TextLabel> textLabels) {
    this.textLabels = textLabels;
  }


  /**
   * @return
   */
  public Set<Entry<String, TextLabel>> textLabels() {
    return textLabels.entrySet();
  }


  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append(" [bigg2nodes=");
    builder.append(bigg2nodes);
    builder.append(", bigg2reactions=");
    builder.append(bigg2reactions);
    builder.append(", canvas=");
    builder.append(canvas);
    builder.append(", compartments=");
    builder.append(compartments);
    builder.append("description=");
    builder.append(description);
    builder.append(", id=");
    builder.append(id);
    builder.append(", name=");
    builder.append(name);
    builder.append(", nodes=");
    builder.append(nodes);
    builder.append(", reactions=");
    builder.append(reactions);
    builder.append(", schema=");
    builder.append(schema);
    builder.append(", textLabels=");
    builder.append(textLabels);
    builder.append(", homepage=");
    builder.append(url);
    builder.append("]");
    return builder.toString();
  }


  /**
   * @return
   */
  public Set<Entry<String, EscherCompartment>> compartments() {
    return compartments.entrySet();
  }


  public void processMap() {
    try {
      // Set midmarker for every reaction by going through its nodes and checking which
      // one is a midmarker.
      reactions.forEach((k, r) -> {
        r.getNodes().forEach((s) -> {
          if (nodes.get(s).getType() == Node.Type.midmarker) {
            r.setMidmarker(nodes.get(s));
          }
        });
      });

      // Set nodeRefIds for metabolites.
      reactions.forEach((k, r) -> {
        r.getMetabolites().forEach((mk, mv) -> {
          r.getNodes().forEach((s) -> {
            try {
              Node node = nodes.get(s);
              if (node.getBiggId() == null) {
                return;
              }
              if (node.getBiggId().equals(mv.getId())) {
                mv.setNodeRefId(node.getId());
              }
            }
            catch (NullPointerException ex) {
              ex.getMessage();
            }

          });
        });
      });

      // Bigg2Reactions.
      reactions.forEach((k, v) -> {
        if (!bigg2reactions.containsKey(v.getBiggId())) {
          Set<String> reactionSet = new HashSet<>();
          reactionSet.add(v.getId());
          bigg2reactions.put(v.getBiggId(), reactionSet);
        }
      });

      // Bigg2Nodes.
      nodes.forEach((k, v) -> {
        if (v.getBiggId() == null) {
          return;
        }
        if (!bigg2nodes.containsKey(v.getBiggId())) {
          Set<String> nodeSet = new HashSet<>();
          nodeSet.add(v.getId());
          bigg2nodes.put(v.getBiggId(), nodeSet);
        }
        else {
          bigg2nodes.get(v.getBiggId()).add(v.getId());
        }
      });

      // Store compartments.
      nodes.forEach((k, v) -> {
        EscherCompartment compartment = new EscherCompartment();

        if (v.getCompartment() == null) {
          return;
        }
        compartment.setId(v.getCompartment());

        if (!compartments.containsKey(compartment.getId())) {
          compartments.put(compartment.getId(), compartment);
        }
      });

    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}

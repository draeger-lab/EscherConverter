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
package edu.ucsd.sbrg.escher;

import static org.sbml.jsbml.util.Pair.pairOf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Logger;

import edu.ucsd.sbrg.escher.models.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sbml.jsbml.util.Pair;
import org.sbml.jsbml.util.ResourceManager;

import de.zbit.sbml.util.SBMLtools;
import de.zbit.util.Utils;

/**
 * @author Andreas Dr&auml;ger
 *
 */
public class EscherParser {

  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(EscherParser.class.getName());

  /**
   * Localization support.
   */
  public static final ResourceBundle bundle = ResourceManager.getBundle("Messages");

  /**
   * 
   * @param jsonFile
   * @return
   * @throws ParseException
   * @throws IOException
   * @throws FileNotFoundException
   */
  public EscherMap parse(File jsonFile) throws FileNotFoundException, IOException, ParseException {
    return parse(new FileInputStream(jsonFile), createMapId(jsonFile));
  }

  /**
   * 
   * @param inputStream
   * @param defaultMapId
   * @return
   * @throws IOException
   * @throws ParseException
   */
  public EscherMap parse(InputStream inputStream, String defaultMapId) throws IOException, ParseException {
    // Read JSON file
    JSONParser parser = new JSONParser();
    Reader reader = new BufferedReader(new InputStreamReader(inputStream));
    Object obj = parser.parse(reader);
    reader.close();

    if (!(obj instanceof JSONArray)) {
      logger.warning(MessageFormat.format(bundle.getString("EscherParser.JSONObjectExpected"), obj, obj.getClass().getName(), JSONArray.class.getName()));
      return null;
    }
    JSONArray json = (JSONArray) obj;
    JSONObject map = (JSONObject) json.get(0);

    /*
     * Create the EscherMap object.
     */
    EscherMap escherMap = new EscherMap();
    Object id = map.get(EscherKeywords.map_id.name());
    escherMap.setId(id != null ? id.toString() : defaultMapId);
    escherMap.setName(map.get(EscherKeywords.map_name.name()).toString());
    escherMap.setDescription(map.get(EscherKeywords.map_description.name()).toString());
    escherMap.setSchema(map.get(EscherKeywords.schema.name()).toString());
    escherMap.setURL(map.get(EscherKeywords.homepage.name()).toString());

    JSONObject parts = (JSONObject) json.get(1);

    Canvas canvas = parseCanvas((JSONObject) parts.get(EscherKeywords.canvas.name()));
    escherMap.setCanvas(canvas);

    /*
     * Nodes
     */
    JSONObject mapNode = (JSONObject) parts.get(EscherKeywords.nodes.name());
    if (mapNode != null) {
      for (Object object : mapNode.keySet()) {
        Node node = parseNode(object, (JSONObject) mapNode.get(object));
        escherMap.addNode(node);
        if (node.isSetCompartment()) {
          try {
            EscherCompartment compartment = escherMap.getCompartment(node.getCompartment());
            double x = node.getX(); // - node.getWidth()/2d;
            double y = node.getY(); // - node.getHeight()/2d;
            if (compartment == null) {
              compartment = new EscherCompartment();
              compartment.setId(node.getCompartment());
              compartment.setX(x);
              compartment.setY(y);
              compartment.setWidth(0d); //node.getWidth());
              compartment.setHeight(0d); //node.getHeight());
              escherMap.addCompartment(compartment);
            } else {
              if (x < compartment.getX()) {
                compartment.setX(x);
              } else if (x /*+ node.getWidth()*/ > compartment.getX() + compartment.getWidth()) {
                compartment.setWidth(x /* + node.getWidth()*/);
              }
              if (y < compartment.getY()) {
                compartment.setY(y);
              } else if (y /*+ node.getHeight()*/ > compartment.getY() + compartment.getHeight()) {
                compartment.setHeight(y /* + node.getHeight()*/);
              }
            }
          } catch (Throwable t) {
            t.printStackTrace();
          }
        }
      }
    }

    /*
     * Reactions
     */
    JSONObject mapReactions = (JSONObject) parts.get(EscherKeywords.reactions.name());
    if (mapReactions != null) {
      for (Object object : mapReactions.keySet()) {
        for (EscherReaction reaction : parseReaction(object, (JSONObject) mapReactions.get(object), escherMap)) {
          escherMap.addReaction(reaction);
        }
      }
    }

    /*
     * Labels
     */
    JSONObject mapText = (JSONObject) parts.get(EscherKeywords.text_labels.name());
    if (mapText != null) {
      for (Object object : mapText.keySet()) {
        escherMap.addTextLabel(parseTextLabel(object, (JSONObject) mapText.get(object)));
      }
    }

    return escherMap;
  }

  /**
   * 
   * @param jsonFile
   * @return
   */
  public static String createMapId(File jsonFile) {
    String modelId = jsonFile.getName();
    modelId = modelId.substring(0, modelId.lastIndexOf('.'));
    return SBMLtools.toSId(modelId);
  }

  /**
   * 
   * @param object
   * @return
   */
  private Boolean parseBoolean(Object object) {
    String string = (object == null) ? null : object.toString();
    return string != null ? Boolean.valueOf(string) : null;
  }

  /**
   * 
   * @param json
   * @return
   */
  private Canvas parseCanvas(JSONObject json) {
    Canvas canvas = new Canvas();
    canvas.setX(parseDouble(json.get(EscherKeywords.x.name())));
    canvas.setY(parseDouble(json.get(EscherKeywords.y.name())));
    canvas.setWidth(parseDouble(json.get(EscherKeywords.width.name())));
    canvas.setHeight(parseDouble(json.get(EscherKeywords.height.name())));
    return canvas;
  }

  /**
   * 
   * @param object
   * @return
   */
  private Double parseDouble(Object object) {
    String string = (object == null) ? null : object.toString();
    if (string != null) {
      try {
        return Double.valueOf(string);
      } catch (NumberFormatException exc) {
        logger.warning(Utils.getMessage(exc));
      }
    }
    return null;
  }

  /**
   * 
   * @param id
   * @param json
   * @return
   */
  private Metabolite parseMetabolite(Object id, JSONObject json) {
    Metabolite metabolite = new Metabolite();
    metabolite.setId(parseString(id));
    metabolite.setCoefficient(parseDouble(json.get(EscherKeywords.coefficient.name())));
    return metabolite;
  }

  /**
   *
   * @return
   */
  private Gene parseGene(Object gene) {
    Gene g = null;
    if (gene != null) {
      if (gene instanceof JSONObject) {
        JSONObject jsonGene = (JSONObject) gene;
        g = new Gene();
        g.setId(parseString(jsonGene.get(EscherKeywords.bigg_id.toString())));
        g.setName(parseString(jsonGene.get(EscherKeywords.name.toString())));
      } else {
        logger.warning(MessageFormat.format(bundle.getString("EscherParser.JSONObjectExpected"), gene, gene.getClass().getName(), JSONObject.class.getName()));
      }
    }
    return g;
  }

  /**
   * 
   * @param id
   * @param json
   * @return
   */
  private Node parseNode(Object id, JSONObject json) {
    Node node = new Node();
    node.setId(parseString(id));
    node.setName(parseString(json.get(EscherKeywords.name.name())));
    node.setBiggId(parseString(parseString(json.get(EscherKeywords.bigg_id.name()))));

    //node.setCompartmentName(parseString(json.get("compartment_name")));
    /* This is based on an older version of the JSON format.
    JSONArray connectedSegments = (JSONArray) json.get(EscherKeywords.connected_segments.name());
    if (connectedSegments != null) {
      node.setConnectedSegments(parseConnectedSegments(connectedSegments));
    } else {
      logger.warning(MessageFormat.format("Node {0} does not have any connected segments.", node.getId()));
    }
     */
    node.setType(Node.Type.valueOf(parseString(json.get(EscherKeywords.node_type.name()))));
    node.setPrimary(parseBoolean(json.get(EscherKeywords.node_is_primary.name())));
    node.setLabelX(parseDouble(json.get(EscherKeywords.label_x.name())));
    node.setLabelY(parseDouble(json.get(EscherKeywords.label_y.name())));
    node.setX(parseDouble(json.get(EscherKeywords.x.name())));
    node.setY(parseDouble(json.get(EscherKeywords.y.name())));
    node.setWidth(parseDouble(json.get(EscherKeywords.width.name())));
    node.setHeight(parseDouble(json.get(EscherKeywords.height.name())));
    return node;
  }

  /**
   * 
   * @param json
   * @return
   */
  private Point parsePoint(JSONObject json) {
    if (json == null) {
      return null;
    }
    Point point = new Point();
    point.setX(parseDouble(json.get(EscherKeywords.x.name())));
    point.setY(parseDouble(json.get(EscherKeywords.y.name())));
    return point;
  }

  /**
   * 
   * @param id
   * @param json
   * @param escherMap
   * @return
   */
  private EscherReaction[] parseReaction(Object id, JSONObject json, EscherMap escherMap) {
    EscherReaction reaction = new EscherReaction();
    reaction.setId(parseString(id));
    reaction.setBiggId(parseString(json.get(EscherKeywords.bigg_id.name())));
    reaction.setLabelX(parseDouble(json.get(EscherKeywords.label_x.name())));
    reaction.setLabelY(parseDouble(json.get(EscherKeywords.label_y.name())));
    reaction.setName(parseString(json.get(EscherKeywords.name.name())));
    reaction.setReversibility(parseBoolean(json.get(EscherKeywords.reversibility.name())));

    if (json.get(EscherKeywords.gene_reaction_rule.toString()) != null) {
      reaction.setGeneReactionRule(json.get(EscherKeywords.gene_reaction_rule.toString()).toString());
    }

    Object object = json.get(EscherKeywords.genes.name());
    if ((object != null) && (object instanceof JSONArray)) {
      JSONArray genes = (JSONArray) object;
      for (Object o : genes) {
        reaction.addGene(parseGene(o));
      }
    } else {
      logger.warning(MessageFormat.format(bundle.getString("EscherParser.cannotParse"), object.getClass().getName()));
    }

    object = json.get(EscherKeywords.metabolites.toString());
    if ((object != null) && (object instanceof JSONArray)) {
      JSONArray metabolites = (JSONArray) object;
      for (int i = 0; i < metabolites.size(); i++) {
        reaction.addMetabolite(parseMetabolite(reaction.getBiggId(), metabolites.get(i)));
      }
    } else {
      logger.warning(MessageFormat.format(bundle.getString("EscherParser.cannotParse"), object.getClass().getName()));
    }

    object = json.get(EscherKeywords.segments.name());
    if ((object != null) && (object instanceof JSONObject)) {
      JSONObject segments = (JSONObject) object;
      List<Segment> listOfSegments = new LinkedList<Segment>();

      // parse all segments and find the midmarker of the reaction
      Set<Node> setOfMidmarkers = new HashSet<Node>();
      Set<Node> setOfConnectedNodes = new HashSet<Node>();
      for (Object key : segments.keySet()) {
        Segment segment = parseSegment(key, (JSONObject) segments.get(key));
        listOfSegments.add(segment);
        Node fromNode = escherMap.getNode(segment.getFromNodeId());
        Node toNode = escherMap.getNode(segment.getToNodeId());
        if (fromNode.isMidmarker()) {
          setOfMidmarkers.add(fromNode);
        } else if (toNode.isMidmarker()) {
          setOfMidmarkers.add(toNode);
        }
        setOfConnectedNodes.add(fromNode);
        setOfConnectedNodes.add(toNode);
      }
      if (setOfMidmarkers.size() > 0) {
        if (setOfMidmarkers.size() == 1) {
          reaction.setMidmarker(setOfMidmarkers.iterator().next());
        } else {
          /*
           * We have to separate all curve segments in this merged reaction, so
           * that there is one such set for each midmarker.
           * 
           */
          Map<Node, Pair<Set<Node>, Set<Segment>>> midmarker2ReachableNodes = new HashMap<>();
          while (!listOfSegments.isEmpty()) {
            for (Node midmarker : setOfMidmarkers) {
              Set<Node> nodes;
              Set<Segment> s;
              if (!midmarker2ReachableNodes.containsKey(midmarker)) {
                nodes = new HashSet<Node>();
                s = new HashSet<Segment>();
                nodes.add(midmarker);
                midmarker2ReachableNodes.put(midmarker, pairOf(nodes, s));
              } else {
                Pair<Set<Node>, Set<Segment>> pair = midmarker2ReachableNodes.get(midmarker);
                nodes = pair.getKey();
                s = pair.getValue();
              }
              separateSegments(nodes, s, listOfSegments, escherMap);
            }
          }

          /*
           * Create one clone reaction for each midmarker and manipulate id and
           * curve segments.
           */
          EscherReaction reactions[] = new EscherReaction[setOfMidmarkers.size()];
          Iterator<Node> iterator = setOfMidmarkers.iterator();
          for (int i = 0; i < reactions.length; i++) {
            Node midmarker = iterator.next();
            reactions[i] = reaction.clone();
            reactions[i].setMidmarker(midmarker);
            reactions[i].setId(reaction.getId() + "_" + (i + 1));
            linkSegmentsToReaction(reactions[i], midmarker2ReachableNodes.get(midmarker).getValue(), escherMap);
          }

          return reactions;
        }
      }

      linkSegmentsToReaction(reaction, listOfSegments, escherMap);
    } else {
      logger.warning(MessageFormat.format(bundle.getString("EscherParser.cannotParse"), object.getClass().getName()));
    }

    return new EscherReaction[] {reaction};
  }

  /**
   * 
   * @param reaction
   * @param collectionOfSegments
   * @param escherMap
   */
  private void linkSegmentsToReaction(EscherReaction reaction,
    Collection<Segment> collectionOfSegments, EscherMap escherMap) {
    // add all segments to the reaction and link nodes.
    Set<String> setOfBiggIds = new HashSet<String>();
    for (Segment segment : collectionOfSegments) {
      Node fromNode = escherMap.getNode(segment.getFromNodeId());
      Node toNode = escherMap.getNode(segment.getToNodeId());
      if (linkNode(fromNode, reaction, escherMap, setOfBiggIds) &&
          linkNode(toNode, reaction, escherMap, setOfBiggIds)) {
        reaction.addSegment(segment);
      }
    }
  }

  /**
   * @param nodes
   * @param s
   * @param listOfSegments
   * @param escherMap
   */
  private void separateSegments(Set<Node> nodes, Set<Segment> s,
    List<Segment> listOfSegments, EscherMap escherMap) {
    for (int i = listOfSegments.size() - 1; i >= 0; i--) {
      Segment segment = listOfSegments.get(i);
      Node fromNode = escherMap.getNode(segment.getFromNodeId());
      Node toNode = escherMap.getNode(segment.getToNodeId());
      if (nodes.contains(fromNode)) {
        nodes.add(toNode);
        s.add(listOfSegments.remove(i));
      } else  if (nodes.contains(toNode)) {
        nodes.add(fromNode);
        s.add(listOfSegments.remove(i));
      }
    }
  }

  /**
   * 
   * @param reactionBiggId
   * @param metabolite
   * @return
   */
  private Metabolite parseMetabolite(String reactionBiggId,
    Object metabolite) {
    Metabolite metab = null;
    if (metabolite != null) {
      if (metabolite instanceof JSONObject) {
        JSONObject m = (JSONObject) metabolite;
        metab = parseMetabolite(m.get(EscherKeywords.bigg_id.toString()), m);
        if (!metab.isSetCoefficient()) {
          logger.warning(MessageFormat.format(bundle.getString("EscherParser.undefinedStoichiometry"), metab.getId(), reactionBiggId));
        } else if (metab.getCoefficient().doubleValue() == 0d) {
          logger.warning(MessageFormat.format(bundle.getString("EscherParser.zeroStoichiometry"), metab.getId(), reactionBiggId));
        }
      } else {
        logger.warning(MessageFormat.format(bundle.getString("EscherParser.JSONObjectExpected"), metabolite, metabolite.getClass().getName(), JSONObject.class.getName()));
      }
    }
    return metab;
  }

  /**
   * 
   * @param node
   * @param reaction
   * @param escherMap
   * @param setOfBiggIds
   * @return {@code false} if the current node cannot be linked to the reaction.
   */
  private boolean linkNode(Node node, EscherReaction reaction, EscherMap escherMap, Set<String> setOfBiggIds) {
    if (node.isMetabolite() && node.isSetBiggId()) {
      if (setOfBiggIds.contains(node.getBiggId())) {
        logger.warning(MessageFormat.format(bundle.getString("EscherParser.multipleParticipantsWithIdenticalBiGGID"), node.getBiggId(), reaction.getId()));
      }
      setOfBiggIds.add(node.getBiggId());
      Metabolite metabolite = reaction.getMetabolite(node.getBiggId());
      if (metabolite != null) {
        metabolite.setNodeRefId(node.getId());
      } else {
        logger.severe(MessageFormat.format(bundle.getString("EscherParser.noMetaboliteWithGivenBiGGID"), node.getBiggId(), reaction.getBiggId()));
      }
    } else if (node.isMidmarker() && !reaction.isSetMidmarker()) {
      reaction.setMidmarker(node);
    }
    return true;
  }

  /**
   * 
   * @param id
   * @param json
   * @return
   */
  private Segment parseSegment(Object id, JSONObject json) {
    Segment segment = new Segment();
    segment.setId(parseString(id));
    segment.setFromNodeId(parseString(json.get(EscherKeywords.from_node_id.name())));
    segment.setBasePoint1(parsePoint((JSONObject) json.get(EscherKeywords.b1.name())));
    segment.setBasePoint2(parsePoint((JSONObject) json.get(EscherKeywords.b2.name())));
    segment.setToNodeId(parseString(json.get(EscherKeywords.to_node_id.name())));
    return segment;
  }

  /**
   * 
   * @param object
   * @return
   */
  private String parseString(Object object) {
    return (object == null) ? null : object.toString();
  }

  /**
   * 
   * @param id
   * @param json
   * @return
   */
  private TextLabel parseTextLabel(Object id, JSONObject json) {
    TextLabel label = new TextLabel();
    label.setId(parseString(id));
    label.setText(parseString(json.get(EscherKeywords.text.name())));
    label.setX(parseDouble(json.get(EscherKeywords.x.name())));
    label.setY(parseDouble(json.get(EscherKeywords.y.name())));
    return label;
  }

}

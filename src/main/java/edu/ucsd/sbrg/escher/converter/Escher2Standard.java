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
package edu.ucsd.sbrg.escher.converter;

import static java.text.MessageFormat.format;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.sbml.jsbml.util.ResourceManager;

import edu.ucsd.sbrg.escher.model.EscherMap;
import edu.ucsd.sbrg.escher.model.EscherReaction;
import edu.ucsd.sbrg.escher.model.Metabolite;
import edu.ucsd.sbrg.escher.model.Node;
import edu.ucsd.sbrg.escher.model.Point;
import edu.ucsd.sbrg.escher.model.Segment;

/**
 * @param T the output format
 * @author Andreas Dr&auml;ger
 * @since 1.0
 */
public abstract class Escher2Standard<T> {

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append(" [canvasDefaultHeight=");
    builder.append(canvasDefaultHeight);
    builder.append(", canvasDefaultWidth=");
    builder.append(canvasDefaultWidth);
    builder.append(", inferCompartmentBoundaries=");
    builder.append(inferCompartmentBoundaries);
    builder.append(", labelHeight=");
    builder.append(labelHeight);
    builder.append(", labelWidth=");
    builder.append(labelWidth);
    builder.append(", primaryNodeHeight=");
    builder.append(primaryNodeHeight);
    builder.append(", primaryNodeWidth=");
    builder.append(primaryNodeWidth);
    builder.append(", reactionNodeRatio=");
    builder.append(reactionNodeRatio);
    builder.append(", secondaryNodeRatio=");
    builder.append(secondaryNodeRatio);
    builder.append("]");
    return builder.toString();
  }


  /**
   * Localization support.
   */
  public static final  ResourceBundle bundle = ResourceManager.getBundle("edu.ucsd.sbrg.escher.Messages");
  /**
   * Compartment codes.
   */
  public static final  ResourceBundle compCode = ResourceManager.getBundle("CompartmentCode");
  /**
   * Used as a key for a mapping between data structures.
   */
  public static final  String ESCHER_NODE_LINK = Escher2Standard.class.getPackage() + ".NodeLink";
  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(Escher2Standard.class.getName());
  /**
   *
   */
  private double  canvasDefaultHeight;
  /*
   * Default values
   * 
   * primary node radius = 15
   * secondary node radius = 10
   * node label font size = 20
   * reaction label font size  = 30
   * reaction segment stroke width = 6
   * text label font size = 50
   */
  private double  canvasDefaultWidth;
  private boolean inferCompartmentBoundaries;
  private double  labelHeight;
  private double  labelWidth;
  private double  primaryNodeHeight;
  private double  primaryNodeWidth;
  private double  reactionNodeRatio;
  private double  secondaryNodeRatio;


  /**
   * @param map
   * @return
   */
  public abstract T convert(EscherMap map);


  /**
   * Returns the reaction id of a set of segments, gives warning if multiple ones are used in segments
   * @param set of segments
   * @return String of the reaction id
   */
  protected String extractReactionId(Set<Entry<String, List<String>>> set) {
    String rId = null;
    for (Entry<String, List<String>> pair : set) {
      if (rId == null) {
        rId = pair.getKey();
      } else if (!rId.equals(pair.getKey())) {
        logger.warning("Error: multiple reaction identifiers used in this node's connected segments.");
        return null;
      }
    }
    return rId;
  }


  /**
   * @return the canvasDefaultHeight
   */
  public double getCanvasDefaultHeight() {
    return canvasDefaultHeight;
  }


  /**
   * @return the canvasDefaultWidth
   */
  public double getCanvasDefaultWidth() {
    return canvasDefaultWidth;
  }


  /**
   * @return the inferCompartmentBoundaries
   */
  public boolean getInferCompartmentBoundaries() {
    return inferCompartmentBoundaries;
  }


  /**
   * @return the labelHeight
   */
  public double getLabelHeight() {
    return labelHeight;
  }


  /**
   * @return the labelWidth
   */
  public double getLabelWidth() {
    return labelWidth;
  }


  /**
   * @return the primaryNodeHeight
   */
  public double getPrimaryNodeHeight() {
    return primaryNodeHeight;
  }


  /**
   * @return the primaryNodeWidth
   */
  public double getPrimaryNodeWidth() {
    return primaryNodeWidth;
  }


  /**
   * @return the reactionNodeRatio
   */
  public double getReactionNodeRatio() {
    return reactionNodeRatio;
  }


  /**
   * @return the secondaryNodeRatio
   */
  public double getSecondaryNodeRatio() {
    return secondaryNodeRatio;
  }


  /**
   * Pre-processes every reaction in a data structure
   * @param escherMap
   */
  public void preprocessDataStructure(EscherMap escherMap) {
    for (Entry<String, EscherReaction> reactions : escherMap.reactions()) {
      EscherReaction reaction = reactions.getValue();
      preProcessReaction(reaction, escherMap);
    }
  }


  /**
   * Pre-process {@link EscherReaction} objects and populates internal helper fields.
   *
   * @param reaction The {@code reaction} object.
   * @param escherMap The parent {@code escher map}.
   */
  private void preProcessReaction(EscherReaction reaction,
    EscherMap escherMap) {
    Set<Segment> segments = new HashSet<Segment>();
    for (Entry<String, Segment> entry : reaction.segments()) {
      Segment segment = entry.getValue();
      Node fromNode = escherMap.getNode(segment.getFromNodeId());
      Node toNode = escherMap.getNode(segment.getToNodeId());
      
      Node srGlyph = null;
      if(toNode == null){
    	  logger.severe(format(bundle.getString("Escher2Standard.missing_node"),
    			  segment.getToNodeId(), reaction.getBiggId()));
      }else if (fromNode == null){
    	  logger.severe(format(bundle.getString("Escher2Standard.missing_node"),
    			  segment.getFromNodeId(), reaction.getBiggId()));
      }else{
      if (fromNode.isMetabolite()) {
        srGlyph = escherMap.getNode(fromNode.getId());
        Metabolite metabolite = reaction.getMetabolite(fromNode.getBiggId());
        if (metabolite == null) {
          logger.severe(format(bundle.getString("Escher2Standard.node_lacking_metabolite"),
            fromNode.getBiggId(), reaction.getBiggId()));
        } else if (metabolite.getCoefficient() > 0d) {
          segment = reverse(segment);
        }
        toNode.addConnectedSegment(reaction.getId(), segment);
      } else if (toNode.isMetabolite()) {
        srGlyph = escherMap.getNode(toNode.getId());
        Metabolite metabolite = reaction.getMetabolite(toNode.getBiggId());
        if (metabolite == null) {
          logger.severe(format(bundle.getString("Escher2Standard.node_lacking_metabolite"),
            toNode.getBiggId(), reaction.getBiggId()));
        } else if (metabolite.getCoefficient() <= 0d) {
          segment = reverse(segment);
        }
        fromNode.addConnectedSegment(reaction.getId(), segment);
      } else {
        // Attach segments to midmarkers and multimarkers.
        // Here we have no information about directionality and just keep it as given.
        fromNode.addConnectedSegment(reaction.getId(), segment);
        toNode.addConnectedSegment(reaction.getId(), segment);
      }
      }
      if (srGlyph != null) {
        List<String>
        segmentIds = srGlyph.getConnectedSegments(reaction.getId());
        if (segmentIds == null) {
          srGlyph.addConnectedSegment(reaction.getId(), segment);
          segmentIds = srGlyph.getConnectedSegments(reaction.getId());
        }
        boolean inconsistency = false;
        if (!segmentIds.contains(segment.getId())) {
          logger.warning(format(bundle.getString("Escher2Standard.inconsistent_data_structure"),
            srGlyph.getId(), segment.getId()));
          inconsistency = true;
        }
        if (segmentIds.size() > 1) {
          logger.warning(format(bundle.getString("Escher2Standard.multiple_arcs"),
            srGlyph.getId(), segments.toString()));
          inconsistency = true;
        }
        if (inconsistency) {
          segmentIds.clear();
          segmentIds.add(segment.getId());
        }
      } else {
        // the set of segments that still need to be processed.
        segments.add(segment);
      }
    }
    for (Entry<String, Metabolite> metabolites : reaction.getMetabolites().entrySet()) {
      Metabolite metabolite = metabolites.getValue();
      Node srGlyph = escherMap.getNode(metabolite.getNodeRefId());
      if (srGlyph == null) {
        logger.warning(format(bundle.getString("Escher2Standard.metabolite_lacking_node"),
          metabolite.getId(), reaction.getBiggId()));
        continue;
      }
      List<String> curve = srGlyph.getConnectedSegments(reaction.getId());
      Segment currSegment = reaction.getSegment(curve.get(0));
      boolean isProduct = metabolite.getCoefficient() > 0d;
      Node currNode = escherMap.getNode(isProduct ? currSegment.getFromNodeId() : currSegment.getToNodeId());
      while (!currNode.isMidmarker() && (currNode.getConnectedSegments().size() != 1 || currNode.isMetabolite())) {
        for (Segment segment : segments) {
          boolean canAttach = false;
          Segment lastSegment = reaction.getSegment(curve.get(curve.size() - 1));
          String toNodeId = isProduct ? lastSegment.getFromNodeId() : lastSegment.getToNodeId();
          if (toNodeId.equals(isProduct ? segment.getToNodeId() : segment.getFromNodeId())) {
            curve.add(segment.getId());
            canAttach = true;
          } else if (toNodeId.equals(isProduct ? segment.getFromNodeId() : segment.getToNodeId())) {
            curve.add(reverse(segment).getId());
            canAttach = true;
          }
          if (canAttach) {
            currNode = escherMap.getNode(isProduct ? segment.getFromNodeId() : segment.getToNodeId());
            if (currNode.isMidmarker() || (currNode.getConnectedSegments().size() == 1 && !currNode.isMetabolite())) {
              break;
            }
          }
        }
      }
      if (isProduct) {
        Collections.reverse(srGlyph.getConnectedSegments(reaction.getId()));
      }
    }
  }


  /**
   * @param compartmentId
   * @return
   */
  public String resolveCompartmentCode(String compartmentId) {
    return compCode.containsKey(compartmentId) ? compCode.getString(compartmentId) : null;
  }


  /**
   * Inverses the given segment in place.
   *
   * @param segment The {@code segment} object.
   * @return The reversed {@code segment}.
   */
  protected Segment reverse(Segment segment) {
    logger.fine(format(bundle.getString("Escher2Standard.reversed_segment"),
      segment.getId(), segment.getFromNodeId(), segment.getToNodeId()));
    Point point = segment.removeBasePoint1();
    segment.setBasePoint1(segment.removeBasePoint2());
    segment.setBasePoint2(point);
    String fromNodeId = segment.unsetFromNodeId();
    segment.setFromNodeId(segment.unsetToNodeId());
    segment.setToNodeId(fromNodeId);
    return segment;
  }


  /**
   * @param canvasDefaultHeight the canvasDefaultHeight to set
   */
  public void setCanvasDefaultHeight(double canvasDefaultHeight) {
    this.canvasDefaultHeight = canvasDefaultHeight;
  }


  /**
   * @param canvasDefaultWidth the canvasDefaultWidth to set
   */
  public void setCanvasDefaultWidth(double canvasDefaultWidth) {
    this.canvasDefaultWidth = canvasDefaultWidth;
  }


  /**
   * @param infer
   */
  public void setInferCompartmentBoundaries(boolean infer) {
    inferCompartmentBoundaries = infer;
  }


  /**
   * @param labelHeight the labelHeight to set
   */
  public void setLabelHeight(double labelHeight) {
    this.labelHeight = labelHeight;
  }


  /**
   * @param labelWidth the labelWidth to set
   */
  public void setLabelWidth(double labelWidth) {
    this.labelWidth = labelWidth;
  }


  /**
   * @param primaryNodeHeight the primaryNodeHeight to set
   */
  public void setPrimaryNodeHeight(double primaryNodeHeight) {
    this.primaryNodeHeight = primaryNodeHeight;
  }


  /**
   * @param primaryNodeWidth the primaryNodeWidth to set
   */
  public void setPrimaryNodeWidth(double primaryNodeWidth) {
    this.primaryNodeWidth = primaryNodeWidth;
  }


  /**
   * @param reactionNodeRatio the reactionNodeRatio to set
   */
  public void setReactionNodeRatio(double reactionNodeRatio) {
    this.reactionNodeRatio = reactionNodeRatio;
  }


  /**
   * @param secondaryNodeRatio the secondaryNodeRatio to set
   */
  public void setSecondaryNodeRatio(double secondaryNodeRatio) {
    this.secondaryNodeRatio = secondaryNodeRatio;
  }

}

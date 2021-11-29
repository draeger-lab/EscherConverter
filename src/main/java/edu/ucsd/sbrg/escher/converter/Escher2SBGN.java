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

import de.zbit.graph.io.def.SBGNProperties;
import de.zbit.graph.io.def.SBGNProperties.ArcType;
import de.zbit.graph.io.def.SBGNProperties.GlyphType;
import de.zbit.sbml.util.SBMLtools;
import edu.ucsd.sbrg.escher.model.*;
import edu.ucsd.sbrg.escher.model.Canvas;
import edu.ucsd.sbrg.escher.model.Point;
import edu.ucsd.sbrg.sbgn.SBGNbuilder;
import org.sbgn.bindings.*;
import org.sbgn.bindings.Arc.End;
import org.sbgn.bindings.Arc.Next;
import org.sbgn.bindings.Glyph.Callout;
import org.sbml.jsbml.util.ResourceManager;
import org.sbml.jsbml.util.StringTools;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * @author Andreas Dr&auml;ger
 * @author Eike Pertuch
 * @since 1.0
 */
public class Escher2SBGN extends Escher2Standard<Sbgn> {


    /**
     * A {@link java.util.logging.Logger} for this class.
     */
    private static final Logger
            logger =
            Logger.getLogger(Escher2SBGN.class.getName());
    /**
     * Localization support.
     */
    public static final ResourceBundle
            bundle =
            ResourceManager.getBundle("edu.ucsd.sbrg.escher.Messages");
    /**
     * The SBGN map builder.
     */
    private SBGNbuilder builder;

    /**
     * Default constructor.
     */
    public Escher2SBGN() {
        super();
        builder = new SBGNbuilder();
    }


    /* (non-Javadoc)
     * @see edu.ucsd.sbrg.escher.converters.Escher2Standard#convert(edu.ucsd.sbrg.escher.model.EscherMap)
     */
    @Override
    public Sbgn convert(EscherMap escherMap) {
        preprocessDataStructure(escherMap);
        Canvas canvas = escherMap.getCanvas();
        double xOffset = canvas.isSetX() ? canvas.getX().doubleValue() : 0d;
        double yOffset = canvas.isSetY() ? canvas.getY().doubleValue() : 0d;
        Sbgn sbgn = builder.createSbgn();
        org.sbgn.bindings.Map
                map =
                builder.createMap(SBGNbuilder.Language.process_description, 0d, 0d,
                        canvas.isSetHeight() ? canvas.getHeight().doubleValue() :
                                getCanvasDefaultHeight(),
                        canvas.isSetWidth() ? canvas.getWidth().doubleValue() :
                                getCanvasDefaultWidth());
        sbgn.setMap(map);
        if (escherMap.isSetDescription()) {
            try {
                // TODO: also insert map's name here if there is any, maybe Escher Homepage
                map.setNotes(builder.createNotes(escherMap.getDescription()));
            } catch (ParserConfigurationException | SAXException | IOException exc) {
                // TODO
                logger.warning(exc.getMessage());
            }
        }
        Map<String, SBGNBase> node2glyph = new HashMap<String, SBGNBase>();
        Map<String, Node> multimarkers = new HashMap<String, Node>();
        if (getInferCompartmentBoundaries()) {
            int i = 0;
            for (Map.Entry<String, EscherCompartment> entry : escherMap
                    .compartments()) {
                String id = entry.getKey();
                if (!(id.equalsIgnoreCase(BiggCompartmentStrings.nucleus) || id.equalsIgnoreCase(BiggCompartmentStrings.extracellularSpace))) {
                    EscherCompartment compartment = entry.getValue();
                    Glyph
                            compGlyph =
                            builder.createGlyph(id, compartment.getName(),
                                    SBGNProperties.GlyphType.compartment,
                                    compartment.getX() - xOffset, compartment.getY() - yOffset,
                                    compartment.getWidth(), compartment.getHeight());
                    node2glyph.put(compGlyph.getId(), compGlyph);
                    map.getGlyph().add(compGlyph);
                    // TODO: arrange compartments according to their size.
                    compGlyph.setCompartmentOrder((float) --i);
                }
            }
        }
        for (Map.Entry<String, Node> entry : escherMap.nodes()) {
            convertNode(entry.getValue(), escherMap, node2glyph, multimarkers, map,
                    xOffset, yOffset);
        }
        for (Map.Entry<String, EscherReaction> entry : escherMap.reactions()) {
            convertProcess(entry.getValue(), escherMap, map, node2glyph, xOffset,
                    yOffset);
        }
        for (Map.Entry<String, TextLabel> entry : escherMap.textLabels()) {
            createTextLabel(entry.getValue(), map, xOffset, yOffset);
        }
        return sbgn;
    }


    /**
     * @param node
     * @param escherMap
     * @param node2glyph
     * @param multimarkers
     * @param xOffset
     * @param yOffset
     */
    private void convertNode(Node node, EscherMap escherMap,
                             Map<String, SBGNBase> node2glyph, Map<String, Node> multimarkers,
                             org.sbgn.bindings.Map map, double xOffset, double yOffset) {
        Glyph glyph = null;
        if (node.isSetType()) {
            switch (node.getType()) {
                case metabolite:
                    glyph =
                            convertMetabolite(node, escherMap, multimarkers, xOffset, yOffset);
                    node2glyph.put(node.getId(), glyph);
                    map.getGlyph().add(glyph);
                    break;
                case midmarker:
                    glyph = convertMidmarker(node, escherMap, node2glyph, map, xOffset, yOffset);
                    break;
                case exchange:
                    // TODO: Exchange!
                    glyph = convertExchange(node, node2glyph, map, xOffset, yOffset);
                    break;
                case multimarker:
                    // This is done when converting reaction arcs.
                    //convertMultimarker(node, multimarkers, xOffset, yOffset);
                    break;
                default:
                    // This is also done at a different time.
                    // convertTextLabel(node, map, xOffset, yOffset);
                    break;
            }
        }
        if ((glyph != null) && getInferCompartmentBoundaries() && node
                .isSetCompartment()) {
            String id = node.getCompartment();
            if (!(id.equalsIgnoreCase(BiggCompartmentStrings.nucleus) || id.equalsIgnoreCase(BiggCompartmentStrings.extracellularSpace))) {
                glyph.setCompartmentRef(builder.getSBGNBase(id));
            }
        }
    }


    /**
     * @param node
     * @param node2glyph
     * @param map
     * @param xOffset
     * @param yOffset
     */
    private Glyph convertExchange(Node node, Map<String, SBGNBase> node2glyph,
                                  org.sbgn.bindings.Map map, double xOffset, double yOffset) {
        // TODO: implement support for exchange reactions!
        logger.warning(MessageFormat
                .format(bundle.getString("Escher2SBGN.cannotConvertExchange"),
                        node.getId()));
        return null;
    }


    /**
     * @param midmarker
     * @param node2glyph
     * @param map
     * @param xOffset
     * @param yOffset
     * @return
     */
    private Glyph convertMidmarker(Node midmarker, EscherMap escherMap,
                                   Map<String, SBGNBase> node2glyph, org.sbgn.bindings.Map map,
                                   double xOffset, double yOffset) {
        String
                rId =
                midmarker
                        .getId(); //extractReactionId(midmarker.getConnectedSegments());
        if (rId != null) {
            if (!node2glyph.containsValue(rId)) {
                double width = getPrimaryNodeWidth() * getReactionNodeRatio();
                double height = getPrimaryNodeHeight() * getReactionNodeRatio();
                double x;
                double y;
                // Also shift node because again the center of the node would be used as coordinate instead of upper-left corner
                EscherReaction reac = escherMap.getReaction(extractReactionId(midmarker.getConnectedSegments()));
                double mid_x = midmarker.getX();
                double mid_y = midmarker.getY();
                if (reac.getMetaboliteCount() == 1) {
                    Node metNode = escherMap.getNode(reac.getMetaboliteList().get(0).getNodeRefId());
                    boolean isReversible = reac.getReversibility();
                    boolean isProduct = reac.getMetaboliteList().get(0).getCoefficient() > 0d;
                    String exId;
                    if(isReversible) {
                        exId = SBMLtools.toSId(rId + exchangeSuffix);
                    }
                    else if(isProduct) {
                        exId = SBMLtools.toSId(rId + sourceSuffix);
                    }
                    else {
                        exId = SBMLtools.toSId(rId + sinkSuffix);
                    }
                    Glyph exGlyph = builder.createGlyph(exId, metNode.getName(),
                            GlyphType.source_and_sink, convertCoordinate(mid_x, xOffset) - metNode.getWidth() / 2d,
                            convertCoordinate(mid_y, yOffset) - metNode.getHeight() / 2d, metNode.getWidth(), metNode.getHeight());
                    map.getGlyph().add(exGlyph);
                    node2glyph.put(exId, exGlyph);
                    Point mid_point = calculateMidpointBetweenNodes(midmarker, metNode);
                    mid_x = mid_point.getX();
                    mid_y = mid_point.getY();
                }
                Glyph
                        rGlyph =
                        builder.createGlyph(SBMLtools.toSId(rId),
                                midmarker.isSetName() ? midmarker.getName() :
                                        midmarker.getBiggId(), GlyphType.process,
                                convertCoordinate(mid_x, xOffset) - width / 2d,
                                convertCoordinate(mid_y, yOffset) - height / 2d,
                                width, height);
                //Do that later... createTextGlyph(midmarker, layout, xOffset, yOffset, rGlyph); (when the actual reaction is treated)
                map.getGlyph().add(rGlyph);
                node2glyph.put(rId, rGlyph);
                return rGlyph;
            }
        }
        return null;
    }

    private Point calculateMidpointBetweenNodes(Node node1, Node node2) {
        double node1_x = node1.getX();
        double node1_y = node1.getY();
        double node2_x = node2.getX();
        double node2_y = node2.getY();
        double new_x = node1_x + ((node2_x - node1_x) / 2d);
        double new_y = node1_y + ((node2_y - node1_y) / 2d);

        return new Point(new_x, new_y);
    }


    /**
     * @param textLabel
     * @param map
     * @param xOffset
     * @param yOffset
     */
    private void createTextLabel(TextLabel textLabel, org.sbgn.bindings.Map map,
                                 double xOffset, double yOffset) {
        try {
            // This is important in order to skip cardinality labels. These are treated directly in the reaction conversion.
            Double.parseDouble(textLabel.getText());
        } catch (NumberFormatException exc) {
            if (!textLabel.isSetId()) {
                // Actually, the id should always be defined!
                textLabel.setId("" + textLabel.hashCode());
            }
            String id = SBMLtools.toSId(textLabel.getId());
            if (builder.getSBGNBase(id) != null) {
                int i = 0;
                do {
                    i++;
                } while (builder.getSBGNBase(id + "_" + i) != null);
                id += "_" + i;
            }
            Glyph
                    glyph =
                    builder.createGlyph(id, textLabel.getText(), GlyphType.annotation,
                            convertCoordinate(textLabel.getX(), xOffset),
                            convertCoordinate(textLabel.getY(), yOffset),
                            textLabel.isSetWidth() ?
                                    SBGNbuilder.toDouble(textLabel.getWidth()) : getLabelWidth(),
                            textLabel.isSetHeight() ?
                                    SBGNbuilder.toDouble(textLabel.getHeight()) :
                                    getLabelHeight());
            Callout
                    callout =
                    builder
                            .createGlyphCallout(convertCoordinate(textLabel.getX(), xOffset),
                                    convertCoordinate(textLabel.getY(), yOffset));
            glyph.setCallout(callout);
            map.getGlyph().add(glyph);
        }
    }


    /**
     * @param reaction
     * @param escherMap
     * @param map
     * @param node2glyph
     * @param xOffset
     * @param yOffset
     */
    private void convertProcess(EscherReaction reaction, EscherMap escherMap,
                                org.sbgn.bindings.Map map, Map<String, SBGNBase> node2glyph,
                                double xOffset, double yOffset) {
        Node midmarker = reaction.getMidmarker();
        if (midmarker == null) {
            logger.warning(MessageFormat
                    .format(bundle.getString("Escher2SBGN.midmarkerMissing"), reaction));
            return;
        }
        Glyph processGlyph = (Glyph) node2glyph.get(midmarker.getId());
        if (processGlyph == null) {
            // We should never get here actually... This is just in case.
            processGlyph =
                    convertMidmarker(midmarker, escherMap, node2glyph, map, xOffset, yOffset);
        }
        List<String> list = midmarker.getConnectedSegments(reaction.getId());
        if (list != null) {
            for (String segmentId : list) {
                Segment segment = reaction.getSegment(segmentId);
                Node
                        node =
                        escherMap.getNode(
                                segment.getFromNodeId().equals(midmarker.getId()) ?
                                        segment.getToNodeId() : segment.getFromNodeId());
                if (node.isMultimarker()) {
                    Port port = createPort(node, processGlyph, xOffset, yOffset);
                    node2glyph.put(port.getId(), port);
                    processGlyph.getPort().add(port);
                } else {
                    logger.info(MessageFormat.format(
                            bundle.getString("Escher2SBGN.midmarkerWithoutMultimarker"),
                            midmarker.getId(),
                            reaction.isSetBiggId() ? reaction.getBiggId() : reaction.getId(),
                            node.getType(),
                            node.isSetBiggId() ? node.getBiggId() : node.getId()));
                }
            }
        } else {
            logger.warning(MessageFormat
                    .format(bundle.getString("Escher2SBGN.reactionNodeWithoutSegments"),
                            reaction.getId()));
        }
        Metabolite metabolite;
        Node srGlyph;
        if (reaction.getMetaboliteCount() != 1) {
            for (Entry<String, Metabolite> entry : reaction.getMetabolites()
                    .entrySet()) {
                metabolite = entry.getValue();
                srGlyph = escherMap.getNode(metabolite.getNodeRefId());
                if (srGlyph != null) {
                    // TODO: First do primary nodes..
                    map.getArc().add(
                            convertSegments(metabolite, reaction, escherMap, map, node2glyph,
                                    xOffset, yOffset));
                }
            }
        } else {
            metabolite = reaction.getMetaboliteList().get(0);
            srGlyph = escherMap.getNode(metabolite.getNodeRefId());
            if (srGlyph != null) {
                convertSegmentsForSourcesAndSinks(metabolite, reaction, escherMap, map, node2glyph,
                        xOffset, yOffset);
            }
        }
    }


    /**
     * @param node
     * @param reaction
     * @param xOffset
     * @param yOffset
     * @return
     */
    private Port createPort(Node node, Glyph reaction, double xOffset,
                            double yOffset) {
        return builder.createPort(createPortId(reaction, node.getId()),
                convertCoordinate(node.getX(), xOffset),
                convertCoordinate(node.getY(), yOffset));
    }


  /**
   * @param escherMap
   * @param map
   * @param xOffset
   * @param yOffset
   * @return
   */
  private Arc convertSegments(Metabolite metabolite, EscherReaction reaction,
    EscherMap escherMap, org.sbgn.bindings.Map map,
    Map<String, SBGNBase> node2glyph, double xOffset, double yOffset) {
    double coeff = SBGNbuilder.toDouble(metabolite.getCoefficient());
    Node metaboliteNode = escherMap.getNode(metabolite.getNodeRefId());
    boolean isProduct = coeff > 0d;
    Glyph
    processGlyph =
    (Glyph) node2glyph.get(reaction.getMidmarker().getId());
    List<String>
    connectedSegments =
    metaboliteNode.getConnectedSegments(reaction.getId());
    Segment segment = reaction.getSegment(connectedSegments.get(0));
    Node source, target;
    SBGNBase sourceBase, targetBase;
    double x1, y1, x2, y2;
    // Connect to ports
    if (isProduct) {
      // -x-->|=|-->x-
      if (connectedSegments.size() > 1) {
        source = escherMap.getNode(segment.getToNodeId()); // port
        sourceBase = node2glyph.get(createPortId(processGlyph, source.getId()));
      } else {
        source = escherMap.getNode(segment.getFromNodeId());
        sourceBase = node2glyph.get(source.getId());
      }
      target = escherMap.getNode(metabolite.getNodeRefId());
      targetBase = node2glyph.get(target.getId());
      x1 = source.getX();
      y1 = source.getY();
      // TODO
      /*Segment lastSegment = reaction.getSegment(target.getConnectedSegments(reaction.getId()).get(0));
      Node fromNode = escherMap.getNode(lastSegment.getFromNodeId());
			Point bp1 = lastSegment.getBasePoint1();
			Point bp2 = lastSegment.getBasePoint2();
			Shape shape = new Ellipse2D.Double(target.getX() - target.getWidth()/2d, target.getY() - target.getHeight()/2d, target.getWidth(), target.getHeight());
			CubicCurve2D curve = new CubicCurve2D.Double(fromNode.getX(), fromNode.getY(), bp1 != null ? bp1.getX() : Double.NaN, bp1 != null ? bp1.getY() : Double.NaN, bp2 != null ? bp2.getX() : Double.NaN, bp2 != null ? bp2.getY() : Double.NaN, target.getX(), target.getY());
			Point2D point = Geometry.findIntersection(shape, curve);
			if (point == null) {*/
      x2 = target.getX();
      y2 = target.getY();
      /*} else {
				x2 = point.getX();
				y2 = point.getY();
			}*/
        } else {
            source = escherMap.getNode(segment.getFromNodeId()); // metabolite
            Segment
                    lastSegment =
                    reaction
                            .getSegment(connectedSegments.get(connectedSegments.size() - 1));
            if (connectedSegments.size() > 1) {
                target = escherMap.getNode(lastSegment.getFromNodeId());
                targetBase = node2glyph.get(createPortId(processGlyph, target.getId()));
            } else {
                target = escherMap.getNode(lastSegment.getToNodeId());
                targetBase = node2glyph.get(target.getId());
            }
            sourceBase = node2glyph.get(source.getId());
            // TODO
      /*Node toNode = escherMap.getNode(segment.getToNodeId());
			Point bp1 = segment.getBasePoint1();
			Point bp2 = segment.getBasePoint2();
			Shape shape = new Ellipse2D.Double(source.getX() - source.getWidth()/2d, source.getY() - source.getHeight()/2d, source.getWidth(), source.getHeight());
			CubicCurve2D curve = new CubicCurve2D.Double(source.getX(), source.getY(), bp1 != null ? bp1.getX() : Double.NaN, bp1 != null ? bp1.getY() : Double.NaN, bp2 != null ? bp2.getX() : Double.NaN, bp2 != null ? bp2.getY() : Double.NaN, toNode.getX(), toNode.getY());
			Point2D point = Geometry.findIntersection(shape, curve);
			if (point == null) {*/
      x1 = source.getX();
      y1 = source.getY();
      /*} else {
				x1 = point.getX();
				y1 = point.getY();
			}*/
      x2 = target.getX();
      y2 = target.getY();
    }
    Arc
    arc =
    builder.createArc(SBMLtools
      .toSId(reactionPrefix + reaction.getId() + "_" + metabolitePrefix + metabolite.getNodeRefId()),
      sourceBase, targetBase,
      isProduct ? ArcType.production : ArcType.consumption);
    arc.setStart(builder.createArcStart(convertCoordinate(x1, xOffset),
      convertCoordinate(y1, yOffset)));
    coeff = Math.abs(coeff);
    if (coeff != 1d) {
      // Create cardinality labels for the edges if necessary.
      Glyph
      cardinalityGlyph =
      builder.createGlyph(
        SBMLtools.toSId(cardinalityGlyphPrefix + metabolite.getNodeRefId()),
        StringTools.toString(Locale.ENGLISH, coeff),
        GlyphType.cardinality);
      double width = getPrimaryNodeWidth() * getReactionNodeRatio();
      double height = getPrimaryNodeHeight() * getReactionNodeRatio();
      Segment targetSegment;
      double x, y, ratio = 1d;
      if (isProduct) {
        targetSegment =
            reaction.getSegment(
              connectedSegments.get(connectedSegments.size() - 1));
        Node end = escherMap.getNode(targetSegment.getToNodeId());
        ratio =
            end.isMetabolite() && end.isPrimary() ? 1d :
              getSecondaryNodeRatio();
        x =
            convertCoordinate(end.getX(), xOffset)
            - getPrimaryNodeWidth() * ratio;
        y =
            convertCoordinate(end.getY(), yOffset)
            - getPrimaryNodeHeight() * ratio;
      } else {
        targetSegment = reaction.getSegment(connectedSegments.get(0));
        Node start = escherMap.getNode(targetSegment.getFromNodeId());
        ratio =
            start.isMetabolite() && start.isPrimary() ? 1d :
              getSecondaryNodeRatio();
        x =
            convertCoordinate(start.getX(), xOffset)
            - getPrimaryNodeWidth() * ratio;
        y =
            convertCoordinate(start.getY(), yOffset)
            - getPrimaryNodeHeight() * ratio;
      }
      cardinalityGlyph.setBbox(builder.createBbox(x, y, width, height));
      arc.getGlyph().add(cardinalityGlyph);
    }
    // add individual segments
      Node prevNode = null;
    for (int i = 1; i < connectedSegments.size(); i++) {
      Segment nextSeg = reaction.getSegment(connectedSegments.get(i));
      Node nextNode = escherMap.getNode(nextSeg.getToNodeId());
      if (nextNode != target) {
        Next
        next =
        builder.createArcNext(convertCoordinate(nextNode.getX(), xOffset),
          convertCoordinate(nextNode.getY(), yOffset));
        if (segment.isSetBasePoint1()) {
          next.getPoint()
          .add(convertPoint(segment.getBasePoint1(), xOffset, yOffset));
        }
        if (segment.isSetBasePoint2()) {
          next.getPoint()
          .add(convertPoint(segment.getBasePoint2(), xOffset, yOffset));
        }
        arc.getNext().add(next);
      }
      segment = nextSeg;
      prevNode = nextNode;
    }
    End
    end =
    builder.createArcEnd(convertCoordinate(x2, xOffset),
      convertCoordinate(y2, yOffset));
    if (connectedSegments.size() < 2) {
      segment =
          reaction
          .getSegment(connectedSegments.get(connectedSegments.size() - 1));
    }
    if (segment.isSetBasePoint1()) {
      end.getPoint()
      .add(convertPoint(segment.getBasePoint1(), xOffset, yOffset));
    }
    if (segment.isSetBasePoint2()) {
      end.getPoint()
      .add(convertPoint(segment.getBasePoint2(), xOffset, yOffset));
    }
    arc.setEnd(end);
    return arc;
  }

    private void convertSegmentsForSourcesAndSinks(Metabolite metabolite, EscherReaction reaction,
                                                  EscherMap escherMap, org.sbgn.bindings.Map map,
                                                  Map<String, SBGNBase> node2glyph, double xOffset, double yOffset) {

        double coeff = SBGNbuilder.toDouble(metabolite.getCoefficient());
        Node metaboliteNode = escherMap.getNode(metabolite.getNodeRefId());
        Node og_midmarker = reaction.getMidmarker();
        Point new_midmarker_point = calculateMidpointBetweenNodes(og_midmarker, metaboliteNode);
        boolean isProduct = coeff > 0d;
        boolean isReversible = reaction.getReversibility();
        SBGNBase sourceBase, targetBase, sourceBase2, targetBase2;
        double x1, y1, x2, y2;

        if(isReversible) {
            sourceBase = sourceBase2 = node2glyph.get(og_midmarker.getId());
            targetBase = node2glyph.get(metaboliteNode.getId());
            targetBase2 = node2glyph.get(SBMLtools.toSId(og_midmarker.getId() + exchangeSuffix));
            x1 = new_midmarker_point.getX();
            y1 = new_midmarker_point.getY();
            x2 = metaboliteNode.getX();
            y2 = metaboliteNode.getY();
        }
        else if(isProduct) {
            sourceBase = targetBase2 = node2glyph.get(og_midmarker.getId());
            sourceBase2 = node2glyph.get(SBMLtools.toSId(og_midmarker.getId() + sourceSuffix));
            targetBase = node2glyph.get(metaboliteNode.getId());
            x1 = new_midmarker_point.getX();
            y1 = new_midmarker_point.getY();
            x2 = metaboliteNode.getX();
            y2 = metaboliteNode.getY();
        }
        else {
            sourceBase = node2glyph.get(metaboliteNode.getId());
            targetBase = sourceBase2 = node2glyph.get(og_midmarker.getId());
            targetBase2 = node2glyph.get(SBMLtools.toSId(og_midmarker.getId() + sinkSuffix));
            x1 = metaboliteNode.getX();
            y1 = metaboliteNode.getY();
            x2 = new_midmarker_point.getX();
            y2 = new_midmarker_point.getY();
        }


        // Create Arc between metabolite and midmarker
        Arc arc = builder.createArc(SBMLtools.toSId(reactionPrefix + reaction.getId() + "_"
                                        + metabolitePrefix + metabolite.getNodeRefId()),
                        sourceBase, targetBase,
                        isReversible || isProduct ? ArcType.production : ArcType.consumption);
        arc.setStart(builder.createArcStart(convertCoordinate(x1, xOffset),
                convertCoordinate(y1, yOffset)));
        arc.setEnd(builder.createArcEnd(convertCoordinate(x2, xOffset), convertCoordinate(y2, yOffset)));

        // Create arc between source/sink and midmarker
        Arc ex_arc = builder.createArc(SBMLtools.toSId(reactionPrefix + reaction.getId() + "_"
                        + metabolitePrefix + metabolite.getNodeRefId() + exchangeSuffix),
                sourceBase2, targetBase2,
                isReversible || !isProduct ? ArcType.production : ArcType.consumption);
        ex_arc.setStart(builder.createArcStart(convertCoordinate(x1, xOffset),
                convertCoordinate(y1, yOffset)));
        ex_arc.setEnd(builder.createArcEnd(convertCoordinate(og_midmarker.getX(), xOffset),
                convertCoordinate(og_midmarker.getY(), yOffset)));

        coeff = Math.abs(coeff);
        if(coeff != 1d) {
            Glyph cardinalityGlyph =
                    builder.createGlyph(
                            SBMLtools.toSId(cardinalityGlyphPrefix + metabolite.getNodeRefId()),
                            StringTools.toString(Locale.ENGLISH, coeff),
                            GlyphType.cardinality);
            Glyph cardinalityGlyphEx =
                    builder.createGlyph(
                            SBMLtools.toSId(cardinalityGlyphPrefix + metabolite.getNodeRefId() + exchangeSuffix),
                            StringTools.toString(Locale.ENGLISH, coeff),
                            GlyphType.cardinality);
            double width = getPrimaryNodeWidth() * getReactionNodeRatio();
            double height = getPrimaryNodeHeight() * getReactionNodeRatio();
            double x, y = 1d;
            x = convertCoordinate(og_midmarker.getX(), xOffset) - getPrimaryNodeWidth();
            y = convertCoordinate(og_midmarker.getY(), yOffset) - getPrimaryNodeHeight();
            cardinalityGlyph.setBbox(builder.createBbox(x, y, width, height));
            arc.getGlyph().add(cardinalityGlyph);
            x = convertCoordinate(metaboliteNode.getX(), xOffset) - getPrimaryNodeWidth();
            y = convertCoordinate(metaboliteNode.getY(), yOffset) - getPrimaryNodeHeight();
            cardinalityGlyphEx.setBbox(builder.createBbox(x, y, width, height));
            ex_arc.getGlyph().add(cardinalityGlyphEx);
        }

        map.getArc().add(arc);
        map.getArc().add(ex_arc);
    }


    /**
     * @param reaction
     * @param nodeId
     * @return
     */
    private String createPortId(Glyph reaction, String nodeId) {
        return SBMLtools.toSId(reaction.getId() + "_" + nodeId);
    }


    /**
     * @param p
     * @param yOffset
     * @param xOffset
     * @return
     */
    private org.sbgn.bindings.Point convertPoint(Point p, double xOffset,
                                                 double yOffset) {
        return builder.createPoint(convertCoordinate(p.getX(), xOffset),
                convertCoordinate(p.getY(), yOffset));
    }


    /**
     * @param coordinate
     * @param offset
     * @return
     */
    private float convertCoordinate(Double coordinate, double offset) {
        return (float) (SBGNbuilder.toDouble(coordinate) - offset);
    }


    /**
     * @param node
     * @param escherMap
     * @param multimarkers
     * @param xOffset
     * @param yOffset
     * @return
     */
    private Glyph convertMetabolite(Node node, EscherMap escherMap,
                                    Map<String, Node> multimarkers, double xOffset, double yOffset) {
        double width = 0d, height = 0d;
        if (node.isSetHeight()) {
            height = node.getHeight().doubleValue();
        } else if (node.isSetPrimary()) {
            height =
                    (node.isPrimary() ? getPrimaryNodeHeight() :
                            getPrimaryNodeHeight() * getSecondaryNodeRatio());
        }
        if (node.isSetWidth()) {
            width = node.getWidth().doubleValue();
        } else if (node.isSetPrimary()) {
            width =
                    (node.isPrimary() ? getPrimaryNodeWidth() :
                            getPrimaryNodeWidth() * getSecondaryNodeRatio());
        }
        // TODO: Node size should be set elsewhere.
        if (!node.isSetWidth()) {
            node.setWidth(width);
        }
        if (!node.isSetHeight()) {
            node.setHeight(height);
        }
        double
                x =
                node.isSetX() ? convertCoordinate(node.getX(), xOffset) - width / 2d :
                        0d;
        double
                y =
                node.isSetY() ? convertCoordinate(node.getY(), yOffset) - height / 2d :
                        0d;
        // name the glyph and add the id globally
        Glyph
                glyph =
                builder.createGlyph(SBMLtools.toSId(node.getId()),
                        GlyphType.simple_chemical, x, y, height, width,
                        node.isSetBiggId() && (escherMap.getNodes(node.getBiggId()).size()
                                > 1));
        if (node.isSetLabelX() && node.isSetLabelY()) {
            glyph.setLabel(builder
                    .createLabel(node.isSetName() ? node.getName() : node.getBiggId(),
                            convertCoordinate(node.getLabelX(), xOffset),
                            convertCoordinate(node.getLabelY(), yOffset),
                            Double.valueOf(getLabelWidth()),
                            Double.valueOf(getLabelHeight())));
        }
        return glyph;
    }
}

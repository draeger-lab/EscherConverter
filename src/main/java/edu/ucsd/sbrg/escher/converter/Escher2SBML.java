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

import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.*;
import org.sbml.jsbml.ext.layout.AbstractReferenceGlyph;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CompartmentGlyph;
import org.sbml.jsbml.ext.layout.CubicBezier;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.LineSegment;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.util.ResourceManager;

import de.zbit.sbml.util.SBMLtools;
import edu.ucsd.sbrg.escher.model.Canvas;
import edu.ucsd.sbrg.escher.model.EscherCompartment;
import edu.ucsd.sbrg.escher.model.EscherMap;
import edu.ucsd.sbrg.escher.model.EscherReaction;
import edu.ucsd.sbrg.escher.model.Metabolite;
import edu.ucsd.sbrg.escher.model.Node;
import edu.ucsd.sbrg.escher.model.Point;
import edu.ucsd.sbrg.escher.model.Segment;
import edu.ucsd.sbrg.escher.model.TextLabel;

/**
 * This class takes an {@link EscherMap} as input and creates an
 * {@link SBMLDocument} based on the information provided.
 *
 * @author Andreas Dr&auml;ger
 * @since 1.0
 */
public class Escher2SBML extends Escher2Standard<SBMLDocument> {

    /**
     * A {@link Logger} for this class.
     */
    static final Logger logger = Logger.getLogger(Escher2SBML.class.getName());
    /**
     * Localization support.
     */
    public static final ResourceBundle bundle = ResourceManager.getBundle("edu.ucsd.sbrg.escher.Messages");
    private String defaultCompartmentId;
    private String defaultCompartmentName;
    private String layoutId;
    private String layoutName;
    private double nodeDepth;
    private double nodeLabelHeight;
    private double reactionLabelHeight;
    private double z;


    /* (non-Javadoc)
     * Converts an {@link EscherMap} to an {@link SBML} document
     * @see edu.ucsd.sbrg.escher.converters.Escher2Standard#convert(edu.ucsd.sbrg.escher.model.EscherMap)
     */
    @Override
    public SBMLDocument convert(EscherMap map) {
        preprocessDataStructure(map);
        // needed for canvas size
        Canvas canvas = map.getCanvas();
        double xOffset = canvas.isSetX() ? canvas.getX().doubleValue() : 0d;
        double yOffset = canvas.isSetY() ? canvas.getY().doubleValue() : 0d;
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getWidth();
        Map<String, String> node2glyph = new HashMap<String, String>();
        Map<String, Node> multimarkers = new HashMap<String, Node>();
        Layout layout = initLayout(map, xOffset, yOffset);
        for (Map.Entry<String, Node> entry : map.nodes()) {
            convertNode(entry.getValue(), map, node2glyph, multimarkers, layout, xOffset, yOffset);
        }
        for (Map.Entry<String, EscherReaction> entry : map.reactions()) {
            convertReaction(entry.getValue(), map, layout, node2glyph, xOffset, yOffset);
        }
        for (Map.Entry<String, TextLabel> entry : map.textLabels()) {
            createTextGlyph(entry.getValue(), layout, xOffset, yOffset);
        }
        if (getInferCompartmentBoundaries()) {
            for (Map.Entry<String, EscherCompartment> entry : map.compartments()) {
                String id = entry.getKey();
                if (!(id.equalsIgnoreCase("n") || id.equalsIgnoreCase(getDefaultCompartmentId())
                        || id.equalsIgnoreCase("e"))) {
                    createCompartmentGlyph(entry.getValue(), layout, xOffset, yOffset, canvasWidth, canvasHeight);
                }
            }
        }
        return layout.getSBMLDocument();
    }


    /**
     * Create a compartment in SBML from an {@link EscherCompartment} and add it to the
     * {@link Layout} object.
     *
     * @param ec      The {@link EscherCompartment} object.
     * @param layout  The {@link Layout} object of the SBML model
     * @param xOffset x-offset.
     * @param yOffset y-offset.
     */
    private void createCompartmentGlyph(EscherCompartment ec, Layout layout,
                                        double xOffset, double yOffset, double canvasWidth, double canvasHeight) {
        //TODO
        //for now, if no x and y coordinates are available, the compartment is set around all nodes
        CompartmentGlyph cg = layout.createCompartmentGlyph(ec.getId() + "_glyph");
        double x;
        double y;
        double width;
        double height;
        if (ec.getX() == null) {
            x = xOffset;
            logger.severe(format(bundle.getString("Escher2SBML.inferredCompartment"),
                    ec.getName(), "x-offset", "x-offset of the canvas: " + xOffset));
        } else {
            x = ec.getX() - xOffset - getPrimaryNodeWidth();

        }
        if (ec.getY() == null) {
            y = yOffset;
            logger.severe(format(bundle.getString("Escher2SBML.inferredCompartment"),
                    ec.getName(), "y-offset", "y-offset of the canvas: " + yOffset));
        } else {
            y = ec.getY() - yOffset - getPrimaryNodeWidth();
        }
        if (ec.getWidth() == null) {
            width = canvasWidth;
            logger.severe(format(bundle.getString("Escher2SBML.inferredCompartment"),
                    ec.getName(), "the width", "the width of the canvas: " + canvasWidth));
        } else {
            width = ec.getWidth();
        }
        if (ec.getHeight() == null) {
            height = canvasHeight;
            logger.severe(format(bundle.getString("Escher2SBML.inferredCompartment"),
                    ec.getName(), "the height", "the height of the canvas: " + canvasHeight));
        } else {
            height = ec.getHeight();
        }
        cg.createBoundingBox(width, height, getNodeDepth(), x, y, getZ());
        cg.setCompartment(SBMLtools.toSId(ec.getId()));
        NamedSBase compartment = cg.getCompartmentInstance();
        if ((compartment != null) && compartment.isSetName()) {
            TextGlyph text = layout.createTextGlyph(cg.getId() + "_tg");
            text.setOriginOfText(compartment);
            text.createBoundingBox(compartment.getName().length() * 5d,
                    getNodeLabelHeight(), getNodeDepth(), x, y, getZ());
        }

    }


    /**
     * Convert an {@link EscherMap} node to a glyph for SBML
     *
     * @param node         The node to be converted
     * @param escherMap    The {@link EscherMap} the node is in
     * @param node2glyph   A hash map of the nodes and converted glyphs
     * @param multimarkers A hash map of the multimarker node ids and the nodes themselves
     * @param layout       The {@link Layout} object of the SBML model
     * @param xOffset      x-offset of the document
     * @param yOffset      y-offset of the document
     */
    private void convertNode(Node node, EscherMap escherMap,
                             Map<String, String> node2glyph, Map<String, Node> multimarkers,
                             Layout layout, double xOffset, double yOffset) {

        if (node.isSetType()) {
            EscherReaction escherReaction = escherMap.getReaction(extractReactionId(node.getConnectedSegments()));
            switch (node.getType()) {
                case metabolite:
                    convertMetabolite(node, node2glyph, layout, xOffset, yOffset);
                    break;
                case midmarker:
                    convertMidmarker(node, escherMap, node2glyph, layout, xOffset, yOffset);
                    break;
                case exchange:
                    convertExchange(node, node2glyph, layout, xOffset, yOffset);
                    break;
                case multimarker:
                    convertMultimarker(node, escherMap, multimarkers, xOffset, yOffset, node2glyph, layout);
                    break;
                default:
                    convertTextLabel(node, layout, xOffset, yOffset);
                    break;
            }
        }
    }


    /**
     * Converts nodes of type exchange
     *
     * @param node       The node to be converted
     * @param node2glyph A hash map of the node ids and their converted glyph ids
     * @param layout     The {@link Layout} object of the SBML model
     * @param xOffset    x-offset of the document
     * @param yOffset    y-offset of the document
     */
    private void convertExchange(Node node, Map<String, String> node2glyph,
                                 Layout layout, double xOffset, double yOffset) {
        Model model = layout.getModel();
        String id = "empty_set";
        int i = 0;
        do {
            i++;
        } while (model.containsUniqueNamedSBase(id + "_" + i));
        id += "_" + i;
        //TODO!
        SpeciesGlyph emptySet = layout.createSpeciesGlyph(id);
        emptySet.createBoundingBox(node.getWidth() / 2d, node.getHeight() / 2d,
                getNodeDepth(), node.getX() - xOffset, node.getY() - yOffset, getZ());
        logger.warning(format(bundle.getString("Escher2SBML.exchangeNotSupported"),
                node.getId()));
    }


    /**
     * Converts (postpones the processing of) nodes of type multimarker
     *
     * @param node         The node to be converted
     * @param multimarkers A hash map of the multimarker node ids and the nodes themselves
     */
    private void convertMultimarker(Node node, EscherMap escherMap, Map<String, Node> multimarkers,
                                    double xOffset, double yOffset, Map<String, String> node2glyph, Layout layout) {
        if (node.isSetId()) {
            // If Reaction multimarker is belonging to is exchange reaction and drawn as a straight line then put
            // multimarker at half the distance from before
            EscherReaction escherReaction = escherMap.getReaction(extractReactionId(node.getConnectedSegments()));
            boolean isExchange = escherReaction.getMetaboliteCount() == 1;
            boolean isStraight = checkIfAllSegmentsStraight(escherReaction.getSegments().values(), escherMap);
            if (isExchange && isStraight) {
                Node metNode = escherMap.getNode(escherReaction.getMetaboliteList().get(0).getNodeRefId());
                double x = metNode.getX() + (node.getX() - metNode.getX()) / 2;
                double y = metNode.getY() + (node.getY() - metNode.getY()) / 2;
                node.setX(x);
                node.setY(y);
            }
            // process these later...
            multimarkers.put(node.getId(), node);
        } else {
            logger.warning(format(bundle.getString("Escher2SBML.undefinedID"),
                    node.toString()));
        }
    }


    /**
     * Converts the text label nodes to text glyphs
     *
     * @param node    The text label node to be converted
     * @param layout  The {@link Layout} object of the SBML model
     * @param xOffset x-offset of the document
     * @param yOffset y-offset of the document
     */
    private void convertTextLabel(Node node, Layout layout, double xOffset,
                                  double yOffset) {
        createTextGlyph(node, layout, xOffset, yOffset);
        logger.info(format(bundle.getString("Escher2SBML.skippingNode"), node.toString()));
    }


    /**
     * Converts an {@link EscherReaction} to a {@link ReactionGlyph}
     *
     * @param escherReaction The reaction to be converted
     * @param escherMap      The {@link EscherMap} the {@code escherReaction} is in
     * @param layout         The {@link Layout} object of the SBML model
     * @param node2glyph     A hash map of the node ids and their converted glyph ids
     * @param xOffset        x-offset of the document
     * @param yOffset        y-offset of the document
     */
    private Reaction convertReaction(EscherReaction escherReaction,
                                     EscherMap escherMap, Layout layout, Map<String, String> node2glyph,
                                     double xOffset, double yOffset) {
        Metabolite met = escherReaction.getMetaboliteList().get(0);
        Node metNode = escherMap.getNode(met.getNodeRefId());
        boolean isExchange = (escherReaction.getMetaboliteCount() == 1);
        String exchangeNodeId = metNode.getId() + "_ex";
        // If reaction is exchange reaction a new node has to be created representing the metabolite in the
        // extracellular space.
        if (isExchange && !node2glyph.containsKey("M_" + exchangeNodeId)) {
            boolean isStraight = checkIfAllSegmentsStraight(escherReaction.getSegments().values(), escherMap);
            Node lastNode = null;
            Node exchangeNode = new Node();
            // Find Multimarker at the end of the reaction (only connected to a single segment)
            // and create new metabolite marker at this position for the exchanged metabolite
            for (String nodeID : escherReaction.getNodes()) {
                lastNode = escherMap.getNode(nodeID);
                if (lastNode.getConnectedSegments().size() == 1) {
                    Node midmarker = escherReaction.getMidmarker();
                    exchangeNode.setId(exchangeNodeId);
                    if (isStraight) {
                        exchangeNode.setX(metNode.getX() + (midmarker.getX() - metNode.getX()) * 2);
                        exchangeNode.setY(metNode.getY() + (midmarker.getY() - metNode.getY()) * 2);
                    } else {
                        exchangeNode.setX(lastNode.getX());
                        exchangeNode.setY(lastNode.getY());
                    }
                    exchangeNode.setHeight(metNode.getHeight());
                    exchangeNode.setWidth(metNode.getWidth());
                    exchangeNode.setName(metNode.getName() + "_ex");
                    exchangeNode.setBiggId(metNode.getBiggId() + "_ex");
                    exchangeNode.setLabelX(exchangeNode.getX() + 20);
                    exchangeNode.setLabelY(exchangeNode.getY() + 20);
                    convertMetabolite(exchangeNode, node2glyph, layout, xOffset, yOffset);
                    break;
                }
            }
            // If all segments of the reaction are roughly straight the label of the reaction also has to be moved
            // Therefore the label is moved to the vicinity of the midmarker. The label is placed on a circle with
            // a radius of 20 around the midmarker, whereby is exact orientation is dependent on the orientation of the
            // reaction segments to the horizontal axis (0: horizontal, 1:vertical).
            if (isStraight) {
                String[] reacNodeIDs = new String[escherReaction.getNodes().size()];
                escherReaction.getNodes().toArray(reacNodeIDs);
                Node midNode = escherReaction.getMidmarker();
                double dx = Math.abs(metNode.getX() - midNode.getX());
                double dy = Math.abs(metNode.getY() - midNode.getY());
                double orient = Math.abs(Math.atan(dy / dx) / (Math.PI / 2.0));
                double midPointX = metNode.getX() + (exchangeNode.getX() - metNode.getX()) / 2;
                double midPointY = metNode.getY() + (exchangeNode.getY() - metNode.getY()) / 2;
                escherReaction.setLabelX(midPointX + orient * 20);
                escherReaction.setLabelY(midPointY + (1 - orient) * 20);
            }
        }
        ReactionGlyph rGlyph = createReactionGlyph(escherReaction, layout, node2glyph, xOffset, yOffset);
        Reaction reaction = (Reaction) rGlyph.getReactionInstance();
        // For exchange reaction the species reference glyphs have to be created for the previously created node
        // of the exchange metabolite
        if (isExchange) {
            String id = metNode.getId() + "_ex";
            SpeciesGlyph sGlyph = layout.getSpeciesGlyph("sg_" + id);
            SpeciesReferenceGlyph srGlyph;
            String srGlyphId = escherReaction.getId() + "_srg_" + metNode.getBiggId() + "_ex";
            if (sGlyph != null) {
                sGlyph.setSBOTerm(291); // turns the node into an empty set node
                srGlyphId = srGlyphId.replaceAll("\\+", "");
                srGlyph = rGlyph.createSpeciesReferenceGlyph(SBMLtools.toSId(srGlyphId), sGlyph.getId());
                System.out.println("SrGlyph: " + srGlyph.getId());
                if (met.getCoefficient() < 0d) {
                    srGlyph.setRole(SpeciesReferenceRole.PRODUCT);
                } else {
                    srGlyph.setRole(SpeciesReferenceRole.SUBSTRATE);
                }
            }
        }
        if (escherReaction.isSetGeneReactionRule()) {
            // TODO
        }
        // Go through each metabolite and connect it to the reaction.
        Map<String, SpeciesReferenceGlyph> srgMap = new HashMap<String, SpeciesReferenceGlyph>();
        for (
                Map.Entry<String, Metabolite> entry : escherReaction.getMetabolites().

                entrySet()) {
            Metabolite metabolite = entry.getValue();
            if (metabolite.getId() != null) {
                // Each metabolite can be represented in multiple nodes, so we need to find those in this reaction, but also these can be multiple...
                Set<Node> setOfNodes = escherReaction.intersect(escherMap.getNodes(metabolite.getId()));
                srgMap.putAll(createSpeciesReferenceGlyphs(metabolite, setOfNodes, layout, node2glyph, rGlyph, reaction));
            }
        }

        // Create a set of all segments to be processed
        Set<Segment> segments = new HashSet<Segment>();
        boolean isStraight = checkIfAllSegmentsStraight(escherReaction.getSegments().values(), escherMap);
        for (
                Entry<String, Segment> entry : escherReaction.segments()) {
            Segment segment = entry.getValue();
            Node fromNode = escherMap.getNode(segment.getFromNodeId());
            Node toNode = escherMap.getNode(segment.getToNodeId());
            SpeciesReferenceGlyph srGlyph = null;
            boolean isProduct = false;
            // If all reaction is exchange reaction and all its segments are roughly straight then put their
            // base points at the halfway point
            if (isExchange && isStraight) {
                Point bp1 = segment.getBasePoint1();
                Point bp2 = segment.getBasePoint2();
                double metX = metNode.getX();
                double metY = metNode.getY();
                if (bp1 != null) {
                    bp1.setX(metX + (bp1.getX() - metX) / 2);
                    bp1.setY(metY + (bp1.getY() - metY) / 2);
                    segment.setBasePoint1(bp1);
                }
                if (bp2 != null) {
                    bp2.setX(metX + (bp2.getX() - metX) / 2);
                    bp2.setY(metY + (bp2.getY() - metY) / 2);
                    segment.setBasePoint2(bp2);
                }
            }
            if (toNode != null && fromNode != null) {
                if (fromNode.isMetabolite()) {
                    srGlyph = srgMap.get(fromNode.getBiggId());
                    //				if (!fromNode.isSetBiggId() || (escherReaction.getMetabolite(fromNode.getBiggId()) == null) || !escherReaction.getMetabolite(fromNode.getBiggId()).isSetCoefficient()) {
                    //				}
                    Metabolite m = escherReaction.getMetabolite(fromNode.getBiggId());
                    if (m != null) {
                        if (m.getCoefficient() > 0d) {
                            segment = reverse(segment);
                            isProduct = true;
                        } else {
                            //TODO: Localize
                            logger.severe(MessageFormat
                                    .format(bundle.getString("Escher2SBML.noNodeWithBiGGId"),
                                            fromNode.getBiggId()));
                        }
                    }
                } else if (toNode.isMetabolite()) {
                    srGlyph = srgMap.get(toNode.getBiggId());
                    Metabolite
                            metabolite =
                            escherReaction.getMetabolite(toNode.getBiggId());
                    if (metabolite == null) {
                        logger.severe(format(
                                bundle.getString("Escher2SBML.metaboliteWithoutStoichiometry"),
                                toNode.getBiggId(), escherReaction.getBiggId()));
                    } else if (metabolite.getCoefficient() <= 0d) {
                        segment = reverse(segment);
                    } else {
                        isProduct = true;
                    }
                }
            }
            if (srGlyph != null) {
                LineSegment ls = convertSegment(segment, escherMap, srGlyph.createCurve(), xOffset, yOffset);
                // memorize the toNode id of the segment in the lineSegment to make access easier later on.
                ls.putUserObject(ESCHER_NODE_LINK,
                        isProduct ? segment.getFromNodeId() : segment.getToNodeId());
            } else {
                segments.add(segment);
            }
        }

        Set<Segment> done = new HashSet<Segment>();
        for (
                SpeciesReferenceGlyph srGlyph : srgMap.values()) {
            Curve curve = srGlyph.getCurve();
            Node toNode = escherMap.getNode(curve.getCurveSegment(curve.getCurveSegmentCount() - 1).getUserObject(ESCHER_NODE_LINK).toString());
            while (!toNode.isMidmarker()) {
                for (Segment segment : segments) {
                    if (tryToAttach(segment, curve, curve.getCurveSegmentCount() - 1,
                            escherMap, xOffset, yOffset)) {
                        done.add(segment);
                        toNode = escherMap.getNode(curve.getCurveSegment(curve.getCurveSegmentCount() - 1).getUserObject(ESCHER_NODE_LINK).toString());
                        if (toNode.isMidmarker()) {
                            break;
                        }
                    }
                }
            }
            if (reaction.isSetListOfProducts() && reaction.getListOfProducts().contains(srGlyph.getSpeciesReferenceInstance())) {
                ListOf<CurveSegment> lcs = curve.getListOfCurveSegments();
                curve.unsetListOfCurveSegments();
                //Collections.reverse(lcs);
                //curve.setListOfCurveSegments(lcs);
                // Reversing all curve segments seems not to be necessary.
                //				for (CurveSegment segment : curve.getListOfCurveSegments()) {
                //					reverse(segment);
                //				}
                for (int i = lcs.size() - 1; i >= 0; i--) {
                    curve.addCurveSegment(lcs.remove(i));
                }
            }
        }
        segments.removeAll(done);
        if (!segments.isEmpty()) {
            logger.warning(format(bundle.getString("Escher2SBML.segmentsLost"), segments));
        }
        logger.fine(bundle.getString("Escher2SBML.done"));
        return reaction;
    }


    /**
     * Converts a segment to a line segment
     *
     * @param fromNode   The node the segment starts from
     * @param toNode     The node where the segment ends
     * @param basePoint1 base point for the curve TODO
     * @param basePoint2 base point for the curve
     * @param curve      A curve object as used by {@link SBML}
     * @param xOffset    x-offset of the document
     * @param yOffset    y-offset of the document
     * @return A line segment (either a cubic bezier or a line)
     */
    private LineSegment convertSegment(Node fromNode, Node toNode,
                                       Point basePoint1, Point basePoint2, Curve curve, double xOffset,
                                       double yOffset) {
        LineSegment lineSegment = null;
        if ((basePoint1 != null) && (basePoint2 != null)) {
            CubicBezier cb = curve.createCubicBezier();
            cb.createBasePoint1(basePoint1.getX() - xOffset,
                    basePoint1.getY() - yOffset, z);
            cb.createBasePoint2(basePoint2.getX() - xOffset,
                    basePoint2.getY() - yOffset, z);
            lineSegment = cb;
        } else {
            lineSegment = curve.createLineSegment();
        }
        double x = fromNode.getX() - xOffset;
        double y = fromNode.getY() - yOffset;
        double width = 0d;
        double height = 0d;
        if (fromNode.isMetabolite() && (lineSegment instanceof CubicBezier)) {
            width = fromNode.getWidth();
            height = fromNode.getHeight();
            // TODO: calculate correct end points using intersection of node borders.
            //			List<Point> intersections = Geometry.intersectBezier3Ellipse(new Point(x, y), new Point(basePoint1.getX(), basePoint1.getY()), new Point(basePoint2.getX(), basePoint2.getY()), new Point(toNode.getX() - xOffset, toNode.getY() - yOffset), new Point(x, y), width, height);
            //			if (intersections.size() > 0) {
            //				Point start = intersections.get(0);
            //				x = start.getX();
            //				y = start.getY();
            //			}
        }
        lineSegment.createStart(x, y, z);
        x = toNode.getX() - xOffset;
        y = toNode.getY() - yOffset;
        lineSegment.createEnd(x, y, z);
        return lineSegment;
    }


    /**
     * Extracts all necessary parameters from the segment needed for converting it to a line segment
     *
     * @param segment The segment to be converted
     * @param map     The {@link EscherMap} the {@code segment} is in
     * @param curve   A curve object as used by SBML
     * @param xOffset x-offset of the document
     * @param yOffset y-offset of the document
     * @return A line segment (either a cubic bezier or a line)
     */
    private LineSegment convertSegment(Segment segment, EscherMap map,
                                       Curve curve, double xOffset, double yOffset) {
        return convertSegment(map.getNode(segment.getFromNodeId()),
                map.getNode(segment.getToNodeId()), segment.getBasePoint1(),
                segment.getBasePoint2(), curve, xOffset, yOffset);
    }


    /**
     * Creates an id
     *
     * @param prefix
     * @param count
     * @return A string id
     */
    private String createId(String prefix, int count) {
        return prefix + (count + 1);
    }


    /**
     * Creates a reaction glyph from an {@link EscherReaction}
     *
     * @param escherReaction The {@link EscherReaction} to be converted
     * @param layout         The {@link Layout} object of the SBML model
     * @param node2glyph     A hash map of the node ids and their converted glyph ids
     * @param xOffset        x-offset of the document
     * @param yOffset        y-offset of the document
     * @return A {@link ReactionGlyph}
     */
    private ReactionGlyph createReactionGlyph(EscherReaction escherReaction,
                                              Layout layout, Map<String, String> node2glyph, double xOffset,
                                              double yOffset) {
        ReactionGlyph rGlyph = null;
        String rId = "R_" + escherReaction.getId();
        if (escherReaction.isSetId() && node2glyph.containsKey(rId)) {
            rGlyph = layout.getReactionGlyph(node2glyph.get(rId));
        }
        if (rGlyph == null) {
            // It can still be null if there is another glyph in the layout that is not a reaction glyph but has an identical id.
            String rGlyphId = escherReaction.isSetId() && !node2glyph.containsKey(escherReaction.getId()) ?
                    SBMLtools.toSId(escherReaction.getId()) : "r_" + (layout.getReactionGlyphCount() + 1);
            rGlyph = layout.createReactionGlyph(rGlyphId);
        }
        if (escherReaction.isSetBiggId()) {
            Reaction reaction = null;
            String reactionId = SBMLtools.toSId(escherReaction.getBiggId());
            Model model = layout.getModel();
            if (!model.containsReaction(reactionId)) {
                reaction = model.createReaction(reactionId);
                if (escherReaction.isSetMidmarker() && escherReaction.getMidmarker()
                        .isSetCompartment()) {
                    reaction.setCompartment(
                            SBMLtools.toSId(escherReaction.getMidmarker().getCompartment()));
                }
            } else if (rGlyph.isSetReaction()) {
                reaction = model.getReaction(rGlyph.getReaction());
            } else {
                reaction = model.getReaction(reactionId);
                rGlyph.setReaction(reaction);
            }
            if (escherReaction.isSetReversibility() && !reaction.isSetReversible()) {
                reaction.setReversible(escherReaction.getReversibility());
            }
            if (!rGlyph.isSetReaction() || !rGlyph.getReaction().equals(reactionId)) {
                rGlyph.setReaction(reactionId);
                if (escherReaction.isSetLabelX() && escherReaction.isSetLabelY()) {
                    createTextGlyph(escherReaction, rGlyph, layout, xOffset, yOffset);
                }
            }
            if (!reaction.isSetName()) {
                reaction.setName(escherReaction.isSetName() ? escherReaction.getName() :
                        escherReaction.getBiggId());
            }
            reaction.setFast(false);
            reaction.setSBOTerm(SBO.getProcess());
        }
        return rGlyph;
    }


    /**
     * Midmarker nodes are converted. These are usually nodes in the middle of an arrow, so usually reactions.
     *
     * @param node       The node to be converted (of type "midmarker")
     * @param node2glyph A hash map of the node ids and their converted glyph ids
     * @param layout     The {@link Layout} object of the SBML model
     * @param xOffset    x-offset of the document
     * @param yOffset    y-offset of the document
     * @return A {@link ReactionGlyph} if an id can be extracted, {@code null} otherwise
     */
    private ReactionGlyph convertMidmarker(Node node, EscherMap escherMap,
                                           Map<String, String> node2glyph, Layout layout, double xOffset,
                                           double yOffset) {
        String ogRId = extractReactionId(node.getConnectedSegments());
        EscherReaction escherReaction = escherMap.getReaction(ogRId);
        boolean isExchange = escherReaction.getMetaboliteCount() == 1;
        boolean isStraight = checkIfAllSegmentsStraight(escherReaction.getSegments().values(), escherMap);
        if (ogRId != null) {
            String rId = "R_" + ogRId;
            String rSId = SBMLtools.toSId(rId);
            Model model = layout.getModel();
            if (!model.containsUniqueNamedSBase(rSId)) {
                ReactionGlyph rGlyph = layout.createReactionGlyph(rSId);
                node2glyph.put(rId, rSId);
                double width;
                double height;
                if (node.isSetWidth()) {
                    width = node.getWidth().doubleValue();
                } else {
                    width = getPrimaryNodeWidth() * getReactionNodeRatio();
                    node.setWidth(width);
                }
                if (node.isSetHeight()) {
                    height = node.getHeight().doubleValue();
                } else {
                    height = getPrimaryNodeHeight() * getReactionNodeRatio();
                    node.setHeight(height);
                }
                double x = node.getX();
                double y = node.getY();
                // If corresponding reaction it is exchange reaction and all its segments are straight
                // move midmarker to the halfway point
                if (isExchange && isStraight) {
                    Node metNode = escherMap.getNode(escherReaction.getMetaboliteList().get(0).getNodeRefId());
                    double metX = metNode.getX();
                    double metY = metNode.getY();
                    x = metX + (x - metX) / 2;
                    y = metY + (y - metY) / 2;
                    node.setX(x);
                    node.setY(y);
                }
                // Also shift node because again the center of the node would be used as coordinate instead of upper-left corner
                rGlyph.createBoundingBox(width, height, nodeDepth,
                        x - xOffset - width / 2d,
                        y - yOffset - height / 2d, z);
                if (node.isSetName()) {
                    rGlyph.setName(node.getName());
                } else if (node.isSetBiggId()) {
                    rGlyph.setName(node.getBiggId());
                }
                rGlyph.putUserObject(ESCHER_NODE_LINK, node);
                //Do that later... createTextGlyph(node, layout, xOffset, yOffset, rGlyph); (when the actual reaction is treated)
                return rGlyph;
            }
        }
        return null;
    }


    /**
     * Convert a node of type "metabolite"
     *
     * @param node       The node to be converted (of type "metabolite")
     * @param node2glyph A hash map of the node ids and their converted glyph ids
     * @param layout     The {@link Layout} object of the SBML model
     * @param xOffset    x-offset of the document
     * @param yOffset    y-offset of the document
     * @return A {@link SpeciesGlyph}
     */
    private SpeciesGlyph convertMetabolite(Node node,
                                           Map<String, String> node2glyph, Layout layout, double xOffset,
                                           double yOffset) {
        SpeciesGlyph sGlyph;
        sGlyph = layout.createSpeciesGlyph(createSpeciesGlyphId(node, layout));
        String nodeId = "M_" + node.getId();
        node2glyph.put(nodeId, sGlyph.getId());
        // Not defined in SBML Layout: sGlyph.setName(node.getName())
        double width;
        double height;
        if (node.isSetWidth()) {
            width = node.getWidth().doubleValue();
        } else {
            width = node.isPrimary() ? getPrimaryNodeWidth() : getSecondaryNodeRatio() * getPrimaryNodeWidth();
            node.setWidth(width);
        }
        if (node.isSetHeight()) {
            height = node.getHeight().doubleValue();
        } else {
            height = node.isPrimary() ? getPrimaryNodeHeight() : getSecondaryNodeRatio() * getPrimaryNodeHeight();
            node.setHeight(height);
        }
        // Correct node position with offset and also because Escher uses the center whereas layout uses the top-left corner of bbox
        sGlyph.createBoundingBox(width, height, nodeDepth,
                node.getX() - xOffset - width / 2d, node.getY() - yOffset - height / 2d,
                z);
        sGlyph.setSBOTerm(SBO.getSimpleMolecule());
        if (node.isSetBiggId()) {
            String sId = SBMLtools.toSId(node.getBiggId());
            Model model = layout.getModel();
            if (!model.containsSpecies(sId)) {
                Compartment compartment;
                if (node.isSetCompartment()) {
                    compartment = createCompartment(model, SBMLtools.toSId(node.getCompartment()), null);
                } else {
                    compartment = createCompartment(model, defaultCompartmentId, defaultCompartmentName);
                }
                Species species = model.createSpecies(sId, compartment);
                species.setConstant(false);
                species.setHasOnlySubstanceUnits(true);
                species.setBoundaryCondition(false);
                //TODO: species.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "http://identifiers.org/bigg.metabolite/" + node.getBiggId()));
                if (node.isSetName()) {
                    species.setName(node.getName());
                } else {
                    species.setName(node.getBiggId());
                }
                species.setSBOTerm(SBO.getSimpleMolecule());
            }
            sGlyph.setSpecies(sId);
        }
        createTextGlyph(node, layout, xOffset, yOffset, sGlyph);
        return sGlyph;
    }


    /**
     * Creates a {@link Compartment}
     *
     * @param model           The SBML model
     * @param compartmentId   Id of the compartment
     * @param compartmentName Name of the compartment
     * @return A {@ link Compartment}
     */
    private Compartment createCompartment(Model model, String compartmentId,
                                          String compartmentName) {
        Compartment compartment = model.getCompartment(compartmentId);
        if (compartment == null) {
            compartment = model.createCompartment(compartmentId);
            compartment.setConstant(true);
            if (compartmentId.equals(defaultCompartmentId) || compartmentId
                    .equals("n")) {
                compartment.setSBOTerm(410); // implicit compartment
            } else {
                compartment.setSBOTerm(SBO.getCompartment()); // physical compartment
            }
            if ((compartmentName == null) || (compartmentName.length() == 0)) {
                compartmentName = resolveCompartmentCode(compartmentId);
            }
            compartment.setName(compartmentName);
            compartment.setSpatialDimensions(3);
        }
        return compartment;
    }


    /**
     * Creates a {@link SpeciesGlyph} id (starting with sg_)
     *
     * @param node   The node for which the id is to be created
     * @param layout The {@link Layout} object of the SBML model
     * @return A a {@link SpeciesGlyph} id (starting with sg_)
     */
    private String createSpeciesGlyphId(Node node, Layout layout) {
        String prefix = "sg_";
        return SBMLtools.toSId(node.isSetId() ? prefix + node.getId() :
                createId(prefix, layout.getSpeciesGlyphCount()));
    }


    /**
     * Creates a hash map of all {@link SpeciesReferenceGlyph}s and their ids corresponding to one {@code metabolite} of an {@link EscherReaction}
     *
     * @param metabolite A {@link Metabolite} of an {@link EscherReaction}
     * @param setOfNodes A set of nodes represented by this {@code metabolite}
     * @param layout     The {@link Layout} object of the SBML model
     * @param node2glyph A hash map of the node ids and their converted glyph ids
     * @param rGlyph     The {@link ReactionGlyph} the {@code metabolite} is in.
     * @param reaction   The {@link Reaction} the {@code metabolite} is in.
     * @return A hash map of all {@link SpeciesReferenceGlyph}s and their ids
     */
    private Map<String, SpeciesReferenceGlyph> createSpeciesReferenceGlyphs(
            Metabolite metabolite, Set<Node> setOfNodes, Layout layout,
            Map<String, String> node2glyph, ReactionGlyph rGlyph, Reaction reaction) {
        String biggID = metabolite.getId();
        String sId = SBMLtools.toSId(biggID);
        SpeciesGlyph sGlyph = null;
        Model model = reaction.getModel();
        Map<String, SpeciesReferenceGlyph>
                srgMap =
                new HashMap<String, SpeciesReferenceGlyph>();
        Set<Compartment> setOfCompartments = new HashSet<Compartment>();
        for (Node node : setOfNodes) {
            sGlyph = layout.getSpeciesGlyph(node2glyph.get("M_" + node.getId()));
            if (sGlyph != null) {
                String srGlyphId = reaction.getId() + "_srg_" + metabolite.getId();
                srGlyphId = srGlyphId.replaceAll("\\+", "");
                if (model.containsUniqueNamedSBase(srGlyphId)) {

                    int i = 0;
                    do {
                        i++;
                    } while (model.containsUniqueNamedSBase(srGlyphId + "_" + i));
                    srGlyphId += "_" + i;
                    logger.warning(format(bundle.getString("Escher2SBML.metaboliteDuplication"),
                            metabolite.getId(), reaction.getId(), ++i));
                }
                SpeciesReferenceGlyph srGlyph = rGlyph.createSpeciesReferenceGlyph(SBMLtools.toSId(srGlyphId), sGlyph.getId());

                sGlyph.putUserObject(ESCHER_NODE_LINK, node);
                // Create the core object for this species reference
                SimpleSpeciesReference ssr = null;
                if (metabolite.isSetCoefficient()) {
                    if (metabolite.getCoefficient().doubleValue() < 0d) {
                        ssr = reaction.createReactant(reaction.getId() + "_reactant_" + (reaction.getReactantCount() + 1), sId);
                        ((SpeciesReference) ssr).setStoichiometry(-metabolite.getCoefficient().doubleValue());
                        ssr.setSBOTerm(SBO.getReactant());
                        srGlyph.setRole(node.isPrimary() ? SpeciesReferenceRole.SUBSTRATE :
                                SpeciesReferenceRole.SIDESUBSTRATE);
                    } else if (metabolite.getCoefficient().doubleValue() > 0d) {
                        ssr = reaction.createProduct(reaction.getId() + "_product_" + (reaction.getProductCount() + 1), sId);
                        ((SpeciesReference) ssr).setStoichiometry(metabolite.getCoefficient().doubleValue());
                        srGlyph.setRole(node.isPrimary() ? SpeciesReferenceRole.PRODUCT : SpeciesReferenceRole.SIDEPRODUCT);
                    }
                }
                if (ssr == null) {
                    ssr = reaction.createModifier(reaction.getId() + "_modifier_" + (reaction.getModifierCount() + 1), sId);
                    if (Double.isNaN(metabolite.getCoefficient())) {
                        srGlyph.setRole(SpeciesReferenceRole.UNDEFINED);
                        ssr.setSBOTerm(SpeciesReferenceRole.UNDEFINED.toSBOterm());
                    } else {
                        srGlyph.setRole(SpeciesReferenceRole.MODIFIER);
                        ssr.setSBOTerm(SBO.getModifier());
                    }
                }
                if (ssr instanceof SpeciesReference) {
                    SpeciesReference sr = (SpeciesReference) ssr;
                    sr.setConstant(true);
                }
                Species species = ssr.getSpeciesInstance();
                if ((species != null) && (species.isSetCompartment())) {
                    setOfCompartments.add(species.getCompartmentInstance());
                }
                srGlyph.setSBOTerm(srGlyph.getSpeciesReferenceRole().toSBOterm());
                srGlyph.setSpeciesReference(ssr);
                if (srgMap.containsKey(metabolite.getId())) {
                    logger.warning(format(bundle.getString("Escher2SBML.replacementGlyph"),
                            metabolite.getId()));
                }
                srgMap.put(metabolite.getId(), srGlyph);
                //createCurve(node, srGlyph, segmentIds, isProduct, escherReaction, escherMap, xOffset, yOffset);
            } else {
                logger.warning(format(bundle.getString("Escher2SBML.glyphIdNull"), biggID));
            }
        }
        if (!reaction.isSetCompartment()) {
            if (setOfCompartments.size() == 1) {
                reaction.setCompartment(setOfCompartments.iterator().next());
            } else {
                //TODO: What to do?
                //reaction.setCompartment(createCompartment(model, compartmentId, compartmentName));
                logger.warning(format(bundle.getString("Escher2SBML.reactionCompartmentUnknown"),
                        reaction.getId()));
            }
        }
        return srgMap;
    }


    /**
     * Create a text glyph from an {@link EscherReaction}
     *
     * @param reaction The {@link EscherReaction} the text glyph is extracted from
     * @param rGlyph   The reaction glyph the text glyph corresponds to and where the text glyph has to be positioned next to
     * @param layout   The {@link Layout} object of the SBML model
     * @param xOffset  x-offset of the document
     * @param yOffset  y-offset of the document
     */
    private void createTextGlyph(EscherReaction reaction, ReactionGlyph rGlyph,
                                 Layout layout, double xOffset, double yOffset) {
        double x = reaction.getLabelX() - xOffset;
        double y = reaction.getLabelY() - yOffset - reactionLabelHeight;
        TextGlyph label = layout.createTextGlyph(createTextGlyphId(layout));
        label.createBoundingBox(getLabelWidth(), reactionLabelHeight, nodeDepth, x, y, z);
        label.setOriginOfText(rGlyph.getReaction());
        label.setGraphicalObject(rGlyph);
    }


    /**
     * Create a text glyph from a node
     *
     * @param node    The node the text glyph is extracted from
     * @param layout  The {@link Layout} object of the SBML model
     * @param xOffset x-offset of the document
     * @param yOffset y-offset of the document
     * @return A {@link TextGlyph}
     */
    private TextGlyph createTextGlyph(Node node, Layout layout, double xOffset,
                                      double yOffset) {
        return createTextGlyph(node, layout, xOffset, yOffset, null);
    }


    /**
     * Create a text glyph from a node
     *
     * @param node           The node the text glyph is extracted from
     * @param layout         The {@link Layout} object of the SBML model
     * @param xOffset        x-offset of the document
     * @param yOffset        y-offset of the document
     * @param referenceGlyph The (reaction or species) glyph the text glyph references
     * @return A {@link TextGlyph}
     */
    private TextGlyph createTextGlyph(Node node, Layout layout, double xOffset,
                                      double yOffset, AbstractReferenceGlyph referenceGlyph) {
        TextGlyph tGlyph = null;
        if (node.isSetLabelX() && node.isSetLabelY()) {
            tGlyph = layout.createTextGlyph(createTextGlyphId(layout));
            double height = getLabelHeight();
            if (node.isMetabolite()) {
                height = nodeLabelHeight;
            } else if (node.isMidmarker() || node.isMultimarker()) {
                height = reactionLabelHeight;
            }
            // In Escher text is positioned at the bottom-left corner, but in layout it is always the top-left corner.
            tGlyph.createBoundingBox(getLabelWidth(), height, nodeDepth,
                    node.getLabelX() - xOffset, node.getLabelY() - yOffset - height, z);
            if ((referenceGlyph == null) && (node.isSetName())) {
                tGlyph.setText(node.getName());
            } else {
                if (referenceGlyph != null) {
                    tGlyph.setGraphicalObject(referenceGlyph);
                    if (referenceGlyph.isSetReference()) {
                        tGlyph.setOriginOfText(referenceGlyph.getReference());
                    }
                }
                if ((referenceGlyph == null) || (!referenceGlyph.isSetReference())) {
                    if (node.isSetBiggId()) {
                        // we hope that there will be a corresponding Escher object...
                        tGlyph.setOriginOfText(SBMLtools.toSId(node.getBiggId()));
                        tGlyph.setText(SBMLtools.toSId(node.getBiggId()));
                    } else if ((referenceGlyph != null) && referenceGlyph.isSetName()) {
                        tGlyph.setOriginOfText(referenceGlyph);
                    }
                }
            }
        }
        return tGlyph;
    }


    /**
     * Creates a text glyph from a text label in an {@link EscherMap}, and puts it into the {@code layout}
     *
     * @param label   A {@link TextLabel} of an {@link EscherMap}
     * @param layout  The {@link Layout} object of the SBML model
     * @param xOffset x-offset of the document
     * @param yOffset y-offset of the document
     */
    private void createTextGlyph(TextLabel label, Layout layout, double xOffset,
                                 double yOffset) {
        String id = createTextGlyphId(label, layout);
        Model model = layout.getModel();
        NamedSBase element = model.findNamedSBase(id);
        if (element != null) {
            logger.warning(format(bundle.getString("Escher2SBML.textLabelIdNotUnique"), id,
                    element.getElementName()));
            id += "_tf";
        }
        TextGlyph tGlyph = layout.createTextGlyph(id);
        BoundingBox bbox = tGlyph.createBoundingBox();
        bbox.createPosition(label.getX() - xOffset, label.getY() - yOffset - getLabelHeight(), z);
        bbox.createDimensions(getLabelWidth(), getLabelHeight(), nodeDepth);
        if (label.isSetText()) {
            tGlyph.setText(label.getText());
        }
    }


    /**
     * Creates a {@link TextGlyph} id (starting with tg_)
     *
     * @param layout The {@link Layout} object of the SBML model
     * @return A string {@link TextGlyph} id (starting with tg_)
     */
    private String createTextGlyphId(Layout layout) {
        return createId("tg_", layout.getTextGlyphCount());
    }


    /**
     * Creates a {@link TextGlyph} id, if possible from the id of the {@link TextLabel}
     *
     * @param label  A {@link TextLabel}
     * @param layout The {@link Layout} object of the SBML model
     * @return A string {@link TextGlyph} id
     */
    private String createTextGlyphId(TextLabel label, Layout layout) {
        return label.isSetId() ? SBMLtools.toSId(label.getId()) : createTextGlyphId(layout);
    }


    /**
     * @return the compartmentId
     */
    public String getDefaultCompartmentId() {
        return defaultCompartmentId;
    }


    /**
     * @return the layoutId
     */
    public String getLayoutId() {
        return layoutId;
    }


    /**
     * @return the layoutName
     */
    public String getLayoutName() {
        return layoutName;
    }


    /**
     * @return the nodeDepth
     */
    public double getNodeDepth() {
        return nodeDepth;
    }


    /**
     * @return the nodeLabelHeight
     */
    public double getNodeLabelHeight() {
        return nodeLabelHeight;
    }


    /**
     * @return the z
     */
    public double getZ() {
        return z;
    }


    /**
     * Initiates a layout as created by the {@link LayoutModelPlugin}
     *
     * @param escherMap The {@link EscherMap} the layout is to be created from
     * @param xOffset   x-offset of the document
     * @param yOffset   y-offset of the document
     */
    private Layout initLayout(EscherMap escherMap, double xOffset,
                              double yOffset) {
        Canvas canvas = escherMap.getCanvas();
        double width = canvas.isSetWidth() ? canvas.getWidth().doubleValue() - xOffset : getCanvasDefaultWidth();
        double height = canvas.isSetHeight() ? canvas.getHeight().doubleValue() - yOffset : getCanvasDefaultHeight();
        SBMLDocument doc = new SBMLDocument(3, 1);
        Model model = doc.createModel(
                escherMap.isSetId() ? SBMLtools.toSId(escherMap.getId()) : "_default");
        if (escherMap.isSetDescription() || escherMap.isSetURL()) {
            try {
                boolean both = false;
                StringBuilder notes = new StringBuilder();
                notes.append("<body xmlns=\"http://www.w3.org/1999/xhtml\">");
                if (escherMap.isSetDescription()) {
                    if (escherMap.isSetURL()) {
                        both = true;
                        notes.append("<p>");
                    }
                    notes.append(escherMap.getDescription());
                    if (both) {
                        notes.append("</p><p>");
                    }
                }
                if (escherMap.isSetURL()) {
                    notes.append("<a href=\"");
                    notes.append(escherMap.getURL());
                    notes.append("\">Escher</a>");
                    if (both) {
                        notes.append("</p>");
                    }
                }
                notes.append("</body>");
                model.appendNotes(notes.toString());
            } catch (XMLStreamException exc) {
                // TODO
                logger.severe(exc.getMessage());
            }
        }
        if (escherMap.isSetName()) {
            model.setName(escherMap.getName());
        }
        LayoutModelPlugin layoutPlugin = (LayoutModelPlugin) model.getPlugin(LayoutConstants.shortLabel);
        if (model.getId().equals(layoutId)) {
            String layoutIdOld = layoutId;
            layoutId = layoutId + "_1";
            logger.warning(format(bundle.getString("Escher2SBML.layoutIDnotunique"), layoutIdOld, layoutId,
                    model.getId()));
        }
        Layout layout = layoutPlugin.createLayout(layoutId);
        layout.setName(layoutName);
        layout.createDimensions(width, height, nodeDepth);
        return layout;
    }


    /**
     * @param compartmentId the compartmentId to set
     */
    public void setDefaultCompartmentId(String compartmentId) {
        defaultCompartmentId = SBMLtools.toSId(compartmentId);
    }


    /**
     * @param compartmentName the compartmentName to set
     */
    public void setDefaultCompartmentName(String compartmentName) {
        defaultCompartmentName = compartmentName;
    }


    /**
     * @param layoutId the layoutId to set
     */
    public void setLayoutId(String layoutId) {
        this.layoutId = layoutId;
    }


    /**
     * @param layoutName the layoutName to set
     */
    public void setLayoutName(String layoutName) {
        this.layoutName = layoutName;
    }


    /**
     * @param nodeDepth the nodeDepth to set
     */
    public void setNodeDepth(double nodeDepth) {
        this.nodeDepth = nodeDepth;
    }


    /**
     * @param nodeLabelHeight the nodeLabelHeight to set
     */
    public void setNodeLabelHeight(double nodeLabelHeight) {
        this.nodeLabelHeight = nodeLabelHeight;
    }


    /**
     * @param z the z to set
     */
    public void setZ(double z) {
        this.z = z;
    }


    /**
     * Tries to attach a segment to a node
     *
     * @param segment                 The {@link Segment} to be attached
     * @param targetCurve             The {@link Curve} object of the current {@link SpeciesReferenceGlyph} the {@code segment} will be part of
     * @param targetCurveSegmentIndex Index of the {@code segment} within the {@code targetCurve}
     * @param escherMap               The {@link EscherMap} the {@code segment} is part of
     * @param xOffset                 x-offset of the document
     * @param yOffset                 y-offset of the document
     * @return A boolean: {@code true} if the segment could be attached, {@code false} otherwise
     */
    private boolean tryToAttach(Segment segment, Curve targetCurve,
                                int targetCurveSegmentIndex, EscherMap escherMap, double xOffset,
                                double yOffset) {
        CurveSegment lastSegment = targetCurve.getListOfCurveSegments().get(targetCurveSegmentIndex);
        String toNodeId = lastSegment.getUserObject(ESCHER_NODE_LINK).toString();
        if (toNodeId.equals(segment.getFromNodeId())) {
            LineSegment ls = convertSegment(segment, escherMap, targetCurve, xOffset, yOffset);
            ls.putUserObject(ESCHER_NODE_LINK, segment.getToNodeId());
            return true;
        } else if (toNodeId.equals(segment.getToNodeId())) {
            ;
            LineSegment ls = convertSegment(reverse(segment), escherMap, targetCurve, xOffset, yOffset);
            ls.putUserObject(ESCHER_NODE_LINK, segment.getFromNodeId());
            return true;
        }
        return false;
    }

    /**
     * Checks from given coordinates if a line could be drawn through
     * There is additionally a threshold parameter which allows points to not exactly fit on the line
     *
     * @param coordinates Coordinates to be checked if line can be drawn
     * @return Whether or not a line can be drawn through the coordinates (with threshold)
     */
    private boolean checkIfStraightLine(List<Point> coordinates) {


        double threshold = 100;

        if (coordinates.size() <= 2) {
            return true;
        }

        Point p1 = coordinates.get(0);
        Point p2 = coordinates.get(1);

        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();

        for (int i = 2; i < coordinates.size(); i++) {
            if ((dy * (coordinates.get(i).getX() - p1.getX()) - dx * (coordinates.get(i).getY() - p1.getY())) > threshold) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks for a collection of segments if all of them ares straight
     * To get the coordinates of the start and end node of a segment the escherMap is needed
     *
     * @param segments  collection of segments to be checked
     * @param escherMap escherMap to which these segments belong
     * @return Whether or not all segments are straight
     */
    private boolean checkIfAllSegmentsStraight(Collection<Segment> segments, EscherMap escherMap) {

        boolean isStraight = true;
        for (Segment seg : segments) {
            Node fromNode = escherMap.getNode(seg.getFromNodeId());
            Node toNode = escherMap.getNode(seg.getToNodeId());
            Point bp1 = seg.getBasePoint1();
            Point bp2 = seg.getBasePoint2();
            List<Point> pointsOfSegment = new ArrayList<Point>();
            if (fromNode != null) {
                pointsOfSegment.add(new Point(fromNode.getX(), fromNode.getY()));
            }
            if (toNode != null) {
                pointsOfSegment.add(new Point(toNode.getX(), toNode.getY()));
            }
            if (bp1 != null) {
                pointsOfSegment.add(bp1);
            }
            if (bp2 != null) {
                pointsOfSegment.add(bp2);
            }
            isStraight = checkIfStraightLine(pointsOfSegment);
            if (!isStraight) {
                break;
            }
        }

        return isStraight;
    }

}

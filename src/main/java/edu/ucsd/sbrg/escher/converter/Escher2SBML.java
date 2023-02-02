/* ---------------------------------------------------------------------
 * This file is part of the program EscherConverter.
 *
 * Copyright (C) 2013-2023 by the University of California, San Diego.
 * and the Eberhard Karl University of Tübingen.
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

import de.zbit.sbml.util.SBMLtools;
import edu.ucsd.sbrg.escher.model.Annotation;
import edu.ucsd.sbrg.escher.model.Point;
import edu.ucsd.sbrg.escher.model.*;
import org.jetbrains.annotations.NotNull;
import org.sbml.jsbml.*;
import org.sbml.jsbml.ext.fbc.*;
import org.sbml.jsbml.ext.fbc.converters.GPRParser;
import org.sbml.jsbml.ext.layout.*;
import org.sbml.jsbml.util.ResourceManager;

import javax.xml.stream.XMLStreamException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

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
  public static final String ID_PREFIX_REACTION = "R_";
  public static final String ID_PREFIX_REACTION_GLYPH = "RG_";
  public static final String ID_PREFIX_SPECIES_GLYPH = "SG_";
  public static final String ID_PREFIX_TEXT_GLYPH = "TG_";
  public static final String COMPARTMENT_EXTRACELLUAR_SPACE = "e";
  public static final String ID_PREFIX_SPECIES = "M_";
  private static final String ID_PREFIX_EMPTY_SET = ID_PREFIX_SPECIES_GLYPH + "empty_set_";
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
    double xOffset = canvas.isSetX() ? canvas.getX() : 0d;
    double yOffset = canvas.isSetY() ? canvas.getY() : 0d;
    double canvasWidth = canvas.getWidth();
    double canvasHeight = canvas.getWidth();
    Layout layout = initLayout(map, xOffset, yOffset);
    for (Map.Entry<String, Node> entry : map.nodes()) {
      convertNode(entry.getValue(), map, layout, xOffset, yOffset);
    }
    for (Map.Entry<String, EscherReaction> entry : map.reactions()) {
      convertReaction(entry.getValue(), map, layout, xOffset, yOffset);
    }
    for (Map.Entry<String, TextLabel> entry : map.textLabels()) {
      createTextGlyph(entry.getValue(), layout, xOffset, yOffset);
    }
    if (getInferCompartmentBoundaries()) {
      for (Map.Entry<String, EscherCompartment> entry : map.compartments()) {
        String id = entry.getKey();
        if (!(id.equalsIgnoreCase(getDefaultCompartmentId()) || id.equalsIgnoreCase(COMPARTMENT_EXTRACELLUAR_SPACE))) {
          createCompartmentGlyph(entry.getValue(), layout, xOffset, yOffset, canvasWidth, canvasHeight);
        }
      }
    }
    SBMLDocument doc = layout.getSBMLDocument();
    Model m = doc.getModel();
    if (m.hasExtension(FBCConstants.shortLabel)) {
      FBCModelPlugin fbcModelPlugin = (FBCModelPlugin) m.getPlugin(FBCConstants.shortLabel);
      fbcModelPlugin.setStrict(true);
    }
    return doc;
  }


  /**
   * Create a compartment in SBML from an {@link EscherCompartment} and add it to the
   * {@link Layout} object.
   *
   * @param ec The {@link EscherCompartment} object.
   * @param layout The {@link Layout} object of the SBML model
   * @param xOffset x-offset.
   * @param yOffset y-offset.
   */
  private void createCompartmentGlyph(EscherCompartment ec, Layout layout,
    double xOffset, double yOffset, double canvasWidth, double canvasHeight) {
	//TODO
	//for now, if no x and y coordinates are available, the compartment is set around all nodes
    CompartmentGlyph cg = layout.createCompartmentGlyph(ec.getId() + "_glyph");
    double x, y, width, height;
    if (ec.getX() == null) {
    	x = xOffset;
    	logger.severe(format(bundle.getString("Escher2SBML.inferredCompartment"),
      	      ec.getName(), "x-offset", "x-offset of the canvas: " + xOffset));
    } else {
    	x= ec.getX() - xOffset - getPrimaryNodeWidth();
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
      TextGlyph text = layout.createTextGlyph(createId(ID_PREFIX_TEXT_GLYPH, cg.getId()));
      text.setOriginOfText(compartment);
      text.createBoundingBox(compartment.getName().length() * 5d,
        getNodeLabelHeight(), getNodeDepth(), x, y, getZ());
    }
    
  }


  /**
   * Convert an {@link EscherMap} node to a glyph for SBML
   * @param node The node to be converted
   * @param escherMap The {@link EscherMap} the node is in
   * @param layout The {@link Layout} object of the SBML model
   * @param xOffset x-offset of the document
   * @param yOffset y-offset of the document
   */
  private void convertNode(Node node, EscherMap escherMap, Layout layout, double xOffset, double yOffset) {
	  
    if (node.isSetType()) {
      switch (node.getType()) {
      case metabolite:
        convertMetabolite(node, layout, xOffset, yOffset);
        break;
      case midmarker:
        convertMidmarker(node, layout, xOffset, yOffset);
        break;
      case exchange:
        convertExchange(node, layout, xOffset, yOffset);
        break;
      case multimarker:
        convertMultimarker(node, layout, xOffset, yOffset);
        break;
      default:
        convertTextLabel(node, layout, xOffset, yOffset);
        break;
      }
    }
  }


  /**
   * Converts nodes of type exchange
   * @param node The node to be converted
   * @param layout The {@link Layout} object of the SBML model
   * @param xOffset x-offset of the document
   * @param yOffset y-offset of the document
   */
  private void convertExchange(Node node, Layout layout, double xOffset, double yOffset) {
    createEmptySetGlyph(node, layout, xOffset, yOffset);
    logger.warning(format(bundle.getString("Escher2SBML.exchangeNotSupported"), node.getId()));
  }

  private void createEmptySetGlyph(Node node, Layout layout, double xOffset, double yOffset) {
    SpeciesGlyph emptySet = layout.createSpeciesGlyph(createId(ID_PREFIX_EMPTY_SET, node.getId()));
    double scale = 3d;
    double width = getPrimaryNodeWidth()/scale;
    double height = getPrimaryNodeWidth()/scale;
    emptySet.createBoundingBox(width, height, 0d,
            shiftCoordinatesToUpperLeftCorner(node.getX(), xOffset, width),
            shiftCoordinatesToUpperLeftCorner(node.getY(), yOffset, height), z);
    emptySet.setSBOTerm(SBO.getEmptySet());
    node.putUserObject(ESCHER_NODE_LINK, emptySet);
  }


  /**
   * Converts (postpones the processing of) nodes of type multimarker
   * @param node The node to be converted
   */
  private void convertMultimarker(Node node, Layout layout, double xOffset, double yOffset) {
    if (!node.isSetId()) {
      logger.warning(format(bundle.getString("Escher2SBML.undefinedID"), node.toString()));
    } else {
      boolean hasMultipleConnections = false;
      for (Iterator<Entry<String, List<String>>> it = node.getConnectedSegments().iterator(); it.hasNext(); ) {
        List<String> segments = it.next().getValue();
        if (segments.size() > 1) {
          hasMultipleConnections = true;
        }
      }
      if (!hasMultipleConnections) {
        /* This multimarker has only one connection to a midmarker, i.e., it represents a source or sink node, which we
         * have to make explicit.
         */
        createEmptySetGlyph(node, layout, xOffset, yOffset);
      }
    }
  }


  /**
   * Converts the text label nodes to text glyphs
   * @param node The text label node to be converted
   * @param layout The {@link Layout} object of the SBML model
   * @param xOffset x-offset of the document
   * @param yOffset y-offset of the document
   */
  private void convertTextLabel(Node node, Layout layout, double xOffset, double yOffset) {
    createTextGlyph(node, layout, xOffset, yOffset);
    logger.info(format(bundle.getString("Escher2SBML.skippingNode"), node.toString()));
  }


  /**
   * Converts an {@link EscherReaction} to a {@link ReactionGlyph}
   * @param escherReaction The reaction to be converted
   * @param escherMap The {@link EscherMap} the {@code escherReaction} is in
   * @param layout The {@link Layout} object of the SBML model
   * @param xOffset x-offset of the document
   * @param yOffset y-offset of the document
   */
  private Reaction convertReaction(EscherReaction escherReaction,
    EscherMap escherMap, Layout layout,  double xOffset, double yOffset) {
    ReactionGlyph rGlyph = createReactionGlyph(escherReaction, layout, xOffset, yOffset);
    Reaction reaction = (Reaction) rGlyph.getReactionInstance();
    convertGeneProteinReactionAssociation(escherReaction, reaction);
    // Go through each metabolite and connect it to the reaction.
    Map<String, SpeciesReferenceGlyph> srgMap = new HashMap<>();
    for (Map.Entry<String, Metabolite> entry : escherReaction.getMetabolites().entrySet()) {
      Metabolite metabolite = entry.getValue();
      if (metabolite.getId() != null) {
        // Each metabolite can be represented in multiple nodes, so we need to find those in this reaction, but also these can be multiple...
        Set<Node> setOfNodes = escherReaction.intersect(escherMap.getNodes(metabolite.getId()));
        srgMap.putAll(createSpeciesReferenceGlyphs(metabolite, setOfNodes, layout, rGlyph, reaction));
      }
    }
    // Create a set of all segments to be processed
    Set<Segment> segments = new HashSet<>();
    Map<String, Segment> toNodeId2Segment = new HashMap<>();
    for (Entry<String, Segment> entry : escherReaction.segments()) {
      Segment segment = entry.getValue();
      Node fromNode = escherMap.getNode(segment.getFromNodeId());
      Node toNode = escherMap.getNode(segment.getToNodeId());
      SpeciesReferenceGlyph srGlyph = null;
      boolean isProduct = false;
      /* Go through all segments and turn them around if necessary according to the definition in the specification of
       * SBML Level 3 Version 1 Layout Release 1, page 17, lines 36-37, https://doi.org/10.1515/jib-2015-267.
       * "the line segments have their start element at the ReactionGlyph and their end element at the SpeciesGlyph" for
       * reactants and products. This means, the end point of a line segment have to be metabolites, irrespective of
       * their role (it does not matter whether they are consumed or produced). Also, segments have to point away from
       * midmarkers, i.e., reaction glyphs.
       */
      if ((toNode != null) && (fromNode != null)) {
        if (fromNode.isMetabolite()) {
          srGlyph = srgMap.get(fromNode.getBiggId());
          Metabolite m = escherReaction.getMetabolite(fromNode.getBiggId());
          if (m != null) {
            if (!m.isSetCoefficient()) {
              logger.severe(format(bundle.getString("Escher2SBML.metaboliteWithoutStoichiometry"),
                      fromNode.getBiggId(), escherReaction.getBiggId()));
            } else if (m.getCoefficient() > 0d) {
              isProduct = true;
            }
            segment = reverse(segment);
          } else {
            logger.severe(format(bundle.getString("Escher2SBML.noNodeWithBiGGId"), fromNode.getBiggId()));
          }
        } else if (toNode.isMetabolite()) {
          srGlyph = srgMap.get(toNode.getBiggId());
          Metabolite m = escherReaction.getMetabolite(toNode.getBiggId());
          if (m == null) {
            logger.severe(format(bundle.getString("Escher2SBML.noNodeWithBiGGId"), toNode.getBiggId()));
          } else if (!m.isSetCoefficient()) {
            logger.severe(format(bundle.getString("Escher2SBML.metaboliteWithoutStoichiometry"),
                    toNode.getBiggId(), escherReaction.getBiggId()));
          } else if (m.getCoefficient() > 0d) {
            isProduct = true;
          }
        } else if (toNode.isMidmarker()) {
          segment = reverse(segment);
          SpeciesGlyph sg = (SpeciesGlyph) fromNode.getUserObject(ESCHER_NODE_LINK);
          if (sg != null) {
            // fromNode has to be a multimarker (now actually the new toNode, but the pointer hasn't changed).
            isProduct = escherReaction.hasReactants();
            String srGlyphId = createSpeciesReferenceGlyphID(sg.getId(), reaction, layout.getModel());
            srGlyph = rGlyph.createSpeciesReferenceGlyph(srGlyphId, sg.getId());
            srGlyph.setRole(isProduct ? SpeciesReferenceRole.PRODUCT : SpeciesReferenceRole.SUBSTRATE);
            srgMap.put(srGlyph.getId(), srGlyph);
          }
        }
      }
      if (srGlyph != null) {
        LineSegment ls = convertSegment(segment, escherMap, srGlyph.createCurve(), xOffset, yOffset);
        // memorize the toNode id of the segment in the lineSegment to make access easier later on.
        ls.putUserObject(ESCHER_NODE_LINK, isProduct ? segment.getFromNodeId() : segment.getToNodeId());
      } else {
        segments.add(segment);
        toNodeId2Segment.put(segment.getToNodeId(), segment);
      }
    }
    // At this point, all segements point from midmarkers to metabolites.

    /* 1) We need to glue the remaining segments to substrate and product first.
     * 2) Next, we need to glue the still remaining segments (if any) to the side substrates and sideproducts
     * 3) Last, we glue the left-over segments to any other speciesReferenceGlyphy we have.
     */
    // So, let's store the diffent kinds of nodes in separate maps so that we can prioritze when drawing connections.
    Map<String, SpeciesReferenceGlyph> srgMapMain = new HashMap<>();
    Map<String, SpeciesReferenceGlyph> srgMapSide = new HashMap<>();
    Map<String, SpeciesReferenceGlyph> srgMapOther = new HashMap<>();
    for (Entry<String, SpeciesReferenceGlyph> entry : srgMap.entrySet()) {
      SpeciesReferenceGlyph srg = entry.getValue();
      switch (srg.getRole()) {
        case SUBSTRATE:
        case PRODUCT:
          srgMapMain.put(entry.getKey(), srg);
          break;
        case SIDESUBSTRATE:
        case SIDEPRODUCT:
          srgMapSide.put(entry.getKey(), srg);
          break;
        case ACTIVATOR:
        case INHIBITOR:
        case MODIFIER:
          // In later versions there might be separate entries for modifiers.
        case UNDEFINED:
        default:
          srgMapOther.put(entry.getKey(), srg);
          break;
      }
    }
    // Now we create the connections from midmarkers to the metabolites
    Set<String> stickyNodes = new HashSet<>(); // collect from node ids that are already in the graph.
    stickyNodes.add(escherReaction.getMidmarker().getId());
    stickyNodes.addAll(connect(srgMapMain, stickyNodes, escherMap, xOffset, yOffset, segments, toNodeId2Segment));
    stickyNodes.addAll(connect(srgMapSide, stickyNodes, escherMap, xOffset, yOffset, segments, toNodeId2Segment));
    connect(srgMapOther, stickyNodes, escherMap, xOffset, yOffset, segments, toNodeId2Segment);
    if (!segments.isEmpty()) {
      logger.warning(format(bundle.getString("Escher2SBML.segmentsLost"), segments, reaction.getId()));
    }
    logger.fine(bundle.getString("Escher2SBML.done"));
    return reaction;
  }

  private Set<String> connect(Map<String, SpeciesReferenceGlyph> map, Set<String> sticky,
                              EscherMap escherMap, double xOffset, double yOffset, Set<Segment> segments,
                              Map<String, Segment> toNodeId2Segment) {
    Set<String> stickyNodes = new HashSet<>(); // collect from node ids that are already in the graph.
    String currFromNode = null;
    for (SpeciesReferenceGlyph srg : map.values()) {
      // Those either (main) substraintes or (main) products)
      do {
        ListOf<CurveSegment> listOfCurveSegments = srg.getCurve().getListOfCurveSegments();
        String fromId = listOfCurveSegments.getFirst().getStart().getUserObject(ESCHER_NODE_LINK).toString();
        Segment segment = toNodeId2Segment.get(fromId);
        if (segment != null) {
          currFromNode = segment.getFromNodeId();
          stickyNodes.add(currFromNode);
          convertSegment(segment, escherMap, srg.getCurve(), xOffset, yOffset);
          // Move the last line segment, i.e., the newly created one, to the first position to retain the order.
          listOfCurveSegments.add(0, listOfCurveSegments.remove(listOfCurveSegments.size() - 1));
          segments.remove(segment);
        }
      } while ((currFromNode != null) && !sticky.contains(currFromNode));
    }
    return stickyNodes;
  }

  /**
   *
   * @param escherReaction
   * @param reaction
   */
  private void convertGeneProteinReactionAssociation(EscherReaction escherReaction, Reaction reaction) {
    if (escherReaction.isSetGeneReactionRule()) {
      GPRParser.parseGPR(reaction, escherReaction.getGeneReactionRule(), true, false);
      FBCModelPlugin fbcModelPlugin = (FBCModelPlugin) reaction.getModel().getPlugin(FBCConstants.shortLabel);
      for (Gene g : escherReaction.getGenes().values()) {
        GeneProduct gp = fbcModelPlugin.getGeneProduct(g.getName());
        if (gp != null) {
          if (g.isSetId()) {
            gp.setLabel(g.getId());
          }
          if (g.isSetAnnotation()) {
            Annotation annotation = g.getAnnotation();
            if (annotation.isSetSBO()) {
              gp.setSBOTerm(annotation.getSBO());
            }
          }
          gp.setName(gp.getId());
        }
      }
    }
  }


  /**
   * Converts a segment to a line segment
   * @param fromNode The node the segment starts from
   * @param toNode The node where the segment ends
   * @param basePoint1 base point for the curve TODO
   * @param basePoint2 base point for the curve
   * @param curve A curve object as used by SBML
   * @param xOffset x-offset of the document
   * @param yOffset y-offset of the document
   * @return A line segment (either a cubic bezier or a line)
   */
  private LineSegment convertSegment(Node fromNode, Node toNode,
    Point basePoint1, Point basePoint2, Curve curve, double xOffset, double yOffset) {
    LineSegment lineSegment;
    if ((basePoint1 != null) && (basePoint2 != null)) {
      CubicBezier cb = curve.createCubicBezier();
      cb.createBasePoint1(basePoint1.getX() - xOffset, basePoint1.getY() - yOffset, z);
      cb.createBasePoint2(basePoint2.getX() - xOffset, basePoint2.getY() - yOffset, z);
      lineSegment = cb;
    } else {
      lineSegment = curve.createLineSegment();
    }
    double x = fromNode.getX() - xOffset;
    double y = fromNode.getY() - yOffset;
    double width = 0d, height = 0d;
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
    lineSegment.createStart(x, y, z).putUserObject(ESCHER_NODE_LINK, fromNode.getId());
    x = toNode.getX() - xOffset;
    y = toNode.getY() - yOffset;
    lineSegment.createEnd(x, y, z).putUserObject(ESCHER_NODE_LINK, toNode.getId());
    return lineSegment;
  }

  @NotNull
  private String createId(String prefix, String id) {
    return SBMLtools.toSId(prefix + id);
  }

  /**
   * Extracts all necessary parameters from the segment needed for converting it to a line segment
   * @param segment The segment to be converted
   * @param map The {@link EscherMap} the {@code segment} is in
   * @param curve A curve object as used by SBML
   * @param xOffset x-offset of the document
   * @param yOffset y-offset of the document
   * @return A line segment (either a cubic bezier or a line)
   */
  private LineSegment convertSegment(Segment segment, EscherMap map, Curve curve, double xOffset, double yOffset) {
    LineSegment ls = convertSegment(map.getNode(segment.getFromNodeId()),
            map.getNode(segment.getToNodeId()), segment.getBasePoint1(),
            segment.getBasePoint2(), curve, xOffset, yOffset);
    return ls;
  }


  /**
   * Creates an id
   * @param prefix
   * @param count
   * @return A string id
   */
  private String createId(String prefix, int count) {
    return prefix + (count + 1);
  }


  /**
   * Creates a reaction glyph from an {@link EscherReaction}
   * @param escherReaction The {@link EscherReaction} to be converted
   * @param layout The {@link Layout} object of the SBML model
   * @param xOffset x-offset of the document
   * @param yOffset y-offset of the document
   * @return A {@link ReactionGlyph}
   */
  private ReactionGlyph createReactionGlyph(@NotNull EscherReaction escherReaction, Layout layout, double xOffset, double yOffset) {
    ReactionGlyph rGlyph = null;
    if (escherReaction.isSetId()) {
      rGlyph = layout.getReactionGlyph(ID_PREFIX_REACTION_GLYPH + escherReaction.getId());
    }
    if (rGlyph == null) {
      // It can still be null if there is another glyph in the layout that is not a reaction glyph but has an identical id.
      String rGlyphId = escherReaction.isSetId() ?
              SBMLtools.toSId(ID_PREFIX_REACTION_GLYPH + escherReaction.getId()) :
              ID_PREFIX_REACTION_GLYPH + (layout.getReactionGlyphCount() + 1);
        rGlyph = layout.createReactionGlyph(rGlyphId);
    }
    if (escherReaction.isSetBiggId()) {
      Reaction reaction;
      String reactionId = createId(ID_PREFIX_REACTION, escherReaction.getBiggId());
      Model model = layout.getModel();
      if (!model.containsReaction(reactionId)) {
        reaction = model.createReaction(reactionId);
        if (escherReaction.isSetMidmarker() && escherReaction.getMidmarker().isSetCompartment()) {
          reaction.setCompartment(SBMLtools.toSId(escherReaction.getMidmarker().getCompartment()));
          if (!model.containsCompartment(reaction.getCompartment())) {
            createCompartment(model, reaction.getCompartment());
          }
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
        reaction.setName(escherReaction.isSetName() ? escherReaction.getName() : escherReaction.getBiggId());
      }
      //noinspection deprecation
      reaction.setFast(false);
      reaction.setSBOTerm(SBO.getProcess());
    }
    return rGlyph;
  }


  /**
   * Midmarker nodes are converted. These are usually nodes in the middle of an arrow, so usually reactions.
   * @param node The node to be converted (of type "midmarker")
   * @param layout The {@link Layout} object of the SBML model
   * @param xOffset x-offset of the document
   * @param yOffset y-offset of the document
   * @return A {@link ReactionGlyph} if an id can be extracted, {@code null} otherwise
   */
  private ReactionGlyph convertMidmarker(Node node, Layout layout, double xOffset, double yOffset) {
    String rId = extractReactionId(node.getConnectedSegments());
    if (rId != null) {
      String rSId = SBMLtools.toSId(ID_PREFIX_REACTION_GLYPH + rId);
      Model model = layout.getModel();
      if (!model.containsUniqueNamedSBase(rSId)) {
        ReactionGlyph rGlyph = layout.createReactionGlyph(rSId);
        double width;
        double height;
        if (node.isSetWidth()) {
          width = node.getWidth();
        } else {
          width = getPrimaryNodeWidth() * getReactionNodeRatio();
          node.setWidth(width);
        }
        if (node.isSetHeight()) {
          height = node.getHeight();
        } else {
          height = getPrimaryNodeHeight() * getReactionNodeRatio();
          node.setHeight(height);
        }
        // Also shift node because again the center of the node would be used as coordinate instead of upper-left corner
        rGlyph.createBoundingBox(width, height, nodeDepth,
                shiftCoordinatesToUpperLeftCorner(node.getX(), xOffset, width),
                shiftCoordinatesToUpperLeftCorner(node.getY(), yOffset, height), z);
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
   * Shifts the postion because Escher uses the center of the node as its coordinate instead of upper-left corner
   * @param position either of the dimensions x, y, or z, just needs to match the size
   * @param offset general offset of the canvas in this dimention (x, y, or z offset)
   * @param size with, height, or depth, matching which coordinate is used.
   * @return shifted coordinate
   */
  private double shiftCoordinatesToUpperLeftCorner(double position, double offset, double size) {
    return position - offset - size / 2d;
  }


  /**
   * Convert a node of type "metabolite"
   * @param node The node to be converted (of type "metabolite")
   * @param layout The {@link Layout} object of the SBML model
   * @param xOffset x-offset of the document
   * @param yOffset y-offset of the document
   * @return A {@link SpeciesGlyph}
   */
  private SpeciesGlyph convertMetabolite(Node node, Layout layout, double xOffset, double yOffset) {
    SpeciesGlyph sGlyph;
    sGlyph = layout.createSpeciesGlyph(createSpeciesGlyphId(node, layout));
    // Not defined in SBML Layout: sGlyph.setName(node.getName());
    double width, height;
    if (node.isSetWidth()) {
      width = node.getWidth();
    } else {
      width = node.isPrimary() ? getPrimaryNodeWidth() : getSecondaryNodeRatio() * getPrimaryNodeWidth();
      node.setWidth(width);
    }
    if (node.isSetHeight()) {
      height = node.getHeight();
    } else {
      height = node.isPrimary() ? getPrimaryNodeHeight() : getSecondaryNodeRatio() * getPrimaryNodeHeight();
      node.setHeight(height);
    }
    // Correct node position with offset and also because Escher uses the center whereas layout uses the top-left corner of bbox
    sGlyph.createBoundingBox(width, height, nodeDepth,
            shiftCoordinatesToUpperLeftCorner(node.getX(), xOffset, width),
            shiftCoordinatesToUpperLeftCorner(node.getY(), yOffset, height), z);
    sGlyph.setSBOTerm(SBO.getSimpleMolecule());
    if (node.isSetBiggId()) {
      String sId = createId(ID_PREFIX_SPECIES, node.getBiggId());
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
        species.setInitialAmount(Double.NaN);
        if (node.isSetBiggId()) {
          species.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "https://identifiers.org/bigg.metabolite/" + node.getBiggId()));
        }
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
   *
   * @param model
   * @param compartmentId
   * @return
   */
  private Compartment createCompartment(Model model, String compartmentId) {
    return createCompartment(model, compartmentId, null);
  }

  /**
   * Creates a {@link Compartment}
   * @param model The SBML model
   * @param compartmentId Id of the compartment
   * @param compartmentName Name of the compartment
   * @return A {@link Compartment}
   */
  private Compartment createCompartment(Model model, String compartmentId, String compartmentName) {
    Compartment compartment = model.getCompartment(compartmentId);
    if (compartment == null) {
      compartment = model.createCompartment(compartmentId);
      compartment.setConstant(true);
      if (compartmentId.equals(defaultCompartmentId)) {
        compartment.setSBOTerm(410); // implicit compartment
      } else {
        compartment.setSBOTerm(SBO.getCompartment()); // physical compartment
      }
      if ((compartmentName == null) || (compartmentName.length() == 0)) {
        compartmentName = resolveCompartmentCode(compartmentId);
      }
      compartment.setName(compartmentName);
      compartment.setSpatialDimensions(3);
      compartment.setSize(Double.NaN);
      if (compCode.containsKey(compartmentId)) {
        compartment.addCVTerm(new CVTerm(CVTerm.Qualifier.BQB_IS, "https://identifiers.org/bigg.compartment/" + compartmentId));
      }
    }
    return compartment;
  }


  /**
   * Creates a {@link SpeciesGlyph} id (starting with sg_)
   * @param node The node for which the id is to be created
   * @param layout The {@link Layout} object of the SBML model
   * @return A a {@link SpeciesGlyph} id (starting with sg_)
   */ 
  private String createSpeciesGlyphId(Node node, Layout layout) {
    return node.isSetId() ? createId(ID_PREFIX_SPECIES_GLYPH, node.getId()) :
            createId(ID_PREFIX_SPECIES_GLYPH, layout.getSpeciesGlyphCount());
  }


  /**
   * Creates a hash map of all {@link SpeciesReferenceGlyph}s and their ids corresponding to one {@code metabolite} of
   * an {@link EscherReaction}
   * @param metabolite A {@link Metabolite} of an {@link EscherReaction}
   * @param setOfNodes A set of nodes represented by this {@code metabolite}
   * @param layout The {@link Layout} object of the SBML model
   * @param rGlyph The {@link ReactionGlyph} the {@code metabolite} is in.
   * @param reaction The {@link Reaction} the {@code metabolite} is in.
   * @return A hash map of all {@link SpeciesReferenceGlyph}s and their ids
   */
  private Map<String, SpeciesReferenceGlyph> createSpeciesReferenceGlyphs(
    Metabolite metabolite, Set<Node> setOfNodes, Layout layout, ReactionGlyph rGlyph, Reaction reaction) {
    String biggID = metabolite.getId();
    SpeciesGlyph sGlyph;
    Model model = reaction.getModel();
    Map<String, SpeciesReferenceGlyph> srgMap = new HashMap<>();
    Set<String> setOfCompartments = new HashSet<>();
    for (Node node : setOfNodes) {
      sGlyph = layout.getSpeciesGlyph(createId(ID_PREFIX_SPECIES_GLYPH, node.getId()));
      if (sGlyph != null) {
        String srGlyphId = createSpeciesReferenceGlyphID(metabolite.getId(), reaction, model);
        SpeciesReferenceGlyph srGlyph = rGlyph.createSpeciesReferenceGlyph(srGlyphId, sGlyph.getId());

        sGlyph.putUserObject(ESCHER_NODE_LINK, node);
        // Create the core object for this species reference
        SimpleSpeciesReference ssr = null;
        String sId = createId(ID_PREFIX_SPECIES, biggID);
        if (metabolite.isSetCoefficient()) {
          /* Note:
           * The SBO terms for the species references in SBML core are just set to reactant/product and not more
           * specific because the same core model could have multiple graphical displays in which the roles may differ.
           * So we want to be more general.
           */
          if (metabolite.getCoefficient() < 0d) {
            ssr = reaction.createReactant(reaction.getId() + "_reactant_" + (reaction.getReactantCount() + 1), sId);
            ((SpeciesReference) ssr).setStoichiometry(-metabolite.getCoefficient());
            srGlyph.setRole(node.isPrimary() ? SpeciesReferenceRole.SUBSTRATE : SpeciesReferenceRole.SIDESUBSTRATE);
            ssr.setSBOTerm(SBO.getReactant());
          } else { // metabolite.getCoefficient() >= 0d
            ssr = reaction.createProduct(reaction.getId() + "_product_" + (reaction.getProductCount() + 1), sId);
            ((SpeciesReference) ssr).setStoichiometry(metabolite.getCoefficient());
            srGlyph.setRole(node.isPrimary() ? SpeciesReferenceRole.PRODUCT : SpeciesReferenceRole.SIDEPRODUCT);
            ssr.setSBOTerm(SBO.getProduct());
          }
        }
        if (ssr == null) {
          ssr = reaction.createModifier(reaction.getId() + "_modifier_" + (reaction.getModifierCount() + 1), sId);
          srGlyph.setRole(Double.isNaN(metabolite.getCoefficient()) ? SpeciesReferenceRole.UNDEFINED : SpeciesReferenceRole.MODIFIER);
          ssr.setSBOTerm(srGlyph.getRole().toSBOterm());
        }
        if (ssr instanceof SpeciesReference) {
          SpeciesReference sr = (SpeciesReference) ssr;
          sr.setConstant(true);
        }
        Species species = ssr.getSpeciesInstance();
        if ((species != null) && (species.isSetCompartment())) {
          setOfCompartments.add(species.getCompartment());
        }
        srGlyph.setSBOTerm(srGlyph.getSpeciesReferenceRole().toSBOterm());
        srGlyph.setSpeciesReference(ssr);
        if (srgMap.containsKey(metabolite.getId())) {
          logger.warning(format(bundle.getString("Escher2SBML.replacementGlyph"), metabolite.getId()));
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
        /* This is normally not an error because some reactions operate across compartments and can't be assigned to
         * either one of them.
         */
        logger.warning(format(bundle.getString("Escher2SBML.reactionCompartmentUnknown"), reaction.getId()));
      }
    }
    return srgMap;
  }

  @NotNull
  private static String createSpeciesReferenceGlyphID(String nodeID, Reaction reaction, Model model) {
    String srGlyphId = reaction.getId() + "_srg_" + nodeID;
    srGlyphId = srGlyphId.replaceAll("\\+", "");
    if (model.containsUniqueNamedSBase(srGlyphId)) {
      int i = 0;
      do {
        i++;
      } while (model.containsUniqueNamedSBase(srGlyphId + "_" + i));
      srGlyphId += "_" + i;
      logger.warning(format(bundle.getString("Escher2SBML.metaboliteDuplication"),
        nodeID, reaction.getId(), ++i));
    }
    return SBMLtools.toSId(srGlyphId);
  }


  /**
   * Create a text glyph from an {@link EscherReaction}
   * @param reaction The {@link EscherReaction} the text glyph is extracted from
   * @param rGlyph The reaction glyph the text glyph corresponds to and where the text glyph has to be positioned next to
   * @param layout The {@link Layout} object of the SBML model
   * @param xOffset x-offset of the document
   * @param yOffset y-offset of the document
   */
  private void createTextGlyph(EscherReaction reaction, ReactionGlyph rGlyph, Layout layout, double xOffset, double yOffset) {
    double x = reaction.getLabelX() - xOffset;
    double y = reaction.getLabelY() - yOffset - reactionLabelHeight;
    TextGlyph label = layout.createTextGlyph(createTextGlyphId(layout));
    label.createBoundingBox(getLabelWidth(), reactionLabelHeight, nodeDepth, x, y, z);
    label.setOriginOfText(rGlyph.getReaction());
    label.setGraphicalObject(rGlyph);
  }


  /**
   * Create a text glyph from a node
   * @param node The node the text glyph is extracted from
   * @param layout The {@link Layout} object of the SBML model
   * @param xOffset x-offset of the document
   * @param yOffset y-offset of the document
   * @return A {@link TextGlyph}
   */
  private TextGlyph createTextGlyph(Node node, Layout layout, double xOffset, double yOffset) {
    return createTextGlyph(node, layout, xOffset, yOffset, null);
  }


  /**
   * Create a text glyph from a node
   * @param node The node the text glyph is extracted from
   * @param layout The {@link Layout} object of the SBML model
   * @param xOffset x-offset of the document
   * @param yOffset y-offset of the document
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
   * @param label A {@link TextLabel} of an {@link EscherMap}
   * @param layout The {@link Layout} object of the SBML model
   * @param xOffset x-offset of the document
   * @param yOffset y-offset of the document
   */
  private void createTextGlyph(TextLabel label, Layout layout, double xOffset,
    double yOffset) {
    String id = createTextGlyphId(label, layout);
    Model model = layout.getModel();
    NamedSBase element = model.findNamedSBase(id);
    if (element != null) {
      logger.warning(format(bundle.getString("Escher2SBML.textLabelIdNotUnique"), id, element.getElementName()));
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
   * @param layout The {@link Layout} object of the SBML model
   * @return A string {@link TextGlyph} id (starting with tg_)
   */
  private String createTextGlyphId(Layout layout) {
    return createId(ID_PREFIX_TEXT_GLYPH, layout.getTextGlyphCount());
  }


  /**
   * Creates a {@link TextGlyph} id, if possible from the id of the {@link TextLabel}
   * @param label A {@link TextLabel}
   * @param layout The {@link Layout} object of the SBML model
   * @return A string {@link TextGlyph} id
   */
  private String createTextGlyphId(TextLabel label, Layout layout) {
    return label.isSetId() ? SBMLtools.toSId(ID_PREFIX_TEXT_GLYPH + label.getId()) : createTextGlyphId(layout);
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
   * @param escherMap The {@link EscherMap} the layout is to be created from
   * @param xOffset x-offset of the document
   * @param yOffset y-offset of the document
   */
  private Layout initLayout(EscherMap escherMap, double xOffset, double yOffset) {
    Canvas canvas = escherMap.getCanvas();
    double width = canvas.isSetWidth() ? canvas.getWidth() - xOffset : getCanvasDefaultWidth();
    double height = canvas.isSetHeight() ? canvas.getHeight() - yOffset : getCanvasDefaultHeight();
    SBMLDocument doc = new SBMLDocument(3, 1);
    Model model = doc.createModel(escherMap.isSetId() ? SBMLtools.toSId(escherMap.getId()) : "_default");
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
    	logger.warning(format(bundle.getString("Escher2SBML.layoutIDnotunique"),
                layoutIdOld, layoutId, model.getId()));
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
   * @param segment The {@link Segment} to be attached
   * @param targetCurve The {@link Curve} object of the current {@link SpeciesReferenceGlyph} the {@code segment} will be part of
   * @param targetCurveSegmentIndex Index of the {@code segment} within the {@code targetCurve}
   * @param escherMap The {@link EscherMap} the {@code segment} is part of
   * @param xOffset x-offset of the document
   * @param yOffset y-offset of the document
   * @return A boolean: {@code true} if the segment could be attached, {@code false} otherwise
   */
  private boolean tryToAttach(Segment segment, Curve targetCurve,
    int targetCurveSegmentIndex, EscherMap escherMap, double xOffset, double yOffset) {
    CurveSegment lastSegment = targetCurve.getListOfCurveSegments().get(targetCurveSegmentIndex);
    String toNodeId = lastSegment.getUserObject(ESCHER_NODE_LINK).toString();
    if (toNodeId.equals(segment.getFromNodeId())) {
      LineSegment ls = convertSegment(segment, escherMap, targetCurve, xOffset, yOffset);
      ls.putUserObject(ESCHER_NODE_LINK, segment.getToNodeId());
      return true;
    } else if (toNodeId.equals(segment.getToNodeId())) {
      LineSegment ls = convertSegment(reverse(segment), escherMap, targetCurve, xOffset, yOffset);
      ls.putUserObject(ESCHER_NODE_LINK, segment.getFromNodeId());
      return true;
    }
    return false;
  }

}
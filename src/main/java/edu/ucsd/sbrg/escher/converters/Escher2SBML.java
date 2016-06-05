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
package edu.ucsd.sbrg.escher.converters;

import de.zbit.sbml.util.SBMLtools;
import edu.ucsd.sbrg.escher.models.*;
import edu.ucsd.sbrg.escher.models.Point;
import org.sbml.jsbml.*;
import org.sbml.jsbml.ext.layout.*;
import org.sbml.jsbml.util.ResourceManager;

import javax.xml.stream.XMLStreamException;
import java.text.MessageFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.0
 */
public class Escher2SBML extends Escher2Standard<SBMLDocument> {

  /**
   * A {@link Logger} for this class.
   */
  static final        Logger
                                     logger =
      Logger.getLogger(Escher2SBML.class.getName());
  /**
   * Localization support.
   */
  public static final ResourceBundle
                                     bundle =
      ResourceManager.getBundle("Messages");
  private String defaultCompartmentId;
  private String defaultCompartmentName;
  private String layoutId;
  private String layoutName;
  private double nodeDepth;
  private double nodeLabelHeight;
  private double reactionLabelHeight;
  private double z;


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.converters.Escher2Standard#convert(edu.ucsd.sbrg.escher.models.EscherMap)
   */
  @Override
  public SBMLDocument convert(EscherMap map) {
    preprocessDataStructure(map);
    Canvas canvas = map.getCanvas();
    double xOffset = canvas.isSetX() ? canvas.getX().doubleValue() : 0d;
    double yOffset = canvas.isSetY() ? canvas.getY().doubleValue() : 0d;
    Map<String, String> node2glyph = new HashMap<String, String>();
    Map<String, Node> multimarkers = new HashMap<String, Node>();
    Layout layout = initLayout(map, xOffset, yOffset);
    for (Map.Entry<String, Node> entry : map.nodes()) {
      convertNode(entry.getValue(), map, node2glyph, multimarkers, layout,
          xOffset, yOffset);
    }
    for (Map.Entry<String, EscherReaction> entry : map.reactions()) {
      convertReaction(entry.getValue(), map, layout, node2glyph, xOffset,
          yOffset);
    }
    for (Map.Entry<String, TextLabel> entry : map.textLabels()) {
      createTextGlyph(entry.getValue(), layout, xOffset, yOffset);
    }
    if (getInferCompartmentBoundaries()) {
      for (Map.Entry<String, EscherCompartment> entry : map.compartments()) {
        String id = entry.getKey();
        if (!(id.equalsIgnoreCase("n") || id
            .equalsIgnoreCase(getDefaultCompartmentId()) || id
            .equalsIgnoreCase("e"))) {
          createCompartmentGlyph(entry.getValue(), layout, xOffset, yOffset);
        }
      }
    }
    return layout.getSBMLDocument();
  }


  /**
   * @param ec
   * @param layout
   * @param xOffset
   * @param yOffset
   */
  private void createCompartmentGlyph(EscherCompartment ec, Layout layout,
      double xOffset, double yOffset) {
    CompartmentGlyph cg = layout.createCompartmentGlyph(ec.getId() + "_glyph");
    double x = ec.getX() - xOffset - getPrimaryNodeWidth();
    double y = ec.getY() - yOffset - getPrimaryNodeHeight();
    cg.createBoundingBox(ec.getWidth(), ec.getHeight(), getNodeDepth(), x, y,
        getZ());
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
   * @param node
   * @param escherMap
   * @param node2glyph
   * @param multimarkers
   * @param layout
   * @param xOffset
   * @param yOffset
   */
  private void convertNode(Node node, EscherMap escherMap,
      Map<String, String> node2glyph, Map<String, Node> multimarkers,
      Layout layout, double xOffset, double yOffset) {
    if (node.isSetType()) {
      switch (node.getType()) {
      case metabolite:
        convertMetabolite(node, node2glyph, layout, xOffset, yOffset);
        break;
      case midmarker:
        convertMidmarker(node, node2glyph, layout, xOffset, yOffset);
        break;
      case exchange:
        convertExchange(node, node2glyph, layout, xOffset, yOffset);
        break;
      case multimarker:
        convertMultimarker(node, multimarkers, xOffset, yOffset);
        break;
      default:
        converteTextLabel(node, layout, xOffset, yOffset);
        break;
      }
    }
  }


  /**
   * @param node
   * @param node2glyph
   * @param layout
   * @param xOffset
   * @param yOffset
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
    logger.warning(MessageFormat
        .format(bundle.getString("Escher2SBML.exchangeNotSupported"),
            node.getId()));
  }


  /**
   * @param node
   * @param multimarkers
   */
  private void convertMultimarker(Node node, Map<String, Node> multimarkers,
      double xOffset, double yOffset) {
    if (node.isSetId()) {
      // process these later...
      multimarkers.put(node.getId(), node);
    } else {
      logger.warning(MessageFormat
          .format(bundle.getString("Escher2SBML.undefinedID"),
              node.toString()));
    }
  }


  /**
   * @param node
   * @param layout
   * @param xOffset
   * @param yOffset
   */
  private void converteTextLabel(Node node, Layout layout, double xOffset,
      double yOffset) {
    createTextGlyph(node, layout, xOffset, yOffset);
    logger.info(MessageFormat
        .format(bundle.getString("Escher2SBML.skippingNode"), node.toString()));
  }


  /**
   * @param escherReaction
   * @param escherMap
   * @param layout
   * @param node2glyph
   * @param xOffset
   * @param yOffset
   */
  private Reaction convertReaction(EscherReaction escherReaction,
      EscherMap escherMap, Layout layout, Map<String, String> node2glyph,
      double xOffset, double yOffset) {
    ReactionGlyph
        rGlyph =
        createReactionGlyph(escherReaction, layout, node2glyph, xOffset,
            yOffset);
    Reaction reaction = (Reaction) rGlyph.getReactionInstance();
    if (escherReaction.isSetGeneReactionRule()) {
      // TODO
    }
    // Go through each metabolite and connect it to the reaction.
    Map<String, SpeciesReferenceGlyph>
        srgMap =
        new HashMap<String, SpeciesReferenceGlyph>();
    for (Map.Entry<String, Metabolite> entry : escherReaction.getMetabolites()
                                                             .entrySet()) {
      Metabolite metabolite = entry.getValue();
      // Each metabolite can be represented in multiple nodes, so we need to find those in this reaction, but also these can be multiple...
      Set<Node>
          setOfNodes =
          escherReaction.intersect(escherMap.getNodes(metabolite.getId()));
      srgMap.putAll(createSpeciesReferenceGlyphs(metabolite, setOfNodes, layout,
          node2glyph, rGlyph, reaction));
    }
    // Create a set of all segments to be processed
    Set<Segment> segments = new HashSet<Segment>();
    for (Entry<String, Segment> entry : escherReaction.segments()) {
      Segment segment = entry.getValue();
      Node fromNode = escherMap.getNode(segment.getFromNodeId());
      Node toNode = escherMap.getNode(segment.getToNodeId());
      SpeciesReferenceGlyph srGlyph = null;
      boolean isProduct = false;
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
          logger.severe(MessageFormat.format(
              bundle.getString("Escher2SBML.metaboliteWithoutStoichiometry"),
              toNode.getBiggId(), escherReaction.getBiggId()));
        } else if (metabolite.getCoefficient() <= 0d) {
          segment = reverse(segment);
        } else {
          isProduct = true;
        }
      }
      if (srGlyph != null) {
        LineSegment
            ls =
            convertSegment(segment, escherMap, srGlyph.createCurve(), xOffset,
                yOffset);
        // memorize the toNode id of the segment in the lineSegment to make access easier later on.
        ls.putUserObject(ESCHER_NODE_LINK,
            isProduct ? segment.getFromNodeId() : segment.getToNodeId());
      } else {
        segments.add(segment);
      }
    }
    Set<Segment> done = new HashSet<Segment>();
    for (SpeciesReferenceGlyph srGlyph : srgMap.values()) {
      Curve curve = srGlyph.getCurve();
      Node
          toNode =
          escherMap.getNode(
              curve.getCurveSegment(curve.getCurveSegmentCount() - 1)
                   .getUserObject(ESCHER_NODE_LINK).toString());
      while (!toNode.isMidmarker()) {
        for (Segment segment : segments) {
          if (tryToAttach(segment, curve, curve.getCurveSegmentCount() - 1,
              escherMap, xOffset, yOffset)) {
            done.add(segment);
            toNode =
                escherMap.getNode(
                    curve.getCurveSegment(curve.getCurveSegmentCount() - 1)
                         .getUserObject(ESCHER_NODE_LINK).toString());
            if (toNode.isMidmarker()) {
              break;
            }
          }
        }
      }
      if (reaction.isSetListOfProducts() && reaction.getListOfProducts()
                                                    .contains(srGlyph
                                                        .getSpeciesReferenceInstance())) {
        Collections.reverse(curve.getListOfCurveSegments());
        // Reversing all curve segments seems not to be necessary.
        //				for (CurveSegment segment : curve.getListOfCurveSegments()) {
        //					reverse(segment);
        //				}
      }
    }
    segments.removeAll(done);
    if (!segments.isEmpty()) {
      logger.warning(MessageFormat
          .format(bundle.getString("Escher2SBML.segmentsLost"), segments));
    }
    logger.fine(bundle.getString("Escher2SBML.done"));
    return reaction;
  }


  /**
   * @param fromNode
   * @param toNode
   * @param basePoint1
   * @param basePoint2
   * @param curve
   * @param xOffset
   * @param yOffset
   * @return
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
   * @param segment
   * @param map
   * @param curve
   * @param xOffset
   * @param yOffset
   * @return
   */
  private LineSegment convertSegment(Segment segment, EscherMap map,
      Curve curve, double xOffset, double yOffset) {
    return convertSegment(map.getNode(segment.getFromNodeId()),
        map.getNode(segment.getToNodeId()), segment.getBasePoint1(),
        segment.getBasePoint2(), curve, xOffset, yOffset);
  }


  /**
   * @param prefix
   * @param count
   * @return
   */
  private String createId(String prefix, int count) {
    return prefix + (count + 1);
  }


  /**
   * @param escherReaction
   * @param layout
   * @param node2glyph
   * @param xOffset
   * @param yOffset
   * @return
   */
  private ReactionGlyph createReactionGlyph(EscherReaction escherReaction,
      Layout layout, Map<String, String> node2glyph, double xOffset,
      double yOffset) {
    ReactionGlyph rGlyph = null;
    if (escherReaction.isSetId() && node2glyph
        .containsKey(escherReaction.getId())) {
      rGlyph = layout.getReactionGlyph(node2glyph.get(escherReaction.getId()));
    } else {
      String
          rGlyphId =
          escherReaction.isSetId() ? SBMLtools.toSId(escherReaction.getId()) :
              "r_" + (layout.getReactionGlyphCount() + 1);
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
   * @param node
   * @param node2glyph
   * @param layout
   * @param xOffset
   * @param yOffset
   * @return
   */
  private ReactionGlyph convertMidmarker(Node node,
      Map<String, String> node2glyph, Layout layout, double xOffset,
      double yOffset) {
    String rId = extractReactionId(node.getConnectedSegments());
    if (rId != null) {
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
        // Also shift node because again the center of the node would be used as coordinate instead of upper-left corner
        rGlyph.createBoundingBox(width, height, nodeDepth,
            node.getX() - xOffset - width / 2d,
            node.getY() - yOffset - height / 2d, z);
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
   * @param node
   * @param node2glyph
   * @param layout
   * @param xOffset
   * @param yOffset
   * @return
   */
  private SpeciesGlyph convertMetabolite(Node node,
      Map<String, String> node2glyph, Layout layout, double xOffset,
      double yOffset) {
    SpeciesGlyph sGlyph;
    sGlyph = layout.createSpeciesGlyph(createSpeciesGlyphId(node, layout));
    node2glyph.put(node.getId(), sGlyph.getId());
    // Not defined in SBML Layout: sGlyph.setName(node.getName());
    double width;
    double height;
    if (node.isSetWidth()) {
      width = node.getWidth().doubleValue();
    } else {
      width =
          node.isPrimary() ? getPrimaryNodeWidth() :
              getSecondaryNodeRatio() * getPrimaryNodeWidth();
      node.setWidth(width);
    }
    if (node.isSetHeight()) {
      height = node.getHeight().doubleValue();
    } else {
      height =
          node.isPrimary() ? getPrimaryNodeHeight() :
              getSecondaryNodeRatio() * getPrimaryNodeHeight();
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
          compartment =
              createCompartment(model, SBMLtools.toSId(node.getCompartment()),
                  null);
        } else {
          compartment =
              createCompartment(model, defaultCompartmentId,
                  defaultCompartmentName);
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
   * @param model
   * @param compartmentId
   * @param compartmentName
   * @return
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
   * @param node
   * @param layout
   * @return
   */
  private String createSpeciesGlyphId(Node node, Layout layout) {
    return SBMLtools.toSId(node.isSetId() ? node.getId() :
        createId("sg_", layout.getSpeciesGlyphCount()));
  }


  /**
   * @param metabolite
   * @param setOfNodes
   * @param layout
   * @param node2glyph
   * @param rGlyph
   * @param reaction
   * @return
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
      sGlyph = layout.getSpeciesGlyph(node2glyph.get(node.getId()));
      if (sGlyph != null) {
        String srGlyphId = reaction.getId() + "_srg_" + metabolite.getId();
        if (model.containsUniqueNamedSBase(srGlyphId)) {
          int i = 0;
          do {
            i++;
          } while (model.containsUniqueNamedSBase(srGlyphId + "_" + i));
          srGlyphId += "_" + i;
          logger.warning(MessageFormat
              .format(bundle.getString("Escher2SBML.metaboliteDuplication"),
                  metabolite.getId(), reaction.getId(), ++i));
        }
        SpeciesReferenceGlyph
            srGlyph =
            rGlyph.createSpeciesReferenceGlyph(SBMLtools.toSId(srGlyphId),
                sGlyph.getId());
        sGlyph.putUserObject(ESCHER_NODE_LINK, node);
        // Create the core object for this species reference
        SimpleSpeciesReference ssr = null;
        if (metabolite.isSetCoefficient()) {
          if (metabolite.getCoefficient().doubleValue() < 0d) {
            ssr =
                reaction.createReactant(reaction.getId() + "_reactant_" + (
                    reaction.getReactantCount() + 1), sId);
            ((SpeciesReference) ssr)
                .setStoichiometry(-metabolite.getCoefficient().doubleValue());
            ssr.setSBOTerm(SBO.getReactant());
            srGlyph.setRole(node.isPrimary() ? SpeciesReferenceRole.SUBSTRATE :
                SpeciesReferenceRole.SIDESUBSTRATE);
          } else if (metabolite.getCoefficient().doubleValue() > 0d) {
            ssr =
                reaction.createProduct(
                    reaction.getId() + "_product_" + (reaction.getProductCount()
                        + 1), sId);
            ((SpeciesReference) ssr)
                .setStoichiometry(metabolite.getCoefficient().doubleValue());
            srGlyph.setRole(node.isPrimary() ? SpeciesReferenceRole.PRODUCT :
                SpeciesReferenceRole.SIDEPRODUCT);
          }
        }
        if (ssr == null) {
          ssr =
              reaction.createModifier(
                  reaction.getId() + "_modifier_" + (reaction.getModifierCount()
                      + 1), sId);
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
          logger.warning(MessageFormat
              .format(bundle.getString("Escher2SBML.replacementGlyph"),
                  metabolite.getId()));
        }
        srgMap.put(metabolite.getId(), srGlyph);
        //createCurve(node, srGlyph, segmentIds, isProduct, escherReaction, escherMap, xOffset, yOffset);
      } else {
        logger.warning(MessageFormat
            .format(bundle.getString("Escher2SBML.glyphIdNull"), biggID));
      }
    }
    if (!reaction.isSetCompartment()) {
      if (setOfCompartments.size() == 1) {
        reaction.setCompartment(setOfCompartments.iterator().next());
      } else {
        //TODO: What to do?
        //reaction.setCompartment(createCompartment(model, compartmentId, compartmentName));
        logger.warning(MessageFormat
            .format(bundle.getString("Escher2SBML.reactionCompartmentUnknown"),
                reaction.getId()));
      }
    }
    return srgMap;
  }


  /**
   * @param reaction
   * @param rGlyph
   * @param layout
   * @param xOffset
   * @param yOffset
   */
  private void createTextGlyph(EscherReaction reaction, ReactionGlyph rGlyph,
      Layout layout, double xOffset, double yOffset) {
    double x = reaction.getLabelX() - xOffset;
    double y = reaction.getLabelY() - yOffset - reactionLabelHeight;
    TextGlyph label = layout.createTextGlyph(createTextGlyphId(layout));
    label.createBoundingBox(getLabelWidth(), reactionLabelHeight, nodeDepth, x,
        y, z);
    label.setOriginOfText(rGlyph.getReaction());
    label.setGraphicalObject(rGlyph);
  }


  /**
   * @param node
   * @param layout
   * @param xOffset
   * @param yOffset
   * @return
   */
  private TextGlyph createTextGlyph(Node node, Layout layout, double xOffset,
      double yOffset) {
    return createTextGlyph(node, layout, xOffset, yOffset, null);
  }


  /**
   * @param node
   * @param layout
   * @param xOffset
   * @param yOffset
   * @param referenceGlyph
   * @return
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
   * @param label
   * @param layout
   * @param xOffset
   * @param yOffset
   */
  private void createTextGlyph(TextLabel label, Layout layout, double xOffset,
      double yOffset) {
    String id = createTextGlyphId(label, layout);
    Model model = layout.getModel();
    NamedSBase element = model.findNamedSBase(id);
    if (element != null) {
      logger.warning(MessageFormat
          .format(bundle.getString("Escher2SBML.textLabelIdNotUnique"), id,
              element.getElementName()));
      id += "_tf";
    }
    TextGlyph tGlyph = layout.createTextGlyph(id);
    BoundingBox bbox = tGlyph.createBoundingBox();
    bbox.createPosition(label.getX() - xOffset,
        label.getY() - yOffset - getLabelHeight(), z);
    bbox.createDimensions(getLabelWidth(), getLabelHeight(), nodeDepth);
    if (label.isSetText()) {
      tGlyph.setText(label.getText());
    }
  }


  /**
   * @param layout
   * @return
   */
  private String createTextGlyphId(Layout layout) {
    return createId("tg_", layout.getTextGlyphCount());
  }


  /**
   * @param label
   * @param layout
   * @return
   */
  private String createTextGlyphId(TextLabel label, Layout layout) {
    return label.isSetId() ? SBMLtools.toSId(label.getId()) :
        createTextGlyphId(layout);
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
   * @param escherMap
   * @param xOffset
   * @param yOffset
   */
  private Layout initLayout(EscherMap escherMap, double xOffset,
      double yOffset) {
    Canvas canvas = escherMap.getCanvas();
    double
        width =
        canvas.isSetWidth() ? canvas.getWidth().doubleValue() - xOffset :
            getCanvasDefaultWidth();
    double
        height =
        canvas.isSetHeight() ? canvas.getHeight().doubleValue() - yOffset :
            getCanvasDefaultHeight();
    SBMLDocument doc = new SBMLDocument(3, 1);
    Model
        model =
        doc.createModel(
            escherMap.isSetId() ? SBMLtools.toSId(escherMap.getId()) :
                "_default");
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
    LayoutModelPlugin
        layoutPlugin =
        (LayoutModelPlugin) model.getPlugin(LayoutConstants.shortLabel);
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
   * @param segment
   * @param targetCurve
   * @param targetCurveSegmentIndex
   * @param escherMap
   * @param xOffset
   * @param yOffset
   * @return
   */
  private boolean tryToAttach(Segment segment, Curve targetCurve,
      int targetCurveSegmentIndex, EscherMap escherMap, double xOffset,
      double yOffset) {
    CurveSegment
        lastSegment =
        targetCurve.getListOfCurveSegments().get(targetCurveSegmentIndex);
    String toNodeId = lastSegment.getUserObject(ESCHER_NODE_LINK).toString();
    if (toNodeId.equals(segment.getFromNodeId())) {
      LineSegment
          ls =
          convertSegment(segment, escherMap, targetCurve, xOffset, yOffset);
      ls.putUserObject(ESCHER_NODE_LINK, segment.getToNodeId());
      return true;
    } else if (toNodeId.equals(segment.getToNodeId())) {
      LineSegment
          ls =
          convertSegment(reverse(segment), escherMap, targetCurve, xOffset,
              yOffset);
      ls.putUserObject(ESCHER_NODE_LINK, segment.getFromNodeId());
      return true;
    }
    return false;
  }
}

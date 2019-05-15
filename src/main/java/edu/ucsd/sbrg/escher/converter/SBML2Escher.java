package edu.ucsd.sbrg.escher.converter;

import static java.text.MessageFormat.format;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.tree.TreeNode;

import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.NamedSBase;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBO;
import org.sbml.jsbml.SimpleSpeciesReference;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.BoundingBox;
import org.sbml.jsbml.ext.layout.CubicBezier;
import org.sbml.jsbml.ext.layout.Curve;
import org.sbml.jsbml.ext.layout.CurveSegment;
import org.sbml.jsbml.ext.layout.Dimensions;
import org.sbml.jsbml.ext.layout.Layout;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;
import org.sbml.jsbml.ext.layout.ReactionGlyph;
import org.sbml.jsbml.ext.layout.SpeciesGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceGlyph;
import org.sbml.jsbml.ext.layout.SpeciesReferenceRole;
import org.sbml.jsbml.ext.layout.TextGlyph;
import org.sbml.jsbml.util.ResourceManager;

import edu.ucsd.sbrg.escher.model.Canvas;
import edu.ucsd.sbrg.escher.model.EscherMap;
import edu.ucsd.sbrg.escher.model.EscherReaction;
import edu.ucsd.sbrg.escher.model.Metabolite;
import edu.ucsd.sbrg.escher.model.Node;
import edu.ucsd.sbrg.escher.model.Point;
import edu.ucsd.sbrg.escher.model.Segment;
import edu.ucsd.sbrg.escher.model.TextLabel;
import edu.ucsd.sbrg.escher.util.EscherOptions;

/**
 * Converter from SBML Layout Extension to Escher.
 *
 * @author Devesh Khandelwal
 * Created on 20/06/16.
 */
public class SBML2Escher {

  /**
   * A {@link java.util.logging.Logger} for this class.
   */
  private static final Logger         logger = Logger.getLogger(SBGN2Escher.class.getName());
  /**
   * Default values,
   */
  public static final  ResourceBundle bundle = ResourceManager.getBundle("Strings");
  /**
   * Localization support.
   */
  public static final  ResourceBundle messages = ResourceManager.getBundle("edu.ucsd.sbrg.escher.Messages");
  /**
   * List of escher maps converted from the layouts exported from the SBML document.
   */
  protected List<EscherMap> escherMaps;
  /**
   * SBML document to extract escher maps from.
   */
  protected SBMLDocument    document;
  /**
   * List of layouts exported from the SBML document.
   */
  protected List<Layout>    layouts;
  
  /**
   * The height of primary nodes; in case bounding boxes have no dimensions
   */
  protected double primary_node_height;
  
  /**
   * The width of primary nodes; in case bounding boxes have no dimensions
   */
  protected double primary_node_width;

  /**
   * Default constructor.
   */
  public SBML2Escher() {
    escherMaps = new ArrayList<>(); 
  }

  /**
   * Converts an {@link SBMLDocument} to a list of {@link EscherMap}s by iteratively creating nodes,
   * reactions, etc.
   *
   * @param document The {@code SBML} document to convert.
   * @return The extracted maps list.
   */
  public List<EscherMap> convert(SBMLDocument doc) {
    logger.fine(format(messages.getString("SBMLImportInit")));
    document = doc;

    layouts = ((LayoutModelPlugin) doc.getModel().getPlugin(LayoutConstants.shortLabel)).getListOfLayouts();
    logger.info(format(messages.getString("SBMLLayoutCount"), layouts.size()));

    layouts.forEach((layout) -> {
      logger.info(format(messages.getString("SBMLLayoutConversionInit")));

      EscherMap map = new EscherMap();

      map.setCanvas(addCanvasInfo(layout));
      logger.fine(messages.getString("EscherCanvasAddSuccess"));
      map.setDescription(bundle.getString("default_description"));
      map.setId(layout.getId());
      //      map.setId(UUID.nameUUIDFromBytes(layout.getId().getBytes()).toString());
      // com.sun.org.apache.xerces.internal.impl.dv.util.HexBin
      //      map.setId(HexBin.encode(layouts.get(0).toString().getBytes()));
      logger.info(messages.getString("EscherIdAddSuccess"));

      logger.info(format(messages.getString("TextGlyphCount"), layout.getTextGlyphCount()));
      layout.getListOfTextGlyphs().forEach(tG -> {
        if (!tG.isSetGraphicalObject()) {
          map.addTextLabel(createTextLabel(tG));
        }
      });

      logger.info(format(messages.getString("SpeciesGlyphCount"), layout.getSpeciesGlyphCount()));
      layout.getListOfSpeciesGlyphs().forEach(sG -> {
        map.addNode(createNode(sG));
      });

      logger.info(format(messages.getString("ReactionGlyphCount"), layout.getReactionGlyphCount()));

      layout.getListOfReactionGlyphs().forEach(rG -> {
        map.addNode(createMidMarker(rG));

        rG.getListOfSpeciesReferenceGlyphs().forEach((sRG) -> {
          createMultiMarkers(sRG).forEach(map::addNode);
        });

        EscherReaction r = createReaction(rG);
        map.addReaction(r);

        for (SpeciesReferenceGlyph sRG: rG.getListOfSpeciesReferenceGlyphs()) {
          logger.info(format(messages.getString("SRGToSegments"), rG.getId(), sRG.getId(), "" + (rG.getId().hashCode() & 0xfffffff)));
          createSegments(sRG, rG).forEach(s -> r.addSegment(s));

          // Setting node_is_primary of nodes.
          if ((sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.PRODUCT) ||
              (sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.SUBSTRATE)) {
            map.getNode(sRG.getSpeciesGlyph()).setPrimary(true);
            logger.info(format(messages.getString("PrimaryNode"), sRG.getSpeciesGlyph()));
          } else if ((sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.SIDEPRODUCT) ||
              (sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.SIDESUBSTRATE)) {
            map.getNode(sRG.getSpeciesGlyph()).setPrimary(false);
            logger.info(format(messages.getString("SecondaryNode"), sRG.getSpeciesGlyph()));
          }
        };

      });

      escherMaps.add(map);
    });

    return escherMaps;
  }


  /**
   * Create a {@link Canvas} instance from an SBML {@link Layout}.
   *
   * @param layout The SBML {@code layout}.
   * @return The {@code canvas} instance.
   */
  protected Canvas addCanvasInfo(Layout layout) {
    Canvas canvas = new Canvas();

    canvas.setX(Double.valueOf(bundle.getString("default_canvas_x")));
    canvas.setY(Double.valueOf(bundle.getString("default_canvas_y")));

    if (layout.getDimensions() == null) {
      logger.info(messages.getString("RootDimensionsFound"));
      canvas.setHeight(Double.valueOf(bundle.getString("default_canvas_height")));
      canvas.setWidth(Double.valueOf(bundle.getString("default_canvas_width")));
    }
    else {
      // Default values if dimensions not found.
      logger.warning(messages.getString("RootDimensionsNotFound"));
      canvas.setHeight(layout.getDimensions().getHeight());
      canvas.setWidth(layout.getDimensions().getWidth());
    }

    return canvas;
  }


  /**
   * Create a {@link TextLabel} from a {@link TextGlyph}.
   *
   * @param textGlyph The {@code text glyph}.
   * @return The created {@code text label.}
   */
  protected TextLabel createTextLabel(TextGlyph textGlyph) {
    TextLabel textLabel = new TextLabel();

    if ((textGlyph.getId() == null) || textGlyph.getId().isEmpty()) {
      textLabel.setId("" + (textGlyph.hashCode() & 0xfffffff));
    } else {
      textLabel.setId(textGlyph.getId());
    }

    if (textGlyph.isSetText() && !textGlyph.getText().isEmpty()) {
      textLabel.setText(textGlyph.getText());
    } else if (textGlyph.isSetOriginOfText()) {
      NamedSBase nsb = textGlyph.getOriginOfTextInstance();
      if (nsb != null) {
        textLabel.setText(nsb.isSetName() ? nsb.getName() : nsb.getId());
      }
    }

    if (!textLabel.isSetText()) {
      // TODO: Log about no text, so ignoring text label.
      logger.warning(format(messages.getString("TextGlyphNoText"), textLabel.getId()));
    }

    BoundingBox bbox = textGlyph.getBoundingBox();
    textLabel.setX(bbox.getPosition().x());
    textLabel.setY(bbox.getPosition().y());

    return textLabel;
  }


  /**
   * Creates a {@link Node}(metabolite) from a {@link SpeciesGlyph}.
   *
   * @param speciesGlyph The {@code species glyph}.
   * @return The created {@code node} of type "metabolite"
   */
  protected Node createNode(SpeciesGlyph speciesGlyph) {
    Node node = new Node();

    node.setType(Node.Type.metabolite);
    node.setId(speciesGlyph.getId());
    node.setBiggId(speciesGlyph.getSpecies());
    if (node.getBiggId().startsWith("M_")) {
      node.setBiggId(node.getBiggId().substring(2));
    }
    NamedSBase species = speciesGlyph.getSpeciesInstance();
    if ((species != null) && (species.isSetName())) {
      node.setName(species.getName());
    }

    BoundingBox bbox = speciesGlyph.getBoundingBox();
    org.sbml.jsbml.ext.layout.Point pos = bbox.getPosition();
    Dimensions dim = bbox.getDimensions();

    double x = (pos != null) && pos.isSetX() ? pos.x() : Double.NaN;
    double y = (pos != null) && pos.isSetY() ? pos.y() : Double.NaN;

    // Escher users the center of the bounding box to place objects, not the upper left corner as in SBML.
    node.setX(x + dim.getWidth() / 2d);
    node.setY(y + dim.getHeight() / 2d);

    // Set label coordinates to the left of BBox and a bit below the middle of the y coordinate (estimate for good display).
    node.setLabelX(x);
    node.setLabelY(y + dim.getHeight() * 3d / 4d);

    logger.info(format(messages.getString("SpeciesGlyphToNode"), speciesGlyph.getId()));
    node.setPrimary(true);

    return node;
  }


  /**
   * Creates a {@link Node}(mid-marker) from a {@link ReactionGlyph}.
   *
   * @param reactionGlyph The {@code reaction glyph}.
   * @return The created {@code node}(mid-marker).
   */
  protected Node createMidMarker(ReactionGlyph reactionGlyph) {
    Node node = new Node();

    node.setId(reactionGlyph.getId());
    node.setType(Node.Type.midmarker);
    logger.info(format(messages.getString("ReactionToMidMarker"), reactionGlyph.getId()));

    Point point = new Point();
    BoundingBox bbox = reactionGlyph.getBoundingBox();
    if ((bbox != null) || !reactionGlyph.isSetCurve()) {
      // If position is available, use its center as anchor.
      logger.info(format(messages.getString("ReactionGlyphBBoxFound"), reactionGlyph.getId()));
      if ((bbox != null) && bbox.isSetDimensions() && bbox.isSetPosition()) {
        org.sbml.jsbml.ext.layout.Point pos = bbox.getPosition();
        Dimensions dim = bbox.getDimensions();
        point.setX(pos.getX() + dim.getWidth() / 2d);
        point.setY(pos.getY() + dim.getHeight()/ 2d);
      } else {
    	  if((bbox != null) && bbox.isSetPosition()){
    		  org.sbml.jsbml.ext.layout.Point pos = bbox.getPosition();
    		  point.setX(pos.getX() + primary_node_width/2);
    	      point.setY(pos.getY() + primary_node_height/2);
    	  }else{
    		  point.setX(Double.NaN);
    		  point.setY(Double.NaN);
    	  }
      }
    }
    else {
      // If position is not available, calculate center of the curve.
      logger.info(format(messages.getString("ReactionGlyphBBoxNotFound"), reactionGlyph.getId()));
      Curve curve = reactionGlyph.getCurve();
      point.setX((curve.getCurveSegment(0).getStart().x() +
          curve.getCurveSegment(curve.getCurveSegmentCount()-1).getStart().x()) / 2d);
      point.setY((curve.getCurveSegment(0).getStart().y() +
          curve.getCurveSegment(curve.getCurveSegmentCount()-1).getStart().y()) / 2d);
    }

    node.setX(point.getX());
    node.setY(point.getY());

    return node;
  }


  /**
   * Creates a list of {@link Node}s(multi-markers) from a {@link SpeciesReferenceGlyph}.
   * A multi-marker is created for every joining of two curve segments in a curve.
   *
   * @param sRG The {@code species reference glyph}.
   * @return The created {@code nodes}(multi-markers).
   */
  protected List<Node> createMultiMarkers(SpeciesReferenceGlyph sRG) {
    logger.info(format(messages.getString("SRGToMultiMarkers"), sRG.getId()));
    List<Node> multiMarkers = new ArrayList<>();

    Node node;
    if (sRG.isSetBoundingBox() && !sRG.isSetCurve()) {
      // Just make up a curve.
      SpeciesGlyph sg = sRG.getSpeciesGlyphInstance();
      ReactionGlyph rg = sRG.getReactionGlyph();
      SpeciesReferenceRole role = determineSpeciesReferenceRole(sRG, rg);
      Curve curve = sRG.createCurve();
      org.sbml.jsbml.ext.layout.Point start = null, end = null, mid = null;
      // TODO: assuming boundingboxes and positions aren't null!
      if ((role == SpeciesReferenceRole.SUBSTRATE) || (role == SpeciesReferenceRole.SIDESUBSTRATE)) {
        start = sg.getBoundingBox().getPosition();
        end = rg.getBoundingBox().getPosition();
      } else if ((role == SpeciesReferenceRole.PRODUCT) || (role == SpeciesReferenceRole.SIDEPRODUCT)) {
        // product
        end = rg.getBoundingBox().getPosition();
        start = sg.getBoundingBox().getPosition();
      }
      if ((start != null) && (end != null)) {
        // Must have at least one mid-marker!
        mid = new org.sbml.jsbml.ext.layout.Point(start.getLevel(), start.getVersion());
        double x1 = Math.min(start.x(), end.x()), y1 = Math.min(start.y(), end.y()), z = start.z();
        double x2 = Math.max(start.x(), end.x()), y2 = Math.max(start.y(), end.y());
        mid.setX(x1 + (x2 - x1) / 2d);
        mid.setY(y1 + (y2 - y1) / 2d);
        mid.setZ(z);
        curve.createLineSegment(start.clone(), mid);
        curve.createLineSegment(mid.clone(), end.clone());
      }
    }
    if (sRG.isSetCurve()) {
      // TODO: it should also work if there is no midmarker!
      List<CurveSegment> cSs = sRG.getCurve().getListOfCurveSegments();
      if (SBO.isChildOf(sRG.getRole().toSBOterm(), SpeciesReferenceRole.PRODUCT.toSBOterm())) {
        for (int i = 0; i < cSs.size() - 1; i++) {
          node = new Node();

          node.setId(createMultimarkerId(sRG, i));
          node.setType(Node.Type.multimarker);

          CurveSegment currSegment = cSs.get(i), nextSegment = cSs.get(i + 1);
          node.setX(midPoint(currSegment.getEnd().x(), nextSegment.getStart().x()));
          node.setY(midPoint(currSegment.getEnd().y(), nextSegment.getStart().y()));

          multiMarkers.add(node);
        }
      } else {
        for (int i = cSs.size() - 1; i > 0; i--) {
          node = new Node();
          node.setId(createMultimarkerId(sRG, cSs.size() - 1 - i));
          node.setType(Node.Type.multimarker);

          CurveSegment currSegment = cSs.get(i), nextSegment = cSs.get(i - 1);
          node.setX(midPoint(currSegment.getStart().x(), nextSegment.getEnd().x()));
          node.setY(midPoint(currSegment.getStart().y(), nextSegment.getEnd().y()));

          multiMarkers.add(node);
        }
      }
    }

    logger.info(format(messages.getString("MultiMarkerCount"), multiMarkers.size(), sRG.getId()));

    return multiMarkers;
  }


  /**
   * @param sRG
   * @param i
   * @return
   */
  private String createMultimarkerId(SpeciesReferenceGlyph sRG, int i) {
    return sRG.getReactionGlyph().getId() + "." + sRG.getId() + ".M" + (i + 1);
  }


  /**
   * Finds out whether a {@link SpeciesReferenceGlyph} is a product, substrate or modifier.
   * @param sRG The {@link SpeciesReferenceGlyph}
   * @param rg The {@link ReactionGlyph} the {@code sRG} is linked to
   * @param role A {@link SpeciesReferenceRole} (product, substrate or modifier)
   */
  public SpeciesReferenceRole determineSpeciesReferenceRole(SpeciesReferenceGlyph sRG,
    ReactionGlyph rg) {
    SpeciesReferenceRole role = sRG.getRole();
    if (role == null) {
      if (sRG.isSetSBOTerm()) {
        role = SpeciesReferenceRole.valueOf(sRG.getSBOTerm());
      } else if (sRG.isSetReference()) {
        NamedSBase nsb = sRG.getReferenceInstance();
        if (nsb != null) {
          if (nsb.isSetSBOTerm()) {
            role = SpeciesReferenceRole.valueOf(nsb.getSBOTerm());
          } else if (nsb instanceof SimpleSpeciesReference) {
            if (nsb instanceof ModifierSpeciesReference) {
              role = SpeciesReferenceRole.MODIFIER;
            } else if (rg != null) {
              TreeNode parent = nsb.getParent();
              Reaction r = (Reaction) rg.getReactionInstance();
              if (r != null) {
                if (r.getListOfReactants() == parent) {
                  role = SpeciesReferenceRole.SUBSTRATE;
                } else {
                  role = SpeciesReferenceRole.PRODUCT;
                }
              }
            }
          }
        }
      }
    }
    return role;
  }


  /**
   * Creates an {@link EscherReaction} from a {@link ReactionGlyph}.
   *
   * @param reactionGlyph The {@code reaction glyph}.
   * @return The created {@code escher reaction}.
   */
  protected EscherReaction createReaction(ReactionGlyph reactionGlyph) {
    EscherReaction reaction = new EscherReaction();

    Reaction reac = (Reaction) reactionGlyph.getReactionInstance();
    reaction.setName(reac.getName());
    reaction.setId("" + (reactionGlyph.getId().hashCode() & 0xfffffff));
    reaction.setBiggId(reac.getId());
    logger.info(format(messages.getString("ReactionGlyphToReaction"), reactionGlyph.getId(),
      reaction.getId()));

    Point point = new Point();
    BoundingBox bbox = reactionGlyph.getBoundingBox();
    if ((bbox != null) || !reactionGlyph.isSetCurve()) {
      // If BBox is available, use its center as anchor.
      logger.info(format(messages.getString("ReactionGlyphBBoxFound"), reactionGlyph.getId()));
      if ((bbox != null) && bbox.isSetPosition() && bbox.isSetDimensions()) {
        org.sbml.jsbml.ext.layout.Point pos = bbox.getPosition();
        Dimensions dim = bbox.getDimensions();
        point.setX(pos.getX() + dim.getWidth() / 2d);
        point.setY(pos.getY() + dim.getHeight()/ 2d);
      } else {
    	  if((bbox != null) && bbox.isSetPosition()){
    		  org.sbml.jsbml.ext.layout.Point pos = bbox.getPosition();
    		  point.setX(pos.getX() + primary_node_width/2);
    	      point.setY(pos.getY() + primary_node_height/2);
    	  }else{
    		  point.setX(Double.NaN);
    		  point.setY(Double.NaN);
    	  }
      }
    }
    else {
      // If BBox is not available, calculate its center using the curve.
      logger.info(format(messages.getString("ReactionGlyphBBoxNotFound")));
      Curve curve = reactionGlyph.getCurve();
      point.setX((curve.getCurveSegment(0).getStart().x() +
          curve.getCurveSegment(curve.getCurveSegmentCount()-1).getStart().x()) / 2d);
      point.setY((curve.getCurveSegment(0).getStart().y() +
          curve.getCurveSegment(curve.getCurveSegmentCount()-1).getStart().y()) / 2d);
    }
    reaction.setLabelX(point.getX());
    reaction.setLabelY(point.getY());

    // Add metabolite with positive coefficient (products).
    logger.info(format(messages.getString("ReactionGlyphProductCount"), reac.getProductCount()));
    for (SpeciesReference p : reac.getListOfProducts()) {
      logger.info(format(messages.getString("MetaboliteCoefficient"), p.getSpecies(), 1d));
      reaction.addMetabolite(createMetabolite(p));
    };

    // Add metabolite with negative coefficient (reactants).
    logger.info(format(messages.getString("ReactionGlyphSubstrateCount"), reac.getListOfReactants().size()));
    for (SpeciesReference r : reac.getListOfReactants()) {
      logger.info(format(messages.getString("MetaboliteCoefficient"), r.getSpecies(), -1d));
      r.setStoichiometry(-r.getStoichiometry());
      reaction.addMetabolite(createMetabolite(r));
    };

    if (reac.isSetReversible()) {
      reaction.setReversibility(reac.isReversible());
    } else {
      // If reversibility attribute is not present of reaction, use true as default.
      reaction.setReversibility(true);
    }

    return reaction;
  }


  /**
   * Creates a list of {@link Segment}s from a {@link SpeciesReferenceGlyph}.
   * A segment is created for every {@link CurveSegment} inside a {@link Curve}.
   *
   * @param sRG The {@code species reference glyph}.
   * @param rG The linked {@code reaction glyph}.
   * @return The list of created {@code segments}.
   */
  protected List<Segment> createSegments(SpeciesReferenceGlyph sRG, ReactionGlyph rG) {
    List<Segment> segments = new ArrayList<>();

    if (sRG.isSetCurve()) {
      List<CurveSegment> cSs = sRG.getCurve().getListOfCurveSegments();

      logger.info(format(messages.getString("CurveSegmentCount"), sRG.getId(), cSs.size()));

      Segment segment = new Segment();
      segment.setId(sRG.getId() + ".S" + 0);
      segment.setFromNodeId(rG.getId());
      for (int i = 0; i < cSs.size() - 1; i++) {
        String currNodeId = createMultimarkerId(sRG, i);
        segment.setToNodeId(currNodeId);

        CurveSegment cs = cSs.get(i);
        if (cs.isCubicBezier()) {
          copyBasePoints(cs, segment, SBO.isChildOf(sRG.getRole().toSBOterm(), SpeciesReferenceRole.PRODUCT.toSBOterm()));
        }
        segments.add(segment);
        logger.info(format(messages.getString("CurveSegmentAdd"),
          segment.getId(), segment.getFromNodeId(), segment.getToNodeId()));

        segment = new Segment();

        segment.setId(sRG.getId() + ".S" + (i + 1));
        segment.setFromNodeId(currNodeId);
      }

      segment.setToNodeId(sRG.getSpeciesGlyph());

      if (!cSs.isEmpty()) {
        CurveSegment cs = cSs.get(cSs.size() - 1);
        if (cs.isCubicBezier()) {
          copyBasePoints(cs, segment, SBO.isChildOf(sRG.getRole().toSBOterm(), SpeciesReferenceRole.PRODUCT.toSBOterm()));
        }
      }
      segments.add(segment);
      logger.info(format(messages.getString("CurveSegmentAdd"),
        segment.getId(), segment.getFromNodeId(), segment.getToNodeId()));
    }else{
    	// at the moment only straight lines are supported
		Segment segment = new Segment();
		segment.setId(sRG.getId() + ".S" + 0);
   	    segment.setFromNodeId(rG.getId());
		segment.setToNodeId(sRG.getSpeciesGlyph());
	    segments.add(segment);
	    logger.info(format(messages.getString("CurveSegmentAdd"),
	        segment.getId(), segment.getFromNodeId(), segment.getToNodeId()));
    	// TODO draw curves instead of straight lines
    }

    return segments;
  }


  /**
   * Gets base points from an SBML {@link CurveSegments} and adds them to an {@link EscherMap} {@link Segment}
   * @param cs A {@link CurveSegment} of an SBML model
   * @param segment A {@link Segment} of an {@link EscherMap}
   */
  private void copyBasePoints(CurveSegment cs, Segment segment, boolean forward) {
    CubicBezier cB = (CubicBezier) cs;
    org.sbml.jsbml.ext.layout.Point point;

    point = cB.getBasePoint1();
    if (forward) {
      segment.setBasePoint1(new Point(point.x(), point.y()));
      point = cB.getBasePoint2();
      segment.setBasePoint2(new Point(point.x(), point.y()));
    } else {
      segment.setBasePoint2(new Point(point.x(), point.y()));
      point = cB.getBasePoint2();
      segment.setBasePoint1(new Point(point.x(), point.y()));
    }
  }


  /**
   * Creates a {@link Metabolite} from a {@link SpeciesReference}.
   *
   * @param speciesReference The {@code species reference}.
   * @return The created {@code metabolite}.
   */
  protected Metabolite createMetabolite(SpeciesReference speciesReference) {
    Metabolite metabolite = new Metabolite();

    metabolite.setId(speciesReference.getSpecies());
    if (metabolite.getId().startsWith("M_")) {
      metabolite.setId(metabolite.getId().substring(2));
    }
    metabolite.setCoefficient(speciesReference.getCalculatedStoichiometry());

    return metabolite;
  }


  /**
   * Calculates mid-point of two decimal values.
   *
   * @param d1 First value.
   * @param d2 Second value.
   * @return Mid-point.
   */
  protected double midPoint(double d1, double d2) {
    return (d1 + d2)/2;
  }
  
  public void setNodeHeight(double height){
	  this.primary_node_height = height;
  }
  
  public void setNodeWidth(double width){
	  this.primary_node_width = width;
  }

}

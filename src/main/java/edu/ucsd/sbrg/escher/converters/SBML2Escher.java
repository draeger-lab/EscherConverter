package edu.ucsd.sbrg.escher.converters;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import edu.ucsd.sbrg.escher.model.*;
import edu.ucsd.sbrg.escher.model.Point;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.*;
import org.sbml.jsbml.util.ResourceManager;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

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
  public static final  ResourceBundle messages = ResourceManager.getBundle("Messages");
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
  public List<EscherMap> convert(SBMLDocument document) {
    logger.fine(format(messages.getString("SBMLImportInit")));
    this.document = document;

    layouts = ((LayoutModelPlugin)document.getModel().getPlugin(LayoutConstants.shortLabel)).getListOfLayouts();
    logger.info(format(messages.getString("SBMLLayoutCount"), layouts.size()));

    layouts.forEach((layout) -> {
      logger.info(format(messages.getString("SBMLLayoutConversionInit")));

      EscherMap map = new EscherMap();

      map.setCanvas(addCanvasInfo(layout));
      logger.fine(messages.getString("EscherCanvasAddSuccess"));
      map.setDescription(bundle.getString("default_description"));
      map.setId(HexBin.encode(layouts.get(0).toString().getBytes()));
      logger.info(messages.getString("EscherIdAddSuccess"));

      logger.info(format(messages.getString("TextGlyphCount"), layout.getTextGlyphCount()));
      layout.getListOfTextGlyphs().forEach(tG -> {
//        map.addTextLabel(createTextLabel(tG));
      });

      logger.info(format(messages.getString("SpeciesGlyphCount"), layout.getSpeciesGlyphCount()));
      layout.getListOfSpeciesGlyphs().forEach((sG) -> {
        map.addNode(createNode(sG));
      });

      logger.info(format(messages.getString("ReactionGlyphCount"), layout.getReactionGlyphCount()));
      layout.getListOfReactionGlyphs().forEach((rG -> {
        map.addNode(createMidMarker(rG));
      }));

      layout.getListOfReactionGlyphs().forEach((rG) -> {
        rG.getListOfSpeciesReferenceGlyphs().forEach((sRG) -> {
          createMultiMarkers(sRG).forEach(m -> map.addNode(m));
        });
      });

      layout.getListOfReactionGlyphs().forEach((rG) -> {
        map.addReaction(createReaction(rG));
      });

      layout.getListOfReactionGlyphs().forEach((rG) -> {
        rG.getListOfSpeciesReferenceGlyphs().forEach((sRG -> {
          logger.info(format(messages.getString("SRGToSegments"), rG.getId(), sRG.getId(), "" + (rG.getId().hashCode() &
              0xfffffff)));
          createSegments(sRG, rG).forEach(s -> map.getReaction("" + (rG.getId().hashCode() &
              0xfffffff)).addSegment(s));
        }));
      });

      // Setting node_is_primary of nodes.
      layout.getListOfReactionGlyphs().forEach(rG -> {
        rG.getListOfSpeciesReferenceGlyphs().forEach(sRG -> {
          if (sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.PRODUCT ||
              sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.SUBSTRATE) {
            map.getNode(sRG.getSpeciesGlyph()).setPrimary(true);
            logger.info(format(messages.getString("PrimaryNode"), sRG.getSpeciesGlyph()));
          }
          else if (sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.SIDEPRODUCT ||
              sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.SIDESUBSTRATE) {
            map.getNode(sRG.getSpeciesGlyph()).setPrimary(false);
            logger.info(format(messages.getString("SecondaryNode"), sRG.getSpeciesGlyph()));
          }
        });
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

    if (textGlyph.getId() == null || textGlyph.getId().isEmpty()) {
      textLabel.setId("" + (textGlyph.hashCode() & 0xfffffff));
    }
    else {
      textLabel.setId(textGlyph.getId());
    }

    if (textGlyph.getText() == null || textGlyph.getText().isEmpty()) {
      // TODO: Log about no text, so ignoring text label.
      logger.warning(format(messages.getString("TextGlyphNoText"), textLabel.getId()));
    }
    else {
      textLabel.setText(textGlyph.getText());
    }

    textLabel.setX(textGlyph.getBoundingBox().getPosition().getX());
    textLabel.setY(textGlyph.getBoundingBox().getPosition().getY());

    return textLabel;
  }


  /**
   * Creates a {@link Node}(metabolite) from a {@link SpeciesGlyph}.
   *
   * @param speciesGlyph The {@code species glyph}.
   * @return The created {@code node}.
   */
  protected Node createNode(SpeciesGlyph speciesGlyph) {
    Node node = new Node();

    node.setType(Node.Type.metabolite);
    node.setId(speciesGlyph.getId());
    node.setBiggId(speciesGlyph.getSpecies());
    node.setName(speciesGlyph.getSpeciesInstance().getName());
    node.setX(speciesGlyph.getBoundingBox().getPosition().x());
    node.setY(speciesGlyph.getBoundingBox().getPosition().y());
    node.setLabelX(speciesGlyph.getBoundingBox().getPosition().x() +
                    speciesGlyph.getBoundingBox().getDimensions().getWidth());
    node.setLabelY(speciesGlyph.getBoundingBox().getPosition().y() +
                    speciesGlyph.getBoundingBox().getDimensions().getHeight());

    logger.info(format(messages.getString("SpeciesGlyphToNode"), speciesGlyph.getId()));
    // TODO: Find out if node is primary by either role or SBO term.
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
    if (reactionGlyph.getBoundingBox() != null) {
      logger.info(format(messages.getString("ReactionGlyphBBoxFound"), reactionGlyph.getId()));
      point.setX(reactionGlyph.getBoundingBox().getPosition().getX() + (0.5 * reactionGlyph
          .getBoundingBox().getDimensions().getWidth()));
      point.setY(reactionGlyph.getBoundingBox().getPosition().getY() + (0.5 * reactionGlyph
          .getBoundingBox().getDimensions().getHeight()));
    }
    else {
      logger.info(format(messages.getString("ReactionGlyphBBoxNotFound"), reactionGlyph.getId()));
      point.setX(0.5 * (reactionGlyph.getCurve()
                                     .getCurveSegment(0)
                                     .getStart()
                                     .x() + reactionGlyph.getCurve()
                                                         .getCurveSegment(reactionGlyph.getCurve()
                                                                                       .getCurveSegmentCount()-1)
                                                         .getStart().x()));
      point.setY(0.5 * (reactionGlyph.getCurve()
                                     .getCurveSegment(0)
                                     .getStart()
                                     .y() + reactionGlyph.getCurve()
                                                         .getCurveSegment(reactionGlyph.getCurve()
                                                                                       .getCurveSegmentCount()-1)
                                                         .getStart().y()));
    }

    node.setX(point.getX());
    node.setY(point.getY());

    return node;
  }


  /**
   * Creates a list of {@link Node}s(multi-markers) from a {@link SpeciesReferenceGlyph}.
   *
   * @param sRG The {@code species reference glyph}.
   * @return The created {@code nodes}(multi-markers).
   */
  protected List<Node> createMultiMarkers(SpeciesReferenceGlyph sRG) {
    logger.info(format(messages.getString("SRGToMultiMarkers"), sRG.getId()));
    List<Node> multiMarkers = new ArrayList<>();

    Node node;
    List<CurveSegment> cSs = sRG.getCurve().getListOfCurveSegments();
    for (int i = 0; i < (cSs.size()-1); i++) {
      node = new Node();

      node.setId(sRG.getSpeciesReference() + ".M" + (i+1));
      node.setType(Node.Type.multimarker);

      node.setX(midPoint(cSs.get(i).getEnd().getX(), cSs.get(i+1).getStart().getX()));
      node.setY(midPoint(cSs.get(i).getEnd().getY(), cSs.get(i+1).getStart().getY()));

      multiMarkers.add(node);
    }

    logger.info(format(messages.getString("MultiMarkerCount"), multiMarkers.size(), sRG.getId()));

    return multiMarkers;
  }


  /**
   * Creates an {@link EscherReaction} from a {@link ReactionGlyph}.
   *
   * @param reactionGlyph The {@code reaction glyph}.
   * @return The created {@code escher reaction}.
   */
  protected EscherReaction createReaction(ReactionGlyph reactionGlyph) {
    EscherReaction reaction = new EscherReaction();

    reaction.setName(reactionGlyph.getReactionInstance().getName());
    reaction.setId("" + (reactionGlyph.getId().hashCode() & 0xfffffff));
    reaction.setBiggId(reactionGlyph.getReactionInstance().getId());
    logger.info(format(messages.getString("ReactionGlyphToReaction"), reactionGlyph.getId(),
        reaction.getId()));

    Point point = new Point();
    if (reactionGlyph.getBoundingBox() != null) {
      logger.info(format(messages.getString("ReactionGlyphBBoxFound")));
      point.setX(reactionGlyph.getBoundingBox().getPosition().getX() + (0.5 * reactionGlyph
          .getBoundingBox().getDimensions().getWidth()));
      point.setY(reactionGlyph.getBoundingBox().getPosition().getY() + (0.5 * reactionGlyph
          .getBoundingBox().getDimensions().getHeight()));
    }
    else {
      logger.info(format(messages.getString("ReactionGlyphBBoxNotFound")));
      point.setX(0.5 * (reactionGlyph.getCurve()
                              .getCurveSegment(0)
                              .getStart()
                              .x() + reactionGlyph.getCurve()
                                                  .getCurveSegment(reactionGlyph.getCurve()
                                                                                .getCurveSegmentCount()-1)
                                                  .getStart().x()));
      point.setY(0.5 * (reactionGlyph.getCurve()
                                     .getCurveSegment(0)
                                     .getStart()
                                     .y() + reactionGlyph.getCurve()
                                                         .getCurveSegment(reactionGlyph.getCurve()
                                                                                       .getCurveSegmentCount()-1)
                                                         .getStart().y()));
    }
    reaction.setLabelX(point.getX());
    reaction.setLabelY(point.getY());

    // Add metabolites.
    logger.info(format(messages.getString("ReactionGlyphProductCount"), ((Reaction)
        reactionGlyph.getReactionInstance()).getListOfProducts().size()));
    ((Reaction) reactionGlyph.getReactionInstance()).getListOfProducts().forEach((p) -> {
      logger.info(format(messages.getString("MetaboliteCoefficient"), p.getSpecies(), 1.0));
      reaction.addMetabolite(createMetabolite(p));
    });

    logger.info(format(messages.getString("ReactionGlyphSubstrateCount"), ((Reaction)
        reactionGlyph.getReactionInstance()).getListOfReactants().size()));
    ((Reaction) reactionGlyph.getReactionInstance()).getListOfReactants().forEach((r) -> {
      logger.info(format(messages.getString("MetaboliteCoefficient"), r.getSpecies(), -1.0));
      r.setStoichiometry(-1 * r.getStoichiometry());
      reaction.addMetabolite(createMetabolite(r));
    });

    if (((Reaction)reactionGlyph.getReactionInstance()).isSetReversible()) {
      reaction.setReversibility(((Reaction)reactionGlyph.getReactionInstance()).isReversible());
    }
    else {
      reaction.setReversibility(true);
    }
    
    return reaction;
  }


  /**
   * Creates a list of {@link Segment}s from a {@link SpeciesReferenceGlyph}.
   *
   * @param sRG The {@code species reference glyph}.
   * @param rG The linked {@code reaction glyph}.
   * @return The list of created {@code segments}.
   */
  protected List<Segment> createSegments(SpeciesReferenceGlyph sRG, ReactionGlyph rG) {
    List<Segment> segments = new ArrayList<>();
    Segment segment = new Segment();
    List<CurveSegment> cSs = sRG.getCurve().getListOfCurveSegments();

    logger.info(format(messages.getString("CurveSegmentCount"), sRG.getId(), cSs.size()));

    segment.setId(sRG.getId() + ".S" + 0);
    segment.setFromNodeId(sRG.getSpeciesGlyph());
    for (int i = 0; i < (cSs.size()-1); i++) {
      segment.setToNodeId(sRG.getSpeciesReference() + ".M" + (i+1));

      if (cSs.get(i).isCubicBezier()) {
        CubicBezier cB = (CubicBezier) cSs.get(i);

        org.sbml.jsbml.ext.layout.Point point;

        point = cB.getBasePoint1();
        segment.setBasePoint1(new Point(point.x(), point.y()));
        point = cB.getBasePoint2();
        segment.setBasePoint2(new Point(point.x(), point.y()));
      }
      segments.add(segment);
      logger.info(format(messages.getString("CurveSegmentAdd"), segment.getId(), segment
          .getFromNodeId(), segment.getToNodeId()));

      segment = new Segment();

      segment.setId(sRG.getId() + ".S" + (i+1));
      segment.setFromNodeId(sRG.getSpeciesReference() + ".M" + (i+1));
    }

    segment.setToNodeId(rG.getId());

    if (cSs.get(cSs.size()-1).isCubicBezier()) {
      CubicBezier cB = (CubicBezier) cSs.get(cSs.size()-1);

      org.sbml.jsbml.ext.layout.Point point;

      point = cB.getBasePoint1();
      segment.setBasePoint1(new Point(point.x(), point.y()));
      point = cB.getBasePoint2();
      segment.setBasePoint2(new Point(point.x(), point.y()));
    }

    segments.add(segment);
    logger.info(format(messages.getString("CurveSegmentAdd"), segment.getId(), segment
        .getFromNodeId(), segment.getToNodeId()));

    return segments;
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

}

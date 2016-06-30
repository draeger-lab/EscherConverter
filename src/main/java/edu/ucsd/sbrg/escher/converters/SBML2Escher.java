package edu.ucsd.sbrg.escher.converters;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import edu.ucsd.sbrg.escher.model.*;
import edu.ucsd.sbrg.escher.model.Point;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.layout.*;
import org.sbml.jsbml.util.ResourceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Created by deveshkhandelwal on 20/06/16.
 */
public class SBML2Escher {

  /**
   * A {@link java.util.logging.Logger} for this class.
   */
  private static final Logger         logger = Logger.getLogger(SBGN2Escher.class.getName());
  /**
   * Localization support.
   */
  public static final  ResourceBundle bundle = ResourceManager.getBundle("Strings");
  protected List<EscherMap> escherMaps;
  protected SBMLDocument    document;
  protected List<Layout>    layouts;

  public SBML2Escher() {
    escherMaps = new ArrayList<>();
  }


  public List<EscherMap> convert(SBMLDocument document) {
    this.document = document;

    layouts = ((LayoutModelPlugin)document.getModel().getPlugin(LayoutConstants.shortLabel)).getListOfLayouts();

    layouts.forEach((layout) -> {

      EscherMap map = new EscherMap();

      map.setCanvas(addCanvasInfo(layouts.get(0)));
      map.setDescription(bundle.getString("default_description"));
      map.setId(HexBin.encode(layouts.get(0).toString().getBytes()));
      layout.getListOfSpeciesGlyphs().forEach((sG) -> {
        map.addNode(createNode(sG));
      });
      layout.getListOfReactionGlyphs().forEach((rG -> {
        map.addNode(createMidmarker(rG));
      }));
      layout.getListOfReactionGlyphs().forEach((rG) -> {
        rG.getListOfSpeciesReferenceGlyphs().forEach((sRG) -> {
          for (int i = 1; i < sRG.getCurve().getCurveSegmentCount(); i++) {
            map.addNode(createMultimarker(sRG.getCurve().getCurveSegment(i).getStart()));
          }
        });
      });
      layout.getListOfReactionGlyphs().forEach((rG) -> {
        map.addReaction(createReaction(rG));
      });
      layout.getListOfReactionGlyphs().forEach((rG) -> {
        rG.getListOfSpeciesReferenceGlyphs().forEach((sRG -> {
          for (int i = 0; i < sRG.getCurve().getCurveSegmentCount(); i++) {
            map.getReaction("" + (rG.getId().hashCode() & 0xfffffff))
               .addSegment(createSegment(sRG.getCurve().getCurveSegment(i), sRG, rG, i));
          }
        }));
      });

      escherMaps.add(map);
    });

    return escherMaps;
  }


  protected Canvas addCanvasInfo(Layout layout) {
    Canvas canvas = new Canvas();

    canvas.setX(Double.valueOf(bundle.getString("default_canvas_x")));
    canvas.setY(Double.valueOf(bundle.getString("default_canvas_y")));

    if (layout.getDimensions() == null) {
      canvas.setHeight(Double.valueOf(bundle.getString("default_canvas_height")));
      canvas.setWidth(Double.valueOf(bundle.getString("default_canvas_width")));
    }
    else {
      canvas.setHeight(layout.getDimensions().getHeight());
      canvas.setWidth(layout.getDimensions().getWidth());
    }

    return canvas;
  }


  protected TextLabel createTextLabel(TextGlyph textGlyph) {
    TextLabel textLabel = new TextLabel();

    if (textGlyph.getId() == null || textGlyph.getId().isEmpty()) {
      // TODO: Log about generating an Id.
      textLabel.setId("" + (textGlyph.hashCode() & 0xfffffff));
    }
    else {
      textLabel.setId(textGlyph.getId());
    }

    if (textGlyph.getText() == null || textGlyph.getText().isEmpty()) {
      // TODO: Log about no text, so ignoring text label.
      return null;
    }
    else {
      textLabel.setText(textGlyph.getText());
    }

    textLabel.setX(textGlyph.getBoundingBox().getPosition().getX());
    textLabel.setY(textGlyph.getBoundingBox().getPosition().getY());

    return textLabel;
  }


  protected Node createNode(SpeciesGlyph speciesGlyph) {
    Node node = new Node();

    node.setType(Node.Type.metabolite);
    node.setId(speciesGlyph.getId());
    node.setBiggId(speciesGlyph.getSpecies());
    node.setName(speciesGlyph.getSpeciesInstance().getName());
    node.setX(speciesGlyph.getBoundingBox().getPosition().x());
    node.setY(speciesGlyph.getBoundingBox().getPosition().y());
    node.setLabelX(speciesGlyph.getBoundingBox().getPosition().x());
    node.setLabelY(speciesGlyph.getBoundingBox().getPosition().y());

    // TODO: Find out if node is primary by either role or SBO term.
    node.setPrimary(true);

    return node;
  }


  protected Node createMidmarker(ReactionGlyph reactionGlyph) {
    Node node = new Node();

    node.setId(reactionGlyph.getId());
    node.setType(Node.Type.midmarker);

    Point point = new Point();
    if (reactionGlyph.getBoundingBox() != null) {
      point.setX(reactionGlyph.getBoundingBox().getPosition().getX() + (0.5 * reactionGlyph
          .getBoundingBox().getDimensions().getWidth()));
      point.setY(reactionGlyph.getBoundingBox().getPosition().getY() + (0.5 * reactionGlyph
          .getBoundingBox().getDimensions().getHeight()));
    }
    else {
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


  protected Node createMultimarker(org.sbml.jsbml.ext.layout.Point point) {
    Node node = new Node();

    node.setId("" + (("" + (point.x() + point.y())).hashCode() & 0xfffffff));
    node.setType(Node.Type.multimarker);
    node.setX(point.x());
    node.setY(point.y());

    return node;
  }


  protected EscherReaction createReaction(ReactionGlyph reactionGlyph) {
    EscherReaction reaction = new EscherReaction();

    reaction.setName(reactionGlyph.getReactionInstance().getName());
    reaction.setId("" + (reactionGlyph.getId().hashCode() & 0xfffffff));
    reaction.setBiggId(reactionGlyph.getReactionInstance().getId());

    Point point = new Point();
    if (reactionGlyph.getBoundingBox() != null) {
      point.setX(reactionGlyph.getBoundingBox().getPosition().getX() + (0.5 * reactionGlyph
          .getBoundingBox().getDimensions().getWidth()));
      point.setY(reactionGlyph.getBoundingBox().getPosition().getY() + (0.5 * reactionGlyph
          .getBoundingBox().getDimensions().getHeight()));
    }
    else {
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
    ((Reaction) reactionGlyph.getReactionInstance()).getListOfProducts().forEach((p) -> {
      reaction.addMetabolite(createMetabolite(p));
    });

    ((Reaction) reactionGlyph.getReactionInstance()).getListOfReactants().forEach((r) -> {
      r.setStoichiometry(-1 * r.getStoichiometry());
      reaction.addMetabolite(createMetabolite(r));
    });

    // TODO: Think of what to do about genes.

    // TODO: Add segments.

    return reaction;
  }


  protected Segment createSegment(CurveSegment cS, SpeciesReferenceGlyph sRG, ReactionGlyph rG, int i) {
    Segment segment = new Segment();

    segment.setId("" + (cS.hashCode() & 0xfffffff));
    if (i == 0 && i == (sRG.getCurve().getCurveSegmentCount() - 1)) {
      if (sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.PRODUCT ||
          sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.SIDEPRODUCT) {
        segment.setFromNodeId(rG.getId());
        segment.setToNodeId(sRG.getSpeciesGlyph());
      }
      else if (sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.SUBSTRATE ||
          sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.SIDESUBSTRATE) {
        segment.setToNodeId(rG.getId());
        segment.setFromNodeId(sRG.getSpeciesGlyph());
      }
    }
    else if (i == 0) {
      if (sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.PRODUCT ||
          sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.SIDEPRODUCT) {
        segment.setFromNodeId(rG.getId());
        segment.setToNodeId("" + (cS.hashCode() & 0xfffffff));
      }
      else if (sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.SUBSTRATE ||
          sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.SIDESUBSTRATE) {
        segment.setToNodeId(rG.getId());
        segment.setFromNodeId(sRG.getSpeciesGlyph());
      }
    }
    else if (i == (sRG.getCurve().getCurveSegmentCount() - 1)) {
      if (sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.PRODUCT ||
          sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.SIDEPRODUCT) {
        segment.setFromNodeId(rG.getId());
        segment.setToNodeId(sRG.getSpeciesGlyph());
      }
      else if (sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.SUBSTRATE ||
          sRG.getSpeciesReferenceRole() == SpeciesReferenceRole.SIDESUBSTRATE) {
        segment.setToNodeId(rG.getId());
        segment.setFromNodeId("" + (cS.hashCode() & 0xfffffff));
      }
    }
    else {
      segment.setToNodeId("" + (cS.hashCode() & 0xfffffff));
      segment.setFromNodeId("" + (cS.hashCode() & 0xfffffff));
    }

    if (cS.getType() == CurveSegment.Type.CUBIC_BEZIER) {
      segment.setBasePoint1(new Point(((CubicBezier)cS).getBasePoint1().x(), ((CubicBezier)cS)
          .getBasePoint1().y()));
      segment.setBasePoint2(new Point(((CubicBezier)cS).getBasePoint2().x(), ((CubicBezier)cS)
          .getBasePoint2().y()));
    }

    return segment;
  }


  protected Metabolite createMetabolite(SpeciesReference speciesReference) {
    Metabolite metabolite = new Metabolite();

    metabolite.setId(speciesReference.getSpecies());
    metabolite.setCoefficient(speciesReference.getCalculatedStoichiometry());

    return metabolite;
  }

}

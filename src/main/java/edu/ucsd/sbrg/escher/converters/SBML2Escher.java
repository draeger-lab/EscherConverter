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


  public EscherMap convert(SBMLDocument document) {
    this.document = document;

    layouts = ((LayoutModelPlugin)document.getModel().getPlugin(LayoutConstants.shortLabel)).getListOfLayouts();
    escherMaps.add(new EscherMap());

    escherMaps.get(0).setCanvas(addCanvasInfo(layouts.get(0)));
    escherMaps.get(0).setDescription(bundle.getString("default_description"));
    escherMaps.get(0).setId(HexBin.encode(layouts.get(0).toString().getBytes()));
    layouts.get(0).getListOfSpeciesGlyphs().forEach((sG) -> {
      escherMaps.get(0).addNode(createNode(sG));
    });
    layouts.get(0).getListOfReactionGlyphs().forEach((rG) -> {
      escherMaps.get(0).addReaction(createReaction(rG));
    });
    return escherMaps.get(0);
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
    node.setId("" + (speciesGlyph.getId().hashCode() & 0xfffffff));
    node.setBiggId(speciesGlyph.getSpecies());
    node.setName(speciesGlyph.getSpeciesInstance().getName());
    node.setX(speciesGlyph.getBoundingBox().getPosition().x());
    node.setY(speciesGlyph.getBoundingBox().getPosition().y());
    node.setLabelX(speciesGlyph.getBoundingBox().getPosition().x());
    node.setLabelY(speciesGlyph.getBoundingBox().getPosition().y());

    return node;
  }


  protected Node createMidmarker(ReactionGlyph reactionGlyph) {
    Node node = new Node();

    node.setType(Node.Type.midmarker);
    node.setX(reactionGlyph.getBoundingBox().getPosition().getX() +
        (reactionGlyph.getBoundingBox().getDimensions().getWidth() * 0.5));
    node.setY(reactionGlyph.getBoundingBox().getPosition().getY() +
        (reactionGlyph.getBoundingBox().getDimensions().getHeight() * 0.5));

    return node;
  }


  protected EscherReaction createReaction(ReactionGlyph reactionGlyph) {
    EscherReaction reaction = new EscherReaction();

    reaction.setName(reactionGlyph.getReactionInstance().getName());
    reaction.setId(reactionGlyph.getId());
    reaction.setBiggId(reactionGlyph.getReactionInstance().getId());

    Point point = new Point();
    if (reactionGlyph.getBoundingBox() != null) {
      point.setX(reactionGlyph.getBoundingBox().getPosition().getX());
      point.setY(reactionGlyph.getBoundingBox().getPosition().getY());
    }
    else {
      point.setX(reactionGlyph.getCurve().getCurveSegment(0).getStart().x());
      point.setY(reactionGlyph.getCurve()
                              .getCurveSegment(reactionGlyph.getCurve()
                                                            .getCurveSegmentCount()-1)
                                                            .getStart().y());
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


  protected Metabolite createMetabolite(SpeciesReference speciesReference) {
    Metabolite metabolite = new Metabolite();

    metabolite.setId(speciesReference.getSpecies());
    metabolite.setCoefficient(speciesReference.getCalculatedStoichiometry());

    return metabolite;
  }

}

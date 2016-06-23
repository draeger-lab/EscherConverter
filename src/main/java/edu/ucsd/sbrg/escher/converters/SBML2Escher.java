package edu.ucsd.sbrg.escher.converters;

import edu.ucsd.sbrg.escher.model.*;
import org.sbml.jsbml.SBMLDocument;
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
    node.setLabelX(speciesGlyph.getBoundingBox().getPosition().y());

    return node;
  }


  protected EscherReaction createReaction(ReactionGlyph reactionGlyph) {
    throw new UnsupportedOperationException("Not yet implemented!");
  }

}

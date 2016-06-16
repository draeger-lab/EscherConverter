package edu.ucsd.sbrg.escher.converters;

import edu.ucsd.sbrg.escher.model.*;
import org.sbgn.bindings.Bbox;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.util.ResourceManager;

import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Created by deveshkhandelwal on 14/06/16.
 */
public abstract class Standard2Escher<T> {

  /**
   * A {@link java.util.logging.Logger} for this class.
   */
  private static final Logger         logger = Logger.getLogger(Standard2Escher.class.getName());
  /**
   * Localization support.
   */
  public static final  ResourceBundle bundle = ResourceManager.getBundle("Messages");
  protected EscherMap escherMap;
  protected T         document;
  protected long escherId;
  protected long segmentId;
  protected long reactionId = 0;



  public Standard2Escher() {
    escherMap = new EscherMap();
  }


  public abstract EscherMap convert(T document);


  protected void addCanvasInfo(Bbox bbox) {
    Canvas canvas = new Canvas();

    if (bbox != null) {
      canvas.setX((double) bbox.getX());
      canvas.setY((double) bbox.getY());
      canvas.setHeight((double) bbox.getH());
      canvas.setWidth((double) bbox.getW());
    }
    else {
      // TODO: Set default canvas values.
    }

    escherMap.setCanvas(canvas);
  }


  protected void addMetaInfo() {
    escherMap.setSchema(bundle.getString("escher_schema"));
    
    // TODO: Meta info is note directly available, needs to be determined carefully.
    throw new UnsupportedOperationException("Not yet implemented.");
  }


  protected Node createNode(Glyph glyph) {
    Node node = new Node();

    node.setId("" + glyph.getId().hashCode());
    node.setX(glyph.getBbox().getX() + glyph.getBbox().getW() * 0.5);
    node.setY(glyph.getBbox().getY() + glyph.getBbox().getH() * 0.5);

    // TODO: The following won't work, type will be determined a different and more involved way.
    switch (glyph.getClazz()) {
    case "simple chemical":
    case "perturbing agent":
    case "macromolecule":
      node.setType(Node.Type.metabolite);
      break;
    default:
      node.setType(Node.Type.midmarker);
    }

    if (node.getType() == Node.Type.metabolite) {
      node.setName(glyph.getLabel().getText());
      node.setLabelX((double) glyph.getBbox().getX());
      node.setLabelY((double) glyph.getBbox().getY());
      node.setBiggId(glyph.getId());
    }

    return node;
  }


  protected EscherReaction createReaction(Glyph glyph) {
    EscherReaction reaction = new EscherReaction();

    reaction.setId(glyph.getId().hashCode() + "");
    if (glyph.getLabel() == null) {
      reaction.setName("R" + reactionId++);
    }
    else {
      reaction.setName(glyph.getLabel().getText());
    }
    reaction.setLabelX(((double) glyph.getBbox().getX()));
    reaction.setLabelY(((double) glyph.getBbox().getY()));
    reaction.setMidmarker(createNode(glyph));

    return reaction;
  }


  protected TextLabel createTextLabel(Glyph glyph) {
    TextLabel textLabel = new TextLabel();

    textLabel.setId(glyph.getId().hashCode() + "");
    textLabel.setX(glyph.getBbox().getX() + glyph.getBbox().getW() * 0.5);
    textLabel.setY(glyph.getBbox().getY() + glyph.getBbox().getH() * 0.5);
    textLabel.setText(glyph.getLabel().getText());

    return textLabel;
  }

}

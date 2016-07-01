package edu.ucsd.sbrg.escher.converters;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import edu.ucsd.sbrg.escher.model.*;
import edu.ucsd.sbrg.escher.model.Point;
import org.sbgn.bindings.*;
import org.sbml.jsbml.util.ResourceManager;

import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Created by deveshkhandelwal on 13/06/16.
 */
public class SBGN2Escher {

  /**
   * A {@link java.util.logging.Logger} for this class.
   */
  private static final Logger         logger = Logger.getLogger(SBGN2Escher.class.getName());
  /**
   * Localization support.
   */
  public static final  ResourceBundle bundle = ResourceManager.getBundle("Strings");
  protected EscherMap escherMap;
  protected Sbgn document;
  protected long escherId;
  protected long segmentId = 0;
  protected long reactionId = 0;


  public SBGN2Escher() {
    escherMap = new EscherMap();
  }


  public void addCanvasInfo(Bbox bbox) {
    Canvas canvas = new Canvas();

    if (bbox != null) {
      canvas.setX((double) bbox.getX());
      canvas.setY((double) bbox.getY());
      canvas.setHeight((double) bbox.getH());
      canvas.setWidth((double) bbox.getW());
    }
    else {
      // TODO: Set default canvas values.
      canvas.setX(Double.valueOf(bundle.getString("default_canvas_x")));
      canvas.setY(Double.valueOf(bundle.getString("default_canvas_y")));
      canvas.setHeight(Double.valueOf(bundle.getString("default_canvas_height")));
      canvas.setWidth(Double.valueOf(bundle.getString("default_canvas_width")));
    }

    escherMap.setCanvas(canvas);
  }


  public void addMetaInfo() {
    escherMap.setSchema(bundle.getString("escher_schema"));
    escherMap.setDescription(bundle.getString("default_description"));
    escherMap.setId(bundle.getString("default_id"));

    // TODO: Meta info is not directly available, needs to be determined carefully.

    escherMap.setId(HexBin.encode(document.getMap().toString().getBytes()));

    if (document.getMap().getNotes()!=null) {
      escherMap.setDescription(document.getMap().getNotes().toString());
    }
  }


  public Node createNode(Glyph glyph) {
    Node node = new Node();

    node.setId("" + (glyph.getId().hashCode() & 0xfffffff));
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


  public EscherReaction createReaction(Glyph glyph) {
    EscherReaction reaction = new EscherReaction();

    reaction.setId((glyph.getId().hashCode() & 0xfffffff) + "");
    if (glyph.getLabel() == null) {
      reaction.setName("R" + reactionId++);
    }
    else {
      reaction.setName(glyph.getLabel().getText());
    }
    reaction.setBiggId(reaction.getName());
    reaction.setLabelX(((double) glyph.getBbox().getX()));
    reaction.setLabelY(((double) glyph.getBbox().getY()));
    reaction.setMidmarker(createNode(glyph));

    document.getMap().getArc().forEach((a) -> {

      if (a.getSource().getClass() == Glyph.class) {
        if (((Glyph) a.getSource()).getId().split(Pattern.quote("."))[0].equals(glyph.getId())) {
          reaction.addSegment(createSegment(a));

          // Metabolite for reaction. Coefficient is negative as its source.
          Metabolite metabolite = new Metabolite();
          metabolite.setId(((Glyph) a.getSource()).getId());
          metabolite.setCoefficient(-1.0);
          reaction.addMetabolite(metabolite);
        }
      }
      else if (a.getSource().getClass() == Port.class) {
        if (((Port) a.getSource()).getId().split(Pattern.quote("."))[0].equals(glyph.getId())) {
          reaction.addSegment(createSegment(a));
        }
      }

      if (a.getTarget().getClass() == Glyph.class) {
        if (((Glyph) a.getTarget()).getId().split(Pattern.quote("."))[0].equals(glyph.getId())) {
          reaction.addSegment(createSegment(a));

          // Metabolite for reaction. Coefficient is positive as its target.
          Metabolite metabolite = new Metabolite();
          metabolite.setId(((Glyph) a.getTarget()).getId());
          metabolite.setCoefficient(1.0);
          reaction.addMetabolite(metabolite);
        }
      }
      else if (a.getTarget().getClass() == Port.class) {
        if (((Port) a.getTarget()).getId().split(Pattern.quote("."))[0].equals(glyph.getId())) {
          reaction.addSegment(createSegment(a));
        }
      }


    });

    return reaction;
  }


  public TextLabel createTextLabel(Glyph glyph) {
    TextLabel textLabel = new TextLabel();

    textLabel.setId((glyph.getId().hashCode() & 0xfffffff) + "");
    textLabel.setX(glyph.getBbox().getX() + glyph.getBbox().getW() * 0.5);
    textLabel.setY(glyph.getBbox().getY() + glyph.getBbox().getH() * 0.5);
    textLabel.setText(glyph.getLabel().getText());

    return textLabel;
  }

  public Segment createSegment(Arc arc) {
    Segment segment = new Segment();

    if (arc.getId() == null) {
      segment.setId("S" + segmentId++);
    }
    else {
      segment.setId(arc.getId());
    }

    try {
        segment.setFromNodeId((((Glyph)arc.getSource()).getId().split(Pattern.quote("."))[0]
            .hashCode
            () &
            0xfffffff)
          + "");
    }
    catch (ClassCastException ex) {
      segment.setFromNodeId((((Port)arc.getSource()).getId().split(Pattern.quote("."))[0]
          .hashCode() &
          0xfffffff) +
          "");
    }

    try {
      segment.setToNodeId((((Glyph)arc.getTarget()).getId().split(Pattern.quote("."))[0].hashCode
          () &
          0xfffffff) +
          "");
    }
    catch (ClassCastException ex) {
      segment.setToNodeId((((Port)arc.getTarget()).getId().split(Pattern.quote("."))[0].hashCode
          () &
          0xfffffff) +
          "");
    }

    segment.setBasePoint1(new Point((double)arc.getStart().getX(), (double)arc.getStart().getY()));
    segment.setBasePoint2(new Point((double)arc.getEnd().getX(), (double)arc.getEnd().getY()));

    return segment;
  }


  private String determineComponent(String classs) {
    // TODO: Determine class according to the SBGN PD Level 1 spec draft.
    switch (classs) {

    case "macromolecule":
    case "simple chemical":
    case "perturbing agent":
    case "unspecified entity":
      return "node";

    case "tag":
    case "annotation":
      return "text_label";

    case "process":
    case "omitted process":
    case "uncertain process":
    case "association":
    case "dissociation":
      return "reaction";

    default:
      return classs;

    }
  }


  public EscherMap convert(Sbgn document) {
    this.document = document;
    Map map = document.getMap();

    addCanvasInfo(map.getBbox());
    addMetaInfo();

    // For every glyph, determine its class and call the appropriate method accordingly.
    map.getGlyph().forEach((g) -> {
      String component = determineComponent(g.getClazz());
      switch (component) {

      case "node":
        // TODO: Call createNode and add to EscherMap properly.
        escherMap.addNode(createNode(g));
        break;

      case "reaction":
        // TODO: Call createReaction and add to EscherMap properly.
        escherMap.addNode(createNode(g));
        escherMap.addReaction(createReaction(g));
        break;

      case "text_label":
        // TODO: Call createTextLabel and add to EscherMap properly.
        escherMap.addTextLabel(createTextLabel(g));
        break;

      default:
        // TODO: Log a message saying unsupported class.
        logger.warning(String.format("Unsupported class: glyph = %s, class =  %s", g.getId(), g
            .getClazz
            ()));
        break;

      }
    });

    return escherMap;
  }
}

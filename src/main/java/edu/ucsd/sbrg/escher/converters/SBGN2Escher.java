package edu.ucsd.sbrg.escher.converters;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import edu.ucsd.sbrg.escher.model.*;
import edu.ucsd.sbrg.escher.model.Point;
import org.sbgn.bindings.*;
import org.sbgn.bindings.Map;
import org.sbml.jsbml.util.ResourceManager;

import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

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
  public static final  ResourceBundle messages = ResourceManager.getBundle("Messages");
  protected EscherMap escherMap;
  protected Sbgn document;
  protected long escherId;
  protected long segmentId = 0;
  protected long reactionId = 0;

  protected Set<String>             glyphIds;
  protected HashMap<String, String> port2GlyphMap;
  protected HashMap<String, String> arc2GlyphMap;
  protected Set<String>             processIds;
  protected Set<String>             metaboliteIds;
  protected HashMap<String, String> glyphId2LabelMap;


  public SBGN2Escher() {
    escherMap = new EscherMap();
    port2GlyphMap = new HashMap<>();
    glyphIds = new HashSet<>();
    arc2GlyphMap = new HashMap<>();
    processIds = new HashSet<>();
    metaboliteIds = new HashSet<>();
    glyphId2LabelMap = new HashMap<>();
  }


  public void addCanvasInfo(Bbox bbox) {
    Canvas canvas = new Canvas();

    if (bbox != null) {
      logger.fine(messages.getString("RootBBoxFound"));
      canvas.setX((double) bbox.getX());
      canvas.setY((double) bbox.getY());
      canvas.setHeight((double) bbox.getH());
      canvas.setWidth((double) bbox.getW());
    }
    else {
      logger.info(messages.getString("RootBBoxNotFound"));
      canvas.setX(Double.valueOf(bundle.getString("default_canvas_x")));
      canvas.setY(Double.valueOf(bundle.getString("default_canvas_y")));
      canvas.setHeight(Double.valueOf(bundle.getString("default_canvas_height")));
      canvas.setWidth(Double.valueOf(bundle.getString("default_canvas_width")));
    }

    escherMap.setCanvas(canvas);
    logger.info(messages.getString("EscherCanvasAddSuccess"));
  }


  public void addMetaInfo() {
    escherMap.setSchema(bundle.getString("escher_schema"));
    escherMap.setDescription(bundle.getString("default_description"));
    escherMap.setId(HexBin.encode(document.getMap().toString().getBytes()));
    logger.fine(messages.getString("EscherIdAddSuccess"));

    if (document.getMap().getNotes()!=null) {
      logger.fine(messages.getString("SBGNNotesFound"));
      escherMap.setDescription(document.getMap().getNotes().toString());
    }
    else {
      logger.info(messages.getString("SBGNNotesNotFound"));
    }
  }


  public Node createNode(Glyph glyph) {
    Node node = new Node();

    node.setId(glyph.getId());
    node.setX(glyph.getBbox().getX() + glyph.getBbox().getW() * 0.5);
    node.setY(glyph.getBbox().getY() + glyph.getBbox().getH() * 0.5);
    node.setType(Node.Type.metabolite);
    node.setName(glyph.getLabel().getText());
    node.setBiggId(glyph.getLabel().getText());

    if (glyph.getLabel() != null && glyph.getLabel().getBbox() != null) {
      node.setLabelX((double) glyph.getLabel().getBbox().getX());
      node.setLabelY((double) glyph.getLabel().getBbox().getY());
    }
    else {
      logger.warning(format(messages.getString("GlyphLabelBBoxUnavailable")));
      node.setLabelX((double) glyph.getBbox().getX());
      node.setLabelY((double) glyph.getBbox().getY());
    }
    return node;
  }


  public Node createMidMarker(Glyph glyph) {
    Node node = new Node();

    node.setId(glyph.getId());
    node.setX(glyph.getBbox().getX() + glyph.getBbox().getW() * 0.5);
    node.setY(glyph.getBbox().getY() + glyph.getBbox().getH() * 0.5);
    node.setType(Node.Type.midmarker);

    return node;
  }


  public Node createMultiMarker(Arc.Next next) {
    Node node = new Node();

    node.setId("" + (next.hashCode() & 0xfffffff));
    node.setX((double) next.getX());
    node.setY((double) next.getY());
    node.setType(Node.Type.multimarker);

    return node;
  }


  public EscherReaction createReaction(Glyph glyph) {
    EscherReaction reaction = new EscherReaction();

    reaction.setId((glyph.getId().hashCode() & 0xfffffff) + "");
    logger.info(format(messages.getString("GlyphToReactionId"), glyph.getId(), reaction.getId()));
    if (glyph.getLabel() == null) {
      reaction.setName("R" + reactionId++);
      logger.info(format(messages.getString("GlyphReactionNoLabel"), glyph.getId(), reaction
          .getName()));
    }
    else {
      logger.fine(format(messages.getString("GlyphReactionLabel"), glyph.getId(), glyph.getLabel
          ().getText()));
      reaction.setName(glyph.getLabel().getText());
    }
    reaction.setBiggId(reaction.getName());
    logger.fine(format(messages.getString("ReactionIdenticalNameAndBigg"), reaction.getId(),
        reaction.getName()));
    reaction.setLabelX(((double) glyph.getBbox().getX()));
    reaction.setLabelY(((double) glyph.getBbox().getY()));
    reaction.setMidmarker(createMidMarker(glyph));

    logger.fine(format(messages.getString("ReactionSegmentAddInit"), reaction.getId()));
    document.getMap().getArc().forEach((a) -> {

      if (glyph.getId().equals(arc2GlyphMap.get(a.getId()))) {
        logger.info(format(messages.getString("ReactionArcsAdd"), a.getId(), reaction.getId()));
        createSegments(a).forEach(reaction::addSegment);
      }

      Metabolite metabolite = new Metabolite();
      if (a.getClazz().equals("consumption")) {
        logger.info(format(messages.getString("ConsumptionArcNegativeCoeff"), a.getId()));
        metabolite.setCoefficient(-1.0);
        metabolite.setId(glyphId2LabelMap.get(getGlyphIdFromPortId(getIdFromSourceOrTarget(a
            .getSource()))));
      }

      if (a.getClazz().equals("production")) {
        logger.info(format(messages.getString("ProductionArcNegativeCoeff"), a.getId()));
        metabolite.setCoefficient(1.0);
        metabolite.setId(glyphId2LabelMap.get(getGlyphIdFromPortId(getIdFromSourceOrTarget(a
            .getTarget()))));
      }

      reaction.addMetabolite(metabolite);
    });
    logger.fine(format(messages.getString("ReactionSegmentAddFinish"), reaction.getId()));

    return reaction;
  }


  public double extractCoefficient(Object object) {
    String id = getGlyphIdFromPortId(getIdFromSourceOrTarget(object));

    Glyph glyph = document.getMap()
                          .getGlyph()
                          .stream()
                          .filter(g -> g.getId()
                                        .equals(id))
                          .collect(Collectors.toList()).get(0);

    if (glyph.getGlyph() == null) {
      return 1.0;
    }
    else if (glyph.getGlyph().get(0).getClazz().equals("state variable")) {
      return Double.parseDouble(glyph.getGlyph().get(0).getState().getValue().replace('P', ' '));
    }
    else {
      return 1.0;
    }
  }


  public TextLabel createTextLabel(Glyph glyph) {
    TextLabel textLabel = new TextLabel();

    textLabel.setId((glyph.getId().hashCode() & 0xfffffff) + "");
    textLabel.setX(glyph.getBbox().getX() + glyph.getBbox().getW() * 0.5);
    textLabel.setY(glyph.getBbox().getY() + glyph.getBbox().getH() * 0.5);
    textLabel.setText(glyph.getLabel().getText());

    return textLabel;
  }


  public List<Segment> createSegments(Arc arc) {
    List<Segment> segments = new ArrayList<>();

    logger.info(format(messages.getString("ArcSegmentCount"), arc.getId(), arc.getNext().size()));
    Segment segment = new Segment();

    segment.setId(arc.getId() + ".S" + 0);
    segment.setFromNodeId(getGlyphIdFromPortId(getIdFromSourceOrTarget(arc.getSource())));

    for (int i = 0; i < arc.getNext().size(); i++) {

      Arc.Next next = arc.getNext().get(i);

      segment.setToNodeId("" + (next.hashCode() & 0xfffffff));

      if (!next.getPoint().isEmpty()) {
        Point point = new Point();

        point.setX((double) next.getPoint().get(0).getX());
        point.setY((double) next.getPoint().get(0).getY());
        segment.setBasePoint1(point);

        point.setX((double) next.getPoint().get(1).getX());
        point.setY((double) next.getPoint().get(1).getY());
        segment.setBasePoint2(point);
      }

      segments.add(segment);
      logger.fine(format(messages.getString("SegmentAdd"), segment.getId(), segment.getFromNodeId
          (), segment.getToNodeId()));

      segment = new Segment();

      segment.setId(arc.getId() + ".S" + (i+1));
      segment.setFromNodeId("" + (next.hashCode() & 0xfffffff));
    }

    segment.setToNodeId(getGlyphIdFromPortId(getIdFromSourceOrTarget(arc.getTarget())));

    if (!arc.getEnd().getPoint().isEmpty()) {
      Point point = new Point();

      point.setX((double) arc.getEnd().getPoint().get(0).getX());
      point.setY((double) arc.getEnd().getPoint().get(0).getY());
      segment.setBasePoint1(point);

      point.setX((double) arc.getEnd().getPoint().get(1).getX());
      point.setY((double) arc.getEnd().getPoint().get(1).getY());
      segment.setBasePoint2(point);
    }

    segments.add(segment);
    logger.fine(format(messages.getString("SegmentAdd"), segment.getId(), segment.getFromNodeId
        (), segment.getToNodeId()));

    return segments;
  }


  public String getIdFromSourceOrTarget(Object sOrT) {
    if (sOrT.getClass().isAssignableFrom(Glyph.class)) {
      return ((Glyph)sOrT).getId();
    }
    else if (sOrT.getClass().isAssignableFrom(Port.class)) {
      return ((Port)sOrT).getId();
    }
    return null;
  }


  public String getGlyphIdFromPortId(String id) {
    if (glyphIds.contains(id)) {
      return id;
    }
    else {
      return port2GlyphMap.get(id);
    }
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
    logger.info(messages.getString("SBGNImportInit"));

    this.document = document;
    Map map = document.getMap();

    addCanvasInfo(map.getBbox());
    addMetaInfo();

    preProcessSbgn();

    // For every glyph, determine its class and call the appropriate method accordingly.
    map.getGlyph().forEach((g) -> {
      String component = determineComponent(g.getClazz());
      switch (component) {

      case "node":
        // TODO: Call createNode and add to EscherMap properly.
        logger.info(format(messages.getString("GlyphNode"), g.getId(), g.getClazz()));
        escherMap.addNode(createNode(g));
        break;

      case "reaction":
        // TODO: Call createReaction and add to EscherMap properly.
        logger.info(format(messages.getString("GlyphReaction"), g.getId(), g.getClazz()));
        escherMap.addNode(createMidMarker(g));
        escherMap.addReaction(createReaction(g));
        break;

      case "text_label":
        // TODO: Call createTextLabel and add to EscherMap properly.
        logger.info(format(messages.getString("GlyphTextLabel"), g.getId(), g.getClazz()));
        escherMap.addTextLabel(createTextLabel(g));
        break;

      default:
        // TODO: Log a message saying unsupported class.
        logger.warning(format(messages.getString("GlyphUnsupportedClass"), g.getId(), g.getClazz()));
        break;

      }
    });

    map.getArc().forEach(a -> {
      logger.info(format(messages.getString("ArcMultiMarkerCount"), a.getId(), a.getNext().size()));
      a.getNext().forEach(next -> {
        escherMap.addNode(createMultiMarker(next));
      });
    });

    return escherMap;
  }


  protected void preProcessSbgn() {
    document.getMap().getGlyph().forEach(g -> {
      // Store all glyph Ids for future retrieval.
      glyphIds.add(g.getId());

      // Store all metabolite Ids for future retrieval.
      if (determineComponent(g.getClazz()).equals("node")) {
        metaboliteIds.add(g.getId());
      }

      // Store all process Ids for future retrieval.
      if (determineComponent(g.getClazz()).equals("reaction")) {
        processIds.add(g.getId());
      }

      // Store all ports with their respective glyph.
      g.getPort().forEach(p -> {
        port2GlyphMap.put(p.getId(), g.getId());
      });

      // Store all labels with their respective labels.
      if (g.getLabel() != null) {
        glyphId2LabelMap.put(g.getId(), g.getLabel().getText());
      }
    });

    document.getMap().getArc().forEach(a -> {
      // Store all arcs with their respective process nodes.
      String id;

      id = getGlyphIdFromPortId(getIdFromSourceOrTarget(a.getSource()));
      if (processIds.contains(id)) {
        arc2GlyphMap.put(a.getId(), id);
      }

      id = getGlyphIdFromPortId(getIdFromSourceOrTarget(a.getTarget()));
      if (processIds.contains(id)) {
        arc2GlyphMap.put(a.getId(), id);
      }
    });
  }
}

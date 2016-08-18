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
  public static final  ResourceBundle messages = ResourceManager.getBundle("Messages");
  /**
   * Default values.
   */
  public static final  ResourceBundle bundle = ResourceManager.getBundle("Strings");
  /**
   * Output Escher map.
   */
  protected EscherMap escherMap;
  /**
   * Input SBGN-ML document.
   */
  protected Sbgn document;
  /**
   * Generated reaction ids.
   */
  protected long reactionId = 0;
  /**
   * List of {@link Glyph} ids.
   */
  protected Set<String>             glyphIds;
  /**
   * {@link Port} ids with their respective {@link Glyph} ids.
   */
  protected HashMap<String, String> port2GlyphMap;
  /**
   * {@link Arc}s' ids with their respective process ({@link Glyph}) nodes ids.
   */
  protected HashMap<String, String> arc2GlyphMap;
  /**
   * List of process ({@link Glyph}) ids.
   */
  protected Set<String>             processIds;
  /**
   * List of {@link Metabolite} ids.
   */
  protected Set<String>             metaboliteIds;
  /**
   * {@link Glyph} ids with their respective label.
   */
  protected HashMap<String, String> glyphId2LabelMap;


  /**
   * Default constructor.
   */
  public SBGN2Escher() {
    escherMap = new EscherMap();
    port2GlyphMap = new HashMap<>();
    glyphIds = new HashSet<>();
    arc2GlyphMap = new HashMap<>();
    processIds = new HashSet<>();
    metaboliteIds = new HashSet<>();
    glyphId2LabelMap = new HashMap<>();
  }


  /**
   * Add canvas info to the internal {@link #escherMap} field.
   *
   * @param bbox BBox containing canvas info.
   */
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
      // Add default values if no BBox is found.
      logger.info(messages.getString("RootBBoxNotFound"));
      canvas.setX(Double.valueOf(bundle.getString("default_canvas_x")));
      canvas.setY(Double.valueOf(bundle.getString("default_canvas_y")));
      canvas.setHeight(Double.valueOf(bundle.getString("default_canvas_height")));
      canvas.setWidth(Double.valueOf(bundle.getString("default_canvas_width")));
    }

    escherMap.setCanvas(canvas);
    logger.info(messages.getString("EscherCanvasAddSuccess"));
  }


  /**
   * Add meta info about the map to the internal {@link #escherMap} field.
   */
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


  /**
   * Create an Escher {@link Node}(metabolite) from EPN {@link Glyph}.
   *
   * @param glyph The {@code glyph}.
   * @return The created {@code node}.
   */
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
    node.setPrimary(true);
    
    return node;
  }


  /**
   * Create an Escher {@link Node}(mid-marker) from process {@link Glyph}.
   *
   * @param glyph The {@code glyph}.
   * @return The create {@code node}.
   */
  public Node createMidMarker(Glyph glyph) {
    Node node = new Node();

    node.setId(glyph.getId());
    node.setX(glyph.getBbox().getX() + glyph.getBbox().getW() * 0.5);
    node.setY(glyph.getBbox().getY() + glyph.getBbox().getH() * 0.5);
    node.setType(Node.Type.midmarker);

    return node;
  }


  /**
   * Create an Escher {@link Node}(multi-marker) from {@link Arc.Next}.
   *
   * @param next The {@code next} element.
   * @return The created {@code node}.
   */
  public Node createMultiMarker(Arc.Next next) {
    Node node = new Node();

    node.setId("" + (next.hashCode() & 0xfffffff));
    node.setX((double) next.getX());
    node.setY((double) next.getY());
    node.setType(Node.Type.multimarker);

    return node;
  }


  /**
   * Create an {@link EscherReaction} from an SBGN-ML {@link Glyph}. The glyph is used to create
   * the mid-marker.
   *
   * @param glyph The {@code glyph}.
   * @return The created {@code reaction}.
   */
  public EscherReaction createReaction(Glyph glyph) {
    EscherReaction reaction = new EscherReaction();
    Set<String> sources = new HashSet<>(), targets = new HashSet<>();


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
    // This adds arcs which are either "production" or "consumption" into the reaction.
    document.getMap().getArc().stream().filter(a -> a.getClazz().equals("production") || a
        .getClazz().equals("consumption")).collect(Collectors.toList()).forEach((a) -> {

      // If the arc is linked to the process node (mid-marker) of the reaction.
      if (glyph.getId().equals(arc2GlyphMap.get(a.getId()))) {
        logger.info(format(messages.getString("ReactionArcsAdd"), a.getId(), reaction.getId()));
        sources.add(port2GlyphMap.get(getIdFromSourceOrTarget(a.getSource())));
        targets.add(port2GlyphMap.get(getIdFromSourceOrTarget(a.getTarget())));
        createSegments(a).forEach(reaction::addSegment);
      }

      Metabolite metabolite = new Metabolite();
      if (a.getClazz().equals("consumption") && glyph.getId().equals(arc2GlyphMap.get(a.getId()))) {
        logger.info(format(messages.getString("ConsumptionArcNegativeCoeff"), a.getId()));
        metabolite.setCoefficient(-1.0);
        metabolite.setId(glyphId2LabelMap.get(getGlyphIdFromPortId(getIdFromSourceOrTarget(a
            .getSource()))));
      }

      if (a.getClazz().equals("production") && glyph.getId().equals(arc2GlyphMap.get(a.getId()))) {
        logger.info(format(messages.getString("ProductionArcNegativeCoeff"), a.getId()));
        metabolite.setCoefficient(1.0);
        metabolite.setId(glyphId2LabelMap.get(getGlyphIdFromPortId(getIdFromSourceOrTarget(a
            .getTarget()))));
      }

      reaction.addMetabolite(metabolite);
    });

    // Encode catalysis arcs as "gene_reaction_rule" for a reaction.
    document.getMap().getArc().stream().filter(a -> a.getClazz().equals("catalysis") &&
        (glyph.getId().equals(port2GlyphMap.get(getIdFromSourceOrTarget(a.getSource()))) ||
            glyph.getId().equals(port2GlyphMap.get(getIdFromSourceOrTarget(a.getTarget())))))
            .collect(Collectors.toList()).forEach((a) -> {

      reaction.setGeneReactionRule(a.getId());
    });

    logger.fine(format(messages.getString("ReactionSegmentAddFinish"), reaction.getId()));

    // If the the set of sources is mutually exclusive from the set of targets, then the reaction
    // is not reversible.
    sources.retainAll(targets);
    reaction.setReversibility(sources.size() > 0);
    
    return reaction;
  }


  /**
   *
   *
   * @param object
   * @return
   */
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


  /**
   * Create a {@link TextLabel} from an SBGN-ML {@link Glyph}.
   *
   * @param glyph The {@code glyph}.
   * @return The created {@code text label}.
   */
  public TextLabel createTextLabel(Glyph glyph) {
    TextLabel textLabel = new TextLabel();

    textLabel.setId((glyph.getId().hashCode() & 0xfffffff) + "");
    textLabel.setX(glyph.getBbox().getX() + glyph.getBbox().getW() * 0.5);
    textLabel.setY(glyph.getBbox().getY() + glyph.getBbox().getH() * 0.5);
    textLabel.setText(glyph.getLabel().getText());

    return textLabel;
  }


  /**
   * Create a list of {@link Segment}s from an SBGN-ML {@link Arc}.
   *
   * @param arc The {@code arc}.
   * @return The list of {@code segments}.
   */
  public List<Segment> createSegments(Arc arc) {
    List<Segment> segments = new ArrayList<>();

    logger.info(format(messages.getString("ArcSegmentCount"), arc.getId(), arc.getNext().size()));
    Segment segment = new Segment();

    segment.setId(arc.getId() + ".S" + 0);
    segment.setFromNodeId(getGlyphIdFromPortId(getIdFromSourceOrTarget(arc.getSource())));

    // A segment is created for every next element in an arc.
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


  /**
   * Extract id from either a {@link Glyph} or a {@link Port}. Since the source/target of an
   * {@link Arc} is a plain {@link Object} and it needs to be cast into a {@link Glyph} or
   * {@link Port}, this methods checks its type and returns the id.
   *
   * @param sOrT The source/target object.
   * @return The id of the source/target.
   */
  public String getIdFromSourceOrTarget(Object sOrT) {
    if (sOrT.getClass().isAssignableFrom(Glyph.class)) {
      // If cast-able to Glyph.
      return ((Glyph)sOrT).getId();
    }
    else if (sOrT.getClass().isAssignableFrom(Port.class)) {
      // If cast-able to Port.
      return ((Port)sOrT).getId();
    }
    return null;
  }


  /**
   * Get {@link Glyph} id from a {@link Port} or {@link Glyph} id.
   *
   * @param id The {@code port} or {@code glyph} id.
   * @return The {@code glyph} id.
   */
  public String getGlyphIdFromPortId(String id) {
    if (glyphIds.contains(id)) {
      // If a glyph id.
      return id;
    }
    else {
      // If a port id.
      return port2GlyphMap.get(id);
    }
  }


  /**
   * Maps SBGN-ML classes to Escher components.
   *
   * @param clazz The SBGN element's {@code class}.
   * @return
   */
  private String determineComponent(String clazz) {
    switch (clazz) {

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
        return clazz;

      }
  }


  /**
   * Converts {@link Sbgn} document to {@link EscherMap} by iteratively creating nodes,
   * reactions, etc.
   *
   * @param document The {@code SBGN} document to convert.
   * @return The converted {@code escher map}.
   */
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
        logger.info(format(messages.getString("GlyphNode"), g.getId(), g.getClazz()));
        escherMap.addNode(createNode(g));
        break;

      case "reaction":
        logger.info(format(messages.getString("GlyphReaction"), g.getId(), g.getClazz()));
        escherMap.addNode(createMidMarker(g));
        escherMap.addReaction(createReaction(g));
        break;

      case "text_label":
        logger.info(format(messages.getString("GlyphTextLabel"), g.getId(), g.getClazz()));
        escherMap.addTextLabel(createTextLabel(g));
        break;

      default:
        logger.warning(format(messages.getString("GlyphUnsupportedClass"), g.getId(), g.getClazz()));
        break;

      }
    });

    map.getArc().stream().filter(a -> a.getClazz().equals("production") || a.getClazz().equals("consumption"))
       .collect(Collectors.toList()).forEach(a -> {

      logger.info(format(messages.getString("ArcMultiMarkerCount"), a.getId(), a.getNext().size()));
      a.getNext().forEach(next -> {
        escherMap.addNode(createMultiMarker(next));
      });
    });

    return escherMap;
  }


  /**
   * Processes the input {@link Sbgn} document and populates internal helper fields.
   */
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
      port2GlyphMap.put(g.getId(), g.getId());

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

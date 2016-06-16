package edu.ucsd.sbrg.escher.converters;

import edu.ucsd.sbrg.escher.model.EscherMap;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Sbgn;

import java.util.logging.Logger;

/**
 * Created by deveshkhandelwal on 13/06/16.
 */
public class SBGN2Escher extends Standard2Escher<Sbgn> {

  /**
   * A {@link java.util.logging.Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(Standard2Escher.class.getName());

  private String determineComponent(String classs) {
    // TODO: Determine class according to the SBGN PD Level 1 spec draft.
    switch (classs) {

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

  @Override
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

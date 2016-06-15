package edu.ucsd.sbrg.escher.converters;

import edu.ucsd.sbrg.escher.model.EscherMap;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Sbgn;

/**
 * Created by deveshkhandelwal on 13/06/16.
 */
public class SBGN2Escher extends Standard2Escher<Sbgn> {

  private String determineComponent(String classs) {
    // TODO: Determine class according to the SBGN PD Level 1 spec draft.
    throw new UnsupportedOperationException("Gotta wait, buddy!");
  }

  @Override
  public EscherMap convert(Sbgn document) {
    this.document = document;
    Map map = document.getMap();

    // For every glyph, determine its class and call the appropriate method accordingly.
    map.getGlyph().forEach((g) -> {
      String component = determineComponent(g.getClazz());
      switch (component) {

      case "node":
        // TODO: Call createNode and add to EscherMap properly.
        break;

      case "reaction":
        // TODO: Call createReaction and add to EscherMap properly.
        break;

      case "segment":
        // TODO: Call createSegment and add to EscherMap properly.
        break;

      case "text_label":
        // TODO: Call createTextLabel and add to EscherMap properly.
        break;

      case "metabolite":
        // TODO: Determine which metabolites to be added to which reaction.
        break;

      default:
        // TODO: Log a message saying unsupported class.
        break;

      }
    });
    throw new UnsupportedOperationException("Not completed yet!");
  }
}

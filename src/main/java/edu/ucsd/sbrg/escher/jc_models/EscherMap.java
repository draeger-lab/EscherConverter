package edu.ucsd.sbrg.escher.jc_models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.ucsd.sbrg.escher.models.AbstractEscherBase;
import edu.ucsd.sbrg.escher.jc_models.TextLabel;
import edu.ucsd.sbrg.escher.models.Canvas;
import edu.ucsd.sbrg.escher.models.EscherReaction;
import edu.ucsd.sbrg.escher.models.Node;

import java.util.LinkedHashMap;

/**
 * Created by Devesh Khandelwal on 06-06-2016.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EscherMap extends AbstractEscherBase{

  @JsonProperty("schema")
  public String schema;

  @JsonProperty("homepage")
  public String homepage;

  @JsonProperty("map_id")
  public String mapId;

  @JsonProperty("map_name")
  public String mapName;

  @JsonProperty("map_description")
  public String mapDescription;

  @JsonProperty("text_labels")
  public LinkedHashMap<String, TextLabel> textLabels;

  @JsonProperty("canvas")
  public Canvas canvas;

  @JsonProperty("nodes")
  public LinkedHashMap<String, Node> nodes;

  @JsonProperty("reactions")
  public LinkedHashMap<String, Reaction> reactions;

  @Override
  public AbstractEscherBase clone() {
    return new EscherMap();
  }
}

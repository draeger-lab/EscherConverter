package edu.ucsd.sbrg.escher.jc_models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by deveshkhandelwal on 07/06/16.
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Segment {

  @JsonProperty("from_node_id")
  public String fromNodeId;

  @JsonProperty("to_node_id")
  public String toNodeId;

  @JsonProperty("b1")
  public Point b1;

  @JsonProperty("b2")
  public Point b2;
}

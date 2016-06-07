package edu.ucsd.sbrg.escher.jc_models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by deveshkhandelwal on 07/06/16.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Node {

  @JsonProperty("node_type")
  public String nodeType;

  @JsonProperty("x")
  public double x;

  @JsonProperty("y")
  public double y;

  @JsonProperty("bigg_id")
  public String biggId;

  @JsonProperty("name")
  public String name;

  @JsonProperty("label_x")
  public double labelX;

  @JsonProperty("label_y")
  public double labelY;

  @JsonProperty("node_is_primary")
  public boolean nodeIsPrimary;

}

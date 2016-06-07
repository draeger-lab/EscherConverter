package edu.ucsd.sbrg.escher.jc_models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by deveshkhandelwal on 07/06/16.
 */
public class Canvas {

  @JsonProperty("x")
  public double x;

  @JsonProperty("y")
  public double y;

  @JsonProperty("width")
  public double width;

  @JsonProperty("height")
  public double height;
}

package edu.ucsd.sbrg.escher.jc_models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by deveshkhandelwal on 07/06/16.
 */
public class Point {

  @JsonProperty("x")
  public double x;

  @JsonProperty("y")
  public double y;
}

package edu.ucsd.sbrg.escher.jc_models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Devesh Khandelwal on 07-06-2016.
 */
public class TextLabel {

  @JsonProperty("x")
  public double x;

  @JsonProperty("y")
  public double y;

  @JsonProperty("text")
  public String text;
}

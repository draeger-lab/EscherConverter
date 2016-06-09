package edu.ucsd.sbrg.escher.jc_models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by deveshkhandelwal on 07/06/16.
 */
public class Gene {

  @JsonProperty("bigg_id")
  public String biggId;

  @JsonProperty("name")
  public String name;
}

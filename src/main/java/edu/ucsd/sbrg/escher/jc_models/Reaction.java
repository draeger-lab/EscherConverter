package edu.ucsd.sbrg.escher.jc_models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.ucsd.sbrg.escher.models.Gene;
import edu.ucsd.sbrg.escher.models.Metabolite;
import edu.ucsd.sbrg.escher.models.Segment;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Devesh Khandelwal on 07-06-2016.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Reaction {

  @JsonProperty("name")
  public String name;

  @JsonProperty("bigg_id")
  public String biggId;

  @JsonProperty("reversibility")
  public boolean reversibility;

  @JsonProperty("label_x")
  public double labelX;

  @JsonProperty("label_y")
  public double labelY;

  @JsonProperty("gene_reaction_rule")
  public String geneReactionRule;

  @JsonProperty("genes")
  public List<Gene> genes;

  @JsonProperty("metabolites")
  public List<Metabolite> metabolites;

  @JsonProperty("segments")
  public LinkedHashMap<String, Segment> segments;
}

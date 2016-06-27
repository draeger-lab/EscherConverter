package edu.ucsd.sbrg.escher.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import edu.ucsd.sbrg.escher.model.EscherMap;
import org.sbgn.bindings.Sbgn;
import org.sbgn.schematron.SchematronValidator;
import org.sbml.jsbml.SBMLDocument;

/**
 * Created by deveshkhandelwal on 27/06/16.
 */
public class Validation {

  private JsonSchema escherSchema;
  private SchematronValidator schematronValidator;

  public Validation() {

  }


  public Validation(JsonSchema jsonSchema) {
    // TODO: Check if schema is valid.
    escherSchema = jsonSchema;
  }


  public Validation(JsonNode jsonNode) {
    // TODO: Parse JSON into schema.
  }


  public void validateEscher(EscherMap map) {
    throw new UnsupportedOperationException("Not yet Implemented!");
  }


  public void validateSbgnml(Sbgn document) {
    throw new UnsupportedOperationException("Not yet implemented!");
  }


  public void validateSbmlLE(SBMLDocument document) {
    throw new UnsupportedOperationException("Not yet implemented!");
  }
}

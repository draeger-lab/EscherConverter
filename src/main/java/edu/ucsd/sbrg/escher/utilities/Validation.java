package edu.ucsd.sbrg.escher.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import de.zbit.util.ResourceManager;
import edu.ucsd.sbrg.escher.EscherConverter;
import edu.ucsd.sbrg.escher.model.EscherMap;
import org.sbgn.bindings.Sbgn;
import org.sbgn.schematron.SchematronValidator;
import org.sbml.jsbml.SBMLDocument;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Created by deveshkhandelwal on 27/06/16.
 */
public class Validation {

  /**
   * Localization support.
   */
  private static final transient ResourceBundle
      bundle           =
      ResourceManager.getBundle("Strings");
  /**
   * A {@link Logger} for this class.
   */
  private static final           Logger
      logger           =
      Logger.getLogger(Validation.class.getName());

  private static final           JsonValidator JSON_VALIDATOR = JsonSchemaFactory.byDefault().getValidator();
  private JsonSchema          escherSchema;
  private SchematronValidator schematronValidator;

  public Validation() throws IOException, ProcessingException {
    this(Utils.defaultEscherSchema());
  }


  public Validation(JsonNode jsonNode) throws IOException, ProcessingException {
    // TODO: Check if schema is valid.
    ProcessingReport report = Utils.jsonSchemaSchema().validate(jsonNode);

    if (report.isSuccess()) {
      // TODO: Log that schema is valid.
      escherSchema = JsonSchemaFactory.byDefault().getJsonSchema(jsonNode);
    }
    else {
      // TODO: Log about invalid schema file.
      throw new IllegalArgumentException("Invalid JSON Schema file!");
    }
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

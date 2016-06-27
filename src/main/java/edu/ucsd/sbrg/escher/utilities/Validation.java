package edu.ucsd.sbrg.escher.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import de.zbit.util.ResourceManager;
import edu.ucsd.sbrg.escher.EscherConverter;
import edu.ucsd.sbrg.escher.model.EscherMap;
import org.sbgn.bindings.Sbgn;
import org.sbgn.schematron.Issue;
import org.sbgn.schematron.SchematronValidator;
import org.sbml.jsbml.SBMLDocument;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.List;
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
  private File logFile;

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


  public boolean validateEscher(File file) throws IOException {
    JsonNode node = JsonLoader.fromFile(file);
    try {
      ProcessingReport report = escherSchema.validate(node);
      return report.isSuccess();
    } catch (ProcessingException e) {
      // TODO: Log.
      e.printStackTrace();
    }
    return false;
  }


  public boolean validateSbgnml(File file) throws IOException {
    try {
      SchematronValidator.setSvrlDump(true);
      List<Issue> issues = SchematronValidator.validate(file);

      if (issues == null || issues.isEmpty()) {
        return true;
      }
    } catch (TransformerException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }

    return false;
  }


  public boolean validateSbmlLE(File file) {
    throw new UnsupportedOperationException("Not yet implemented!");
  }


  public void log(Issue issue) {
    throw new UnsupportedOperationException("Not yet implemented!");
  }


  public void log(ProcessingMessage message) {
    throw new UnsupportedOperationException("Not yet implemented!");
  }

  public void setLogFile(File logFile) {
    this.logFile = logFile;
  }
}

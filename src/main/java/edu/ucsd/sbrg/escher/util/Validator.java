package edu.ucsd.sbrg.escher.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import de.zbit.util.ResourceManager;
import org.jdom.JDOMException;
import org.sbgn.ConvertMilestone1to2;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Sbgn;
import org.sbgn.schematron.Issue;
import org.sbgn.schematron.SchematronValidator;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Validator for input files.
 *
 * @author Devesh Khandelwal
 * Created by deveshkhandelwal on 27/06/16.
 */
public class Validator {

  /**
   * Default values.
   */
  private static final transient ResourceBundle bundle           = ResourceManager.getBundle("Strings");
  /**
   * Localization support.
   */
  private static final transient ResourceBundle messages           = ResourceManager.getBundle("edu.ucsd.sbrg.escher.Messages");
  /**
   * A {@link Logger} for this class.
   */
  private static final           Logger logger           = Logger.getLogger(Validator.class.getName());
  /**
   * JSON Validator to validate Escher JSON files.
   */
  private static final           JsonValidator JSON_VALIDATOR = JsonSchemaFactory.byDefault().getValidator();
  /**
   * JSON Schema to validate against.
   */
  private JsonSchema          escherSchema;
  private SchematronValidator schematronValidator;


  /**
   * Default constructor.
   *
   * @throws IOException See {@link Utils#defaultEscherSchema()}.
   * @throws ProcessingException See {@link Utils#defaultEscherSchema()}.
   */
  public Validator() throws IOException, ProcessingException {
    this(Utils.defaultEscherSchema());
  }


  /**
   * Constructor. Takes a custom schema to validate Escher against.
   *
   * @param jsonNode The {@code JSON Schema}.
   * @throws IOException See {@link Utils#jsonSchemaSchema()}.
   * @throws ProcessingException See {@link Utils#jsonSchemaSchema()}.
   */
  public Validator(JsonNode jsonNode) throws IOException, ProcessingException {
    // TODO: Check if schema is valid.
    ProcessingReport report = Utils.jsonSchemaSchema().validate(jsonNode);

    if (report.isSuccess()) {
      logger.fine(messages.getString("JSONSchemaValid"));
      escherSchema = JsonSchemaFactory.byDefault().getJsonSchema(jsonNode);
    }
    else {
      logger.fine(messages.getString("JSONSchemaInvalid"));
      throw new IllegalArgumentException("Invalid JSON Schema file!");
    }
  }


  /**
   * Validates an Escher JSON file.
   *
   * @param file The input {@code file}.
   * @return True if valid, false otherwise.
   * @throws IOException Thrown if problems in accessing {@code file}.
   */
  public boolean validateEscher(File file) throws IOException {
    JsonNode node = JsonLoader.fromFile(file);
    try {
      ProcessingReport report = escherSchema.validate(node);
      return report.isSuccess();
    } catch (ProcessingException e) {
      logger.warning(messages.getString("EscherValidationFail"));
    }
    return false;
  }


  /**
   * Validates an SBGN-ML XML file.
   *
   * @param file The input {@code file}.
   * @return True if valid, false otherwise.
   * @throws IOException Thrown if problems in accessing {@code file}.
   */
  public boolean validateSbgnml(File file) throws IOException {
    try {
      Sbgn document = SbgnUtil.readFromFile(file);

      if (document.getMap().getLanguage() == null || !document.getMap().getLanguage().equals
          ("process description")) {
        logger.warning(messages.getString("SBGNLanguageUnspecified"));
        return false;
      }
    } catch (UnmarshalException e) {
      // TODO: Check if this exception is caused due to milestone mismatch and return false if the file is not a valid SBGN file at all.
      try {
        // If parsing fails, try converting from milestone 1 to 2 first.
        logger.warning(messages.getString("ConvertM1toM2"));
        ConvertMilestone1to2.main(new String[] {file.getAbsolutePath(), file.getAbsolutePath()});
      } catch (JDOMException e1) {
        logger.severe(messages.getString("ConvertM1toM2Fail"));
        e1.printStackTrace();
      }
    } catch (JAXBException e) {
      logger.warning(messages.getString("SBGNReadFail"));
      e.printStackTrace();
      return false;
    }
    try {
      SchematronValidator.setSvrlDump(true);
      List<Issue> issues = SchematronValidator.validate(file);

      if (issues == null || issues.isEmpty()) {
        return true;
      }
    } catch (TransformerException | SAXException | ParserConfigurationException e) {
      logger.warning(messages.getString("SBGNValidationFail"));
      e.printStackTrace();
    }
    return false;
  }


  /**
   * Validates an SBGN-ML XML file.
   *
   * @param file The input {@code file}.
   * @return True if valid, false otherwise.
   */
  public boolean validateSbmlLE(File file) {
    // TODO: SBML offline/online validation.
    return true;
  }

}

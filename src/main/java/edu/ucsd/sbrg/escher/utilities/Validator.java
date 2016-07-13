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
 * Created by deveshkhandelwal on 27/06/16.
 */
public class Validator {

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
      Logger.getLogger(Validator.class.getName());

  private static final           JsonValidator JSON_VALIDATOR = JsonSchemaFactory.byDefault().getValidator();
  private JsonSchema          escherSchema;
  private SchematronValidator schematronValidator;
  private File logFile;

  public Validator() throws IOException, ProcessingException {
    this(Utils.defaultEscherSchema());
  }


  public Validator(JsonNode jsonNode) throws IOException, ProcessingException {
    // TODO: Check if schema is valid.
    ProcessingReport report = Utils.jsonSchemaSchema().validate(jsonNode);

    if (report.isSuccess()) {
      logger.fine(bundle.getString("JSONSchemaValid"));
      escherSchema = JsonSchemaFactory.byDefault().getJsonSchema(jsonNode);
    }
    else {
      logger.fine(bundle.getString("JSONSchemaInvalid"));
      throw new IllegalArgumentException("Invalid JSON Schema file!");
    }
  }


  public boolean validateEscher(File file) throws IOException {
    JsonNode node = JsonLoader.fromFile(file);
    try {
      ProcessingReport report = escherSchema.validate(node);
      return report.isSuccess();
    } catch (ProcessingException e) {
      logger.warning(bundle.getString("EscherValidationFail"));
    }
    return false;
  }


  public boolean validateSbgnml(File file) throws IOException {
    try {
      Sbgn document = SbgnUtil.readFromFile(file);

      if (document.getMap().getLanguage() == null || !document.getMap().getLanguage().equals
          ("process description")) {
        logger.warning(bundle.getString("SBGNLanguageUnspecified"));
        return false;
      }
    } catch (UnmarshalException e) {
      try {
        logger.warning(bundle.getString("ConvertM1toM2"));
        ConvertMilestone1to2.main(new String[] {file.getAbsolutePath(), file.getAbsolutePath()});
      } catch (JDOMException e1) {
        logger.severe(bundle.getString("ConvertM1toM2Fail"));
        e1.printStackTrace();
      }
    } catch (JAXBException e) {
      logger.warning(bundle.getString("SBGNReadFail"));
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
      logger.warning(bundle.getString("SBGNValidationFail"));
      e.printStackTrace();
    }
    return false;
  }


  public boolean validateSbmlLE(File file) {
    // TODO: SBML oofline/online validation.
    return true;
//    throw new UnsupportedOperationException("Not yet implemented!");
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

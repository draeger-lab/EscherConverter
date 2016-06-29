package edu.ucsd.sbrg.escher;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import edu.ucsd.sbrg.escher.utilities.Validation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Devesh Khandelwal on 27-06-2016.
 */
public class ValidationTest {

  public Validation validation;
  public File file;

  @Before
  public void setUp() {
    validation = null;
  }


  @Ignore("Any schema is valid as long as it has an 'id' field. So, this test is pointless right "
      + "now.")
  @Test(expected = IllegalArgumentException.class)
  public void failsOnInvalidJsonSchemaFile() throws IOException, ProcessingException {
    JsonNode jsonFile = JsonLoader.fromPath("data/e_coli_core.escher.json");

    validation = new Validation(jsonFile);
  }


  @Test
  public void canValidateJsonSchemaFile() throws IOException, ProcessingException {
    JsonNode jsonFile = JsonLoader.fromPath("data/cobra_json_schema.json");

    validation = new Validation(jsonFile);
  }


  @Test
  public void failsOnInvalidJsonEscherFile() throws IOException, ProcessingException {
    validation = new Validation();

    file = new File("data/TestMap_vcard.escher.json");

    assertFalse("failure - validation passes on invalid JSON", validation.validateEscher(file));
  }


  @Test
  public void validatesEscherJson() throws IOException, ProcessingException {
    validation = new Validation();
    file = new File("data/e_coli_core_metabolism.escher.json");

    assertTrue("failure - validation failing on valid JSON", validation.validateEscher(file));
  }


  @Ignore
  @Test
  public void validatesSbgnDocument() throws IOException, ProcessingException {
    validation = new Validation();
    file = new File("data/central_plant_metabolism.sbgn.xml");

    assertTrue("failure - validation failing on valid SBGN", validation.validateSbgnml(file));
  }


  @Test(expected = IllegalArgumentException.class)
  public void failsOnInvalidSbgnDocument() throws IOException, ProcessingException {
    validation = new Validation();
    file = new File("data/mapk_cascade.sbgn.xml");

    assertFalse("failure - validation passing on invalid SBGN", validation.validateSbgnml(file));
  }


  @Test(expected = IllegalArgumentException.class)
  public void failsWhenLanguageNotPresentOnSbgn() throws IOException, ProcessingException {
    validation = new Validation();
    file = new File("data/mapk_cascade.sbgn.xml");

    assertFalse("failure - validation passing w/o lang on SBGN", validation.validateSbgnml(file));
  }


  @Ignore
  @Test
  public void validatesSbmlDocument() {

  }


  @Ignore
  @Test
  public void failsOnInvalidSbmlDocument() {

  }

}

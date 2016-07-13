package edu.ucsd.sbrg.escher;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import edu.ucsd.sbrg.escher.utilities.Validator;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Devesh Khandelwal on 27-06-2016.
 */
@RunWith(JUnitParamsRunner.class)
public class ValidationTest {

  public Validator validator;
  public File      file;

  @Before
  public void setUp() {
    validator = null;
  }


  @Ignore("Any schema is valid as long as it has an 'id' field. So, this test is pointless right "
      + "now.")
  @Test(expected = IllegalArgumentException.class)
  public void failsOnInvalidJsonSchemaFile() throws IOException, ProcessingException {
    JsonNode jsonFile = JsonLoader.fromPath("data/e_coli_core.escher.json");

    validator = new Validator(jsonFile);
  }


  @Test
  public void canValidateJsonSchemaFile() throws IOException, ProcessingException {
    JsonNode jsonFile = JsonLoader.fromPath("data/cobra_json_schema.json");

    validator = new Validator(jsonFile);
  }


  @Test
  @Parameters({
      "data/mapk_cascade.escher.json|false",
      "data/e_coli_core_metabolism.escher.json|true",
      "data/TestMap_vcard.escher.json|false"
  })
  public void escherValidationTest(String filePath, boolean isValid) throws IOException,
      ProcessingException {
    validator = new Validator();
    file = new File(filePath);

    assertEquals("failure - validator failing on valid JSON", isValid, validator.validateEscher
        (file));
  }


  // TODO: Add more SBGN files to test validator.
  @Test
  @Parameters({
      "data/mapk_cascade.sbgn.xml|true",
      "data/central_plant_metabolism.sbgn.xml|true"
  })
  public void sbgnValidationTest(String filePath, boolean isValid) throws IOException,
      ProcessingException {
    validator = new Validator();
    file = new File(filePath);

    assertEquals("failure - validator failing on valid SBGN", isValid, validator.validateSbgnml
        (file));
  }


  @Test(expected = IllegalArgumentException.class)
  public void failsOnInvalidSbgnDocument() throws IOException, ProcessingException {
    validator = new Validator();
    file = new File("data/mapk_cascade.sbgn.xml");

    assertFalse("failure - validator passing on invalid SBGN", validator.validateSbgnml(file));
  }


  @Test(expected = IllegalArgumentException.class)
  public void failsWhenLanguageNotPresentOnSbgn() throws IOException, ProcessingException {
    validator = new Validator();
    file = new File("data/mapk_cascade.sbgn.xml");

    assertFalse("failure - validator passing w/o lang on SBGN", validator.validateSbgnml(file));
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

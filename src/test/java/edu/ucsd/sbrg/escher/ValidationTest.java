/* ---------------------------------------------------------------------
 * This file is part of the program EscherConverter.
 *
 * Copyright (C) 2013-2017 by the University of California, San Diego.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package edu.ucsd.sbrg.escher;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import edu.ucsd.sbrg.escher.util.Validator;
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
    "data/central_plant_metabolism.sbgn.xml|false"
  })
  public void sbgnValidationTest(String filePath, boolean isValid) throws IOException,
  ProcessingException {
    validator = new Validator();
    file = new File(filePath);

    assertEquals("failure - validator failing on valid SBGN", isValid, validator.validateSbgnml
      (file));
  }


  @Ignore("See Validator.java#136.")
  @Test
  public void failsOnInvalidSbgnDocument() throws IOException, ProcessingException {
    validator = new Validator();
    file = new File("data/e_coli_core.sbml.xml");

    assertFalse("failure - validator passing on invalid SBGN", validator.validateSbgnml(file));
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

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

import edu.ucsd.sbrg.escher.converter.SBML2Escher;
import edu.ucsd.sbrg.escher.model.EscherMap;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by deveshkhandelwal on 20/06/16.
 */
public class SBML2EscherTest {

  private static File            file;
  private static SBMLDocument    sbml;
  private static SBML2Escher     converter;
  private static List<EscherMap> maps;

  @BeforeClass
  public static void createTheWorld() throws JAXBException, IOException, XMLStreamException {
    file = new File("data/example2.sbml.xml");
    sbml = SBMLReader.read(file);
    converter = new SBML2Escher();
    maps = converter.convert(sbml);
  }

  @Test(expected = IOException.class)
  @Ignore
  public void failsOnNonExistentFileTest() throws IOException {
    // TODO

    File file = new File("data/file_which_does_not_exists");
  }

  @Test
  @Ignore
  public void failsOnInvalidSbgnFile() throws IOException, JAXBException {

    // This just checks that a ParseException is thrown on invalid file.
    // TODO
    File file = new File("data/e_coli_core_metabolism.escher.json");

  }
}

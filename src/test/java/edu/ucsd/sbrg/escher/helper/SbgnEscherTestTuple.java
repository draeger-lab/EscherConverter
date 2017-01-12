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
package edu.ucsd.sbrg.escher.helper;

import static org.powermock.api.mockito.PowerMockito.spy;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Sbgn;

import edu.ucsd.sbrg.escher.converter.SBGN2Escher;
import edu.ucsd.sbrg.escher.model.EscherMap;

/**
 * Created by deveshkhandelwal on 01/07/16.
 */
public class SbgnEscherTestTuple {

  public File      file;
  public Sbgn      sbgn;
  public EscherMap map;
  public SBGN2Escher converter = spy(new SBGN2Escher());
  public int       numberOfMetabolites;
  public int       numberOfReactions;


  public SbgnEscherTestTuple(File file) throws JAXBException {
    this.file = file;
    sbgn = SbgnUtil.readFromFile(file);
    map = converter.convert(sbgn);
    numberOfMetabolites = (int) sbgn.getMap()
        .getGlyph()
        .stream()
        .filter(n -> n.getClazz()
          .matches("simple "
              + "chemical|macromolecule|perturbing "
              + "agent|process|omitted process|uncertain "
              + "process|association|dissociation"))
        .count();
    numberOfReactions = (int) sbgn.getMap().getGlyph().stream().filter(n -> n.getClazz().contains
      ("process")).count();
  }
}

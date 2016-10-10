/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the program BioNetView.
 *
 * Copyright (C) 2013-2016 by the University of California, San Diego.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package edu.ucsd.sbrg.escher.gui;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.ProgressMonitorInputStream;
import javax.swing.SwingWorker;

import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.util.ResourceManager;

import de.zbit.io.OpenedFile;
import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.prefs.SBProperties;
import edu.ucsd.sbrg.escher.EscherConverter;
import edu.ucsd.sbrg.escher.model.EscherMap;
import edu.ucsd.sbrg.escher.util.EscherOptions;

/**
 * @author Andreas Dr&auml;ger
 */
public class EscherParserWorker
extends SwingWorker<List<OpenedFile<EscherMap>>, OpenedFile<EscherMap>> {

  /**
   *
   */
  public static final String INTERMERIM_RESULTS = "INTERMERIM_RESULTS";
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(EscherParserWorker.class.getName());
  /**
   * Localization support.
   */
  public static final transient ResourceBundle bundle = ResourceManager.getBundle("edu.ucsd.sbrg.escher.Messages");
  private File      input[];
  private Component parentComponent;


  /**
   * @param input
   */
  public EscherParserWorker(Component parentComponent, File... input) {
    super();
    this.parentComponent = parentComponent;
    this.input = input;
  }


  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  protected List<OpenedFile<EscherMap>> doInBackground() throws Exception {
    List<OpenedFile<EscherMap>> listOfFiles = new ArrayList<OpenedFile<EscherMap>>();

    for (File inputFile : input) {
      logger.info(MessageFormat.format(
        bundle.getString("EscherConverter.readingFile"), inputFile));

      ProgressMonitorInputStream is = new ProgressMonitorInputStream(parentComponent,
        MessageFormat.format(
          bundle.getString("EscherParserWorker.progressMessage"),
          inputFile.getName()), new FileInputStream(inputFile));

      if (SBFileFilter.isJSONFile(inputFile)) {
        EscherMap map = EscherConverter.parseEscherJson(is);
        appendOpenedFile(listOfFiles, inputFile, map);

      } else {
        SBPreferences prefs = SBPreferences.getPreferencesFor(EscherOptions.class);
        SBProperties props = prefs.toProperties();

        if (SBFileFilter.isSBGNFile(inputFile)) {
          appendOpenedFile(listOfFiles, inputFile, EscherConverter.parseSBGNML(is, props));
        } else if (SBFileFilter.isSBMLFile(inputFile)) {
          List<EscherMap> listOfMaps = EscherConverter.convert(
            SBMLReader.read(is), props);
          for (EscherMap escherMap : listOfMaps) {
            appendOpenedFile(listOfFiles, inputFile, escherMap);
          }
        }
      }
    }

    return listOfFiles;
  }


  /**
   * @param listOfFiles
   * @param inputFile
   * @param map
   */
  private void appendOpenedFile(List<OpenedFile<EscherMap>> listOfFiles,
    File inputFile, EscherMap map) {
    OpenedFile<EscherMap> openedFile = new OpenedFile<EscherMap>(inputFile, map);
    logger.info(MessageFormat.format(
      bundle.getString("EscherConverter.readingDone"), inputFile));
    publish(openedFile);
    listOfFiles.add(openedFile);
  }

  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#process(java.util.List)
   */
  @Override
  protected void process(List<OpenedFile<EscherMap>> chunks) {
    firePropertyChange(INTERMERIM_RESULTS, null, chunks);
  }

}

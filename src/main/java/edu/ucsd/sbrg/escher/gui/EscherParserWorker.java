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
package edu.ucsd.sbrg.escher.gui;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import static java.text.MessageFormat.format;
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
 * Handles by which parser a given file should be parsed depending on the format and calls convert function on it.
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
  
  /**
   * (List of) input files
   */
  private File      input[];
  
  /**
   * The component which called this class.
   */
  private Component parentComponent;


  /** Constructor
   * @param parentComponent The component which called this class.
   * @param input The file which is to be parsed
   */
  public EscherParserWorker(Component parentComponent, File... input) {
    super();
    this.parentComponent = parentComponent;
    this.input = input;
  }


  /* (non-Javadoc)
   * Selects how the input files are to be parsed depending on their format. Then calls function to convert them.
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  protected List<OpenedFile<EscherMap>> doInBackground() throws Exception {
    List<OpenedFile<EscherMap>> listOfFiles = new ArrayList<OpenedFile<EscherMap>>();

    for (File inputFile : input) {
      logger.info(format(bundle.getString("EscherConverter.readingFile"), inputFile));

      ProgressMonitorInputStream is = new ProgressMonitorInputStream(parentComponent,
        format(bundle.getString("EscherParserWorker.progressMessage"),
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
   * Adds an input file to the list of already opened files and gives status information
   * @param listOfFiles List of already opened files
   * @param inputFile The just opened file
   * @param map EscherMap of the input file
   */
  private void appendOpenedFile(List<OpenedFile<EscherMap>> listOfFiles,
    File inputFile, EscherMap map) {
    OpenedFile<EscherMap> openedFile = new OpenedFile<EscherMap>(inputFile, map);
    logger.info(format(bundle.getString("EscherConverter.readingDone"), inputFile));
    publish(openedFile);
    listOfFiles.add(openedFile);
  }

  /* (non-Javadoc)
   * Shows information about current status of opening files to user
   * @see javax.swing.SwingWorker#process(java.util.List)
   */
  @Override
  protected void process(List<OpenedFile<EscherMap>> chunks) {
    firePropertyChange(INTERMERIM_RESULTS, null, chunks);
  }

}

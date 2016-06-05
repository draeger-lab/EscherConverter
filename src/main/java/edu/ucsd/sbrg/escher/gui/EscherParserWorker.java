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

import de.zbit.io.OpenedFile;
import edu.ucsd.sbrg.escher.models.EscherMap;
import edu.ucsd.sbrg.escher.utilities.EscherParser;
import org.sbml.jsbml.util.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * @author Andreas Dr&auml;ger
 */
public class EscherParserWorker
    extends SwingWorker<List<OpenedFile<EscherMap>>, OpenedFile<EscherMap>> {

  /**
   *
   */
  public static final            String
                                                INTERMERIM_RESULTS =
      "INTERMERIM_RESULTS";
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger
                                                logger             =
      Logger.getLogger(EscherParserWorker.class.getName());
  /**
   * Localization support.
   */
  public static final transient  ResourceBundle
                                                bundle             =
      ResourceManager.getBundle("edu.ucsd.sbrg.escher.Messages");
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
    List<OpenedFile<EscherMap>>
        listOfFiles =
        new ArrayList<OpenedFile<EscherMap>>();
    EscherParser parser = new EscherParser();
    for (File jsonFile : input) {
      logger.info(MessageFormat
          .format(bundle.getString("EscherConverter.readingFile"), jsonFile));
      EscherMap
          map =
          parser.parse(new ProgressMonitorInputStream(parentComponent,
                  MessageFormat.format(
                      bundle.getString("EscherParserWorker.progressMessage"),
                      jsonFile.getName()), new FileInputStream(jsonFile)),
              EscherParser.createMapId(jsonFile));
      OpenedFile<EscherMap>
          openedFile =
          new OpenedFile<EscherMap>(jsonFile, map);
      logger.info(MessageFormat
          .format(bundle.getString("EscherConverter.readingDone"), jsonFile));
      publish(openedFile);
      listOfFiles.add(openedFile);
    }
    return listOfFiles;
  }


  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#process(java.util.List)
   */
  @Override
  protected void process(List<OpenedFile<EscherMap>> chunks) {
    firePropertyChange(INTERMERIM_RESULTS, null, chunks);
  }
}

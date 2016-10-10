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
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Sbgn;

import javax.swing.*;
import java.io.File;

/**
 * @author Andreas Dr&auml;ger
 */
public class SBGNWritingTask extends SwingWorker<File, Void> {

  private OpenedFile<Sbgn> openedFile;

  /**
   * @param openedFile
   */
  public SBGNWritingTask(OpenedFile<Sbgn> openedFile) {
    this.openedFile = openedFile;
  }


  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  protected File doInBackground() throws Exception {
    try {
      SbgnUtil.writeToFile(openedFile.getDocument(), openedFile.getFile());
    } catch (Throwable t) {
      t.printStackTrace();
      throw t;
    }
    return openedFile.getFile();
  }

}

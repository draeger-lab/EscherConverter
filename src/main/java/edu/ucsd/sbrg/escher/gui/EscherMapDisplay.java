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

import java.util.concurrent.ExecutionException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.ext.layout.LayoutConstants;
import org.sbml.jsbml.ext.layout.LayoutModelPlugin;

import de.zbit.gui.GUITools;
import de.zbit.gui.layout.LayoutHelper;
import de.zbit.io.OpenedFile;
import de.zbit.util.prefs.SBProperties;
import edu.ucsd.sbrg.escher.model.EscherMap;

/**
 * @author Andreas Dr&auml;ger
 */
public class EscherMapDisplay extends JPanel {

  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -5400175730284648548L;
  /**
   *
   */
  private OpenedFile<EscherMap> openedFile;
  /**
   *
   */
  private SBMLDocument          doc;

  /**
   * @param openedFile
   * @param properties
   */
  public EscherMapDisplay(OpenedFile<EscherMap> openedFile, SBProperties properties) {
    this.openedFile = openedFile;
    EscherConverterWorker<SBMLDocument> converter = new EscherConverterWorker<SBMLDocument>(
        openedFile.getDocument(), SBMLDocument.class, properties);
    converter.addPropertyChangeListener(evt -> {
      if (evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
        try {
          doc = (SBMLDocument) ((EscherConverterWorker<?>) evt.getSource()).get();
          displaySBML();
        } catch (InterruptedException | ExecutionException exc) {
          GUITools.showErrorMessage(this, exc);
        }
      }
    });
    converter.execute();
  }

  /**
   * First attempts to display the content of the first layout in the given SBML
   * document as a graph. If this fails, it displays the whole SBML document as
   * a tree.
   */
  private void displaySBML() {
    removeAll();
    LayoutHelper lh = new LayoutHelper(this);
    try {
      lh.add(new SBMLLayoutViewPanel(((LayoutModelPlugin) doc.getModel().getPlugin(LayoutConstants.layout)).getListOfLayouts().getFirst()));
    } catch (Throwable t) {
      t.printStackTrace();
      lh.add(new JScrollPane(new JTree(doc)));
    }
  }

  /**
   * @return
   */
  public OpenedFile<EscherMap> getOpenedFile() {
    return openedFile;
  }

  /**
   * @return
   */
  public SBMLDocument getSBMLDocument() {
    return doc;
  }

}

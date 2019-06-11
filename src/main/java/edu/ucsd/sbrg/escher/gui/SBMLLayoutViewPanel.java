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

import static java.text.MessageFormat.format;

import java.awt.BorderLayout;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.ext.layout.Layout;

import de.zbit.sbml.layout.y.LayoutDirectionTask;
import de.zbit.util.Utils;

/**
 * The panel the SBML is displayed in.
 * @author Andreas Dr&auml;ger
 */
public class SBMLLayoutViewPanel extends JPanel {

  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -8308286885334833785L;

  /**
   * A {@link Logger} for this class.
   */
  private static final Logger logger = Logger.getLogger(SBMLLayoutViewPanel.class.getName());

  /**
   *
   */
  public SBMLLayoutViewPanel() {
    super(new BorderLayout());
  }


  /**
   * @param layout
   */
  public SBMLLayoutViewPanel(Layout layout) {
    this();
    setSBMLLayout(layout);
  }

  /**
   * Draws a given layout, using the SysBio library
   * @param layout A {@link Layout} from an {@link SBMLDocument}
   */
  public void setSBMLLayout(Layout layout) {
    logger.fine(format("Received layout with id=''{0}''.", layout.getId()));
    LayoutDirectionTask layoutTask = new LayoutDirectionTask(layout, this);
    layoutTask.addPropertyChangeListener(evt -> {
      if (evt.getPropertyName().equals("state")
          && evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
        Model model = layout.getModel();
        if (layout.isSetName()) {
          setName(layout.getName());
        } else if (layout.isSetId()) {
          setName(layout.getId());
        } else if (model.isSetName()) {
          setName(model.getName());
        } else {
          setName(model.getId());
        }
        try {
          add(((LayoutDirectionTask) evt.getSource()).get(), BorderLayout.CENTER);
        } catch (InterruptedException | ExecutionException exc) {
          logger.log(Level.WARNING, Utils.getMessage(exc), exc);
        }
      }
    });
    layoutTask.execute();
  }

}

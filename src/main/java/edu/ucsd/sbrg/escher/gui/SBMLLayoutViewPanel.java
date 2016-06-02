/**
 * 
 */
package edu.ucsd.sbrg.escher.gui;

import java.awt.BorderLayout;
import java.text.MessageFormat;
import java.util.logging.Logger;

import javax.swing.JPanel;

import org.sbml.jsbml.ext.layout.Layout;


/**
 * @author Andreas Dr&auml;ger
 *
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
   * 
   * @param layout
   */
  public SBMLLayoutViewPanel(Layout layout) {
    this();
    logger.fine(MessageFormat.format("Received layout with id=''{0}''.", layout.getId()));
  }

}

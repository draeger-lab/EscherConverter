package edu.ucsd.sbrg.escher.converters;

import edu.ucsd.sbrg.escher.model.EscherMap;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.util.ResourceManager;

import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Created by deveshkhandelwal on 20/06/16.
 */
public class SBML2Escher {

  /**
   * A {@link java.util.logging.Logger} for this class.
   */
  private static final Logger         logger = Logger.getLogger(SBGN2Escher.class.getName());
  /**
   * Localization support.
   */
  public static final  ResourceBundle bundle = ResourceManager.getBundle("Strings");
  protected EscherMap escherMap;
  protected SBMLDocument document;


  public SBML2Escher() {
    escherMap = new EscherMap();
  }


  public EscherMap convert(SBMLDocument document) {
    return null;
  }

}

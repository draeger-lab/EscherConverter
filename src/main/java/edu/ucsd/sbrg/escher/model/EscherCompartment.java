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
package edu.ucsd.sbrg.escher.model;

import org.sbml.jsbml.util.ResourceManager;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * @author Andreas Dr&auml;ger
 */
public class EscherCompartment extends AbstractBox implements Element {

  /**
   * Localization support.
   */
  private static final ResourceBundle bundle = ResourceManager.getBundle("edu.ucsd.sbrg.escher.Messages");
  /**
   * The mapping between compartment codes and human-readable names.
   */
  private static final ResourceBundle compartmentCode = ResourceManager.getBundle("CompartmentCode");
  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger logger = Logger.getLogger(EscherCompartment.class.getName());
  /**
   *
   */
  private String id;


  /**
   *
   */
  public EscherCompartment() {
    id = null;
  }


  /**
   *
   */
  public EscherCompartment(EscherCompartment compartment) {
    this();
    setId(compartment.getId());
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.AbstractEscherBase#clone()
   */
  @Override
  public EscherCompartment clone() {
    return new EscherCompartment(this);
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.Element#getId()
   */
  @Override
  public String getId() {
    return id;
  }


  /**
   * @return
   */
  public String getName() {
    if (isSetId() && compartmentCode.containsKey(getId())) {
      return compartmentCode.getString(getId());
    }
    return null;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.Element#isSetId()
   */
  @Override
  public boolean isSetId() {
    return id != null;
  }


  /**
   * @return
   */
  public boolean isSetName() {
    return getName() != null;
  }


  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.model.Element#setId(java.lang.String)
   */
  @Override
  public void setId(String id) {
    if (!compartmentCode.containsKey(id)) {
      logger.warning(MessageFormat.format(
        bundle.getString("EscherCompartment.invalidId"), id));
    }
    this.id = id;
  }
  
}

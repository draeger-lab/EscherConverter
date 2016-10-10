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

import de.zbit.util.prefs.SBProperties;
import edu.ucsd.sbrg.escher.EscherConverter;
import edu.ucsd.sbrg.escher.model.EscherMap;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.util.ResourceManager;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @param <T> can either be {@link SBMLDocument} or {@link Sbgn}.
 * @author Andreas Dr&auml;ger
 */
public class EscherConverterWorker<T> extends SwingWorker<T, Void> {

  /**
   * Localization support.
   */
  public static final ResourceBundle bundle = ResourceManager.getBundle("edu.ucsd.sbrg.escher.Messages");
  private EscherMap          map;
  private Class<? extends T> format;
  private SBProperties       properties;

  /**
   * @param map
   * @param format
   * @param properties
   */
  public EscherConverterWorker(EscherMap map, Class<? extends T> format,
    SBProperties properties) {
    if (!format.isAssignableFrom(SBMLDocument.class) && !format.isAssignableFrom(Sbgn.class)) {
      throw new IllegalArgumentException(MessageFormat.format(
        bundle.getString("EscherConverterWorker.unknownFormat"), format.getName()));
    }
    this.map = map;
    this.format = format;
    this.properties = properties;
  }


  /* (non-Javadoc)
   * @see javax.swing.SwingWorker#doInBackground()
   */
  @Override
  protected T doInBackground() throws Exception {
    return EscherConverter.convert(map, format, properties);
  }

}

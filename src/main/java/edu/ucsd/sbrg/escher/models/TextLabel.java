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
package edu.ucsd.sbrg.escher.models;

/**
 * @author Andreas Dr&auml;ger
 *
 */
public class TextLabel extends AbstractBox implements Element {

  /**
   * 
   */
  private String id;
  /**
   * 
   */
  private String text;

  /**
   * 
   */
  public TextLabel() {
    id = text = null;
    x = y = null;
  }

  /**
   * 
   * @param textLabel
   */
  public TextLabel(TextLabel textLabel) {
    super(textLabel);
    if (textLabel.isSetId()) {
      setId(textLabel.getId());
    }
    if (textLabel.isSetText()) {
      setText(textLabel.getText());
    }
  }

  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.AbstractEscherBase#clone()
   */
  @Override
  public TextLabel clone() {
    return new TextLabel(this);
  }

  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.Element#getId()
   */
  @Override
  public String getId() {
    return id;
  }

  /**
   * @return the text
   */
  public String getText() {
    return text;
  }

  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.Element#isSetId()
   */
  @Override
  public boolean isSetId() {
    return id != null;
  }

  /**
   * 
   * @return {@code true} if the requested property is not {@code null}.
   */
  public boolean isSetText() {
    return text != null;
  }

  /* (non-Javadoc)
   * @see edu.ucsd.sbrg.escher.models.Element#setId(java.lang.String)
   */
  @Override
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @param text the text to set
   */
  public void setText(String text) {
    this.text = text;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(getClass().getSimpleName());
    builder.append(" [id=");
    builder.append(id);
    builder.append(", x=");
    builder.append(x);
    builder.append(", y=");
    builder.append(y);
    builder.append(", text=");
    builder.append(text);
    builder.append("]");
    return builder.toString();
  }

}

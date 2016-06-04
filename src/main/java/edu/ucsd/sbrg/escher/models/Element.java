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
public interface Element extends EscherBase {

	/**
	 * @return the id
	 */
	public String getId();

	/**
	 * 
	 * @return {@code true} if the requested property is not {@code null}.
	 */
	public boolean isSetId();

	/**
	 * @param id the id to set
	 */
	public void setId(String id);

}

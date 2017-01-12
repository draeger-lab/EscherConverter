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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


/**
 * @author Andreas Dr&auml;ger
 * @version $Rev$
 * @since 1.2
 * @date 21.07.2016
 */
public class NodeTest {

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }


  /**
   * Test method for {@link edu.ucsd.sbrg.escher.model.Node#setBiggId(java.lang.String)}.
   */
  @Test
  public void testSetBiggId() {
    String biggIds[] = {"dad_2_c", "alpha_hs_18_0_c"};

    for (String biggId : biggIds) {
      Node node = new Node();
      node.setBiggId(biggId);
      System.out.println(node.getBiggId() + "\t" + node.getCompartment());
      assertEquals("c", node.getCompartment());
    }

  }
}

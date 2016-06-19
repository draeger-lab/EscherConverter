package edu.ucsd.sbrg.escher;

import edu.ucsd.sbrg.escher.converters.SBGN2Escher;
import edu.ucsd.sbrg.escher.model.Canvas;
import edu.ucsd.sbrg.escher.model.EscherMap;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Sbgn;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by deveshkhandelwal on 13/06/16.
 */
public class SBGN2EscherTest {

  private static File        file;
  private static Sbgn        sbgn;
  private static SBGN2Escher converter;
  private static EscherMap   map;

  @BeforeClass
  public static void createTheWorld() throws JAXBException {
    file = new File("data/mapk_cascade.sbgn.xml");
    sbgn = SbgnUtil.readFromFile(file);
    converter = new SBGN2Escher();
    map = converter.convert(sbgn);
  }

  @Test(expected = IOException.class)
  public void failsOnNonExistentFileTest() throws IOException {
    File file = new File("data/file_which_does_not_exists");
    EscherConverter.parseEscherJson(file);
  }

  @Test(expected = JAXBException.class)
  public void failsOnInvalidSbgnFile() throws IOException, JAXBException {

    // This just checks that a ParseException is thrown on invalid file.

    File file = new File("data/e_coli_core_metabolism.escher.json");
    SbgnUtil.readFromFile(file);
  }

  @Test
  public void canGetCanvasInfo() {
    Canvas canvas = new Canvas();

    canvas.setHeight(1000.0);
    canvas.setWidth(1000.0);
    canvas.setY(0.0);
    canvas.setX(0.0);

    assertEquals("failure - canvas info not matching", canvas, map.getCanvas());
  }

}

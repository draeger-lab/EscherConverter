package edu.ucsd.sbrg.escher;

import com.fasterxml.jackson.core.JsonParseException;
import edu.ucsd.sbrg.escher.converters.SBGN2Escher;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Map;
import org.sbgn.bindings.Sbgn;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

/**
 * Created by deveshkhandelwal on 13/06/16.
 */
public class SBGN2EscherTest {

  static File file;
  static Sbgn sbgn;
  static SBGN2Escher converter;

  @BeforeClass
  public static void createTheWorld() throws JAXBException {
    file = new File("data/mapk_cascade.sbgn.xml");
    sbgn = SbgnUtil.readFromFile(file);
    converter = new SBGN2Escher();
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

  }

  @Test
  public void canGetMetaInfo() {

  }

  @Test
  public void canCreateNodes() {

  }

  @Test
  public void canCreateReactions() {

  }

  @Test
  public void canCreateTextLabels() {

  }

}

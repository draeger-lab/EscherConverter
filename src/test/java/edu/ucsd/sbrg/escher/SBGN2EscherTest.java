package edu.ucsd.sbrg.escher;

import edu.ucsd.sbrg.escher.converters.SBGN2Escher;
import edu.ucsd.sbrg.escher.helper.SbgnEscherTestTuple;
import edu.ucsd.sbrg.escher.model.Canvas;
import edu.ucsd.sbrg.escher.model.EscherMap;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Sbgn;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * Created by deveshkhandelwal on 13/06/16.
 */
@RunWith(JUnitParamsRunner.class)
public class SBGN2EscherTest {

  private static File        file;
  private static Sbgn        sbgn;
  private static SBGN2Escher converter;
  private static EscherMap   map;
  private static int numberOfMetabolites;
  private static List<String> listOfFilePaths = Arrays.asList(
      "data/mapk_cascade.sbgn.xml",
//      "data/central_plant_metabolism.sbgn.xml",
      "data/e_coli_core_metabolism.sbgn.xml"
  );

  private static List<SbgnEscherTestTuple> list = new ArrayList<>();;

  @BeforeClass
  public static void createTheWorld() throws JAXBException {
    listOfFilePaths.forEach(fP -> {
      try {
        list.add(spy(new SbgnEscherTestTuple(new File(fP))));
      } catch (JAXBException e) {
        e.printStackTrace();
      }
    });
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
  @Parameters({
      "0", "1"
  })
  public void canGetCanvasInfo(int index) {
    SbgnEscherTestTuple tuple = list.get(index);
    Canvas canvas = new Canvas();

    canvas.setHeight(1000.0);
    canvas.setWidth(1000.0);
    canvas.setY(0.0);
    canvas.setX(0.0);

    assertEquals("failure - canvas info not matching", canvas, tuple.map.getCanvas());
  }


  @Test
  @Parameters({
      "0", "1"
  })
  public void nodesShouldBeEqualTest(int index) {
    SbgnEscherTestTuple tuple = list.get(index);
    assertEquals("failure - " + tuple.file.getName() + " doesn't have " + tuple
        .numberOfMetabolites +
        " nodes"
        + ".", tuple.numberOfMetabolites, tuple.map.getNodesCount());
  }



  @Test
  @Parameters({
      "0", "1"
  })
  public void createReactionCallsCountMatches(int index) {
    SbgnEscherTestTuple tuple = list.get(index);

    Mockito.verify(tuple.converter, times(tuple.numberOfReactions)).createReaction(any());
  }

}

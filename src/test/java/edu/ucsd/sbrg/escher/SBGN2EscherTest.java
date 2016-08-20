package edu.ucsd.sbrg.escher;

import edu.ucsd.sbrg.escher.helper.SbgnEscherTestTuple;
import edu.ucsd.sbrg.escher.model.Node;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sbgn.SbgnUtil;

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

  private static List<String> listOfFilePaths = Arrays.asList(
      "data/mapk_cascade.sbgn.xml",
      "data/e_coli_core_metabolism.sbgn.xml"
  );

  private static List<SbgnEscherTestTuple> list = new ArrayList<>();

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
  public void nodesShouldBeEqualTest(int index) {
    SbgnEscherTestTuple tuple = list.get(index);
    long nodeCountWOMultiMarkers;
    // This is done because multi-markers are created from arcs instead of glyphs.
    nodeCountWOMultiMarkers = tuple.map
        .getNodes()
        .entrySet()
        .stream()
        .filter(e -> e.getValue().getType() != Node.Type.multimarker).count();

    assertEquals("failure - " + tuple.file.getName() + " doesn't have " + tuple
        .numberOfMetabolites +
        " nodes"
        + ".", tuple.numberOfMetabolites, nodeCountWOMultiMarkers);
  }



  @Test
  @Parameters({
      "0", "1"
  })
  public void createReactionCallsCountMatches(int index) {
    SbgnEscherTestTuple tuple = list.get(index);

    Mockito.verify(tuple.converter, times(tuple.numberOfReactions)).createReaction(any());
  }


  @Test
  @Parameters({
      "0", "1"
  })
  public void addCanvasCalled(int index) {
    SbgnEscherTestTuple tuple = list.get(index);

    Mockito.verify(tuple.converter).addCanvasInfo(any());
  }

}

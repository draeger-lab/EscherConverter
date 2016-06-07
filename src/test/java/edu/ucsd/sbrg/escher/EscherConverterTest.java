package edu.ucsd.sbrg.escher;

import edu.ucsd.sbrg.escher.models.EscherMap;
import edu.ucsd.sbrg.escher.models.Node;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by Devesh Khandelwal on 07-06-2016.
 */
@RunWith(Enclosed.class)
public class EscherConverterTest {

  public static class ParsingTests {

    File      file;
    EscherMap escherMap;


    public ParsingTests() throws IOException, ParseException {
      file = new File("data/e_coli_core_metabolism.json");
      escherMap = EscherConverter.parseEscherJson(file);
    }


    @Test(expected = IOException.class)
    public void failsOnNonExistantFileTest() throws IOException, ParseException {
      File file = new File("data/file_which_does_not_exists");
      EscherMap escherMap = EscherConverter.parseEscherJson(file);
    }


    @Test
    public void canParseMetaInfoTest() {
      assertEquals("failure - didn't parsed map name", "e_coli_core.Core metabolism",
          escherMap.getName());
      assertEquals("failure - didn't parsed map id", "0df3827fde8464e80f455a773a52c274",
          escherMap.getId());
      assertEquals("failure - didn't parsed map homepage", "https://escher.github.io",
          escherMap.getURL());
      assertEquals("failure - didn't parsed map schema",
          "https://escher.github" + ".io/escher/jsonschema/1-0-0#", escherMap.getSchema());
    }


    @Test
    public void canParseCanvasInfo() {
      assertEquals("failure - wrong canvas' x value", 7.0, escherMap.getCanvas().getX(), 1.0);
      assertEquals("failure - wrong canvas' y value", 314.0, escherMap.getCanvas().getY(), 1.0);
      assertEquals("failure - wrong canvas' width", 5894.0, escherMap.getCanvas().getWidth(), 1.0);
      assertEquals("failure - wrong canvas' height", 4860.0, escherMap.getCanvas().getHeight(),
          1.0);
    }


    @Test
    public void canParseTextLabels() {
      assertEquals("failure - incorrect text label count", 0, escherMap.getTextLabelCount());
    }


    @Test
    public void canParseNodes() {
      assertEquals("failure - un-equal nodes count", 462, escherMap.getNodesCount());

      Node node = new Node();
      node.setId("1576485");
      node.setX((double) 1145);
      node.setY((double) 2805);
      node.setType(Node.Type.metabolite);
      node.setBiggId("atp_c");
      node.setLabelX((double) 1165);
      node.setLabelY((double) 2805);
      node.setName("ATP");
      node.setPrimary(false);

      assertEquals("failure - node 1576485 not found", node, escherMap.getNode("1576485"));

      node = new Node();
      node.setId("1576929");
      node.setX((double) 4690);
      node.setY((double) 1900);
      node.setType(Node.Type.multimarker);

      assertEquals("failure - node 1576929 not found", node, escherMap.getNode("1576929"));
    }

    @Test
    public void canParseReactions() {
      assertEquals("failure - un-equal reactions count", 95, escherMap.getReactionCount());


    }
  }
}

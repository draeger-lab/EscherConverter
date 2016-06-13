package edu.ucsd.sbrg.escher;

import com.fasterxml.jackson.core.JsonParseException;
import edu.ucsd.sbrg.escher.model.*;
import org.json.simple.parser.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Devesh Khandelwal on 07-06-2016.
 */
@RunWith(Enclosed.class)
public class EscherConverterTest {

  public static class DeserializationTests {

    static File      file;
    static EscherMap escherMap;

    @BeforeClass
    public static void createTheWorld() throws IOException, ParseException {
      file = new File("data/e_coli_core_metabolism.escher.json");
      escherMap = EscherConverter.parseEscherJson(file);
    }

    @Test(expected = IOException.class)
    public void failsOnNonExistantFileTest() throws IOException {
      File file = new File("data/file_which_does_not_exists");
      EscherConverter.parseEscherJson(file);
    }

    @Test(expected = JsonParseException.class)
    public void failsOnInvalidJsonFile() throws IOException {

      // This just checks that a ParseException is thrown on invalid file.

      File file = new File("data/e_coli_core_metabolism.sbml.xml");
      EscherConverter.parseEscherJson(file);
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

      Node parsedNode = escherMap.getNode("1576485");

      assertEquals("failure - node 1576485 not found", node, parsedNode);
      assertTrue("failure - not set", parsedNode.isSetBiggId());
      assertTrue("failure - not set", parsedNode.isSetCompartment());
      assertFalse("failure - not set", parsedNode.isSetConnectedSegments());
      assertTrue("failure - not set", parsedNode.isSetId());
      assertTrue("failure - not set", parsedNode.isSetIsPrimary());
      assertTrue("failure - not set", parsedNode.isSetLabelX());
      assertTrue("failure - not set", parsedNode.isSetLabelY());
      assertTrue("failure - not set", parsedNode.isSetName());
      assertTrue("failure - not set", parsedNode.isSetPrimary());
      assertTrue("failure - not set", parsedNode.isSetType());
      assertTrue("failure - not set", parsedNode.isSetX());
      assertTrue("failure - not set", parsedNode.isSetY());

      node = new Node();
      node.setId("1576929");
      node.setX((double) 4690);
      node.setY((double) 1900);
      node.setType(Node.Type.multimarker);

      parsedNode = escherMap.getNode("1576929");

      assertEquals("failure - node 1576929 not found", node, escherMap.getNode("1576929"));
      assertFalse("failure - not set", parsedNode.isSetBiggId());
      assertFalse("failure - not set", parsedNode.isSetCompartment());
      assertFalse("failure - not set", parsedNode.isSetConnectedSegments());
      assertTrue("failure - not set", parsedNode.isSetId());
      assertFalse("failure - not set", parsedNode.isSetIsPrimary());
      assertFalse("failure - not set", parsedNode.isSetLabelX());
      assertFalse("failure - not set", parsedNode.isSetLabelY());
      assertFalse("failure - not set", parsedNode.isSetName());
      assertFalse("failure - not set", parsedNode.isSetPrimary());
      assertTrue("failure - not set", parsedNode.isSetType());
      assertTrue("failure - not set", parsedNode.isSetX());
      assertTrue("failure - not set", parsedNode.isSetY());

      // The following code checks if there are any connected segments
      // in any of the nodes. So far, I've not found one.
      for (Map.Entry<String, Node> entry : escherMap.nodes()) {
        try {
          if (entry.getValue().isSetConnectedSegments())
          {
            throw new Exception();
          }
        }
        catch (Exception ex) {
          entry.getValue().getConnectedSegments();
        }
      }
    }

    @Test
    public void canParseReactions() {
      assertEquals("failure - un-equal reactions count", 95, escherMap.getReactionCount());

      EscherReaction reaction = escherMap.getReaction("1576721");

      assertEquals("failure - reaction name mismatch", "Phosphofructokinase", reaction.getName());
      assertEquals("failure - reaction reversiblity mismatch", false, reaction.getReversibility());
      assertEquals("failure - reaction gene-reaction-rule mismatch", "b3916 or b1723", reaction.getGeneReactionRule());
      assertEquals("failure - reaction laebl y mismatch", (double)1725, reaction.getLabelY
          (), 1.0);

      assertTrue("failure - not set", reaction.isSetName());
      assertTrue("failure - not set", reaction.isSetId());
      assertTrue("failure - not set", reaction.isSetBiggId());
      assertTrue("failure - not set", reaction.isSetGeneReactionRule());
      assertTrue("failure - not set", reaction.isSetLabelX());
      assertTrue("failure - not set", reaction.isSetLabelY());
      assertTrue("failure - not set", reaction.isSetReversibility());
    }

    @Test
    public void canParseSegments() {
      assertEquals("failure - un-equal genes count", 4, escherMap.getReaction("1576697")
                                                                 .getSegmentCount());

      Segment segment = new Segment();
      segment.setId("315");
      segment.setBasePoint1(new Point(2674.55, 3093.4));
      segment.setBasePoint2(new Point(2652.5, (double) 3134));
      segment.setFromNodeId("1576845");
      segment.setToNodeId("1576504");

      assertEquals("failure - segment 315 not found", segment, escherMap.getReaction("1576697")
                                                                        .getSegment("315"));

      segment = escherMap.getReaction("1576697").getSegment("315");

      assertTrue("failure - not set", segment.isSetBasePoint1());
      assertTrue("failure - not set", segment.isSetBasePoint2());
      assertTrue("failure - not set", segment.isSetFromNodeId());
      assertTrue("failure - not set", segment.isSetToNodeId());
      assertTrue("failure - not set", segment.isSetId());

      segment = escherMap.getReaction("1576697").getSegment("314");

      assertFalse("failure - not set", segment.isSetBasePoint1());
      assertFalse("failure - not set", segment.isSetBasePoint2());
      assertTrue("failure - not set", segment.isSetFromNodeId());
      assertTrue("failure - not set", segment.isSetToNodeId());
      assertTrue("failure - not set", segment.isSetId());
    }

    @Test
    public void canParseMetabolites() {
      assertEquals("failure - un-equal metabolite count", 6, escherMap.getReaction("1576700").getMetaboliteCount());

      Metabolite metabolite = new Metabolite();
      metabolite.setId("h_c");
      metabolite.setCoefficient((double) 1);
      metabolite.setNodeRefId("1576519");

      Metabolite parsedMetabolite = escherMap.getReaction("1576700").getMetabolites().get("h_c");

      assertEquals("failure - metabolite h_c in reaction 1576700 not found", metabolite, parsedMetabolite);

      assertTrue("failure - not set", parsedMetabolite.isSetId());
      assertTrue("failure - not set", parsedMetabolite.isSetCoefficient());
      assertTrue("failure - not set", parsedMetabolite.isSetNodeRefId());
    }

    @Test
    public void canParseGenes() {
      assertEquals("failure - un-equal genes count", 2, escherMap.getReaction("1576703").getGeneCount());

      Gene gene = new Gene();
      gene.setId("b0451");
      gene.setName("amtB");

      assertEquals("failure - gene bo451 not found", gene, escherMap.getReaction("1576703")
                                                                    .getGenes().get("b0451"));
      assertTrue("failure - not set", escherMap.getReaction("1576703").getGenes().get("b0451").isSetId());
      assertTrue("failure - not set", escherMap.getReaction("1576703").getGenes().get("b0451").isSetName());
    }

    @Test
    public void canProcessCompartments() {
      assertEquals("failure - compartments count mismatch", 2, escherMap.getCompartmentCount());
    }

    @Test
    public void canProcessBigg2Nodes() {
      assertNotEquals("failure - bigg2nodes count mismatch", 0, escherMap.getBigg2nodes().size());
    }

    @Test
    public void canProcessBigg2Reactions() {
      assertNotEquals("failure - bigg2reactions count mismatch", 0, escherMap.getBigg2reactions().size());
    }

  }
}

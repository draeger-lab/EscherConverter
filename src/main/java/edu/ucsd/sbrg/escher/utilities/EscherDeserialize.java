package edu.ucsd.sbrg.escher.utilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.zbit.util.ResourceManager;
import edu.ucsd.sbrg.escher.EscherConverter;
import edu.ucsd.sbrg.escher.jc_models.EscherMap;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Created by Devesh Khandelwal on 06-06-2016.
 */
public class EscherDeserialize {

  /**
   * Localization support.
   */
  private static final transient ResourceBundle
      baseBundle       =
      ResourceManager.getBundle("Messages");
  /**
   * Localization support.
   */
  private static final transient ResourceBundle
      bundle           =
      ResourceManager.getBundle("Messages");
  /**
   * A {@link Logger} for this class.
   */
  private static final           Logger
      logger           =
      Logger.getLogger(EscherConverter.class.getName());

  public static void parseAndDump() {
    File file = new File("data/iMM904.Central_carbon_metabolism.json");
    ObjectMapper jsonMapper = new ObjectMapper();
    jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    jsonMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    jsonMapper.enable(SerializationFeature.INDENT_OUTPUT);

    logger.info(MessageFormat
        .format(bundle.getString("EscherConverter.readingFile"), file));

    JsonNode node;
    EscherMap metaMap, dataMap;
    try {
      node = jsonMapper.readTree(file);
      metaMap = jsonMapper.treeToValue(node.get(0), EscherMap.class);
      dataMap = jsonMapper.treeToValue(node.get(1), EscherMap.class);

      ArrayList arrayList = new ArrayList(2);
      arrayList.add(metaMap);
      arrayList.add(dataMap);

      jsonMapper.writeValue(new File("data/temp.json"), arrayList);
      logger.info(MessageFormat
          .format(bundle.getString("EscherConverter.readingDone"), file));

    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}

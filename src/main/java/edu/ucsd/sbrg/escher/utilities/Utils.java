package edu.ucsd.sbrg.escher.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import de.zbit.util.ResourceManager;

import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Created by Devesh Khandelwal on 27-06-2016.
 */
public class Utils {

  /**
   * Localization support.
   */
  private static final transient ResourceBundle
      bundle           =
      ResourceManager.getBundle("Strings");
  /**
   * A {@link Logger} for this class.
   */
  private static final           Logger
      logger           =
      Logger.getLogger(Utils.class.getName());

  public static JsonSchema jsonSchemaSchema() throws IOException, ProcessingException {
    JsonNode jsonNode = JsonLoader.fromResource(bundle.getString("meta_schema"));

    return JsonSchemaFactory.byDefault().getJsonSchema(jsonNode);
  }


  public static JsonNode defaultEscherSchema() throws IOException, ProcessingException {
    JsonNode jsonNode = JsonLoader.fromResource(bundle.getString("default_escher_schema"));

    return jsonNode;
  }
}

package edu.ucsd.sbrg.escher.util;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import de.zbit.util.ResourceManager;

/**
 * Utility methods.
 *
 * @author Devesh Khandelwal, Andreas Dr&auml;ger
 * Created on 27-06-2016.
 */
public class Utils {

  /**
   * Default values.
   */
  private static final transient ResourceBundle bundle = ResourceManager.getBundle("Strings");


  /**
   * Get the schema validation schema (meta-schema) file.
   *
   * @return The {@code JSON Schema}.
   * @throws IOException Thrown if error in accessing the file.
   * @throws ProcessingException Thrown if problem in parsing JSON.
   */
  public static JsonSchema jsonSchemaSchema() throws IOException, ProcessingException {
    JsonNode jsonNode = JsonLoader.fromResource(bundle.getString("meta_schema_file"));

    return JsonSchemaFactory.byDefault().getJsonSchema(jsonNode);
  }


  /**
   * Get the default Escher Schema (v1.0.0).
   *
   * @return The {@code JSON Schema}.
   * @throws IOException Thrown if error in accessing the schema file.
   * @throws ProcessingException Thrown if problem in parsing JSON.
   */
  public static JsonNode defaultEscherSchema() throws IOException, ProcessingException {
    JsonNode jsonNode = JsonLoader.fromResource(bundle.getString("default_escher_schema_file"));

    return jsonNode;
  }


  /**
   * Get the pre-configured {@link ObjectMapper} for (de)serialization. Necessary settings needed
   * are set for our use case.
   *
   * @return The {@code object mapper}.
   */
  public static ObjectMapper getObjectMapper() {

    ObjectMapper objectMapper = new ObjectMapper();

    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
    objectMapper.disable(MapperFeature.AUTO_DETECT_SETTERS);
    objectMapper.disable(MapperFeature.AUTO_DETECT_CREATORS);
    objectMapper.disable(MapperFeature.AUTO_DETECT_FIELDS);
    objectMapper.disable(MapperFeature.AUTO_DETECT_GETTERS);
    objectMapper.disable(MapperFeature.AUTO_DETECT_IS_GETTERS);
    objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
    objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    return objectMapper;
  }

}

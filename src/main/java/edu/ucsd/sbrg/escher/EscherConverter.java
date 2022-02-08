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
package edu.ucsd.sbrg.escher;

import static java.text.MessageFormat.format;

import java.awt.Window;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import org.json.simple.parser.ParseException;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.TidySBMLWriter;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;

import de.zbit.AppConf;
import de.zbit.Launcher;
import de.zbit.gui.GUIOptions;
import de.zbit.io.FileTools;
import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.util.ResourceManager;
import de.zbit.util.Utils;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBProperties;
import edu.ucsd.sbrg.escher.converter.Escher2SBGN;
import edu.ucsd.sbrg.escher.converter.Escher2SBML;
import edu.ucsd.sbrg.escher.converter.Escher2Standard;
import edu.ucsd.sbrg.escher.converter.SBGN2Escher;
import edu.ucsd.sbrg.escher.converter.SBML2Escher;
import edu.ucsd.sbrg.escher.gui.EscherConverterUI;
import edu.ucsd.sbrg.escher.model.EscherMap;
import edu.ucsd.sbrg.escher.util.EscherIOOptions;
import edu.ucsd.sbrg.escher.util.EscherOptions;
import edu.ucsd.sbrg.escher.util.Validator;
import edu.ucsd.sbrg.escher.util.EscherOptions.InputFormat;
import edu.ucsd.sbrg.escher.util.EscherOptions.OutputFormat;

/**
 * Main class of the application.
 * @author Andreas Dr&auml;ger
 * @author Devesh Khandelwal
 */
public class EscherConverter extends Launcher {

  /**
   * Localization support.
   */
  private static final transient ResourceBundle baseBundle       = ResourceManager.getBundle("edu.ucsd.sbrg.escher.Messages");
  /**
   * Localization support.
   */
  private static final transient ResourceBundle bundle           = ResourceManager.getBundle("edu.ucsd.sbrg.escher.Messages");
  /**
   * A {@link Logger} for this class.
   */
  private static final           Logger logger           = Logger.getLogger(EscherConverter.class.getName());
  /**
   * Generated serial version identifier.
   */
  private static final           long serialVersionUID = 9037698164025548416L;


  /**
   * This configures the generic {@link Escher2Standard} and sets the common layout options.
   *
   * @param converter The converter to configure.
   * @param properties Command line options, to configure the converter with.
   * @return Returns the configured converter.
   */
  public static <T> Escher2Standard<T> configure(Escher2Standard<T> converter,
    SBProperties properties) {
    converter.setLabelHeight(properties.getDoubleProperty(EscherOptions.LABEL_HEIGHT));
    converter.setLabelWidth(properties.getDoubleProperty(EscherOptions.LABEL_WIDTH));
    converter.setPrimaryNodeHeight(properties.getDoubleProperty(EscherOptions.PRIMARY_NODE_HEIGHT));
    converter.setPrimaryNodeWidth(properties.getDoubleProperty(EscherOptions.PRIMARY_NODE_WIDTH));
    converter.setReactionNodeRatio(properties.getDoubleProperty(EscherOptions.REACTION_NODE_RATIO));
    converter.setSecondaryNodeRatio(properties.getDoubleProperty(EscherOptions.SECONDARY_NODE_RATIO));
    converter.setInferCompartmentBoundaries(properties.getBooleanProperty(EscherOptions.INFER_COMPARTMENT_BOUNDS));

    return converter;
  }


  /**
   * Generic method which converts the given {@link EscherMap} instance to the specified
   * generic parameter {@code format}, by calling the corresponding converter.
   *
   * @param map {@link EscherMap} instance to convert.
   * @param format Output format to convert to.
   * @param properties Command line options, if any.
   * @return An instance of {@code format} type created from the escher map.
   */
  @SuppressWarnings("unchecked")
  public static <T> T convert(EscherMap map, Class<? extends T> format,
    SBProperties properties) {
    if (format.isAssignableFrom(SBMLDocument.class)) {
      // File is SBML, so convert to it.
      Escher2SBML converter = new Escher2SBML();
      configure(converter, properties);
      converter.setCanvasDefaultHeight(properties.getDoubleProperty(EscherOptions.CANVAS_DEFAULT_HEIGHT));
      converter.setCanvasDefaultWidth(properties.getDoubleProperty(EscherOptions.CANVAS_DEFAULT_WIDTH));
      converter.setDefaultCompartmentId(properties.getProperty(EscherOptions.COMPARTMENT_ID));
      converter.setDefaultCompartmentName(properties.getProperty(EscherOptions.COMPARTMENT_NAME));
      converter.setLayoutId(properties.getProperty(EscherOptions.LAYOUT_ID));
      converter.setLayoutName(properties.getProperty(EscherOptions.LAYOUT_NAME));
      converter.setNodeDepth(properties.getDoubleProperty(EscherOptions.NODE_DEPTH));
      converter.setNodeLabelHeight(properties.getDoubleProperty(EscherOptions.NODE_LABEL_HEIGHT));
      converter.setZ(properties.getDoubleProperty(EscherOptions.Z));
      return (T) converter.convert(map);
    } else if (format.isAssignableFrom(Sbgn.class)) {
      // File is SBGN-ML, so convert to it.
      Escher2Standard<?> converter = configure(new Escher2SBGN(), properties);
      return (T) converter.convert(map);
    }
    return null;
  }


  /**
   * Calls SBGN-ML to Escher converter. See {@link SBGN2Escher}.
   *
   * @param document SBGN-ML ({@link Sbgn}) document to convert.
   * @param properties Command line options, if any.
   * @return The exported {@link EscherMap} instance.
   */
  public static EscherMap convert(Sbgn document, SBProperties properties) {
    SBGN2Escher converter = new SBGN2Escher();

    return converter.convert(document);
  }


  /**
   * Calls SBML Layout Extension to Escher converter. See {@link SBML2Escher}.
   *
   * @param document {@link SBMLDocument} to convert.
   * @param properties Command line options, if any.
   * @return A list of {@link EscherMap} extracted from the SBML file, as there may be more than
   * one.
   */
  public static List<EscherMap> convert(SBMLDocument document, SBProperties properties) {
    SBML2Escher converter = new SBML2Escher();
    converter.setNodeHeight(properties.getDoubleProperty(EscherOptions.PRIMARY_NODE_HEIGHT));
    converter.setNodeWidth(properties.getDoubleProperty(EscherOptions.PRIMARY_NODE_WIDTH));
    return converter.convert(document);
  }

  /**
   * Converts given JSON {@code input} to given output {@code format}. Simply calls the
   * {@link #convert(EscherMap, Class, SBProperties)} with the parsed JSON.
   *
   * @param input JSON file which has an {@link EscherMap}.
   * @param format Output format to convert to.
   * @param properties Command line options, if any.
   * @return An instance of {@code format} type created from the escher map.
   * @throws IOException Thrown if there are problems in reading the {@code input} file.
   * @throws ParseException Thrown if the JSON in {@code input} file is invalid.
   */
  public static <T> T convert(File input, Class<? extends T> format,
    SBProperties properties) throws IOException, ParseException {
    return convert(parseEscherJson(input), format, properties);
  }

  /**
   * Parses given JSON file into an {@link EscherMap} instance using Jackson.
   * 
   * @param input The {@link File} to parse.
   * @return The {@link EscherMap} instance.
   * @throws IOException Thrown if there are problems in reading the {@code input} file.
   */
  public static EscherMap parseEscherJson(File input) throws IOException {
    logger.info(format(bundle.getString("EscherConverter.readingFile"), input));

    EscherMap map = parseEscherJson(new FileInputStream(input));

    logger.info(format(bundle.getString("EscherConverter.readingDone"), input));

    return map;
  }


  /**
   * Parses an {@link InputStream} that represents an Escher JSON file into an
   * {@link EscherMap} instance using Jackson.
   * 
   * @param stream
   * @return
   * @throws IOException
   * @throws JsonProcessingException
   */
  public static EscherMap parseEscherJson(InputStream stream)
      throws IOException, JsonProcessingException {
    ObjectMapper objectMapper = edu.ucsd.sbrg.escher.util.Utils.getObjectMapper();
    // An Escher array contains meta-info about the map as the first object and the actual map as
    // second map.
    try {
      JsonNode escherJson = objectMapper.readTree(stream);

      if(escherJson.get(0) == null) {
        logger.severe(format(bundle.getString("EscherConverter.missingMetaInfo")));
        throw new IOException(format(bundle.getString("EscherConverter.missingMetaInfo"))){};
      }
      // Meta-info.
      EscherMap meta = objectMapper.treeToValue(escherJson.get(0), EscherMap.class);
      // Layout map (nodes, reactions, text labels and canvas info).
      EscherMap map = objectMapper.treeToValue(escherJson.get(1), EscherMap.class);

      // Put meta-info from first to second.
      map.setId(meta.getId());
      map.setName(meta.getName());
      map.setDescription(meta.getDescription());
      map.setSchema(meta.getSchema());
      map.setURL(meta.getURL());

      map.postprocessMap();
      return map;
    } catch(JsonProcessingException e) {
      logger.severe(bundle.getString("EscherValidationFail.NotJson"));
      throw e;
    }
  }

  /**
   * Starts the program. For a description of possible command-line arguments launch with option -?.
   *
   * @param args Command line options, if any.
   */
  public static void main(String args[]) {
	new EscherConverter(args);
  }


  /**
   * Called by the main method. Necessary for the SysBio library.
   *
   * @param args Command line options, if any.
   */
  public EscherConverter(String... args) {
    super(args);
  }


  /* (non-Javadoc)
   * @see de.zbit.Launcher#addCopyrightToSplashScreen()
   */
  @Override
  protected boolean addCopyrightToSplashScreen() {
    return false;
  }


  /* (non-Javadoc)
   * @see de.zbit.Launcher#addVersionNumberToSplashScreen()
   */
  @Override
  protected boolean addVersionNumberToSplashScreen() {
    return false;
  }


  /**
   * Convert files one by one by calling the {@link #convert(File, File, SBProperties)} recursively.
   * Only called once if single file.
   *
   * @param input Single file or input directory to convert from.
   * @param output Single file or output directory to convert to.
   * @param properties Command line options, if any.
   * @throws IOException Thrown if there are problems in reading the {@code input} file(s).
   * @throws TransformerException Thrown if there are problems in parsing XML file(s).
   * @throws ParserConfigurationException Thrown if there are problems in parsing XML file(s).
   * @throws SAXException Thrown if there are problems in parsing XML file(s).
   * @throws JAXBException Thrown if there are problems in parsing XML file(s).
   * @throws XMLStreamException Thrown if there are problems in parsing XML file(s).
   * @throws ParseException Thrown if there are problems in parsing JSON file(s).
   * @throws SBMLException Thrown if there are problems in parsing XML file(s).
   */
  public void batchProcess(File input, File output, SBProperties properties)
      throws ParseException, JAXBException, IOException, XMLStreamException,
      ParserConfigurationException, SAXException, TransformerException {
    // TODO: Warn before overwriting.

    // Checks if output/input is directory, if it doesn't, create one.
    if (!output.exists() && (output.getName().lastIndexOf('.') < 0) &&
        !(input.isFile() && input.getName().equals(output.getName()))) {
      logger.info(format(
        bundle.getString("EscherConverter.creatingDir"),
        output.getAbsolutePath()));

      // TODO: A directory shouldn't be directly created, instead a file should be created.
      output.mkdir();
    }
    if (input.isFile()) {

      if (SBFileFilter.isJSONFile(input)) {
        logger.info(bundle.getString("AutoDetectJSON"));

        if (output.isDirectory()) {
          String fName = input.getName();
          fName = FileTools.removeFileExtension(fName) + ".xml";
          output = new File(Utils.ensureSlash(output.getAbsolutePath()) + fName);
        }
        properties.put(InputFormat.class.getSimpleName(), InputFormat.Escher);
        convert(input, output, properties);
      }
      else if (SBFileFilter.isSBMLFile(input)) {
        logger.info(bundle.getString("AutoDetectSBML"));
        properties.put(InputFormat.class.getSimpleName(), InputFormat.SBML);
        convert(input, output, properties);
      }
      else if (SBFileFilter.isSBGNFile(input)) {
        logger.info(bundle.getString("AutoDetectSBGN"));

        if (output.isDirectory()) {
          String fName = input.getName();
          fName = FileTools.removeFileExtension(fName) + ".json";
          output = new File(Utils.ensureSlash(output.getAbsolutePath()) + fName);
        }
        properties.put(InputFormat.class.getSimpleName(), InputFormat.SBGN);
        convert(input, output, properties);
      }
      else {
        logger.severe(bundle.getString("AutoDetectFail"));
      }

    } else {
      if (!output.isDirectory()) {
        throw new IOException(format(
          bundle.getString("EscherConverter.cannotWriteToFile"),
          output.getAbsolutePath()));
      }
      for (File file : input.listFiles()) {
        File target = new File(
          Utils.ensureSlash(output.getAbsolutePath()) + input.getName());
        batchProcess(file, target, properties);
      }
    }
  }


  /* (non-Javadoc)
   * @see de.zbit.Launcher#commandLineMode(de.zbit.AppConf)
   */
  @Override
  public void commandLineMode(AppConf appConf) {
    if (props.containsKey(EscherIOOptions.INPUT) && props.containsKey(EscherIOOptions.OUTPUT)) {
      // TODO: Allow output to be empty, create file/directory if doesn't exists.
      try {
        File input = replaceUnixPathAbbreviations(props.getProperty(EscherIOOptions.INPUT.toString()));
        File output = replaceUnixPathAbbreviations(props.getProperty(EscherIOOptions.OUTPUT.toString()));
        if (input.isDirectory()) {
          if (output.isFile()) {
            logger.severe(bundle.getString("BatchModeOutputNotDirectory"));
            return;
          }
          logger.info(format(
            bundle.getString("EscherConverter.launchingBatchProcessing"),
            input.getAbsolutePath()));
        }
        // Can also be used if only a single file is to be converted:
        batchProcess(input, output, props);
      } catch (SBMLException | XMLStreamException | IOException | ParseException | JAXBException
          | SAXException | ParserConfigurationException | TransformerException exc) {
        exc.printStackTrace();
      }
    } else {
      logger.warning(bundle.getString("EscherConverter.incompleteCMDArgs"));
    }
  }


  /**
   * Does some very basic file path interpretation.
   * 
   * @param path an input path (can be relative or start with tilde)
   * @return a {@link File} representing the absolute path.
   */
  private File replaceUnixPathAbbreviations(String path) {
    if (path.startsWith("~")) {
      path = System.getProperty("user.home") + path.substring(1);
    } else if (path.startsWith(".")) {
      path = System.getProperty("user.dir") + path.substring(1);
    }
    return new File(path);
  }


  /**
   * Called by {@link #batchProcess(File, File, SBProperties)} for every file. Calls the
   * appropriate overloads.
   *
   * @param input Input (single) {@link File}.
   * @param output Output (single) {@link File}.
   * @param properties Command line options, if any.
   * @throws IOException Thrown if there are problems in reading the {@code input} file(s).
   * @throws XMLStreamException Thrown if there are problems in parsing XML file(s).
   * @throws TransformerException Thrown if there are problems in parsing XML file(s).
   * @throws ParserConfigurationException Thrown if there are problems in parsing XML file(s).
   * @throws SAXException Thrown if there are problems in parsing XML file(s).
   * @throws JAXBException Thrown if there are problems in parsing XML file(s).
   * @throws ParseException Thrown if there are problems in parsing JSON file(s).
   * @throws SBMLException Thrown if there are problems in parsing XML file(s).
   */
  public void convert(File input, File output, SBProperties properties)
      throws IOException, ParseException,
      XMLStreamException, SBMLException, JAXBException, SAXException,
      ParserConfigurationException, TransformerException {
    InputFormat inputFormat = InputFormat.valueOf(properties.get(InputFormat.class.getSimpleName()));
    OutputFormat outputFormat = OutputFormat.valueOf(properties.getProperty(EscherOptions.FORMAT));

    logger.warning(bundle.getString("ValidationStart"));
    try {
      if (!validateInput(input, inputFormat)) {
        logger.warning(bundle.getString("ValidationFailed"));

        // Unless the --ignore-validation option has been set to true, do not continue
        if (properties.getBoolean(EscherOptions.IGNORE_VALIDATION)) {
          logger.warning(bundle.getString("ValidationSkip"));
        } else {
          logger.warning(bundle.getString("ValidationAbort"));
          return;
        }
      }

      boolean success = false;

      // Check output format.
      switch (outputFormat) {

        case SBML:
          SBMLDocument doc = convert(input, SBMLDocument.class, properties);
          TidySBMLWriter.write(doc, output, System.getProperty("app.name"),
                  getVersionNumber(), ' ', (short) 2);
          success = true;
          break;

        case SBGN:
          Sbgn sbgn = convert(input, Sbgn.class, properties);
          SbgnUtil.writeToFile(sbgn, output);
          success = true;
          break;

        case Escher:
          // Check input format.
          switch (inputFormat) {

            case SBGN:
              EscherMap map = parseSBGNML(input, properties);
              writeEscherJson(map, output);
              success = true;
              break;

            case SBML:
              if (properties.getBooleanProperty(EscherOptions.EXTRACT_COBRA)) {
                extractCobraModel(input);
              }
              List<EscherMap> maps = convert(SBMLReader.read(input), properties);
              writeEscherJson(maps, output);
              success = true;
              break;

            case Escher:
              logger.info(bundle.getString("InputOutputFormatIdentical"));
              break;
          }
          break;

        default:
          logger.severe(bundle.getString("UnsupportedFormat"));
          break;
      }

      if (success) {
        logger.info(format(
                "Output successfully written to file {0}.", output));
      }
    } catch(JsonProcessingException e) {
      logger.severe(bundle.getString("EscherValidationFail.NotJson"));
    }
  }


  /**
   * @param input
   * @param properties
   * @return
   * @throws JAXBException
   */
  public static EscherMap parseSBGNML(File input, SBProperties properties) throws JAXBException {
    return convert(SbgnUtil.readFromFile(input), properties);
  }

  /**
   * Parses an SBGNML input and calls a convert method to {@link EscherMap}.
   * @param is InputStream of the SBGNML file.
   * @param properties Command line arguments, if applicable
   * @return The file {@code is} converted to {@link EscherMap}
   * @throws JAXBException
   */
  public static EscherMap parseSBGNML(InputStream is, SBProperties properties) throws JAXBException {
    JAXBContext context = JAXBContext.newInstance("org.sbgn.bindings");
    Unmarshaller unmarshaller = context.createUnmarshaller();

    // Now read from "is" and put the result in "sbgn"
    Sbgn sbgnDoc = (Sbgn) unmarshaller.unmarshal(is);

    return convert(sbgnDoc, properties);
  }


  /**
   * Extracts CoBRA from {@link SBMLDocument} if it is FBC compliant. cobrapy must be present for
   * this.
   *
   * @param file Input file.
   * @return Result of extraction.
   * @throws IOException Thrown if there are problems in reading the {@code input} file(s).
   * @throws XMLStreamException Thrown if there are problems in parsing XML file(s).
   */
  public static boolean extractCobraModel(File file) throws IOException, XMLStreamException {
    if (false) {
      logger.warning(format(bundle.getString("SBMLFBCNotAvailable"), file.getName()));
      return false;
    }
    else {
      logger.info(format(bundle.getString("SBMLFBCInit"), file.getName()));
      // Execute: py3 -c "from cobra import io;
      // io.save_json_model(model=io.read_sbml_model('FILENAME'), file_name='FILENAME')"

      String[] command;
      command = new String[]{"python3", "-c", "\"print('yo');from cobra import io;"
          + "io.save_json_model(model=io.read_sbml_model('" + file
          .getAbsolutePath() + "'), file_name='" + file
          .getAbsolutePath() + ".json" + "');print('yo')\"", "> /temp/log"};
      // command = new String[] {"/usr/local/bin/python3", "-c", "\"print('yo')\""};
      command = new String[] {"python3"};
      Process p;
      try {
        // p = new ProcessBuilder(command).redirectErrorStream(true).start();
        p = Runtime.getRuntime().exec(command);
        p.waitFor();
        if (p.exitValue() == 0) {
          logger.info(format(bundle.getString("SBMLFBCExtractionSuccessful"),
            file.getAbsolutePath(), file.getAbsolutePath()));
          InputStream is = p.getErrorStream();
          is = p.getInputStream();
          OutputStream os = p.getOutputStream();
          BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
          String cobrapy_output = "";
          cobrapy_output = reader.readLine();
          while (cobrapy_output != null) {
            logger.warning(cobrapy_output);
            cobrapy_output = reader.readLine();
          }
          return true;
        }
        else {
          logger.info(format(bundle.getString("SBMLFBCExtractionFailed")));
          BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
          String cobrapy_output = "";
          cobrapy_output = reader.readLine();
          while (cobrapy_output != null) {
            logger.warning(cobrapy_output);
            cobrapy_output = reader.readLine();
          }
          return false;
        }
      } catch (InterruptedException e) {
        e.printStackTrace();
        return false;
      }
    }
  }


  /**
   * Calls appropriate validator for given {@code file} using {@link InputFormat}.
   *
   * @param input Input file.
   * @param inputFormat Format of input file.
   * @return Result of validation.
   * @throws IOException Thrown if there are problems in reading the {@code input} file(s).
   */
  private boolean validateInput(File input, InputFormat inputFormat) throws IOException {
    Validator validator;
    try {
      // TODO: Add support for custom schema file.
      validator = new Validator();
    } catch (ProcessingException e) {
      return false;
    }

    // Call appropriate validator according to input format.
    switch (inputFormat) {
    case SBGN:
      logger.info(bundle.getString("ValidatingSBGN"));
      return validator.validateSbgnml(input);

    case SBML:
      logger.info(bundle.getString("ValidatingSBML"));
      return validator.validateSbmlLE(input);

    case Escher:
      logger.info(bundle.getString("ValidatingEscher"));
      return validator.validateEscher(input);

    }
    return false;
  }


  /**
   * Serializes an {@link EscherMap} instance to JSON and writes to {@code output} file.
   *
   * @param map Escher map to serialize.
   * @param output Output file to write to.
   * @throws IOException Thrown if there are problems in reading the {@code input} file(s).
   */
  public static void writeEscherJson(EscherMap map, File output) throws IOException {
    // An Escher array contains meta-info about the map as the first object and the actual map as
    // second map. That's why we need an array of size 2.
    List<EscherMap> mapList = new ArrayList<>(2);

    // Add meta info to the new object.
    mapList.add(new EscherMap());
    mapList.get(0).setId(map.getId());
    mapList.get(0).setDescription(map.getDescription());
    mapList.get(0).setName(output.getName());
    mapList.get(0).setSchema(map.getSchema());
    mapList.get(0).setURL(map.getURL());

    // And remove it from the other object.
    map.setId(null);
    map.setName(null);
    map.setDescription(null);
    map.setSchema(null);
    map.setURL(null);

    mapList.add(map);

    edu.ucsd.sbrg.escher.util.Utils.getObjectMapper().writeValue(output, mapList);
  }


  /**
   * Serializes a {@link List<EscherMap>} to JSON and writes to {@code output} directory.
   *
   * @param mapList List of Escher maps to serialize.
   * @param output Directory to create files in.
   */
  public static void writeEscherJson(List<EscherMap> mapList, File output) {
    try {
      if (mapList.size() == 0) {
        // SBML file has no layout.
        logger.warning(bundle.getString("SBMLNoLayout"));
        return;
      }
      if (output.exists() && output.isFile()) {
        // If output is a file, we can only write one layout.
        if (mapList.size() == 1) {
          writeEscherJson(mapList.get(0), output);
        }
        else {
          logger.severe(bundle.getString("SingleFileMultipleLayout"));
        }
      }
      else {
        // Write all maps to their own file.
        for (EscherMap map : mapList) {
          File file;
          if ((mapList.size() > 1) || (output.exists() && output.isDirectory())) {
            file = new File(Utils.ensureSlash(output.getPath()) + map.getId() + ".json");
          } else if (SBFileFilter.isJSONFile(output)) {
            file = output;
          } else {
            file = new File(output.getAbsolutePath() + ".json");
          }
          try {
            if (!file.exists()) {
            	if(file.getParentFile() != null){
            		file.getParentFile().mkdirs();
            	}
              file.createNewFile();
            }
            writeEscherJson(map, file);
          } catch (IOException e) {
            logger.severe(format(bundle.getString("FileIOError"), file.getAbsolutePath()));
          }
        }
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }


  /* (non-Javadoc)
   * @see de.zbit.Launcher#getCmdLineOptions()
   */
  @Override
  public List<Class<? extends KeyProvider>> getCmdLineOptions() {
    List<Class<? extends KeyProvider>> list = new ArrayList<Class<? extends KeyProvider>>(3);
    list.add(EscherIOOptions.class);
    list.add(EscherOptions.class);
    list.add(GUIOptions.class);
    return list;
  }


  /* (non-Javadoc)
   * @see de.zbit.Launcher#getInstitute()
   */
  @Override
  public String getInstitute() {
    return baseBundle.getString("INSTITUTE");
  }


  /* (non-Javadoc)
   * @see de.zbit.Launcher#getInteractiveOptions()
   */
  @Override
  public List<Class<? extends KeyProvider>> getInteractiveOptions() {
    List<Class<? extends KeyProvider>> list = new ArrayList<Class<? extends KeyProvider>>(1);
    list.add(EscherOptions.class);
    return list;
  }


  /* (non-Javadoc)
   * @see de.zbit.Launcher#getLogPackages()
   */
  @Override
  public String[] getLogPackages() {
    List<String> packages = new ArrayList<String>();
    packages.addAll(Arrays.asList(super.getLogPackages()));
    packages.add("edu.ucsd");
    return packages.toArray(new String[] {});
  }


  /* (non-Javadoc)
   * @see de.zbit.Launcher#getOrganization()
   */
  @Override
  public String getOrganization() {
    return baseBundle.getString("ORGANIZATION");
  }


  /* (non-Javadoc)
   * @see de.zbit.Launcher#getProvider()
   */
  @Override
  public String getProvider() {
    return baseBundle.getString("PROVIDER");
  }


  /* (non-Javadoc)
   * @see de.zbit.Launcher#getURLlicenseFile()
   */
  @Override
  public URL getURLlicenseFile() {
    try {
      return new URL(bundle.getString("EscherConverter.licenseURL"));
    } catch (MalformedURLException exc) {
      logger.warning(Utils.getMessage(exc));
      return null;
    }
  }


  /* (non-Javadoc)
   * @see de.zbit.Launcher#getURLOnlineUpdate()
   */
  @Override
  public URL getURLOnlineUpdate() {
    return null;
  }


  /* (non-Javadoc)
   * @see de.zbit.Launcher#getVersionNumber()
   */
  @Override
  public String getVersionNumber() {
    return baseBundle.getString("EscherConverter.version");
  }


  /* (non-Javadoc)
   * @see de.zbit.Launcher#getYearOfProgramRelease()
   */
  @Override
  public short getYearOfProgramRelease() {
    try {
      return Short.parseShort(baseBundle.getString("YEAR"));
    } catch (NumberFormatException exc) {
      return (short) Calendar.getInstance().get(Calendar.YEAR);
    }
  }


  /* (non-Javadoc)
   * @see de.zbit.Launcher#getYearWhenProjectWasStarted()
   */
  @Override
  public short getYearWhenProjectWasStarted() {
    try {
      return Short.parseShort(baseBundle.getString("INCEPTION_YEAR"));
    } catch (Throwable t) {
      return (short) 2015;
    }
  }


  /* (non-Javadoc)
   * @see de.zbit.Launcher#initGUI(de.zbit.AppConf)
   */
  @Override
  public Window initGUI(AppConf appConf) {
    return new EscherConverterUI(appConf);
  }


  /* (non-Javadoc)
   * @see de.zbit.Launcher#showsGUI()
   */
  @Override
  public boolean showsGUI() {
	  	return !props.containsKey(GUIOptions.GUI) || props.getBooleanProperty(GUIOptions.GUI);
  }

}

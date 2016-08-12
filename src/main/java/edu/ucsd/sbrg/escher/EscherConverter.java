/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of the program BioNetView.
 *
 * Copyright (C) 2013-2016 by the University of California, San Diego.
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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
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
import edu.ucsd.sbrg.escher.converters.Escher2SBGN;
import edu.ucsd.sbrg.escher.converters.Escher2SBML;
import edu.ucsd.sbrg.escher.converters.Escher2Standard;
import edu.ucsd.sbrg.escher.converters.SBGN2Escher;
import edu.ucsd.sbrg.escher.converters.SBML2Escher;
import edu.ucsd.sbrg.escher.gui.EscherConverterUI;
import edu.ucsd.sbrg.escher.model.EscherMap;
import edu.ucsd.sbrg.escher.utilities.EscherIOOptions;
import edu.ucsd.sbrg.escher.utilities.EscherOptions;
import edu.ucsd.sbrg.escher.utilities.EscherOptions.InputFormat;
import edu.ucsd.sbrg.escher.utilities.EscherOptions.OutputFormat;
import edu.ucsd.sbrg.escher.utilities.Validator;

/**
 * @author Andreas Dr&auml;ger
 */
public class EscherConverter extends Launcher {

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
  /**
   * Generated serial version identifier.
   */
  private static final           long
  serialVersionUID =
  9037698164025548416L;


  /**
   * @param converter
   * @param properties
   * @return
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
   * @param map
   * @param format
   * @param properties
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> T convert(EscherMap map, Class<? extends T> format,
    SBProperties properties) {
    if (format.isAssignableFrom(SBMLDocument.class)) {
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
      Escher2Standard<?> converter = configure(new Escher2SBGN(), properties);
      return (T) converter.convert(map);
    }
    return null;
  }


  public static EscherMap convert(Sbgn document, SBProperties
    properties) {
    SBGN2Escher converter = new SBGN2Escher();

    return converter.convert(document);
  }


  public static List<EscherMap> convert(SBMLDocument document, SBProperties properties) {
    SBML2Escher converter = new SBML2Escher();

    return converter.convert(document);
  }

  /**
   * @param input
   * @param format
   * @param properties
   * @return
   * @throws IOException
   * @throws ParseException
   */
  public static <T> T convert(File input, Class<? extends T> format,
    SBProperties properties) throws IOException, ParseException {
    return convert(parseEscherJson(input), format, properties);
  }

  /**
   * Reads an Escher input file in JSON format and returns data structures for
   * an in-memory representation of the information provided.
   * 
   * @param input
   * @return An {@link EscherMap} object representing the information from the
   *         parsed file in memory.
   * @throws IOException
   */
  public static EscherMap parseEscherJson(File input) throws IOException{
    ObjectMapper objectMapper = edu.ucsd.sbrg.escher.utilities.Utils.getObjectMapper();

    logger.info(format(bundle.getString("EscherConverter.readingFile"), input));

    JsonNode escherJson = objectMapper.readTree(input);
    EscherMap meta = objectMapper.treeToValue(escherJson.get(0), EscherMap.class);
    EscherMap map = objectMapper.treeToValue(escherJson.get(1), EscherMap.class);

    map.setId(meta.getId());
    map.setName(meta.getName());
    map.setDescription(meta.getDescription());
    map.setSchema(meta.getSchema());
    map.setURL(meta.getURL());

    map.processMap();

    logger.info(format(bundle.getString("EscherConverter.readingDone"), input));

    return map;
  }

  /**
   * Starts the program.
   *
   * @param args for a description of possible command-line arguments launch with
   *             option -?.
   */
  public static void main(String args[]) {
    // Export validation reports to file for debugging
    new EscherConverter(args);
  }


  /**
   * @param args
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
   * @param input
   * @param output
   * @param properties
   * @throws IOException
   * @throws TransformerException
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws JAXBException
   * @throws XMLStreamException
   * @throws ParseException
   * @throws SBMLException
   */
  public void batchProcess(File input, File output, SBProperties properties)
      throws IOException, SBMLException, ParseException, XMLStreamException,
      JAXBException, SAXException, ParserConfigurationException,
      TransformerException {
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
          output =
              new File(Utils.ensureSlash(output.getAbsolutePath()) + fName);
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
        File
        target =
        new File(
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
      } catch (SBMLException | XMLStreamException | IOException | ParseException | JAXBException | SAXException | ParserConfigurationException | TransformerException exc) {
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
   * @param input
   * @param output
   * @param properties
   * @throws IOException
   * @throws ParseException
   * @throws XMLStreamException
   * @throws SBMLException
   * @throws JAXBException
   * @throws SAXException
   * @throws TransformerException
   * @throws ParserConfigurationException
   */
  public void convert(File input, File output, SBProperties properties)
      throws IOException, ParseException,
      XMLStreamException, SBMLException, JAXBException, SAXException,
      ParserConfigurationException, TransformerException {
    OutputFormat format = OutputFormat.valueOf(properties.getProperty(EscherOptions.FORMAT));
    InputFormat inputFormat = InputFormat.valueOf(properties.get(InputFormat.class.getSimpleName()));

    logger.warning(bundle.getString("ValidationStart"));
    if (!validateInput(input, inputFormat)) {
      logger.warning(bundle.getString("ValidationFailed"));

      // TODO: Replace condition variable to a --ignore-validation variable.
      if (true) {
        logger.warning(bundle.getString("ValidationSkip"));
      }
    }

    boolean success = false;

    switch (format) {
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

      switch (inputFormat) {

      case SBGN:
        EscherMap map = convert(SbgnUtil.readFromFile(input), properties);
        writeEscherJson(map, output);
        success = true;
        break;

      case SBML:
        extractCobraModel(input);
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
  }


  public static boolean extractCobraModel(File file) throws IOException, XMLStreamException {
    if (false) {
      logger.warning(format(bundle.getString("SBMLFBCNotAvailable"), file.getName()));
      return false;
    }
    else {
      logger.info(format(bundle.getString("SBMLFBCInit"), file.getName()));
      // Execute: py3 -c "from cobra import io;
      // io.save_json_model(model=io.read_sbml_model('FILENAME'), file_name='FILENAME')"

      String[] command = {"python3", "-c", "\"from cobra import io;"
          + "io.save_json_model(model=io.read_sbml_model('" + file
          .getAbsolutePath() + "'), file_name='" + file
          .getAbsolutePath() + ".json" + "')\""};
      Process p;
      try {
        p = new ProcessBuilder(command).start();
        p.waitFor();
        if (p.exitValue() == 0) {
          logger.info(format(bundle.getString("SBMLFBCExtractionSuccessful"), file
              .getAbsolutePath(), file.getAbsolutePath()));
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


  private boolean validateInput(File input, InputFormat inputFormat) throws IOException {
    Validator validator;
    try {
      // TODO: Add support for custom schema file.
      validator = new Validator();
    } catch (ProcessingException e) {
      return false;
    }

    //    switch (inputFormat) {
    //    case SBGN:
    //      logger.info(bundle.getString("ValidatingSBGN"));
    //      return validator.validateSbgnml(input);
    //
    //    case SBML:
    //      logger.info(bundle.getString("ValidatingSBML"));
    //      return validator.validateSbmlLE(input);
    //
    //    case Escher:
    //      logger.info(bundle.getString("ValidatingEscher"));
    //      return validator.validateEscher(input);
    //
    //    }
    return false;
  }


  private void writeEscherJson(EscherMap map, File output) throws IOException {

    List<EscherMap> mapList = new ArrayList<>(2);

    mapList.add(new EscherMap());
    mapList.get(0).setId(map.getId());
    mapList.get(0).setDescription(map.getDescription());
    mapList.get(0).setName(output.getName());
    mapList.get(0).setSchema(map.getSchema());
    mapList.get(0).setURL(map.getURL());

    map.setId(null);
    map.setName(null);
    map.setDescription(null);
    map.setSchema(null);
    map.setURL(null);

    mapList.add(map);

    edu.ucsd.sbrg.escher.utilities.Utils.getObjectMapper().writeValue(output, mapList);

  }


  private void writeEscherJson(List<EscherMap> mapList, File output) {
    try {
      if (mapList.size() == 0) {
        logger.warning(bundle.getString("SBMLNoLayout"));
        return;
      }
      if (output.exists() && output.isFile()) {
        if (mapList.size() == 1) {
          writeEscherJson(mapList.get(0), output);
        }
        else {
          logger.severe(bundle.getString("SingleFileMultipleLayout"));
        }
      }
      else {
        mapList.forEach(map -> {
          File file = new File(Utils.ensureSlash(output.getPath()) + map.getId() + ".json");
          try {
            if (!file.exists()) {
              file.getParentFile().mkdirs();
              file.createNewFile();
            }
            writeEscherJson(map, file);
          } catch (IOException e) {
            logger.severe(format(bundle.getString("FileIOError"), file.getAbsolutePath()));
          }
        });
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
    return props.containsKey(GUIOptions.GUI) && props.getBooleanProperty(GUIOptions.GUI);
  }

}

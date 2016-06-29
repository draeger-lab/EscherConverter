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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.*;
import de.zbit.AppConf;
import de.zbit.Launcher;
import de.zbit.gui.GUIOptions;
import de.zbit.io.FileTools;
import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.util.ResourceManager;
import de.zbit.util.Utils;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.SBProperties;
import edu.ucsd.sbrg.escher.converters.*;
import edu.ucsd.sbrg.escher.gui.EscherConverterUI;
import edu.ucsd.sbrg.escher.model.EscherMap;
import edu.ucsd.sbrg.escher.utilities.EscherIOOptions;
import edu.ucsd.sbrg.escher.utilities.EscherOptions;
import edu.ucsd.sbrg.escher.utilities.EscherOptions.OutputFormat;
import org.json.simple.parser.ParseException;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Sbgn;
import org.sbgn.schematron.Issue;
import org.sbgn.schematron.SchematronValidator;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.SBMLWriter;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

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
    converter.setLabelHeight(
        properties.getDoubleProperty(EscherOptions.LABEL_HEIGHT));
    converter
        .setLabelWidth(properties.getDoubleProperty(EscherOptions.LABEL_WIDTH));
    converter.setPrimaryNodeHeight(
        properties.getDoubleProperty(EscherOptions.PRIMARY_NODE_HEIGHT));
    converter.setPrimaryNodeWidth(
        properties.getDoubleProperty(EscherOptions.PRIMARY_NODE_WIDTH));
    converter.setReactionNodeRatio(
        properties.getDoubleProperty(EscherOptions.REACTION_NODE_RATIO));
    converter.setSecondaryNodeRatio(
        properties.getDoubleProperty(EscherOptions.SECONDARY_NODE_RATIO));
    converter.setInferCompartmentBoundaries(
        properties.getBooleanProperty(EscherOptions.INFER_COMPARTMENT_BOUNDS));
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
      converter.setCanvasDefaultHeight(
          properties.getDoubleProperty(EscherOptions.CANVAS_DEFAULT_HEIGHT));
      converter.setCanvasDefaultWidth(
          properties.getDoubleProperty(EscherOptions.CANVAS_DEFAULT_WIDTH));
      converter.setDefaultCompartmentId(
          properties.getProperty(EscherOptions.COMPARTMENT_ID));
      converter.setDefaultCompartmentName(
          properties.getProperty(EscherOptions.COMPARTMENT_NAME));
      converter.setLayoutId(properties.getProperty(EscherOptions.LAYOUT_ID));
      converter
          .setLayoutName(properties.getProperty(EscherOptions.LAYOUT_NAME));
      converter
          .setNodeDepth(properties.getDoubleProperty(EscherOptions.NODE_DEPTH));
      converter.setNodeLabelHeight(
          properties.getDoubleProperty(EscherOptions.NODE_LABEL_HEIGHT));
      converter.setZ(properties.getDoubleProperty(EscherOptions.Z));
      return (T) converter.convert(map);
    } else if (format.isAssignableFrom(Sbgn.class)) {
      Escher2Standard<?> converter = configure(new Escher2SBGN(), properties);
      return (T) converter.convert(map);
    }
    return null;
  }


  /**
   * @param input
   * @param format
   * @param properties
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   * @throws ParseException
   */
  public static <T> T convert(File input, Class<? extends T> format,
      SBProperties properties)
      throws IOException, ParseException {

    return convert(parseEscherJson(input), format, properties);
  }

  public static EscherMap parseEscherJson(File input) throws IOException{
    ObjectMapper objectMapper = new ObjectMapper();

    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    objectMapper.disable(MapperFeature.AUTO_DETECT_SETTERS);
    objectMapper.disable(MapperFeature.AUTO_DETECT_CREATORS);
    objectMapper.disable(MapperFeature.AUTO_DETECT_FIELDS);
    objectMapper.disable(MapperFeature.AUTO_DETECT_GETTERS);
    objectMapper.disable(MapperFeature.AUTO_DETECT_IS_GETTERS);
    objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
    objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE);

    logger.info(MessageFormat
        .format(bundle.getString("EscherConverter.readingFile"), input));

    JsonNode escherJson = objectMapper.readTree(input);
    EscherMap meta = objectMapper.treeToValue(escherJson.get(0), EscherMap.class);
    EscherMap map = objectMapper.treeToValue(escherJson.get(1), EscherMap.class);

    map.setId(meta.getId());
    map.setName(meta.getName());
    map.setDescription(meta.getDescription());
    map.setSchema(meta.getSchema());
    map.setURL(meta.getURL());

    map.processMap();

    logger.info(MessageFormat
        .format(bundle.getString("EscherConverter.readingDone"), input));

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
    if (!output.exists() && !output.isFile() && !(input.isFile() && input
        .getName().equals(output.getName()))) {
      logger.info(MessageFormat
          .format(bundle.getString("EscherConverter.cratingDir"),
              output.getAbsolutePath()));
      output.mkdir();
    }
    if (input.isFile()) {
      if (SBFileFilter.isJSONFile(input)) {
        if (output.isDirectory()) {
          String fName = input.getName();
          fName = FileTools.removeFileExtension(fName) + ".xml";
          output =
              new File(Utils.ensureSlash(output.getAbsolutePath()) + fName);
        }
        convert(input, output, properties);
        logger.info(MessageFormat
            .format("Output successfully written to file {0}.", output));
      }
      else {
        convert(input, output, properties);
      }
    } else {
      if (!output.isDirectory()) {
        throw new IOException(MessageFormat
            .format(bundle.getString("EscherConverter.cannotWriteToFile"),
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
    SBProperties properties = appConf.getCmdArgs();
    if (properties.containsKey(EscherIOOptions.INPUT) && properties
        .containsKey(EscherIOOptions.OUTPUT)) {
      try {
        File
            input =
            new File(properties.getProperty(EscherIOOptions.INPUT.toString()));
        File
            output =
            new File(properties.getProperty(EscherIOOptions.OUTPUT.toString()));
        if (input.isDirectory()) {
          logger.info(MessageFormat.format(
              bundle.getString("EscherConverter.launchingBatchProcessing"),
              input.getAbsolutePath()));
        }
        // Can also be used if only a single file is to be converted:
        batchProcess(input, output, properties);
      } catch (SBMLException | XMLStreamException | IOException | ParseException | JAXBException | SAXException | ParserConfigurationException | TransformerException exc) {
        exc.printStackTrace();
      }
    } else {
      logger.warning(bundle.getString("EscherConverter.incompleteCMDArgs"));
    }
  }


  /**
   * @param input
   * @param output
   * @param properties
   * @throws FileNotFoundException
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
    OutputFormat
        format =
        OutputFormat.valueOf(properties.getProperty(EscherOptions.FORMAT));
    if (output.isDirectory()) {
      String fileName = input.getName();
      String extension = format.toString().toLowerCase();
      output =
          new File(Utils.ensureSlash(output.getAbsolutePath()) + fileName
              .substring(0,
                  fileName.length() - FileTools.getExtension(fileName).length())
              + extension);
    }
    switch (format) {
    case SBML:
      SBMLDocument doc = convert(input, SBMLDocument.class, properties);
      SBMLWriter.write(doc, output, System.getProperty("app.name"),
          getVersionNumber(), ' ', (short) 2);
      break;
    case SBGN:
      Sbgn sbgn = convert(input, Sbgn.class, properties);
      SbgnUtil.writeToFile(sbgn, output);
      // TODO: Validate file.
      List<Issue> issues = SchematronValidator.validate(output);
      if (issues.size() > 0) {
        // print each issue individually.
        logger.warning(MessageFormat
            .format(bundle.getString("EscherConverter.validationErrors"),
                issues.size()));
        for (Issue issue : issues) {
          logger.warning(issue.toString());
        }
      }
      break;

    case Escher:
      SBML2Escher converter = new SBML2Escher();
      EscherMap map = converter.convert(SBMLReader.read(input));
      writeEscherJson(map, output);
      break;

    default:
      break;
    }
  }


  private void writeEscherJson(EscherMap map, File output) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();

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

    objectMapper.writeValue(output, mapList);

  }


  /* (non-Javadoc)
   * @see de.zbit.Launcher#getCmdLineOptions()
   */
  @Override
  public List<Class<? extends KeyProvider>> getCmdLineOptions() {
    List<Class<? extends KeyProvider>>
        list =
        new ArrayList<Class<? extends KeyProvider>>(3);
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
    List<Class<? extends KeyProvider>>
        list =
        new ArrayList<Class<? extends KeyProvider>>(1);
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
    return "@app.version@";
  }


  /* (non-Javadoc)
   * @see de.zbit.Launcher#getYearOfProgramRelease()
   */
  @Override
  public short getYearOfProgramRelease() {
    return 2015;
  }


  /* (non-Javadoc)
   * @see de.zbit.Launcher#getYearWhenProjectWasStarted()
   */
  @Override
  public short getYearWhenProjectWasStarted() {
    return 2014;
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
    if (getAppConf().getCmdArgs().containsKey(GUIOptions.GUI)) {
      return getAppConf().getCmdArgs().getBoolean(GUIOptions.GUI);
    }
    return false;
  }
}

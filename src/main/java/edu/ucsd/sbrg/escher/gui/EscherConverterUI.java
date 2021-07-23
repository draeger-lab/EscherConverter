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
package edu.ucsd.sbrg.escher.gui;

import static java.text.MessageFormat.format;

import java.awt.Component;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import org.sbgn.bindings.Sbgn;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.util.ResourceManager;

import de.zbit.AppConf;
import de.zbit.gui.BaseFrame;
import de.zbit.gui.GUIOptions;
import de.zbit.gui.GUITools;
import de.zbit.gui.JTabbedPaneDraggableAndCloseable;
import de.zbit.io.OpenedFile;
import de.zbit.io.filefilter.GeneralFileFilter;
import de.zbit.io.filefilter.SBFileFilter;
import de.zbit.sbml.gui.SBMLWritingTask;
import de.zbit.util.prefs.SBPreferences;
import de.zbit.util.prefs.SBProperties;
import edu.ucsd.sbrg.escher.EscherConverter;
import edu.ucsd.sbrg.escher.model.EscherMap;
import edu.ucsd.sbrg.escher.util.EscherIOOptions;
import edu.ucsd.sbrg.escher.util.EscherOptions;

/**
 * 
 * Main class of the User Interface of EscherConverter
 * @author Andreas Dr&auml;ger
 */
public class EscherConverterUI extends BaseFrame {

  /**
   * A {@link Logger} for this class.
   */
  private static final transient Logger
  logger = Logger.getLogger(EscherConverterUI.class.getName());
  /**
   * Localization support.
   */
  public static final            ResourceBundle
  bundle = ResourceManager.getBundle("edu.ucsd.sbrg.escher.Messages");
  /**
   * Program icons
   */
  private static List<Image> icons;

  /*
   * Initialize icons and images.
   */
  static {
    final String iconName = "escher-logo_";
    int resolutions[] = new int[] {16, 32, 48, 128, 256};
    for (int resolution : resolutions) {
      String key = iconName + resolution;
      URL url = EscherConverterUI.class.getResource(key + ".png");
      if (url != null) {
        UIManager.put(key, new ImageIcon(url));
        if (UIManager.getIcon(key) == null) {
          logger.warning(format(bundle.getString("EscherConverterUI.couldNotLoadImage"), key));
        }
      } else {
        logger.warning(format(bundle.getString("EscherConverterUI.invalidURL"), key));
      }
    }
    icons = new LinkedList<Image>();
    for (int res : resolutions) {
      Object icon = UIManager.get(iconName + res);
      if ((icon != null) && (icon instanceof ImageIcon)) {
        icons.add(((ImageIcon) icon).getImage());
      }
    }
    String image = "SBRG_385x54";
    URL url = EscherConverterUI.class.getResource("/edu/ucsd/sbrg/" + image + ".png");
    if (url != null) {
      UIManager.put(image, new ImageIcon(url));
      UIManager.put("UT_WBMW_mathnat_4C_380x45", UIManager.getIcon(image));
    }
    String imageName = "EscherWatermark";
    url = EscherConverterUI.class.getResource(imageName + ".png");
    if (url != null) {
      UIManager.put(imageName, new ImageIcon(url));
    }
  }

  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = 3462628903511253555L;
  /**
   * List in case more than one file is open at the same time.
   */
  private List<OpenedFile<EscherMap>> listOfOpenedFiles;
  /**
   * The pane containing all tabs for different files.
   */
  private JTabbedPane                 tabbedPane;


  /** Starts the user interface.
   * @param appConf
   */
  public EscherConverterUI(AppConf appConf) {
    super(appConf);
    setIconImages(icons);
    listOfOpenedFiles = new ArrayList<OpenedFile<EscherMap>>();
    SBProperties props = appConf.getCmdArgs();
    if (props.containsKey(EscherIOOptions.INPUT)) {
      openFile(new File(props.get(EscherIOOptions.INPUT)));
    }
  }


  /**
   * Checks whether a given file can be written into.
   * @param f The file which is to be checked
   * @return The file if it can be written into, null otherwise.
   */
  private File checkFile(File f) {
    if (f.exists()) {
      if (!f.canWrite()) {
        GUITools.showNowWritingAccessWarning(this, f);
      } else if (!f.exists() || (GUITools.overwriteExistingFile(this, f))) {
        return f;
      } else {
        return f;
      }
    } else {
      return f;
    }
    return null;
  }


  /* (non-Javadoc)
   * @see de.zbit.UserInterface#closeFile()
   */
  @Override
  public boolean closeFile() {
    if (tabbedPane.getTabCount() > 0) {
      tabbedPane.remove(tabbedPane.getSelectedIndex());
      return true;
    }
    return false;
  }


  /* (non-Javadoc)
   * @see de.zbit.gui.BaseFrame#createJToolBar()
   */
  @Override
  protected JToolBar createJToolBar() {
    return createDefaultToolBar();
  }


  /* (non-Javadoc)
   * @see de.zbit.gui.BaseFrame#createMainComponent()
   */
  @Override
  protected Component createMainComponent() {
    Icon icon = UIManager.getIcon("EscherWatermark");
    tabbedPane = new JTabbedPaneDraggableAndCloseable((ImageIcon) icon);
    tabbedPane.addChangeListener(evt -> {
      GUITools.setEnabled(tabbedPane.getTabCount() > 0, getJMenuBar(),
        getJToolBar(), BaseAction.FILE_CLOSE, BaseAction.FILE_SAVE_AS);
    });
    return tabbedPane;
  }


  /* (non-Javadoc)
   * @see de.zbit.UserInterface#getURLAboutMessage()
   */
  @Override
  public URL getURLAboutMessage() {
    return getClass().getResource(bundle.getString("EscherConverterUI.About"));
  }


  /* (non-Javadoc)
   * @see de.zbit.UserInterface#getURLLicense()
   */
  @Override
  public URL getURLLicense() {
    return getClass().getResource(bundle.getString("EscherConverterUI.License"));
  }


  /* (non-Javadoc)
   * @see de.zbit.UserInterface#getURLOnlineHelp()
   */
  @Override
  public URL getURLOnlineHelp() {
    return getClass().getResource(bundle.getString("EscherConverterUI.Help"));
  }


  /* 
   * This essentially calls the file chooser, checks the opened files and calls a worker to read the files
   * @param files Only applicable if files are provided via the command line
   * (non-Javadoc)
   * @see de.zbit.gui.BaseFrame#openFile(java.io.File[])
   */
  @Override
  protected File[] openFile(File... files) {
	// Create filters for all three possible input types
    GeneralFileFilter filterJSON = SBFileFilter.createJSONFileFilter();
    GeneralFileFilter filterSBML = SBFileFilter.createSBMLFileFilter();
    GeneralFileFilter filterSBGN = SBFileFilter.createSBGNFileFilter();
    
    // if this is true, no input files were given via the command line, therefore a file chooser needs to be opened
    if ((files == null) || (files.length == 0)) {
      SBPreferences prefs = SBPreferences.getPreferencesFor(GUIOptions.class);
      files = GUITools.openFileDialog(this, prefs.get(GUIOptions.OPEN_DIR), false, true, JFileChooser.FILES_AND_DIRECTORIES, filterJSON, filterSBML, filterSBGN);
    }
    
    // if files are provided, they are checked
    if ((files != null) && (files.length > 0)) {
      List<File> accepted = new LinkedList<File>();
      List<File> notAccepted = new LinkedList<File>();
      for (File file : files) {
        if (filterJSON.accept(file) || filterSBML.accept(file) || filterSBGN.accept(file)) {
          // TODO: ignore empty files here?
          accepted.add(file);
        } else {
          notAccepted.add(file);
        }
      }
      files = accepted.toArray(new File[0]);
      EscherParserWorker worker = new EscherParserWorker(this, files);
      worker.addPropertyChangeListener(evt -> {
        if (evt.getPropertyName().equals(EscherParserWorker.INTERMERIM_RESULTS)) {
          @SuppressWarnings("unchecked") List<OpenedFile<EscherMap>>
          list = (List<OpenedFile<EscherMap>>) evt.getNewValue();
          for (OpenedFile<EscherMap> openedFile : list) {
            // TODO: add to GUI
            logger.info(openedFile.getFile().getAbsolutePath());
            
            // takes care that tabs have correct names
            String title = openedFile.getDocument().getName();
            if ((title == null) || (title.length() == 0)) {
              title = openedFile.getDocument().getId();
              if ((title == null) || (title.length() == 0)) {
                title = openedFile.getFile().getName();
              }
            }
            SBPreferences prefs = SBPreferences.getPreferencesFor(EscherOptions.class);
            // TODO: does not work when loading an SBML file.
            tabbedPane.addTab(title, new EscherMapDisplay(openedFile, prefs.toProperties()));
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
          }
          listOfOpenedFiles.addAll(list);
        }
      });
      
      // this executes doInBackground in EscherParserWorker 
      worker.execute();
      if (!notAccepted.isEmpty()) {
        logger.warning(format(
          "Unable to open {0,choice,1#file|1<the {0,number,integer} files} {1}.",
          notAccepted.toString()));
      }
    }
    return files;
  }


  /* (non-Javadoc)
   * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
  }


  /**
   * Calls the correct writer depending on the format the file is to be saved as
   * @param result      can be {@link EscherMap}, {@link SBMLDocument}, or {@link Sbgn}
   * @param destination File in which the result is to be saved
   * @throws FileNotFoundException
   */
  private <T> void saveFile(final T result, final File destination)
      throws FileNotFoundException {
    SwingWorker<?, ?> writer;
    if (result instanceof EscherMap) {
      writer = new SwingWorker<File, Void>() {
        /* (non-Javadoc)
         * @see javax.swing.SwingWorker#doInBackground()
         */
        @Override
        protected File doInBackground() throws Exception {
          try {
            EscherConverter.writeEscherJson((EscherMap) result, destination);
          } catch (Throwable t) {
            t.printStackTrace();
            throw t;
          }
          return destination;
        }
      };

    } else if (result instanceof Sbgn) {
      writer = new SBGNWritingTask(new OpenedFile<Sbgn>(destination, (Sbgn) result));
    } else if (result instanceof SBMLDocument) {
      writer = new SBMLWritingTask(
        new OpenedFile<SBMLDocument>(destination, (SBMLDocument) result),
        this);
    } else {
      throw new IllegalArgumentException(format(
        bundle.getString("EscherConverterWorker.unknownFormat"),
        result.getClass().getName()));
    }
    writer.addPropertyChangeListener(evt -> {
      if (evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
        logger.info(format(bundle.getString("EscherConverterUI.fileWritten"),  destination));
      }
    });
    writer.execute();
  }


  /* 
   * Calls a file chooser for saving. In case the output is SBML, it is converted again.
   * The file is then saved with the correct output extension.
   * (non-Javadoc)
   * @see de.zbit.gui.BaseFrame#saveFileAs()
   */
  @Override
  public File saveFileAs() {
    String dir = SBPreferences.getPreferencesFor(GUIOptions.class).get(GUIOptions.SAVE_DIR);
    
    // make sure the extension filter corresponds to the preferred output format
    String format = SBPreferences.getPreferencesFor(EscherOptions.class).get(EscherOptions.FORMAT);
    GeneralFileFilter filter = null;
    switch (format){
    case "SBGN": filter = SBFileFilter.createSBGNFileFilter(); break;
    case "JSON": filter = SBFileFilter.createJSONFileFilter(); break;
    default: filter = SBFileFilter.createSBMLFileFilterL3V1(); break;
    }
    JFileChooser fc = GUITools.createJFileChooser(dir, false, false, JFileChooser.FILES_ONLY, filter);
    File savedFile = null;
    if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        savedFile = checkFile(fc.getSelectedFile());
      if ((savedFile != null) && (savedFile.getParentFile() != null)) {
        // Do conversion
        EscherMapDisplay display = (EscherMapDisplay) tabbedPane.getSelectedComponent();
        EscherMap map = display.getOpenedFile().getDocument();
        
        String filename = fc.getSelectedFile().getAbsolutePath().toString();
        
        if (!SBFileFilter.isJSONFile(fc.getSelectedFile())) {
          EscherConverterWorker<?> converter;
          if (fc.getFileFilter().getDescription().toUpperCase().contains("SBGN")) {
            converter = new EscherConverterWorker<Sbgn>(map, Sbgn.class,
                SBPreferences.getPreferencesFor(EscherOptions.class).toProperties());
            
            // make sure that pathname ends with .sbgn or .xml
            if (!filename.toLowerCase().endsWith(".sbgn") && !filename.toLowerCase().endsWith(".xml"))
                   filename += ".sbgn";
            savedFile = checkFile(new File(filename));
            
            /*} else if (fc.getFileFilter().getDescription().toUpperCase().contains("PNG")) {
          // TODO: save as image!!!
             */
          } else /* SBML */ {
            // Always do a new conversion because the in-memory SBMLDocument might
            // have been changed in order to improve the display. Remember, this
            // is also our internal data structure and hence might need to be
            // changed when necessary; it is not a 1:1 mapping from the original
            // map! Hence, even though we have already an SBMLDocument in memory
            // we still need to do a fresh conversion.
            converter = new EscherConverterWorker<SBMLDocument>(map, SBMLDocument.class,
                SBPreferences.getPreferencesFor(EscherOptions.class).toProperties());
            
            // make sure that pathname ends with .sbml or .xml
            if (!filename.toLowerCase().endsWith(".sbml") && !filename.toLowerCase().endsWith(".xml"))
                   filename += ".sbml";
            savedFile = checkFile(new File(filename));
          }
          
          final File output = savedFile;
          final Component window = this;
          converter.addPropertyChangeListener(evt -> {
            if (evt.getNewValue().equals(SwingWorker.StateValue.DONE)) {
              try {
                saveFile(((EscherConverterWorker<?>) evt.getSource()).get(), output);
              } catch (FileNotFoundException | InterruptedException | ExecutionException exc) {
                GUITools.showErrorMessage(window, exc);
              }
            }
          });
          converter.execute();
        } else {
        	// make sure that pathname ends with .json
           if (!filename.toLowerCase().endsWith(".json"))
                 filename += ".json";
           savedFile = checkFile(new File(filename));
          // Just save the JSON map, nothing to do.
          try {
            saveFile(map, savedFile);
          } catch (FileNotFoundException exc) {
            GUITools.showErrorMessage(this, exc);
          }
        }
      }
    }
    return savedFile;
  }

}

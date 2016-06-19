/*
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
package edu.ucsd.sbrg.escher.utilities;

import de.zbit.util.objectwrapper.ValuePairUncomparable;
import de.zbit.util.prefs.KeyProvider;
import de.zbit.util.prefs.Option;
import de.zbit.util.prefs.OptionGroup;
import de.zbit.util.prefs.Range;
import org.sbml.jsbml.util.ResourceManager;

import java.util.ResourceBundle;

/**
 * These options influence how the output will be created and how the Escher
 * maps are interpreted.
 *
 * @author Andreas Dr&auml;ger
 */
public interface EscherOptions extends KeyProvider {

  /**
   * Helper constant for internal use.
   */
  Range<OutputFormat>
                                     SBML_CONDITION =
      new Range<OutputFormat>(OutputFormat.class, OutputFormat.SBML);
  /**
   * Localization support.
   */
  ResourceBundle
                                     bundle         =
      ResourceManager.getBundle("Messages");


  /**
   * Enumeration of allowable output file formats.
   *
   * @author Andreas Dr&auml;ger
   */
  enum OutputFormat {
    /**
     * Systems Biology Graphical Notation Markup Language (SBGN-ML).
     */
    SBGN,
    /**
     * Systems Biology Markup Language with layout extension.
     */
    SBML,
    /**
     * Escher JSON format.
     */
    Escher
  }


  /**
   * This converter can infer where the boundaries of compartments could be
   * drawn. To this end, it uses each node's BiGG ids to identify the
   * compartment of all metabolites. Assuming that compartments have rectangular
   * shapes, the algorithm can find the outermost node on each side of the box
   * and hence obtain the boundaries of the compartment. However, this methods
   * will fail when metabolites are drawn inside of such a box that belong to a
   * different compartment that is actually further outside. For this reason,
   * this option is deactivated by default.
   */
  Option<Boolean>
                                           INFER_COMPARTMENT_BOUNDS =
      new Option<Boolean>("INFER_COMPARTMENT_BOUNDS", Boolean.class, bundle,
          Boolean.FALSE);
  /**
   * The desired format for the conversion, e.g., SBML.
   */
  Option<OutputFormat>
                                           FORMAT                   =
      new Option<OutputFormat>("FORMAT", OutputFormat.class, bundle,
          new Range<OutputFormat>(OutputFormat.class,
              Range.toRangeString(OutputFormat.class)), OutputFormat.SBML);
  /**
   * This value is used when no width has been defined for the canvas. Since
   * the width attribute is mandatory for the layout, a default value must be
   * provided in these cases.
   */
  Option<Double>
                                           CANVAS_DEFAULT_WIDTH     =
      new Option<Double>("CANVAS_DEFAULT_WIDTH", Double.class, bundle,
          new Range<Double>(Double.class, "{[1,1E9]}"), 250d);
  /**
   * Just as in the case of the width of the canvas, this value needs to be
   * specified for cases where the JSON input file lacks an explicit
   * specification of the canvas height.
   */
  Option<Double>
                                           CANVAS_DEFAULT_HEIGHT    =
      new Option<Double>("CANVAS_DEFAULT_HEIGHT", Double.class, bundle,
          new Range<Double>(Double.class, "{[1,1E9]}"), 250d);
  /**
   * A compartment needs to have a unique identifier, which needs to be a
   * machine-readable Sting that must start with a letter or underscore and
   * can only contain ASCII characters. Since the JSON file does not provide
   * this information, this option allows you to specify the required
   * identifier.
   */
  @SuppressWarnings("unchecked")
                      Option<String>
                                           COMPARTMENT_ID           =
      new Option<String>("COMPARTMENT_ID", String.class, bundle,
          bundle.getString("COMPARTMENT_ID_DEFAULT_VALUE"),
          new ValuePairUncomparable<Option<OutputFormat>, Range<OutputFormat>>(
              FORMAT, SBML_CONDITION));
  /**
   * With this option it is possible to define a name for the default
   * compartment can be that needs to be generated for the conversion to SBML.
   * The name does not have any restrictions, i.e., any UTF-8 character can be
   * used.
   */
  @SuppressWarnings("unchecked")
                      Option<String>
                                           COMPARTMENT_NAME         =
      new Option<String>("COMPARTMENT_NAME", String.class, bundle,
          bundle.getString("COMPARTMENT_NAME_DEFAULT_VALUE"),
          new ValuePairUncomparable<Option<OutputFormat>, Range<OutputFormat>>(
              FORMAT, SBML_CONDITION));
  /**
   * This should be a human-readable name for the layout that is to be
   * created. This name might be displayed to describe the figure and should
   * therefore be explanatory.
   */
  @SuppressWarnings("unchecked")
                      Option<String>
                                           LAYOUT_NAME              =
      new Option<String>("LAYOUT_NAME", String.class, bundle,
          bundle.getString("LAYOUT_NAME_DEFAULT_VALUE"),
          new ValuePairUncomparable<Option<OutputFormat>, Range<OutputFormat>>(
              FORMAT, SBML_CONDITION));
  /**
   * In contrast to the name, this identifier does not have to be
   * human-readable. This is a machine identifier, which must start with a
   * letter or underscore and can only contain ASCII characters.
   */
  @SuppressWarnings("unchecked")
                      Option<String>
                                           LAYOUT_ID                =
      new Option<String>("LAYOUT_ID", String.class, bundle,
          bundle.getString("LAYOUT_ID_DEFAULT_NAME"),
          new ValuePairUncomparable<Option<OutputFormat>, Range<OutputFormat>>(
              FORMAT, SBML_CONDITION));
  /**
   * This option defines the width of bounding boxes for text labels.
   */
  Option<Double>
                                           LABEL_WIDTH              =
      new Option<Double>("LABEL_WIDTH", Double.class, bundle,
          new Range<Double>(Double.class, "{[1,1E9]}"), 160d);
  /**
   * With this option you can specify the height of the bounding box of text
   * labels.
   */
  Option<Double>
                                           LABEL_HEIGHT             =
      new Option<Double>("LABEL_HEIGHT", Double.class, bundle,
          new Range<Double>(Double.class, "{[1,1E9]}"), 50d);
  /**
   * The length of nodes along z-coordinate. Escher maps are actually
   * two-dimensional, but in general, a layout can be three-dimensional. This
   * value should be an arbitrary value greater than zero, because some
   * rendering engines might not display the node if its depth is zero.
   */
  @SuppressWarnings("unchecked")
                      Option<Double>
                                           NODE_DEPTH               =
      new Option<Double>("NODE_DEPTH", Double.class, bundle,
          new Range<Double>(Double.class, "{[1,1E9]}"), 1d,
          new ValuePairUncomparable<Option<OutputFormat>, Range<OutputFormat>>(
              FORMAT, SBML_CONDITION));
  /**
   * The position on the z-axis where the entire two-dimensional graph should
   * be drawn.
   */
  @SuppressWarnings("unchecked")
                      Option<Double>
                                           Z                        =
      new Option<Double>("Z", Double.class, bundle,
          new Range<Double>(Double.class, "{[-1E9,1E9]}"), 0d,
          new ValuePairUncomparable<Option<OutputFormat>, Range<OutputFormat>>(
              FORMAT, SBML_CONDITION));
  /**
   * Node labels can have a size different from general labels in the graph.
   * Here you can specify how height the bounding box of the labels for nodes
   * should be.
   */
  Option<Double>
                                           NODE_LABEL_HEIGHT        =
      new Option<Double>("NODE_LABEL_HEIGHT", Double.class, bundle,
          new Range<Double>(Double.class, "{[1,1E9]}"), 20d);
  /**
   * This option allows you to specify the height of labels for reactions.
   * This value can be different from other labels in the network.
   */
  Option<Double>
                                           REACTION_LABEL_HEIGHT    =
      new Option<Double>("REACTION_LABEL_HEIGHT", Double.class, bundle,
          new Range<Double>(Double.class, "{[1,1E9]}"), 30d);
  /**
   * Escher maps distinguish between primary and secondary nodes. Primary
   * nodes should be larger than secondary nodes and display the main flow of
   * matter through the network. This option allows you to specify the width
   * of primary nodes.
   */
  Option<Double>
                                           PRIMARY_NODE_WIDTH       =
      new Option<Double>("PRIMARY_NODE_WIDTH", Double.class, bundle,
          new Range<Double>(Double.class, "{[1,1E9]}"), 30d);
  /**
   * The primary node should be bigger than the secondary node. With this
   * option you can specify the height of this type of nodes.
   */
  Option<Double>
                                           PRIMARY_NODE_HEIGHT      =
      new Option<Double>("PRIMARY_NODE_HEIGHT", Double.class, bundle,
          new Range<Double>(Double.class, "{[1,1E9]}"), 30d);
  /**
   * This value is used as a conversion factor to determine the size of the
   * reaction display box depending on the size of primary nodes. Height and
   * width of reaction nodes are determined by dividing the corresponding
   * values from the primary node size by this factor.
   */
  Option<Double>
                                           REACTION_NODE_RATIO      =
      new Option<Double>("REACTION_NODE_RATIO", Double.class, bundle,
          new Range<Double>(Double.class, "{[0,1]}"), 1d / 2d);
  /**
   * Similar to the reaction node ratio, the size of secondary nodes (width
   * and height) is determined by dividing the corresponding values from the
   * primary nodes by this value.
   */
  Option<Double>
                                           SECONDARY_NODE_RATIO     =
      new Option<Double>("SECONDARY_NODE_RATIO", Double.class, bundle,
          new Range<Double>(Double.class, "{[0,1]}"), 2d / 3d);
  //TODO: include styles (color etc.)
  /* *
   * These options allow you to customize the appearance of network elements.
   */
  //public static final OptionGroup<?> GROUP_STYLES = new OptionGroup<Object>("GROUP_STYLES", bundle, );
  /**
   * The options in this group allow you to influence how large certain
   * elements should be displayed.
   */
  @SuppressWarnings("unchecked")
                      OptionGroup<?>
                                           GROUP_LAYOUT             =
      new OptionGroup<Object>("GROUP_LAYOUT", bundle, CANVAS_DEFAULT_HEIGHT,
          CANVAS_DEFAULT_WIDTH, LABEL_HEIGHT, LABEL_WIDTH, NODE_DEPTH,
          NODE_LABEL_HEIGHT, PRIMARY_NODE_HEIGHT, PRIMARY_NODE_WIDTH,
          REACTION_LABEL_HEIGHT, REACTION_NODE_RATIO, SECONDARY_NODE_RATIO, Z);
  /**
   * Here you can influence how elements in the layout are called or
   * identified.
   */
  @SuppressWarnings("unchecked")
                      OptionGroup<?>
                                           GROUP_NAMING             =
      new OptionGroup<Object>("GROUP_NAMING", bundle, FORMAT, LAYOUT_ID,
          LAYOUT_NAME, COMPARTMENT_ID, COMPARTMENT_NAME,
          INFER_COMPARTMENT_BOUNDS);
}

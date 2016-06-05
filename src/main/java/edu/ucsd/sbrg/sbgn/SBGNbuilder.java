/**
 *
 */
package edu.ucsd.sbrg.sbgn;

import de.zbit.graph.io.def.SBGNProperties.ArcType;
import de.zbit.graph.io.def.SBGNProperties.GlyphOrientation;
import de.zbit.graph.io.def.SBGNProperties.GlyphType;
import org.sbgn.bindings.*;
import org.sbgn.bindings.Arc.End;
import org.sbgn.bindings.Arc.Next;
import org.sbgn.bindings.Arc.Start;
import org.sbgn.bindings.Glyph.Callout;
import org.sbgn.bindings.SBGNBase.Notes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;

/**
 * A helper class that facilitates the creation of SBGN map objects. This class
 * wraps an instance of {@link ObjectFactory}, which is used to create all the
 * elements. For this reason, the individual methods are not static, because
 * they all rely on one specific {@link ObjectFactory}.
 *
 * @author Andreas Dr&auml;ger
 */
public class SBGNbuilder {

  /**
   * Enumeration of SBGN languages.
   *
   * @author Andreas Dr&auml;ger
   */
  public enum Language {
    /**
     *
     */
    activity_flow,
    /**
     *
     */
    entity_relationship,
    /**
     *
     */
    process_description;


    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
      return super.toString().replace('_', ' ');
    }
  }


  /**
   * @param value
   * @return
   */
  public static double toDouble(Double value) {
    return value == null ? Double.NaN : value.doubleValue();
  }


  private java.util.Map<String, SBGNBase> id2element;
  /**
   *
   */
  private ObjectFactory                   objectFactory;


  /**
   *
   */
  public SBGNbuilder() {
    super();
    objectFactory = new ObjectFactory();
    id2element = new HashMap<String, SBGNBase>();
  }


  /**
   * @param id
   * @param base
   */
  private void checkId(String id, SBGNBase base) {
    if (id2element.containsKey(id)) {
      throw new IllegalArgumentException(
          MessageFormat.format("Duplicate id ''{0}''", id));
    }
    id2element.put(id, base);
  }


  /**
   * @param id
   * @param source
   * @param target
   * @param arcType
   * @return
   */
  public Arc createArc(String id, SBGNBase source, SBGNBase target,
      ArcType arcType) {
    Arc arc = objectFactory.createArc();
    checkId(id, arc);
    arc.setId(id);
    arc.setSource(source);
    arc.setTarget(target);
    arc.setClazz(arcType.toString());
    return arc;
  }


  /**
   * @param x
   * @param y
   * @return
   */
  public End createArcEnd(double x, double y) {
    End end = objectFactory.createArcEnd();
    end.setX((float) x);
    end.setY((float) y);
    return end;
  }


  /**
   * @param x
   * @param y
   * @return
   */
  public Next createArcNext(double x, double y) {
    Next next = objectFactory.createArcNext();
    next.setX((float) x);
    next.setY((float) y);
    return next;
  }


  /**
   * @param x
   * @param y
   * @return
   */
  public Start createArcStart(double x, double y) {
    Start start = objectFactory.createArcStart();
    start.setX((float) x);
    start.setY((float) y);
    return start;
  }


  /**
   * @param x
   * @param y
   * @param width
   * @param height
   * @return
   */
  public Bbox createBbox(double x, double y, double width, double height) {
    Bbox bbox = objectFactory.createBbox();
    bbox.setX((float) x);
    bbox.setY((float) y);
    bbox.setW((float) width);
    bbox.setH((float) height);
    return bbox;
  }


  /**
   * @param x
   * @param y
   * @param height
   * @param width
   * @return
   */
  public Bbox createBbox(Double x, Double y, Double width, Double height) {
    return createBbox(toDouble(x), toDouble(y), toDouble(width),
        toDouble(height));
  }


  /**
   * @param id
   * @param type
   * @return
   */
  public Glyph createGlyph(String id, GlyphType type) {
    Glyph glyph = objectFactory.createGlyph();
    checkId(id, glyph);
    glyph.setId(id);
    glyph.setClazz(type.toString());
    return glyph;
  }


  /**
   * @param id
   * @param type
   * @param bbox
   * @return
   */
  public Glyph createGlyph(String id, GlyphType type, Bbox bbox) {
    return createGlyph(id, null, type, bbox);
  }


  /**
   * @param id
   * @param type
   * @param bbox
   * @param isClone
   * @return
   */
  public Glyph createGlyph(String id, GlyphType type, Bbox bbox,
      boolean isClone) {
    Glyph glyph = createGlyph(id, type, bbox);
    if (isClone) {
      glyph.setClone(objectFactory.createGlyphClone());
    }
    return glyph;
  }


  /**
   * @param id
   * @param type
   * @param bbox
   * @param orientation
   * @return
   */
  public Glyph createGlyph(String id, GlyphType type, Bbox bbox,
      GlyphOrientation orientation) {
    return createGlyph(id, type, bbox, orientation, false);
  }


  /**
   * @param id
   * @param type
   * @param bbox
   * @param orientation
   * @param isClone
   * @return
   */
  public Glyph createGlyph(String id, GlyphType type, Bbox bbox,
      GlyphOrientation orientation, boolean isClone) {
    Glyph glyph = createGlyph(id, type, bbox, isClone);
    glyph.setOrientation(orientation.toString());
    return glyph;
  }


  /**
   * @param id
   * @param type
   * @param x
   * @param y
   * @param width
   * @param height
   * @return
   */
  public Glyph createGlyph(String id, GlyphType type, double x, double y,
      double width, double height) {
    return createGlyph(id, type, createBbox(x, y, width, height));
  }


  /**
   * @param id
   * @param type
   * @param x
   * @param y
   * @param width
   * @param height
   * @param isClone
   * @return
   */
  public Glyph createGlyph(String id, GlyphType type, double x, double y,
      double width, double height, boolean isClone) {
    return createGlyph(id, type, createBbox(x, y, width, height), isClone);
  }


  /**
   * @param id
   * @param type
   * @param x
   * @param y
   * @param width
   * @param height
   * @param orientation
   * @return
   */
  public Glyph createGlyph(String id, GlyphType type, double x, double y,
      double width, double height, GlyphOrientation orientation) {
    return createGlyph(id, type, createBbox(x, y, width, height), orientation);
  }


  /**
   * @param id
   * @param labelText
   * @param type
   * @return
   */
  public Glyph createGlyph(String id, String labelText, GlyphType type) {
    Glyph glyph = createGlyph(id, type);
    if ((labelText != null) && (labelText.length() > 0)) {
      glyph.setLabel(createLabel(labelText));
    }
    return glyph;
  }


  /**
   * @param id
   * @param labelText
   * @param type
   * @param bbox
   * @return
   */
  public Glyph createGlyph(String id, String labelText, GlyphType type,
      Bbox bbox) {
    Glyph glyph = createGlyph(id, labelText, type);
    if (bbox != null) {
      glyph.setBbox(bbox);
    }
    return glyph;
  }


  /**
   * @param id
   * @param labelText
   * @param type
   * @param x
   * @param y
   * @param width
   * @param height
   * @return
   */
  public Glyph createGlyph(String id, String labelText, GlyphType type,
      double x, double y, double width, double height) {
    return createGlyph(id, labelText, type, createBbox(x, y, width, height));
  }


  /**
   * @param x
   * @param y
   * @return
   */
  public Callout createGlyphCallout(double x, double y) {
    return createGlyphCallout(createPoint(x, y));
  }


  /**
   * @param point
   * @return
   */
  public Callout createGlyphCallout(org.sbgn.bindings.Point point) {
    Callout callout = objectFactory.createGlyphCallout();
    callout.setPoint(point);
    return callout;
  }


  /**
   * @param text
   * @return
   */
  public Label createLabel(String text) {
    Label label = objectFactory.createLabel();
    label.setText(text);
    return label;
  }


  /**
   * @param text
   * @param bbox
   * @return
   */
  public Label createLabel(String text, Bbox bbox) {
    Label label = createLabel(text);
    label.setBbox(bbox);
    return label;
  }


  /**
   * @param text
   * @param x
   * @param y
   * @param width
   * @param height
   * @return
   */
  public Label createLabel(String text, double x, double y, double width,
      double height) {
    return createLabel(text, createBbox(x, y, width, height));
  }


  /**
   * @param language
   * @return
   */
  public Map createMap(Language language) {
    Map map = objectFactory.createMap();
    map.setLanguage(language.toString());
    return map;
  }


  /**
   * @param language
   * @param bbox
   * @return
   */
  public Map createMap(Language language, Bbox bbox) {
    Map map = createMap(language);
    map.setBbox(bbox);
    return map;
  }


  /**
   * @param language
   * @param x
   * @param y
   * @param width
   * @param height
   * @return
   */
  public Map createMap(Language language, double x, double y, double width,
      double height) {
    return createMap(language, createBbox(x, y, width, height));
  }


  /**
   * @param text
   * @return
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  public Notes createNotes(String text)
      throws ParserConfigurationException, SAXException, IOException {
    Notes notes = objectFactory.createSBGNBaseNotes();
    if ((text != null) && (text.length() > 0)) {
      DocumentBuilder
          db =
          DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document doc = db.newDocument();
      Element
          element =
          doc.createElementNS("http://www.w3.org/1999/xhtml", "html:body");
      doc.appendChild(element);
      element.appendChild(doc.createTextNode(text));
      notes.getAny().add(element);
    }
    return notes;
  }


  /**
   * @param x
   * @param y
   * @return
   */
  public org.sbgn.bindings.Point createPoint(double x, double y) {
    org.sbgn.bindings.Point point = objectFactory.createPoint();
    point.setX((float) x);
    point.setY((float) y);
    return point;
  }


  /**
   * @param id
   * @param x
   * @param y
   * @return
   */
  public Port createPort(String id, double x, double y) {
    Port port = objectFactory.createPort();
    checkId(id, port);
    port.setId(id);
    port.setX((float) x);
    port.setY((float) y);
    return port;
  }


  /**
   * @return
   */
  public Sbgn createSbgn() {
    return objectFactory.createSbgn();
  }


  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SBGNbuilder other = (SBGNbuilder) obj;
    if (objectFactory == null) {
      if (other.objectFactory != null) {
        return false;
      }
    } else if (!objectFactory.equals(other.objectFactory)) {
      return false;
    }
    return true;
  }


  /**
   * @return the objectFactory
   */
  public ObjectFactory getObjectFactory() {
    return objectFactory;
  }


  /**
   * @param id
   * @return
   */
  public SBGNBase getSBGNBase(String id) {
    return id2element.get(id);
  }


  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((objectFactory == null) ? 0 :
            objectFactory.hashCode());
    return result;
  }


  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("SBGNbuilder [objectFactory=");
    builder.append(objectFactory);
    builder.append("]");
    return builder.toString();
  }
}

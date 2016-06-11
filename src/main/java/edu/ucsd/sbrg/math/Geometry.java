/*
 * 
 */
package edu.ucsd.sbrg.math;

import edu.ucsd.sbrg.escher.model.Point;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * @author Andreas Dr&auml;ger
 * @since 1.0
 * TODO: Avoid creating new objects when calculating bezier curves and intersection points!!! This costs memory and is less efficient!
 */
public class Geometry {

  /**
   * Gives the Bezi&eacute;r point at parameter <i>t</i> for the given definition
   * and control points.
   *
   * @param t     must be between zero and one.
   * @param start must not be {@code null}
   * @param end   must not be {@code null}
   * @param b1    can be {@code null}
   * @param b2    can be {@code null}
   * @return
   */
  public static Point bezier(double t, Point start, Point end, Point b1,
      Point b2) {
    double x1 = start.getX(), y1 = start.getY(), x2 = end.getX(),
        y2 =
            end.getY();
    double b1x = Double.NaN, b1y = Double.NaN, b2x = Double.NaN,
        b2y =
            Double.NaN;
    return bezier(t, x1, y1, x2, y2, b1x, b1y, b2x, b2y);
  }


  /**
   * @param t
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   * @param b1x
   * @param b1y
   * @param b2x
   * @param b2y
   * @return
   */
  public static Point bezier(double t, double x1, double y1, double x2,
      double y2, double b1x, double b1y, double b2x, double b2y) {
    assert 0d <= t && t <= 1d;
    double diff = 1d - t;
    double c0x = x1 * diff, c0y = y1 * diff;
    double c1x = 0d, c1y = 0d;
    double c2x = 0d, c2y = 0d;
    double c3x = x2 * t, c3y = y2 * t;
    if (!Double.isNaN(b1x) && !Double.isNaN(b1y)) {
      c1x = b1x * t;
      c1y = b1y * t;
      c0x += c1x;
      c0y += c1y;
      c2x = b1x * diff;
      c2y = b1y * diff;
    }
    c0x *= diff;
    c0y *= diff;
    if (!Double.isNaN(b2x) && !Double.isNaN(b2y)) {
      double x = b2x * diff;
      double y = b2y * diff;
      c1x += x;
      c1y += y;
      c2x += b2x * diff;
      c2y += b2y * diff;
      c3x += x;
      c3y += y;
    }
    c1x *= t;
    c1y *= t;
    c2x *= diff;
    c2y *= diff;
    c3x *= t;
    c3y *= t;
    c0x = (c0x + c1x) * diff + (c2x + c3x) * t;
    c0y = (c0y + c1y) * diff + (c2y + c3y) * t;
    return new Point(c0x, c0y);
  }


  public static void main(String args[]) {
    final Point start = new Point(4649.858d / 1000d, 2646.7302d / 1000d);
    final Point end = new Point(4908.858d / 1000d, 2817.7302d / 1000d);
    final Point b1 = new Point(Double.NaN, Double.NaN);
    final Point b2 = new Point(Double.NaN, Double.NaN);
    final double x = 4893.858d / 1000d, y = 2802.73025d / 1000d, w = 30d,
        h =
            30d;
    final Shape shape = new Ellipse2D.Double(x, y, w, h);
    //		for (double t = 0d; t <= 1d; t += .01) {
    //			Point bezier = bezier(t, start, end, b1, b2);
    //			System.out.println(bezier.getX() + "," + bezier.getY() + "\t" + shape.contains(bezier.getX(), bezier.getY()));
    //		}
    JOptionPane.showMessageDialog(null, new JScrollPane(new JPanel() {

      /**
       * Generated serial version identifier.
       */
      private static final long serialVersionUID = 1197741259084339082L;


      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = 2, h = 2;
        Graphics2D g2 = (Graphics2D) g;
        g2.setBackground(Color.WHITE);
        g2.setColor(Color.BLUE);
        g2.draw(shape);
        g2.setColor(Color.BLACK);
        g2.fillOval(start.getX().intValue(), start.getY().intValue(), w, h);
        g2.fillOval(end.getX().intValue(), end.getY().intValue(), w, h);
        if ((b1 != null) && b1.isSetX() && b1.isSetY()) {
          g2.fillOval(b1.getX().intValue(), b1.getY().intValue(), w, h);
        }
        if ((b2 != null) && b2.isSetX() && b2.isSetY()) {
          g2.fillOval(b2.getX().intValue(), b2.getY().intValue(), w, h);
        }
        g2.draw(new CubicCurve2D.Double(start.getX(), start.getY(), b1.getX(),
            b1.getY(), b2.getX(), b2.getY(), end.getX(), end.getY()));
        Point2D
            point =
            findIntersection(shape,
                new CubicCurve2D.Double(start.getX(), start.getY(), b1.getX(),
                    b1.getY(), b2.getX(), b2.getY(), end.getX(), end.getY()));
        System.out.println(point);
        if (point != null) {
          g2.setColor(Color.RED);
          g2.fillOval((int) Math.round(point.getX()),
              (int) Math.round(point.getY()), w + 5, h + 5);
        }
      }
    }));
  }


  /**
   * @param t
   * @param curve
   * @return
   */
  public static Point2D.Double bezier(double t, CubicCurve2D curve) {
    Point2D start = curve.getP1();
    Point2D end = curve.getP2();
    Point2D b1 = curve.getCtrlP1();
    Point2D b2 = curve.getCtrlP2();
    return convert(
        bezier(t, start.getX(), start.getY(), end.getX(), end.getY(), b1.getX(),
            b1.getY(), b2.getX(), b2.getY()));
  }


  /**
   * @param point
   * @return
   */
  public static Point convert(Point2D point) {
    return new Point(point.getX(), point.getY());
  }


  /**
   * @param point
   * @return
   */
  public static Point2D.Double convert(Point point) {
    return new Point2D.Double(point.getX(), point.getY());
  }


  /**
   * Be careful, when using this method!!! It has been implemented to
   * specifically find one intersection point based on the assumption that the
   * shape contains either the beginning or the end of the curve. In other
   * cases, {@code null} will be returned!
   *
   * @param shape
   * @param curve
   * @return {@code null} if no intersection exists.
   */
  public static Point2D.Double findIntersection(Shape shape,
      CubicCurve2D curve) {
    double l = 0d, r = 1d, t, error = 1e-25d;
    int count = 0, maxCount = 100;
    do {
      count++;
      t = (r - l) / 2d + l;
      Point2D p1 = bezier(l, curve);
      Point2D p2 = bezier(t, curve);
      Point2D p3 = bezier(r, curve);
      if ((count == 1) && (!shape.contains(p1) && !shape.contains(p3))) {
        return null;
      }
      if (shape.contains(p3)) {
        // right end is inside of the shape
        if (shape.contains(p2)) {
          // we are very deep in the shape, need to go back again.
          r = t;
        } else {
          // we are still far away from the shape, let's get closer.
          l = t;
        }
      } else if (shape.contains(p1)) {
        if (shape.contains(p2)) {
          l = t;
        } else {
          r = t;
        }
      }
    } while ((r - l > error) && (count < maxCount));
    return bezier((r - l) / 2d + l, curve);
  }
}

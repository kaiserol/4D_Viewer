package de.uzk.markers;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.uzk.utils.ColorUtils;
import de.uzk.utils.GraphicsUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

/**
 * Ein einzelner Marker bzw. dessen Parameter: Position, Aussehen, etc.
 * */
public class Marker {
    public static final int LINE_WIDTH = 5;

    // Mittelpunkt des Markers
    private Point pos;

    // Zeitraum, in dem der Marker sichtbar sein soll
    private int from;
    private int to;

    // Höhe und Breite
    private Dimension size;

    // Rotation (in Grad)
    private int rotation;

    // Aussehen
    private MarkerShape shape;
    private Color color;
    private String label;


    public Marker(int start) {
        this(new Point(250, 100), new Dimension(500, 200), start, start, MarkerShape.RECTANGLE, Color.RED, "Marker");
    }

    /**
     * Default-Konstruktor (Rotes 500x200-Rechteck mit Beschriftung "Marker" in der oberen linken Ecke bei t=0)
     *
     */
    public Marker() { this(0); }

    public Marker(Marker other) {
        this.cloneFrom(other);
    }

    public Marker(Point pos, Dimension size, int from, int to, MarkerShape shape, Color color, String label) {
        setPos(pos);
        setSize(size);
        setShape(shape);
        setLabel(label);
        setColor(color);
        setFrom(from);
        setTo(to);
    }

    //region Getter- und Settermethoden
    public Point getPos() {
        return pos;
    }

    @JsonSetter("pos")
    public void setPos(Point pos) {
        this.pos = pos;
    }

    public void setWidth(int width) {
        setSize(new Dimension(width, size.height));
    }

    public void setHeight(int height) {
        setSize(new Dimension(size.width, height));
    }

    public Dimension getSize() {
        return size;
    }

    @JsonSetter("size")
    public void setSize(Dimension size) {
        if (size.width < 0 || size.height < 0) throw new IllegalArgumentException("Size cannot be negative!");
        this.size = size;
    }

    public int getFrom() {
        return from;
    }

    @JsonSetter("from")
    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    @JsonSetter("to")
    public void setTo(int to) {
        this.to = to;
    }

    public MarkerShape getShape() {
        return shape;
    }

    @JsonSetter("shape")
    public void setShape(MarkerShape shape) {
        this.shape = shape;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @JsonGetter("color")
    private String getHexColor() {
        return ColorUtils.colorToHex(this.color);
    }

    @JsonSetter("color")
    private void setHexColor(String color) {
        setColor(Color.decode(color));
    }

    public String getLabel() {
        return label;
    }

    @JsonSetter("label")
    public void setLabel(String label) {
        this.label = label;
    }


    public void rotate(int rotation) {
        this.rotation = (this.rotation + rotation) % 360;
    }

    public int getRotation() {
        return rotation;
    }

    //endregion

    //region Zeichnen
    public void draw(Graphics2D g2d) {
        Rectangle actualBounds = getShapeBounds();
        AffineTransform rot = getRotationTransform();
        Shape finalShape = shape.createShape(actualBounds);


        g2d = (Graphics2D) g2d.create();
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(LINE_WIDTH));

        {
            Graphics2D g2dCopy = (Graphics2D) g2d.create();
            g2dCopy.transform(rot);
            g2dCopy.draw(finalShape);
            drawName(g2d);
        }

    }

    /**
     * Zeichnet die Dragpunkte dieses Markers auf das gegebene <code>Graphics2D</code>-Objekt.
     * Siehe <code>getDragPoints</code> und <code>getRotatePoint</code>.
     *
     * @param g2d Graphics2D-Objekt, auf das gezeichnet werden soll. Es wird eine Kopie angelegt, das
     *            ürsprüngliche Objekt wird nicht modifiziert.
     * */
    public void drawDragPoints(Graphics2D g2d) {
        g2d = (Graphics2D) g2d.create();
        g2d.setColor(Color.WHITE);

        Point[] scalePoints = getScalePoints();
        for (Point point : scalePoints) {
            Shape c = new Ellipse2D.Float(point.x - (float) LINE_WIDTH, point.y - (float) LINE_WIDTH, 2.0f * LINE_WIDTH, 2.0f * LINE_WIDTH);
            g2d.fill(c);
        }

        Point rotPoint = getRotatePoint();
        Point topCenter = scalePoints[3];

        Shape r = new Ellipse2D.Float(rotPoint.x - (float) LINE_WIDTH, rotPoint.y - (float) LINE_WIDTH, 2.0f * LINE_WIDTH, 2.0f * LINE_WIDTH);
        g2d.fill(r);
        g2d.setStroke(new BasicStroke(1));

        g2d.drawLine(topCenter.x , topCenter.y, rotPoint.x, rotPoint.y);
    }

    /**
     * Zeichnet die Beschriftung dieses Markers auf das gegebene <code>Graphics2D</code>-Objekt.
     * Siehe <code>getLabelArea</code>.
     *
     * @param g2d Graphics2D-Objekt, auf das gezeichnet werden soll. Es wird eine Kopie angelegt, das
     *            ürsprüngliche Objekt wird nicht modifiziert.
     * */
    private void drawName(Graphics2D g2d) {
        g2d = (Graphics2D) g2d.create();
        Shape labelArea = getLabelArea(g2d);
        g2d.fill(labelArea);
        boolean lightColor = ColorUtils.calculatePerceivedBrightness(color) > 0.5;
        g2d.setColor(lightColor ? Color.BLACK : Color.WHITE);

        FontMetrics metrics = GraphicsUtils.updateMetrics(g2d);
        g2d.drawString(label, labelArea.getBounds().x, labelArea.getBounds().y + metrics.getAscent());
    }
    //endregion

    //region Geometrische Hilfsmethoden

    /**
     * @return den Bereich, in den der Marker gezeichnet werden soll, unter Berücksichtigung der Skalierung.
     * Diese Methode ist notwendig, da {@link java.awt.Rectangle} annimmt, dass die Position des Rechtecks der
     * der oberen linken Ecke des Rechtecks entspricht, wir die Position des Markers aber als den Mittelpunkt
     * dieses Bereiches definieren.
     *
     */
    @JsonIgnore
    public Rectangle getShapeBounds() {
        Point corner = getShapeCorner();
        return new Rectangle(corner, size);
    }

    /**
     * @return die linke obere Ecke des Bereichs, in den der Marker gezeichnet werden soll.
     * */
    @JsonIgnore
    public Point getShapeCorner() {
        Point center = pos;
        return new Point(center.x - (size.width / 2), center.y - (size.height / 2));
    }

    /**
     * @return den Bereich, auf den die Beschriftung des Markers gezeichnet werden soll;
     * Dieser ist so rotiert, dass er relativ zum Bild immer waagerecht und der Text somit leicht
     * lesbar ist.
     * */
    @JsonIgnore
    public Shape getLabelArea(Graphics onto) {
        FontMetrics metrics = GraphicsUtils.updateMetrics(onto);
        Point2D corner = getRotationTransform().transform(getShapeCorner(), null);
        return new Rectangle(
            new Point((int)corner.getX(), (int)corner.getY()),
            new Dimension(metrics.stringWidth(label), metrics.getHeight())
        );

    }

    /**
     * @return die Acht "Drag-Punkte", mit denen der Marker vergrößert oder verkleinert werden kann.
     * Diese sind bereits so rotiert, dass sie auch visuell an den Rändern des Markers liegen.
     * */
    @JsonIgnore
    public Point[] getScalePoints() {
        Point[] points = new Point[8];
        AffineTransform rot = getRotationTransform();
        Rectangle bounds = getShapeBounds();
        int i = 0;
        for (int x = 0; x <= 2; x++) {
            for (int y = 0; y <= 2; y++) {
                if (x == 1 && y == 1) continue;
                int dx = (bounds.width / 2) * x;
                int dy = (bounds.height / 2) * y;

                Point2D rotated = rot.transform(new Point(dx + bounds.x,dy + bounds.y), null);
                points[i] = new Point((int) rotated.getX(), (int) rotated.getY());
                i++;
            }
        }

        return points;
    }

    /**
     * @return Den "Drag-Punkt", der benutzt wird, um den Marker zu rotieren.
     * Seine Position ist selbst so rotiert, dass er immer auch visuell über dem Mittelpunkt des
     * Markers angezeigt wird.
     * */
    @JsonIgnore
    public Point getRotatePoint() {
        Point2D point = getRotationTransform().transform(new Point(pos.x, pos.y - size.height/2 - 100), null);
        return new Point((int) point.getX(), (int) point.getY());
    }
    //endregion

    public boolean shouldRender(int at) {
        return this.from <= at && this.to >= at;
    }

    /**
     * Hilfsmethode.
     * @return einen {@link AffineTransform}, der die aktuelle individuelle Rotation des Markers repräsentiert.
     * */
    @JsonIgnore
    public AffineTransform getRotationTransform() {
        return AffineTransform.getRotateInstance(Math.toRadians(rotation), pos.x, pos.y);
    }

    /**
     * Ähnlich wie ein Copy-Konstruktor, überschreibt jedoch diesen Marker mit den Werten eines
     * anderen, statt einen neuen zu konstruieren.
     * */
    public void cloneFrom(Marker other) {
        setPos(other.getPos());
        setSize(other.getSize());
        setShape(other.getShape());
        setLabel(other.getLabel());
        setColor(other.getColor());
        setFrom(other.getFrom());
        setTo(other.getTo());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Marker other) {
            return pos.equals(other.pos) && size.equals(other.size) && from == other.from && to == other.to && shape == other.shape && color.equals(other.color) && label.equals(other.label);
        }
        return false;
    }

}

package de.uzk.markers;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.uzk.utils.ColorUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

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

    // Ob der Marker gerade dabei ist, im {@link de.uzk.markers.VisualMarkerEditor} resized zu werden
    // TODO: auslagern (gehört nicht zu den Markereigenschaften)
    @JsonIgnore
    private boolean resizing = false;

    /**
     * Default-Konstruktor (Rotes 500x200-Rechteck mit Beschriftung "Marker" in der oberen linken Ecke bei t=0)
     *
     */
    public Marker() {
        this(new Point(250, 100), new Dimension(500, 200), 0, 0, MarkerShape.RECTANGLE, Color.RED, "Marker");
    }


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

    public Point getRotatePoint() {
        Point2D point = getRotationTransform().transform(new Point(pos.x, pos.y - size.height/2 - 100), null);
        return new Point((int) point.getX(), (int) point.getY());
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
    //endregion

    @JsonSetter("label")
    public void setLabel(String label) {
        this.label = label;
    }

    //region Eigene öffentliche Methoden
    public void setResizing(boolean resizing) {
        this.resizing = resizing;
    }

    public boolean shouldRender(int at) {
        return this.from <= at && this.to >= at;
    }

    public void draw(Graphics2D to) {
        Rectangle actualBounds = getShapeBounds();
        AffineTransform rot = getRotationTransform();
        Shape finalShape = rot.createTransformedShape(switch (shape) {
            case RECTANGLE -> actualBounds;
            case ELLIPSE ->
                new Ellipse2D.Float(actualBounds.x, actualBounds.y, actualBounds.width, actualBounds.height);

        });

        to = (Graphics2D) to.create();
        to.setColor(color);
        to.setStroke(new BasicStroke(LINE_WIDTH));

        to.draw(finalShape);

        to.transform(rot);
        drawName(to);
        to.rotate(Math.toRadians(-rotation), pos.x, pos.y);



        if (resizing) {

            to.setColor(Color.WHITE);

            Point[] scalePoints = getScalePoints();
            for (Point point : scalePoints) {
                Shape c = new Ellipse2D.Float(point.x - (float) LINE_WIDTH, point.y - (float) LINE_WIDTH, 2.0f * LINE_WIDTH, 2.0f * LINE_WIDTH);
                to.fill(c);
            }

            Point rotPoint = getRotatePoint();
            Point topCenter = scalePoints[3];

            Shape r = new Ellipse2D.Float(rotPoint.x - (float) LINE_WIDTH, rotPoint.y - (float) LINE_WIDTH, 2.0f * LINE_WIDTH, 2.0f * LINE_WIDTH);
            to.fill(r);
            to.setStroke(new BasicStroke(1));

            to.drawLine(topCenter.x , topCenter.y, rotPoint.x, rotPoint.y);
        }
    }

    public void rotate(int rotation) {
        this.rotation = (this.rotation + rotation) % 360;
    }

    public int getRotation() {
        return rotation;
    }

    @JsonIgnore
    public AffineTransform getRotationTransform() {
        return AffineTransform.getRotateInstance(Math.toRadians(rotation), pos.x, pos.y);
    }

    /**
     * Liefert den Bereich, in den der Marker gezeichnet werden soll, unter Berücksichtigung der Skalierung.
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

    @JsonIgnore
    public Point getShapeCorner() {
        Point center = pos;
        return new Point(center.x - (size.width / 2), center.y - (size.height / 2));
    }

    @JsonIgnore
    public Rectangle getLabelArea(Graphics onto) {
        FontMetrics metrics = onto.getFontMetrics();
        Point corner = getShapeCorner();
        return new Rectangle(corner, new Dimension(metrics.stringWidth(label), metrics.getHeight()));
    }

    public void cloneFrom(Marker other) {
        setPos(other.getPos());
        setSize(other.getSize());
        setShape(other.getShape());
        setLabel(other.getLabel());
        setColor(other.getColor());
        setFrom(other.getFrom());
        setTo(other.getTo());
        resizing = other.resizing;
    }

    //endregion

    //region Overrides
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Marker other) {
            return pos.equals(other.pos) && size.equals(other.size) && from == other.from && to == other.to && shape == other.shape && color.equals(other.color) && label.equals(other.label);
        }
        return false;
    }
    //endregion

    //region Private- und Helfermethoden
    private void drawName(Graphics2D to) {
        Rectangle labelArea = getLabelArea(to);
        to.fill(labelArea);
        boolean lightColor = ColorUtils.calculatePerceivedBrightness(color) > 0.5;
        to.setColor(lightColor ? Color.BLACK : Color.WHITE);

        FontMetrics metrics = to.getFontMetrics();
        to.drawString(label, labelArea.x, labelArea.y + metrics.getAscent());
    }
    //endregion

}

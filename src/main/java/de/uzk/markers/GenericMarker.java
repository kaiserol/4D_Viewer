package de.uzk.markers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.uzk.markers.interactions.GenericMarkerModificator;
import de.uzk.markers.interactions.MarkerModificator;
import de.uzk.utils.GraphicsUtils;
import de.uzk.utils.NumberUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public class GenericMarker extends AbstractMarker {
    // Höhe und Breite
    protected Dimension size;
    // Mittelpunkt des Markers
    protected Point pos;
    private int rotation;
    private GenericMarkerShape shape;

    public GenericMarker(int start) {
        this(new Point(250, 100), new Dimension(500, 200), start, start, GenericMarkerShape.RECTANGLE, Color.RED, "Marker");
    }

    @SuppressWarnings("unused") // Jackson benutzt diesen Konstruktor zur Deserialisierung
    public GenericMarker() {
        this(0);
    }

    public GenericMarker(GenericMarker other) {
        this(new Point(other.pos), new Dimension(other.size), other.from, other.to, other.shape, other.color, other.label);
        setRotation(other.rotation);
    }

    public GenericMarker(AbstractMarker abstractMarker, GenericMarkerShape shape) {
        setFrom(abstractMarker.getFrom());
        setTo(abstractMarker.getTo());
        setColor(abstractMarker.getColor());
        setLabel(abstractMarker.getLabel());
        Point[] scalePoints = abstractMarker.getScalePoints();
        Point p1 = scalePoints[0];
        Point p2 = scalePoints[scalePoints.length - 1];
        int xMin = Math.min(p1.x, p2.x);
        int xMax = Math.max(p1.x, p2.x);
        int yMin = Math.min(p1.y, p2.y);
        int yMax = Math.max(p1.y, p2.y);
        setSize(new Dimension(xMax - xMin, yMax - yMin));
        setPos(new Point(xMin + size.width / 2, yMin + size.height / 2));
        setShape(shape);
    }

    public GenericMarker(Point pos, Dimension size, int from, int to, GenericMarkerShape shape, Color color, String label) {
        setPos(pos);
        setSize(size);
        setShape(shape);
        setLabel(label);
        setColor(color);
        setFrom(from);
        setTo(to);

    }

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

    public GenericMarkerShape getShape() {
        return shape;
    }

    @JsonSetter("shape")
    public void setShape(GenericMarkerShape shape) {
        this.shape = shape;
    }

    public int getRotation() {
        return rotation;
    }

    @JsonSetter("rotation")
    public void setRotation(int newRotation) {
        this.rotation = NumberUtils.normalizeAngle(newRotation);
    }

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
     * @return die linke obere Ecke des Bereichs, in den der Marker gezeichnet werden soll.
     *
     */
    @JsonIgnore
    public Point getShapeCorner() {
        Point center = pos;
        return new Point(center.x - (size.width / 2), center.y - (size.height / 2));
    }

    /**
     * @return den Bereich, in den der Marker gezeichnet werden soll, unter Berücksichtigung der Skalierung.
     * Diese Methode ist notwendig, da {@link java.awt.Rectangle} annimmt, dass die Position des Rechtecks der
     * der oberen linken Ecke des Rechtecks entspricht, wir die Position des Markers aber als den Mittelpunkt
     * dieses Bereiches definieren.
     *
     */
    @JsonIgnore
    public Rectangle getShapeBounds() {
        return new Rectangle(getShapeCorner(), size);
    }

    @Override
    public void drawDragPoints(Graphics2D g2d) {
        super.drawDragPoints(g2d);

        Point rotPoint = getRotatePoint();
        Point topCenter = getScalePoints()[3];

        g2d.setColor(Color.WHITE);
        Shape r = new Ellipse2D.Float(rotPoint.x - (float) LINE_WIDTH, rotPoint.y - (float) LINE_WIDTH, 2.0f * LINE_WIDTH, 2.0f * LINE_WIDTH);
        g2d.fill(r);
        g2d.setStroke(new BasicStroke(1));

        g2d.drawLine(topCenter.x, topCenter.y, rotPoint.x, rotPoint.y);
    }

    /**
     * @return den Bereich, auf den die Beschriftung des Markers gezeichnet werden soll;
     * Dieser ist so rotiert, dass er relativ zum Bild immer waagerecht und der Text somit leicht
     * lesbar ist.
     *
     */
    @Override
    @JsonIgnore
    public Shape getLabelArea(Graphics onto) {
        FontMetrics metrics = GraphicsUtils.updateMetrics(onto);
        Point2D corner = getRotationTransform().transform(getShapeCorner(), null);
        return new Rectangle(new Point((int) corner.getX(), (int) corner.getY()), new Dimension(metrics.stringWidth(label), metrics.getHeight()));

    }

    /**
     * @return die Acht "Drag-Punkte", mit denen der Marker vergrößert oder verkleinert werden kann.
     * Diese sind bereits so rotiert, dass sie auch visuell an den Rändern des Markers liegen.
     *
     */
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

                Point2D rotated = rot.transform(new Point(dx + bounds.x, dy + bounds.y), null);
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
     *
     */
    @JsonIgnore
    public Point getRotatePoint() {
        Point2D point = getRotationTransform().transform(new Point(pos.x, pos.y - size.height / 2 - 100), null);
        return new Point((int) point.getX(), (int) point.getY());
    }

    /**
     * Hilfsmethode.
     *
     * @return einen {@link AffineTransform}, der die aktuelle individuelle Rotation des Markers repräsentiert.
     *
     */
    @JsonIgnore
    public AffineTransform getRotationTransform() {
        return AffineTransform.getRotateInstance(Math.toRadians(rotation), pos.x, pos.y);
    }

    @Override
    public AbstractMarker copy() {
        return new GenericMarker(this);
    }

    @Override
    public MarkerModificator getSuitableModificator() {
        return new GenericMarkerModificator(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericMarker that = (GenericMarker) o;
        return super.equals(that) && this.pos.equals(that.pos) && this.size.equals(that.size) && this.shape.equals(that.shape);


    }
}

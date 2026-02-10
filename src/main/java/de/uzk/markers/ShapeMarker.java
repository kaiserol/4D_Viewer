package de.uzk.markers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.uzk.markers.interactions.ShapeMarkerModificator;
import de.uzk.markers.interactions.MarkerModificator;
import de.uzk.utils.GraphicsUtils;
import de.uzk.utils.NumberUtils;

import java.awt.*;
import java.awt.geom.*;


public class ShapeMarker extends Marker {
    // Höhe und Breite
    protected double width;
    protected double height;
    // Mittelpunkt des Markers
    protected Point2D.Double pos;
    private int rotation;
    private MarkerShape shape;

    public ShapeMarker(int start, String label) {
        this(new Point2D.Double(250, 100), 500,200, start, start, MarkerShape.RECTANGLE, Color.RED, label);
    }

    @SuppressWarnings("unused") // Jackson benutzt diesen Konstruktor zur Deserialisierung
    public ShapeMarker() {
        this(0, "DESERIALIZE");
    }

    public ShapeMarker(ShapeMarker other) {
        this(new Point2D.Double(other.pos.getX(), other.pos.getY()), other.width, other.height, other.from, other.to, other.shape, other.color, other.label);
        setRotation(other.rotation);
    }

    public ShapeMarker(Marker abstractMarker, MarkerShape shape) {
        setFrom(abstractMarker.getFrom());
        setTo(abstractMarker.getTo());
        setColor(abstractMarker.getColor());
        setLabel(abstractMarker.getLabel());
        Point2D[] scalePoints = abstractMarker.getScalePoints();
        Point2D p1 = scalePoints[0];
        Point2D p2 = scalePoints[scalePoints.length - 1];
        double xMin = Math.min(p1.getX(), p2.getX());
        double xMax = Math.max(p1.getX(), p2.getX());
        double yMin = Math.min(p1.getY(), p2.getY());
        double yMax = Math.max(p1.getX(), p2.getY());
        setSize(xMax - xMin, yMax - yMin);
        setPos(new Point2D.Double(xMin + width / 2, yMin + height / 2));
        setShape(shape);
    }

    public ShapeMarker(Point2D.Double pos, double width, double height, int from, int to, MarkerShape shape, Color color, String label) {
        setPos(pos);
        setWidth(width);
        setHeight(height);
        setShape(shape);
        setLabel(label);
        setColor(color);
        setFrom(from);
        setTo(to);

    }

    public Point2D.Double getPos() {
        return pos;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    @JsonSetter("pos")
    public void setPos(Point2D.Double pos) {
        this.pos = pos;
    }

    @JsonSetter("height")
    public void setWidth(double width) {
        if(width < 0) throw new IllegalArgumentException("Width cannot be negative!");
        this.width = width;
    }

    @JsonSetter("width")
    public void setHeight(double height) {
        if(height < 0) throw new IllegalArgumentException("Height cannot be negative!");
        this.height = height;
    }


    public void setSize(double width, double height) {
        setWidth(width);
        setHeight(height);
    }

    public MarkerShape getShape() {
        return shape;
    }

    @JsonSetter("shape")
    public void setShape(MarkerShape shape) {
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
        Rectangle2D actualBounds = getShapeBounds();
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
    public Point2D getShapeCorner() {
        Point2D center = pos;
        return new Point2D.Double(center.getX() - (width / 2), center.getY() - (height / 2));
    }

    /**
     * @return den Bereich, in den der Marker gezeichnet werden soll, unter Berücksichtigung der Skalierung.
     * Diese Methode ist notwendig, da {@link java.awt.Rectangle} annimmt, dass die Position des Rechtecks der
     * der oberen linken Ecke des Rechtecks entspricht, wir die Position des Markers aber als den Mittelpunkt
     * dieses Bereiches definieren.
     *
     */
    @JsonIgnore
    public Rectangle2D getShapeBounds() {
        Point2D corner = getShapeCorner();
        return new Rectangle2D.Double(corner.getX(), corner.getY(), width, height);
    }

    @Override
    public void drawDragPoints(Graphics2D g2d) {
        super.drawDragPoints(g2d);

        Point2D rotPoint = getRotatePoint();
        Point2D topCenter = getScalePoints()[3];

        g2d.setColor(Color.WHITE);
        Shape r = new Ellipse2D.Double(rotPoint.getX() - (double) LINE_WIDTH, rotPoint.getY() - (double) LINE_WIDTH, 2.0 * LINE_WIDTH, 2.0 * LINE_WIDTH);
        g2d.fill(r);
        g2d.setStroke(new BasicStroke(1));

        g2d.drawLine((int)topCenter.getX(), (int)topCenter.getY(), (int)rotPoint.getX(), (int)rotPoint.getY());
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
        return new Rectangle2D.Double(corner.getX(),  corner.getY(), metrics.stringWidth(label), metrics.getHeight());

    }

    /**
     * @return die Acht "Drag-Punkte", mit denen der Marker vergrößert oder verkleinert werden kann.
     * Diese sind bereits so rotiert, dass sie auch visuell an den Rändern des Markers liegen.
     *
     */
    @JsonIgnore
    @Override
    public Point2D[] getScalePoints() {
        Point2D[] points = new Point2D[8];
        AffineTransform rot = getRotationTransform();
        Rectangle2D bounds = getShapeBounds();
        int i = 0;
        for (int x = 0; x <= 2; x++) {
            for (int y = 0; y <= 2; y++) {
                if (x == 1 && y == 1) continue;
                double dx = (bounds.getWidth() / 2) * x;
                double dy = (bounds.getHeight() / 2) * y;

                points[i] = rot.transform(new Point2D.Double(dx + bounds.getX(), dy + bounds.getY()), null);
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
    public Point2D getRotatePoint() {
        return getRotationTransform().transform(new Point2D.Double(pos.getX(), pos.getY() - height / 2 - 100), null);
    }

    /**
     * Hilfsmethode.
     *
     * @return einen {@link AffineTransform}, der die aktuelle individuelle Rotation des Markers repräsentiert.
     *
     */
    @JsonIgnore
    public AffineTransform getRotationTransform() {
        return AffineTransform.getRotateInstance(Math.toRadians(rotation), pos.getX(), pos.getY());
    }

    @Override
    public Marker copy() {
        return new ShapeMarker(this);
    }

    @Override
    public MarkerModificator getSuitableModificator() {
        return new ShapeMarkerModificator(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShapeMarker that = (ShapeMarker) o;
        return super.equals(that) && this.pos.equals(that.pos) && this.width == that.width && this.height == that.height && this.shape.equals(that.shape);


    }
}

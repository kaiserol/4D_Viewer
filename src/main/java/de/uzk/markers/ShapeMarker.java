package de.uzk.markers;

import com.fasterxml.jackson.annotation.*;
import de.uzk.markers.interactions.MarkerModificator;
import de.uzk.markers.interactions.ShapeMarkerModificator;
import de.uzk.utils.GraphicsUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static de.uzk.Main.workspace;


public class ShapeMarker extends RotatableMarker {
    // Höhe und Breite
    protected double width;
    protected double height;
    // Mittelpunkt des Markers
    private MarkerShape shape;


    public ShapeMarker(Point2D.Double pos, double width, double height, int timeStart, int timeEnd, int levelStart, int levelEnd, int initialTime, int initialLevel, MarkerShape shape, Color color, String label) {
        setCenter(pos);
        setWidth(width);
        setHeight(height);
        setShape(shape);
        setLabel(label);
        setColor(color);
        setTimeStart(timeStart);
        setTimeEnd(timeEnd);
        setLevelStart(levelStart);
        setLevelEnd(levelEnd);
        this.initialTime = initialTime;
        this.initialLevel = initialLevel;
    }

    public ShapeMarker(int initialTime, int initialLevel, String label) {
        this(new Point2D.Double(), 500, 200, 0, workspace.getMaxTime(), 0, workspace.getMaxLevel(), initialTime, initialLevel, MarkerShape.RECTANGLE, Color.RED, label);
        Dimension size = workspace.getCurrentImageSize();
        center = new Point2D.Double((double) size.width / 2, (double) size.height / 2);
    }

    @SuppressWarnings("unused")
    public ShapeMarker() {
        this(0, 0, "DESERIALIZE");
    }

    public ShapeMarker(ShapeMarker other) {
        this(new Point2D.Double(other.center.getX(), other.center.getY()), other.width, other.height, other.timeStart, other.timeEnd, other.levelStart, other.levelEnd, other.initialTime, other.initialLevel, other.shape, other.color, other.label);
        setRotation(other.rotation);
    }

    public ShapeMarker(Marker abstractMarker, MarkerShape shape) {
        copyFrom(abstractMarker);
        Point2D[] scalePoints = abstractMarker.getScalePoints();
        Point2D p1 = scalePoints[0];
        Point2D p2 = scalePoints[scalePoints.length - 1];
        setCenter(GraphicsUtils.getCenter(p1, p2));
        setWidth(Math.abs(p1.getX() - center.getX()) + Math.abs(p2.getX() - center.getX()));
        setHeight(Math.abs(p1.getY() - center.getY()) + Math.abs(p2.getY() - center.getY()));
        setShape(shape);
    }

    public ShapeMarker(RotatableMarker marker, MarkerShape shape) {
        this((Marker)marker, shape);
        setWidth(marker.getWidth());
        setHeight(marker.getHeight());
        setRotation(marker.getRotation());
    }

    @Override
    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        if (width < 0) throw new IllegalArgumentException("Width cannot be negative!");
        this.width = width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        if (height < 0) throw new IllegalArgumentException("Height cannot be negative!");
        this.height = height;
    }

    @JsonIgnore
    public void setSize(double width, double height) {
        setWidth(width);
        setHeight(height);
    }

    public MarkerShape getShape() {
        return shape;
    }


    public void setShape(MarkerShape shape) {
        this.shape = shape;
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
        Point2D center = this.center;
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
        return new Rectangle2D.Double(corner.getX(), corner.getY(), metrics.stringWidth(label), metrics.getHeight());
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
    @Override
    public Point2D getRotatePointPosition() {
        return new Point2D.Double(center.getX(), center.getY() - height / 2 - 100);
    }

    /**
     * Hilfsmethode.
     *
     * @return einen {@link AffineTransform}, der die aktuelle individuelle Rotation des Markers repräsentiert.
     *
     */
    @JsonIgnore
    public AffineTransform getRotationTransform() {
        return AffineTransform.getRotateInstance(Math.toRadians(rotation), center.getX(), center.getY());
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
        return super.equals(that) && center.equals(that.center) && width == that.width && height == that.height && shape.equals(that.shape);
    }
}

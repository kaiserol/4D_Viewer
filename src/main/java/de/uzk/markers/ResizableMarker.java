package de.uzk.markers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.uzk.utils.GraphicsUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public abstract class ResizableMarker extends RotatableMarker {
    protected double width;
    protected double height;


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
        if (height < 0) throw new IllegalArgumentException("Width cannot be negative!");

        this.height = height;
    }

    @JsonIgnore
    public void setSize(double width, double height) {
        setHeight(height);
        setWidth(width);
    }

    @Override
    protected void copyFrom(Marker marker) {
        super.copyFrom(marker);
        if (marker instanceof RotatableMarker rotatableMarker) {
            setWidth(rotatableMarker.getWidth());
            setHeight(rotatableMarker.getHeight());
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
}

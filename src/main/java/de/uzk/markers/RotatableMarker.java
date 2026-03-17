package de.uzk.markers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.uzk.utils.NumberUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

public abstract class RotatableMarker extends Marker {
    protected int rotation;


    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
    protected Point2D center;

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = NumberUtils.normalizeAngle(rotation);
    }

    @JsonIgnore
    public AffineTransform getRotationTransform() {
        return AffineTransform.getRotateInstance(Math.toRadians(rotation), center.getX(), center.getY());
    }

    public Point2D getCenter() {
        return center;
    }

    public void setCenter(Point2D center) {
        this.center = center;
    }

    @JsonIgnore
    public Point2D getRotatePoint() {
        return getRotationTransform().transform(getRotatePointPosition(), null);
    }

    @JsonIgnore
    protected abstract Point2D getRotatePointPosition();

    @Override
    public void drawDragPoints(Graphics2D g2d) {
        super.drawDragPoints(g2d);
        drawRotatePoint(g2d);
    }

    protected void drawRotatePoint(Graphics2D g2d) {
        Point2D rotPoint = getRotatePoint();
        Point2D topCenter = getRotationTransform().transform(new Point2D.Double(center.getX(), center.getY() - getHeight() / 2), null);


        g2d.setColor(Color.WHITE);
        Shape r = new Ellipse2D.Double(rotPoint.getX() - (double) LINE_WIDTH, rotPoint.getY() - (double) LINE_WIDTH, 2.0 * LINE_WIDTH, 2.0 * LINE_WIDTH);
        g2d.fill(r);
        g2d.setStroke(new BasicStroke(1));

        g2d.drawLine((int) topCenter.getX(), (int) topCenter.getY(), (int) rotPoint.getX(), (int) rotPoint.getY());

    }

    public abstract double getWidth();
    public abstract double getHeight();
}

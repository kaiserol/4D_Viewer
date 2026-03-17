package de.uzk.markers;

import de.uzk.markers.interactions.MarkerModificator;
import de.uzk.markers.interactions.PointMarkerModificator;
import de.uzk.utils.GraphicsUtils;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class PointMarker extends RotatableMarker {
    public static final int SIZE = 50;
    private PointMarkerShape shape;

    public PointMarker(Point2D center, PointMarkerShape shape) {
        setCenter(center);
        this.shape = shape;
    }

    @SuppressWarnings("unused")
    public PointMarker() {
        this(new Point2D.Double(0, 0), PointMarkerShape.CROSS);
        setLabel("DESERIALIZE");
    }

    public PointMarker(PointMarker other) {
        this(other, other.shape);
        rotation = other.getRotation();
    }

    public PointMarker(Marker abstractMarker, PointMarkerShape shape) {
        Point2D[] scalePoints = abstractMarker.getScalePoints();
        Point2D p1 = scalePoints[0];
        Point2D p2 = scalePoints[scalePoints.length - 1];
        setCenter(GraphicsUtils.getCenter(p1, p2));
        copyFrom(abstractMarker);
        setShape(shape);
    }

    @Override
    protected Point2D getRotatePointPosition() {
        return new Point2D.Double(center.getX(), center.getY() - SIZE * 3.0/2);
    }

    public PointMarkerShape getShape() {
        return shape;
    }

    public void setShape(PointMarkerShape shape) {
        this.shape = shape;
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d = (Graphics2D) g2d.create();
        g2d.setColor(color);
        Graphics2D g2dCopy = (Graphics2D) g2d.create();
        g2dCopy.transform(getRotationTransform());
        Shape shape1 = shape.getShape(center);
        g2dCopy.fill(shape1);
        drawName(g2d);
    }

    @Override
    public Point2D[] getScalePoints() {
        return new Point2D[] { center };
    }

    @Override
    public Shape getLabelArea(Graphics g2d) {
        FontMetrics metrics = GraphicsUtils.updateMetrics(g2d);

        int x = (int)center.getX() - SIZE;
        int y = (int)center.getY() - SIZE;
        return new Rectangle2D.Double(x, y, metrics.stringWidth(label), metrics.getHeight());

    }

    @Override
    public void drawDragPoints(Graphics2D g2d) {
        if(shape == PointMarkerShape.DOT) return;
        drawRotatePoint(g2d);
    }

    @Override
    public Marker copy() {
        return new PointMarker(this);
    }

    @Override
    public MarkerModificator getSuitableModificator() {
        return new PointMarkerModificator(this);
    }

    @Override
    public double getWidth() { return SIZE; }
    public double getHeight() { return SIZE; }
}

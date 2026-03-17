package de.uzk.markers;

import de.uzk.markers.interactions.MarkerModificator;
import de.uzk.markers.interactions.ResizableMarkerModificator;
import de.uzk.utils.GraphicsUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static de.uzk.Main.workspace;


public class ShapeMarker extends ResizableMarker {

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
        this(other, other.shape);
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

    @Override
    public Marker copy() {
        return new ShapeMarker(this);
    }

    @Override
    public MarkerModificator getSuitableModificator() {
        return new ResizableMarkerModificator<>(this);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && shape.equals(((ShapeMarker)o).shape);
    }
}

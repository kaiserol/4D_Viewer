package de.uzk.markers;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.uzk.markers.interactions.ArrowMarkerModificator;
import de.uzk.markers.interactions.MarkerModificator;
import de.uzk.utils.GraphicsUtils;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static de.uzk.Main.workspace;
import static java.lang.Math.sin;

public class ArrowMarker extends Marker {
    private Point2D base;
    private Point2D head;

    @SuppressWarnings("unused") // Jackson benutzt diesen Konstruktor zur Deserialisierung
    public ArrowMarker() {
        this(new Point(250, 100), new Point(500, 500), 0, workspace.getMaxTime(), 0, workspace.getMaxLevel(), 0, 0, Color.RED, "Arrow");
    }

    public ArrowMarker(Point2D start, Point2D tip, int timeStart, int timeEnd, int levelStart, int levelEnd, int initialTime, int initialLevel, Color color, String label) {
        setBase(start);
        setHead(tip);
        setLabel(label);
        setColor(color);
        setTimeStart(timeStart);
        setTimeEnd(timeEnd);
        setLevelStart(levelStart);
        setLevelEnd(levelEnd);
        this.initialTime = initialTime;
        this.initialLevel = initialLevel;
    }

    public ArrowMarker(ArrowMarker other) {
        this((Point2D) other.base.clone(), (Point2D) other.head.clone(), other.getTimeStart(), other.getTimeEnd(), other.getLevelStart(), other.getLevelEnd(), other.getInitialTime(), other.getInitialLevel(), other.getColor(), other.getLabel());
    }

    public ArrowMarker(Marker abstractMarker) {
        setTimeStart(abstractMarker.getTimeStart());
        setTimeEnd(abstractMarker.getTimeEnd());
        setLevelStart(abstractMarker.getLevelStart());
        setLevelEnd(abstractMarker.getLevelEnd());
        setColor(abstractMarker.getColor());
        setLabel(abstractMarker.getLabel());
        Point2D[] scalePoints = abstractMarker.getScalePoints();
        setBase(scalePoints[0]);
        setHead(scalePoints[scalePoints.length - 1]);
        initialTime = abstractMarker.getInitialTime();
        initialLevel = abstractMarker.getInitialLevel();
    }

    public ArrowMarker(RotatableMarker rotatableMarker) {
        this((Marker) rotatableMarker);
        Point2D newBase = new Point2D.Double(base.getX() - rotatableMarker.getWidth() / 2, base.getY() - rotatableMarker.getHeight() / 2);
        setBase(newBase);
        setHead(rotatableMarker.getCenter());
    }

    @JsonGetter("start")
    public Point2D getBase() {
        return base;
    }

    @JsonSetter("start")
    public void setBase(Point2D base) {
        this.base = base;
    }

    @JsonGetter("tip")
    public Point2D getHead() {
        return head;
    }

    @JsonSetter("tip")
    public void setHead(Point2D head) {
        this.head = head;
    }

    @Override
    public void draw(Graphics2D g2d) {
        Path2D path = new Path2D.Double();
        double dx = head.getX() - base.getX();
        double dy = head.getY() - base.getY();
        double baseAngle = Math.atan2(dy, dx);
        double leftAngle = baseAngle + Math.PI / 4;
        double rightAngle = baseAngle - Math.PI / 4;
        double length = Math.sqrt(dx * dx + dy * dy) / 10;
        path.moveTo(base.getX(), base.getY());
        path.lineTo(head.getX(), head.getY());
        path.moveTo(head.getX(), head.getY());

        path.lineTo(head.getX() - length * Math.cos(leftAngle), head.getY() - length * sin(leftAngle));
        path.moveTo(head.getX(), head.getY());
        path.lineTo(head.getX() - length * Math.cos(rightAngle), head.getY() - length * sin(rightAngle));
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(LINE_WIDTH));
        g2d.draw(path);
        drawName(g2d);
    }

    @Override
    public Point2D[] getScalePoints() {
        return new Point2D[]{base, head};
    }

    @Override
    public Shape getLabelArea(Graphics g2d) {
        FontMetrics metrics = GraphicsUtils.updateMetrics(g2d);
        return new Rectangle2D.Double(base.getX(), base.getY(), metrics.stringWidth(label), metrics.getHeight());
    }

    @Override
    public Marker copy() {
        return new ArrowMarker(this);
    }

    @Override
    public MarkerModificator getSuitableModificator() {
        return new ArrowMarkerModificator(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrowMarker that = (ArrowMarker) o;
        return super.equals(that) && base.equals(that.base) && head.equals(that.head);
    }
}

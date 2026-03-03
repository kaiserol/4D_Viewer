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

import static java.lang.Math.sin;

public class ArrowMarker extends Marker {
    private Point2D start;
    private Point2D tip;

    @SuppressWarnings("unused") // Jackson benutzt diesen Konstruktor zur Deserialisierung
    public ArrowMarker() {
        this(new Point(250, 100), new Point(500, 500), 0, 0, Color.RED, "Arrow");
    }

    public ArrowMarker(Point2D start, Point2D tip, int from, int to, Color color, String label) {
        setStart(start);
        setTip(tip);
        setLabel(label);
        setColor(color);
        setFrom(from);
        setTo(to);
    }

    public ArrowMarker(ArrowMarker other) {
        this((Point2D) other.start.clone(), (Point2D) other.tip.clone(), other.getFrom(), other.getTo(), other.getColor(), other.getLabel());
    }

    public ArrowMarker(Marker abstractMarker) {
        setFrom(abstractMarker.getFrom());
        setTo(abstractMarker.getTo());
        setColor(abstractMarker.getColor());
        setLabel(abstractMarker.getLabel());
        Point2D[] scalePoints = abstractMarker.getScalePoints();
        setStart(scalePoints[0]);
        setTip(scalePoints[scalePoints.length - 1]);
    }

    @JsonGetter("start")
    public Point2D getStart() {
        return start;
    }

    @JsonSetter("start")
    public void setStart(Point2D start) {
        this.start = start;
    }

    @JsonGetter("tip")
    public Point2D getTip() {
        return tip;
    }

    @JsonSetter("tip")
    public void setTip(Point2D tip) {
        this.tip = tip;
    }

    @Override
    public void draw(Graphics2D g2d) {
        Path2D path = new Path2D.Double();
        double dx = tip.getX() - start.getX();
        double dy = tip.getY() - start.getY();
        double baseAngle = Math.atan2(dy, dx);
        double leftAngle = baseAngle + Math.PI / 4;
        double rightAngle = baseAngle - Math.PI / 4;
        double length = Math.sqrt(dx * dx + dy * dy) / 10;
        path.moveTo(start.getX(), start.getY());
        path.lineTo(tip.getX(), tip.getY());
        path.moveTo(tip.getX(), tip.getY());

        path.lineTo(tip.getX() - length * Math.cos(leftAngle), tip.getY() - length * sin(leftAngle));
        path.moveTo(tip.getX(), tip.getY());
        path.lineTo(tip.getX() - length * Math.cos(rightAngle), tip.getY() - length * sin(rightAngle));
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(LINE_WIDTH));
        g2d.draw(path);
        drawName(g2d);
    }

    @Override
    public Point2D[] getScalePoints() {
        return new Point2D[]{start, tip};
    }

    @Override
    public Shape getLabelArea(Graphics g2d) {
        FontMetrics metrics = GraphicsUtils.updateMetrics(g2d);
        return new Rectangle2D.Double(start.getX(), start.getY(), metrics.stringWidth(label), metrics.getHeight());
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
        return super.equals(that) && start.equals(that.start) && tip.equals(that.tip);
    }
}

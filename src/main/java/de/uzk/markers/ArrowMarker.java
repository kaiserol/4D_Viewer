package de.uzk.markers;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.uzk.markers.interactions.ArrowMarkerModificator;
import de.uzk.markers.interactions.MarkerModificator;
import de.uzk.utils.GraphicsUtils;

import java.awt.*;
import java.awt.geom.Path2D;

import static java.lang.Math.sin;

public class ArrowMarker extends AbstractMarker {

    private Point start;
    private Point tip;

    public ArrowMarker(int start) {
        this(new Point(250, 100), new Point(500, 500), start, start, Color.RED, "Arrow");
    }

    /**
     * Default-Konstruktor (Rotes 500x200-Rechteck mit Beschriftung "Marker" in der oberen linken Ecke bei t=0)
     *
     */
    public ArrowMarker() { this(0); }


    @JsonGetter("start")
    public Point getStart() { return start; }

    @JsonSetter("start")
    public void setStart(Point start) {
        this.start = start;
    }

    @JsonGetter("tip")
    public Point getTip() { return tip; }

    @JsonSetter("tip")
    public void setTip(Point tip) {
        this.tip = tip;
    }

    public ArrowMarker(Point start, Point tip, int from, int to, Color color, String label) {
        setStart(start);
        setTip(tip);
        setLabel(label);
        setColor(color);
        setFrom(from);
        setTo(to);
    }

    public ArrowMarker(ArrowMarker other) {
        this(new Point(other.getStart()), other.getTip(), other.getFrom(), other.getTo(), other.getColor(), other.getLabel());
    }

    @Override
    public void draw(Graphics2D g2d) {
        Path2D path = new Path2D.Double();
        int dx = tip.x - start.x;
        int dy = tip.y - start.y;
        double baseAngle = Math.atan2(dy, dx);
        double leftAngle = baseAngle + Math.PI / 4;
        double rightAngle = baseAngle - Math.PI / 4;
        double length = Math.sqrt(dx * dx + dy * dy) / 10;
        path.moveTo(start.x, start.y);
        path.lineTo(tip.x, tip.y);
        path.moveTo(tip.x, tip.y);

        path.lineTo(tip.x - length * Math.cos(leftAngle), tip.y - length * sin(leftAngle));
        path.moveTo(tip.x, tip.y);
        path.lineTo(tip.x - length * Math.cos(rightAngle), tip.y - length * sin(rightAngle));
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(LINE_WIDTH));
        g2d.draw(path);
        drawName(g2d);
    }

    @Override
    public Point[] getScalePoints() {
        return new Point[] { start, tip };
    }

    @Override
    public Shape getLabelArea(Graphics g2d) {
        FontMetrics metrics = GraphicsUtils.updateMetrics(g2d);
        return new Rectangle(
            start,
            new Dimension(metrics.stringWidth(label), metrics.getHeight())
        );
    }

    @Override
    public AbstractMarker copy() {
        return new ArrowMarker(this);
    }

    @Override
    public MarkerModificator getSuitableModificator() {
        return new ArrowMarkerModificator(this);
    }
}

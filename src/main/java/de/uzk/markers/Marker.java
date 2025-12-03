package de.uzk.markers;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import de.uzk.utils.ColorUtils;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

public class Marker {
    private static final int LINE_WIDTH = 5;

    private Point pos;
    private int from;
    private int to;
    private Dimension size;
    private MarkerShape shape;
    private Color color;
    private String label;
    @JsonIgnore
    private boolean resizing = false;

    public Marker() {
        this(new Point(0,0), new Dimension(500, 200), 0, 0, MarkerShape.RECTANGLE, Color.RED, "Marker");
    }

    public Marker(Marker other) {
        this.cloneFrom(other);
    }

    public Marker(Point pos, Dimension size, int from, int to, MarkerShape shape, Color color, String label) {
        setPos(pos);
        setSize(size);
        setShape(shape);
        setLabel(label);
        setColor(color);
        setFrom(from);
        setTo(to);
    }


    //region Getter- und Settermethoden
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
        this.size = size;
    }

    public int getFrom() {
        return from;
    }

    @JsonSetter("from")
    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    @JsonSetter("to")
    public void setTo(int to) {
        this.to = to;
    }

    public MarkerShape getShape() {
        return shape;
    }

    @JsonSetter("shape")
    public void setShape(MarkerShape shape) {
        this.shape = shape;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @JsonGetter("color")
    private String getHexColor() {
        return ColorUtils.colorToHex(this.color);
    }

    @JsonSetter("color")
    private void setHexColor(String color) {
        setColor(Color.decode(color));
    }

    public String getLabel() {
        return label;
    }

    @JsonSetter("label")
    public void setLabel(String label) {
        this.label = label;
    }
    //endregion

    //region Eigene öffentliche Methoden
    public void setResizing(boolean resizing) {
        this.resizing = resizing;
    }

    public boolean shouldRender(int at) {
        return this.from <= at && this.to >= at;
    }

    public void draw(Graphics2D to, Rectangle imageArea) {
        Rectangle actualBounds = this.getBounds();
        actualBounds.translate(imageArea.x, imageArea.y);
        Shape finalShape = switch (this.shape) {
            case RECTANGLE -> actualBounds;
            case ELLIPSE ->
                new Ellipse2D.Float(actualBounds.x, actualBounds.y, actualBounds.width, actualBounds.height);

        };

        to = (Graphics2D) to.create();
        to.setColor(this.color);
        to.setStroke(new BasicStroke(LINE_WIDTH));
        to.draw(finalShape);

        if(resizing) {
            for(Point point : getScalePoints()) {
                Shape c = new Ellipse2D.Float(point.x - 5, point.y - 5, 10, 10);
                to.fill(c);
            }
        }

        this.drawName(to, actualBounds.x, actualBounds.y);
    }

    @JsonIgnore
    public Rectangle getBounds() {
        return new Rectangle(pos, size);
    }

    public Dimension getLabelSize(Graphics onto) {
        FontMetrics metrics = onto.getFontMetrics();
        return new Dimension(metrics.stringWidth(label), metrics.getHeight());
    }

    public void cloneFrom(Marker other) {
        setPos(other.getPos());
        setSize(other.getSize());
        setShape(other.getShape());
        setLabel(other.getLabel());
        setColor(other.getColor());
        setFrom(other.getFrom());
        setTo(other.getTo());
    }

    public java.util.List<Point> getScalePoints() {
        ArrayList<Point> points = new ArrayList<>();
        for(int i = 0; i <= 2; i++) {
            for(int j = 0; j <= 2; j++) {
                if(i == 1 && j == 1) continue;
                int dx = (size.width / 2) * i;
                int dy = (size.height / 2) * j;
                points.add(new Point(pos.x + dx, pos.y + dy));
            }
        }
        return points;
    }

    //endregion

    //region Overrides
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Marker other) {
            return pos.equals(other.pos) && size.equals(other.size) && from == other.from && to == other.to && shape == other.shape && color.equals(other.color) && label.equals(other.label);
        }
        return false;
    }
    //endregion

    //region Private- und Helfermethoden
    private void drawName(Graphics2D to, int x, int y) {

        to.fill(new Rectangle(new Point(x, y), getLabelSize(to)));
        boolean lightColor = ColorUtils.calculatePerceivedBrightness(color) > 0.5;
        to.setColor(lightColor ? Color.BLACK : Color.WHITE);

        FontMetrics metrics = to.getFontMetrics();
        to.drawString(label, x, y + metrics.getAscent());
    }
    //endregion

}

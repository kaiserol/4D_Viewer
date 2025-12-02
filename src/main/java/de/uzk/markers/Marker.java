package de.uzk.markers;

import com.fasterxml.jackson.annotation.*;
import de.uzk.utils.ColorUtils;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Marker {
    private static final int LINE_WIDTH = 5;

    private int x;
    private int y;
    private int from;
    private int to;
    private int width;
    private int height;
    private MarkerShape shape;
    private Color color;
    private String label;

    public Marker() {
        this(0, 0, 500, 200, 0, 0, MarkerShape.RECTANGLE, Color.RED, "Marker");
    }

    public Marker(Marker other) {
        this.cloneFrom(other);
    }

    public Marker(int x, int y, int width, int height, int from, int to, MarkerShape shape, Color color, String label) {
        this.setX(x);
        this.setY(y);
        this.setWidth(width);
        this.setHeight(height);
        this.setShape(shape);
        this.setLabel(label);
        this.setColor(color);
        this.setFrom(from);
        this.setTo(to);
    }


    //region Getter- und Settermethoden

    public int getY() {
        return y;
    }

    @JsonSetter("y")
    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    @JsonSetter("x")
    public void setX(int x) {
        this.x = x;
    }

    public int getWidth() {
        return width;
    }

    @JsonSetter("width")
    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    @JsonSetter("height")
    public void setHeight(int height) {
        this.height = height;
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

    @JsonGetter("color")
    private String getHexColor() {
        return ColorUtils.colorToHex(this.color);
    }

    @JsonSetter("color")
    private void setHexColor(String color) {
        setColor(Color.decode(color));
    }

    public void setColor(Color color) {
        this.color = color;
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

        this.drawName(to, actualBounds.x, actualBounds.y);


    }

    @JsonIgnore
    public Rectangle getBounds() {
        return new Rectangle(this.x, this.y, this.width, this.height);
    }

    public Dimension getLabelSize(Graphics onto) {
        FontMetrics metrics = onto.getFontMetrics();
        return new Dimension(metrics.stringWidth(label), metrics.getHeight());
    }

    public void cloneFrom(Marker other) {
        this.setX(other.getX());
        this.setY(other.getY());
        this.setWidth(other.getWidth());
        this.setHeight(other.getHeight());
        this.setShape(other.getShape());
        this.setLabel(other.getLabel());
        this.setColor(other.getColor());
        this.setFrom(other.getFrom());
        this.setTo(other.getTo());
    }

    //endregion

    //region Overrides
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Marker other) {
            return this.x == other.x && this.y == other.y &&
                this.width == other.width && this.height == other.height &&
                this.from == other.from && this.to == other.to &&
                this.shape == other.shape && this.color.equals(other.color) &&
                this.label.equals(other.label);
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

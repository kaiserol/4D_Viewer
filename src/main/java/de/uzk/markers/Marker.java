package de.uzk.markers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.JsonAdapter;
import de.uzk.utils.NumberUtils;
import de.uzk.utils.StringUtils;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Marker {
    private static final int LINE_WIDTH = 5;

    private int x;
    private int y;
    private int width;
    private int height;
    private MarkerShape shape;
    private Color color;
    private String label;

    public Marker() {
        this(0, 0, 0, 0, MarkerShape.RECTANGLE, Color.RED, "Marker");
    }

    public Marker(Marker other) {
        this(
                other.x,
                other.y,
                other.width,
                other.height,
                other.shape,
                other.color,
                other.label
        );
    }

    public Marker(int x, int y, int width, int height, MarkerShape shape, Color color, String label) {
        this.setX(x);
        this.setY(y);
        this.setWidth(width);
        this.setHeight(height);
        this.setShape(shape);
        this.setLabel(label);
        this.setColor(color);
    }

    @JsonCreator
    public Marker(
            @JsonProperty("x")
            int x,
            @JsonProperty("y")
            int y,
            @JsonProperty("width")
            int width,
            @JsonProperty("height")
            int height,
            @JsonProperty(value = "shape", defaultValue = "RECTANGLE")
            MarkerShape shape,
            @JsonProperty(value = "color", defaultValue = "#000000")
            String color,
            @JsonProperty("label")
            String label) {
        this(x, y, width, height, shape, Color.decode(color), label);
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public MarkerShape getShape() {
        return shape;
    }

    public void setShape(MarkerShape shape) {
        this.shape = shape;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void draw(Graphics2D to, Rectangle imageArea, double scaleFactor) {
        Rectangle actualBounds = this.getActualBounds(imageArea, scaleFactor);
        Shape finalShape = switch (this.shape) {
            case RECTANGLE -> actualBounds;
            case ELLIPSE ->
                    new Ellipse2D.Float(actualBounds.x, actualBounds.y, actualBounds.width, actualBounds.height);

        };


        Color prevColor = to.getColor();
        Stroke prevStroke = to.getStroke();

        to.setColor(this.color);
        to.setStroke(new BasicStroke(LINE_WIDTH * (float) scaleFactor));
        to.draw(finalShape);
        this.drawName(to, actualBounds.x, actualBounds.y);

        to.setColor(prevColor);
        to.setStroke(prevStroke);
    }

    private void drawName(Graphics2D to, int x, int y) {
        FontMetrics metrics = to.getFontMetrics();
        int width = metrics.stringWidth(this.label);
        int height = metrics.getHeight();
        to.fillRect(x, y - metrics.getAscent(), width, height);

        double brightness = NumberUtils.calculatePerceivedBrightness(this.color);
        if (brightness > 186) {
            to.setColor(Color.BLACK);
        } else {
            to.setColor(Color.WHITE);
        }

        to.drawString(this.label, x, y);
    }

    private Rectangle getActualBounds(Rectangle imageBounds, double scale) {
        int x = imageBounds.x + (int) (this.x * scale);
        int y = imageBounds.y + (int) (this.y * scale);

        int width = (int) (this.width * scale);
        int height = (int) (this.height * scale);

        return new Rectangle(x, y, width, height);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @JsonGetter("color")
    private String getHexColor() {
        return StringUtils.colorToHex(this.color);
    }


}

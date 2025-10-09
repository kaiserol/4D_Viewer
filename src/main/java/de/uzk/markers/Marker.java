package de.uzk.markers;

import de.uzk.gui.GuiUtils;

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
    private String name;



    public Marker(int x, int y, int width, int height, MarkerShape shape, Color color, String name) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.shape = shape;
        this.color = color;
        this.name = name;
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

    public  void draw(Graphics2D to, Rectangle imageArea, double scaleFactor) {
        Rectangle actualBounds = this.getActualBounds(imageArea, scaleFactor);
        Shape finalShape = switch(this.shape) {
            case RECTANGLE -> actualBounds;
            case ELLIPSE -> new Ellipse2D.Float(actualBounds.x, actualBounds.y, actualBounds.width, actualBounds.height);

        };



        Color prevColor = to.getColor();
        Stroke prevStroke = to.getStroke();

        to.setColor(this.color);
        to.setStroke(new BasicStroke(LINE_WIDTH * (float)scaleFactor));
        to.draw(finalShape);
        this.drawName(to, actualBounds.x, actualBounds.y);

        to.setColor(prevColor);
        to.setStroke(prevStroke);
    }

    private void drawName(Graphics2D to, int x, int y) {
        FontMetrics metrics = to.getFontMetrics();
        int width = metrics.stringWidth(this.name);
        int height = metrics.getHeight();
        to.fillRect(x,y - metrics.getAscent()  ,width,height);

        double brightness = GuiUtils.calculatePerceivedBrightness(this.color);
        if (brightness > 186) {
           to.setColor(Color.BLACK);
        } else {
            to.setColor(Color.WHITE);
        }

        to.drawString(this.name, x, y);
    }


    private Rectangle getActualBounds(Rectangle imageBounds, double scale) {
        int x = imageBounds.x + (int)(this.x * scale);
        int y = imageBounds.y + (int)(this.y * scale);

        int width = (int)(this.width * scale);
        int height = (int)(this.height * scale);

        return new Rectangle(x, y, width,height);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

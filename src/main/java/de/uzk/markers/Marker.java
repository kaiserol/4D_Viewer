package de.uzk.markers;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Marker {

    private int x;
    private int y;
    private int width;
    private int height;
    private MarkerShape shape;
    private Color color;



    public Marker(int x, int y, int width, int height, MarkerShape shape, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.shape = shape;
        this.color = color;
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
            case CIRCLE -> new Ellipse2D.Float(actualBounds.x, actualBounds.y, actualBounds.width, actualBounds.height);
        };



        Color prev = to.getColor();
        to.setColor(this.color);
        to.draw(finalShape);
        to.setColor(prev);
    }


    private Rectangle getActualBounds(Rectangle imageBounds, double scale) {
        int x = imageBounds.x + (int)(this.x * scale);
        int y = imageBounds.y + (int)(this.y * scale);

        int width = (int)(this.width * scale);
        int height = (int)(this.height * scale);

        return new Rectangle(x, y, width,height);
    }
}

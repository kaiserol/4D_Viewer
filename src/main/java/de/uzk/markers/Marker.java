package de.uzk.markers;

import java.awt.*;
import java.awt.geom.Point2D;

public abstract class Marker {

    protected int x;
    protected int y;
    protected int width;
    protected int height;

    public Marker(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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

    public abstract void draw(Graphics2D to, Rectangle imageArea, double scaleFactor);

    protected Rectangle getActualBounds(Rectangle imageBounds, double scale) {
        int x = imageBounds.x + (int)(this.x * scale);
        int y = imageBounds.y + (int)(this.y * scale);

        int width = (int)(this.width * scale);
        int height = (int)(this.height * scale);

        return new Rectangle(x, y, width,height);
    }
}

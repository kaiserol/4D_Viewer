package de.uzk.markers;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

public class RectMarker extends Marker {
    public RectMarker(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void draw(Graphics2D to, Rectangle imageBounds, double scaleFactor) {
        System.out.println("Scale factor: "+ scaleFactor);
        Color prev = to.getColor();
        to.setColor(Color.GREEN);
        to.draw(this.getActualBounds(imageBounds, scaleFactor));

        to.setColor(prev);
    }
}

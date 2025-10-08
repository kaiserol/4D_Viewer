package de.uzk.markers;

import java.awt.*;

public class RectMarker extends Marker {
    public RectMarker(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void draw(Graphics2D to, Rectangle imageBounds, double scaleFactor) {
        Color prev = to.getColor();
        to.setColor(Color.GREEN);
        to.draw(this.getActualBounds(imageBounds, scaleFactor));

        to.setColor(prev);
    }
}

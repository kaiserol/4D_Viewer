package de.uzk.markers.interactions;

import de.uzk.markers.TextMarker;

import java.awt.*;
import java.awt.geom.Point2D;

public class TextMarkerModificator extends ResizableMarkerModificator<TextMarker> {
    public TextMarkerModificator(TextMarker marker) {
        super(marker);
    }

    @Override
    public void handleResize(Point mousePos) {
        Point2D center = marker.getCenter();
        super.handleResize(mousePos);
        marker.setCenter(center);
    }
}

package de.uzk.edit.markers;

import de.uzk.markers.ArrowMarker;

import java.awt.*;
import java.awt.geom.Point2D;

public class MoveArrowEdit extends MoveMarkerEdit {
    private final ArrowMarker marker;

    public MoveArrowEdit(ArrowMarker marker) {
        this.marker = marker;
    }

    @Override
    protected void moveMarker(double dx, double dy) {
        Point2D start = marker.getStart();
        Point2D tip  = marker.getTip();
        marker.setStart(new Point2D.Double(start.getX() + dx, start.getY() + dy));
        marker.setTip(new Point2D.Double(tip.getX() + dx, tip.getY() + dy));

    }

}

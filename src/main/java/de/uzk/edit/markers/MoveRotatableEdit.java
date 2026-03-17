package de.uzk.edit.markers;

import de.uzk.markers.RotatableMarker;

import java.awt.geom.Point2D;

public class MoveRotatableEdit extends MoveMarkerEdit {
    private final RotatableMarker marker;

    public MoveRotatableEdit(RotatableMarker marker) {
        this.marker = marker;
    }

    @Override
    protected void moveMarker(double dx, double dy) {
        Point2D pos = marker.getCenter();
        marker.setCenter(new Point2D.Double(pos.getX() + dx, pos.getY() + dy));
    }
}

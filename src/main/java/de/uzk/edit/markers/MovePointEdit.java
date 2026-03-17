package de.uzk.edit.markers;

import de.uzk.markers.PointMarker;

import java.awt.geom.Point2D;

public class MovePointEdit extends MoveMarkerEdit {
    private final PointMarker marker;

    public MovePointEdit(PointMarker marker) {
        this.marker = marker;
    }

    @Override
    protected void moveMarker(double dx, double dy) {
        Point2D pos = marker.getCenter();
        marker.setCenter(new Point2D.Double(pos.getX() + dx, pos.getY() + dy));
    }
}

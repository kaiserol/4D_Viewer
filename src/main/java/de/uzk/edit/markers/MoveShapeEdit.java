package de.uzk.edit.markers;

import de.uzk.markers.ShapeMarker;

import java.awt.geom.Point2D;

public class MoveShapeEdit extends MoveMarkerEdit {
    private final ShapeMarker marker;

    public MoveShapeEdit(ShapeMarker marker) {
        this.marker = marker;
    }

    @Override
    protected void moveMarker(double dx, double dy) {
        Point2D pos = marker.getPos();
        marker.setPos(new Point2D.Double(pos.getX() + dx, pos.getY() + dy));
    }
}

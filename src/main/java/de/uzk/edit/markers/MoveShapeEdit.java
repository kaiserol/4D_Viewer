package de.uzk.edit.markers;

import de.uzk.markers.ShapeMarker;

import java.awt.*;

public class MoveShapeEdit extends MoveMarkerEdit {
    private final ShapeMarker marker;

    public MoveShapeEdit(ShapeMarker marker) {
        this.marker = marker;
    }

    @Override
    protected void moveMarker(int dx, int dy) {
        Point pos = marker.getPos();
        marker.setPos(new Point(pos.x + dx, pos.y + dy));
    }
}

package de.uzk.edit.markers;

import de.uzk.markers.ArrowMarker;

import java.awt.*;

public class MoveArrowEdit extends MoveMarkerEdit {
    private final ArrowMarker marker;

    public MoveArrowEdit(ArrowMarker marker) {
        this.marker = marker;
    }

    @Override
    protected void moveMarker(int dx, int dy) {
        Point start = marker.getStart();
        Point tip  = marker.getTip();
        marker.setStart(new Point(start.x + dx, start.y + dy));
        marker.setTip(new Point(tip.x + dx, tip.y + dy));

    }

}

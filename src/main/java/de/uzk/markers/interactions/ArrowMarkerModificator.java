package de.uzk.markers.interactions;

import de.uzk.markers.AbstractMarker;
import de.uzk.markers.ArrowMarker;

import java.awt.*;

public class ArrowMarkerModificator implements MarkerModificator {
    private final ArrowMarker marker;
    private DragPoint dragPoint;

    public ArrowMarkerModificator(ArrowMarker marker) {
        this.marker = marker;
    }


    @Override
    public void handleRotate(Point mousePos) {
        // noop
    }

    @Override
    public void handleResize(Point mousePos) {
        if(dragPoint == null) return;
        else if(dragPoint == DragPoint.START) marker.setStart(mousePos);
        else if(dragPoint == DragPoint.TIP) marker.setTip(mousePos);
    }

    @Override
    public void handleMove(Point mousePos) {

    }

    @Override
    public MarkerInteractionHandler.EditMode checkEditMode(Point mousePos) {
        int distance = AbstractMarker.LINE_WIDTH * AbstractMarker.LINE_WIDTH;
        if(mousePos.distance(marker.getStart()) <= distance) {
            dragPoint = DragPoint.START;

        } else if(mousePos.distance(marker.getTip()) <= distance) {
            dragPoint = DragPoint.TIP;
        } else {
            dragPoint = null;
        }
        return MarkerInteractionHandler.EditMode.RESIZE;
    }

    @Override
    public AbstractMarker getCurrentFocused() {
        return marker;
    }

    private enum DragPoint {
        START, TIP;
    }
}

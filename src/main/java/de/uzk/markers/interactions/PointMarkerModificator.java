package de.uzk.markers.interactions;

import de.uzk.markers.Marker;
import de.uzk.markers.PointMarker;
import de.uzk.markers.PointMarkerShape;

import java.awt.*;

public class PointMarkerModificator extends RotatableMarkerModificator<PointMarker> {
     public PointMarkerModificator(PointMarker marker) { super(marker); }

    @Override
    public void handleResize(Point mousePos) {
        // noop
    }

    @Override
    public MarkerInteractionHandler.EditMode checkEditMode(Point mousePos) {
        if(marker.getShape() == PointMarkerShape.DOT) return MarkerInteractionHandler.EditMode.NONE;
        return super.checkEditMode(mousePos);
    }

    @Override
    public Marker getCurrentFocused() {
        return marker;
    }
}

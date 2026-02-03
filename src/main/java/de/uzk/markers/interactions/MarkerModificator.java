package de.uzk.markers.interactions;

import de.uzk.markers.Marker;

import java.awt.*;

public interface MarkerModificator {
    void handleRotate(Point mousePos);

    void handleResize(Point mousePos);

    void handleMove(Point mousePos);

    MarkerInteractionHandler.EditMode checkEditMode(Point mousePos);

    Marker getCurrentFocused();
}

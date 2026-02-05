package de.uzk.markers.interactions;

import de.uzk.markers.Marker;

import java.awt.*;

public interface MarkerModificator {
    default void beginResize() {
    }

    void handleResize(Point mousePos);

    default void finishResize() {
    }

    default void beginRotate() {
    }

    void handleRotate(Point mousePos);

    default void finishRotate() {
    }

    default void beginMove() {
    }

    void handleMove(Point mousePos);

    default void finishMove() {
    }

    MarkerInteractionHandler.EditMode checkEditMode(Point mousePos);

    Marker getCurrentFocused();
}

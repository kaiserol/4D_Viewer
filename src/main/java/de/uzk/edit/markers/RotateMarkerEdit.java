package de.uzk.edit.markers;

import de.uzk.action.ActionType;
import de.uzk.edit.MaybeRedundantEdit;
import de.uzk.markers.RotatableMarker;

public class RotateMarkerEdit extends MaybeRedundantEdit {
    private final RotatableMarker marker;
    private int degrees;

    public RotateMarkerEdit(RotatableMarker marker) {
        this.marker = marker;
    }

    public void rotate(int degrees) {
        this.degrees += degrees;
    }

    @Override
    public boolean isRedundant() {
        return degrees == 0;
    }

    @Override
    public boolean perform() {
        marker.setRotation(marker.getRotation() + degrees);
        return true;
    }

    @Override
    public void undo() {
        marker.setRotation(marker.getRotation() - degrees);
    }

    @Override
    public ActionType getActionType() {
        return ActionType.ACTION_EDIT_MARKER;
    }
}

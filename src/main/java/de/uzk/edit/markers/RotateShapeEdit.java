package de.uzk.edit.markers;

import de.uzk.action.ActionType;
import de.uzk.edit.MaybeRedundantEdit;
import de.uzk.markers.ShapeMarker;

public class RotateShapeEdit extends MaybeRedundantEdit {
    private final ShapeMarker marker;
    private int degrees;

    public RotateShapeEdit(ShapeMarker marker) {
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

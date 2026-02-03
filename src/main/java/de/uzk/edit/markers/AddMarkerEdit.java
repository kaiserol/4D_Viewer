package de.uzk.edit.markers;

import de.uzk.action.ActionType;
import de.uzk.edit.Edit;
import de.uzk.markers.Marker;

import static de.uzk.Main.workspace;

public class AddMarkerEdit extends Edit {
    private final Marker marker;

    public AddMarkerEdit(Marker marker) {
        this.marker = marker;
    }

    @Override
    public boolean perform() {
        return workspace.getMarkers().addMarker(this.marker);
    }

    @Override
    public void undo() {
        workspace.getMarkers().remove(this.marker);
    }

    @Override
    public ActionType getActionType() {
        return ActionType.ACTION_ADD_MARKER;
    }
}

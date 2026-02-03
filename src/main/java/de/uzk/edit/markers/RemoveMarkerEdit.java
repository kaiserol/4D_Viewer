package de.uzk.edit.markers;

import de.uzk.action.ActionType;
import de.uzk.edit.Edit;
import de.uzk.markers.Marker;

import static de.uzk.Main.workspace;

public class RemoveMarkerEdit extends Edit {
    private final Marker marker;

    public RemoveMarkerEdit(Marker marker) {
        this.marker = marker;
    }

    @Override
    public boolean perform() {
        return workspace.getMarkers().remove(marker);
    }

    @Override
    public void undo() {
        workspace.getMarkers().addMarker(marker);
    }

    @Override
    public ActionType getActionType() {
        return ActionType.ACTION_REMOVE_MARKER;
    }
}

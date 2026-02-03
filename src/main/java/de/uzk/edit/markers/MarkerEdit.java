package de.uzk.edit.markers;

import de.uzk.action.ActionType;
import de.uzk.edit.Edit;
import de.uzk.markers.Marker;

import static de.uzk.Main.workspace;

public class MarkerEdit extends Edit {
    private final Marker before;
    private final Marker after;

    public MarkerEdit(Marker before, Marker after) {
        this.before = before;
        this.after = after;
    }

    @Override
    public boolean perform() {
        return workspace.getMarkers().replace(before, after);
    }

    @Override
    public void undo() {
        workspace.getMarkers().replace(after, before);
    }

    @Override
    public ActionType getActionType() {
        return ActionType.ACTION_EDIT_MARKER;
    }
}

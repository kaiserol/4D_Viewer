package de.uzk.edit.markers;

import de.uzk.action.ActionType;
import de.uzk.edit.MaybeRedundantEdit;

public abstract class MoveMarkerEdit extends MaybeRedundantEdit {
    private double dx;
    private double dy;

    public void move(double dx, double dy) {
        this.dx += dx;
        this.dy += dy;
    }

    protected abstract void moveMarker(double dx, double dy);

    @Override
    public boolean isRedundant() {
        return (dx * dx + dy * dy) <= 100;
    }

    @Override
    public boolean perform() {
        moveMarker(dx, dy);
        return true;
    }

    @Override
    public void undo() {
        moveMarker(-dx, -dy);
    }

    @Override
    public ActionType getActionType() {
        return ActionType.ACTION_EDIT_MARKER;
    }
}

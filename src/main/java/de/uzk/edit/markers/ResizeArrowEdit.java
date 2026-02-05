package de.uzk.edit.markers;

import de.uzk.action.ActionType;
import de.uzk.edit.MaybeRedundantEdit;
import de.uzk.markers.ArrowMarker;

import java.awt.*;

public class ResizeArrowEdit extends MaybeRedundantEdit {
    private final ArrowMarker marker;
    private final boolean isTip;
    private int dx;
    private int dy;

    public ResizeArrowEdit(ArrowMarker marker, boolean tip) {
        this.marker = marker;
        isTip = tip;
    }

    public void resize(int dx, int dy) {
        this.dx += dx;
        this.dy += dy;
    }

    @Override
    public boolean isRedundant() {
        return (this.dx * this.dx + this.dy * this.dy) <= 25;
    }

    @Override
    public boolean perform() {

        if(isTip) {
            Point tip = marker.getTip();
            marker.setTip(new Point(tip.x + dx, tip.y + dy));
        } else {
            Point start = marker.getStart();
            marker.setStart(new Point(start.x + dx, start.y + dy));
        }

        return true;
    }

    @Override
    public void undo() {
        if(isTip) {
            Point tip = marker.getTip();
            marker.setTip(new Point(tip.x - dx, tip.y - dy));
        } else {
            Point start = marker.getStart();
            marker.setStart(new Point(start.x - dx, start.y - dy));
        }
    }

    @Override
    public ActionType getActionType() {
        return ActionType.ACTION_EDIT_MARKER;
    }
}

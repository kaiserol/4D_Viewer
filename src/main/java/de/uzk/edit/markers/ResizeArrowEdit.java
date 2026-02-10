package de.uzk.edit.markers;

import de.uzk.action.ActionType;
import de.uzk.edit.MaybeRedundantEdit;
import de.uzk.markers.ArrowMarker;

import java.awt.*;
import java.awt.geom.Point2D;

public class ResizeArrowEdit extends MaybeRedundantEdit {
    private final ArrowMarker marker;
    private final boolean isTip;
    private double dx;
    private double dy;

    public ResizeArrowEdit(ArrowMarker marker, boolean tip) {
        this.marker = marker;
        isTip = tip;
    }

    public void resize(double dx, double dy) {
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
            Point2D tip = marker.getTip();
            marker.setTip(new Point2D.Double(tip.getX() + dx, tip.getY() + dy));
        } else {
            Point2D start = marker.getStart();
            marker.setStart(new Point2D.Double(start.getX() + dx, start.getY() + dy));
        }
        return true;
    }

    @Override
    public void undo() {
        if(isTip) {
            Point2D tip = marker.getTip();
            marker.setTip(new Point2D.Double(tip.getX() - dx, tip.getY() - dy));
        } else {
            Point2D start = marker.getStart();
            marker.setStart(new Point2D.Double(start.getX() - dx, start.getY() - dy));
        }
    }

    @Override
    public ActionType getActionType() {
        return ActionType.ACTION_EDIT_MARKER;
    }
}

package de.uzk.edit.markers;

import de.uzk.action.ActionType;
import de.uzk.edit.MaybeRedundantEdit;
import de.uzk.markers.ShapeMarker;

import java.awt.geom.Point2D;

public class ResizeShapeEdit extends MaybeRedundantEdit {
    private final ShapeMarker marker;
    private double dWidth;
    private double dHeight;
    private double dx;
    private double dy;

    public ResizeShapeEdit(ShapeMarker marker) {
        this.marker = marker;
    }

    public void resize(double dW, double dH, double dX, double dY) {
        dWidth += dW;
        dHeight += dH;
        dx += dX;
        dy += dY;
    }

    @Override
    public boolean isRedundant() {
        return (dWidth * dWidth + dHeight * dHeight) <= 25;
    }

    @Override
    public boolean perform() {
        double oldWidth = marker.getWidth();
        double oldHeight = marker.getHeight();
        Point2D oldPos = marker.getPos();
        marker.setSize(oldWidth + dWidth, oldHeight + dHeight);
        marker.setPos(new Point2D.Double(oldPos.getX() + dx, oldPos.getY() + dy));
        return true;
    }

    @Override
    public void undo() {
        double oldWidth = marker.getWidth();
        double oldHeight = marker.getHeight();
        Point2D oldPos = marker.getPos();
        marker.setSize(oldWidth - dWidth, oldHeight - dHeight);
        marker.setPos(new Point2D.Double(oldPos.getX() - dx, oldPos.getY() - dy));

    }

    @Override
    public ActionType getActionType() {
        return ActionType.ACTION_EDIT_MARKER;
    }
}

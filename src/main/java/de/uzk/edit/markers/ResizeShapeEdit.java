package de.uzk.edit.markers;

import de.uzk.action.ActionType;
import de.uzk.edit.MaybeRedundantEdit;
import de.uzk.markers.ShapeMarker;

import java.awt.*;

public class ResizeShapeEdit extends MaybeRedundantEdit {
    private final ShapeMarker marker;
    private final Dimension sizeChange = new Dimension();
    private final Dimension posShift = new Dimension();

    public ResizeShapeEdit(ShapeMarker marker) {
        this.marker = marker;
    }

    public void resize(int dW, int dH, int dX, int dY) {
        sizeChange.width += dW;
        sizeChange.height += dH;
        posShift.width += dX;
        posShift.height += dY;
    }

    @Override
    public boolean isRedundant() {
        return (sizeChange.width * sizeChange.width + sizeChange.height * sizeChange.height) <= 25;
    }

    @Override
    public boolean perform() {
        Dimension oldSize = marker.getSize();
        Point oldPos = marker.getPos();
        marker.setSize(new Dimension(oldSize.width + sizeChange.width, oldSize.height + sizeChange.height));
        marker.setPos(new Point(oldPos.x + posShift.width, oldPos.y + posShift.height));
        return true;
    }

    @Override
    public void undo() {
        Dimension oldSize = marker.getSize();
        Point oldPos = marker.getPos();
        marker.setSize(new Dimension(oldSize.width - sizeChange.width, oldSize.height - sizeChange.height));
        marker.setPos(new Point(oldPos.x - posShift.width, oldPos.y - posShift.height));

    }

    @Override
    public ActionType getActionType() {
        return ActionType.ACTION_EDIT_MARKER;
    }
}

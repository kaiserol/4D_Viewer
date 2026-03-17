package de.uzk.markers.interactions;

import de.uzk.edit.markers.MovePointEdit;
import de.uzk.markers.Marker;
import de.uzk.markers.PointMarker;
import de.uzk.markers.PointMarkerShape;

import java.awt.*;
import java.awt.geom.Point2D;

import static de.uzk.Main.workspace;

public class PointMarkerModificator extends RotatableMarkerModificator<PointMarker> {
    private MovePointEdit edit;
    public PointMarkerModificator(PointMarker marker) { super(marker); }

    @Override
    public void handleResize(Point mousePos) {
        // noop
    }

    @Override
    public void beginMove() {
        edit = new MovePointEdit(marker);
    }

    @Override
    public void handleMove(Point mousePos) {
        double newX = mousePos.x + PointMarker.SIZE;
        double newY = mousePos.y + PointMarker.SIZE;
        edit.move(newX - marker.getCenter().getX(), newY - marker.getCenter().getY());
        marker.setCenter(new Point2D.Double(newX, newY));
    }

    @Override
    public void finishMove() {
        if(edit == null) return;
        workspace.getEditManager().registerEdit(edit);
        edit = null;
    }

    @Override
    public MarkerInteractionHandler.EditMode checkEditMode(Point mousePos) {
        if(marker.getShape() == PointMarkerShape.DOT) return MarkerInteractionHandler.EditMode.NONE;
        return super.checkEditMode(mousePos);
    }

    @Override
    public Marker getCurrentFocused() {
        return marker;
    }
}

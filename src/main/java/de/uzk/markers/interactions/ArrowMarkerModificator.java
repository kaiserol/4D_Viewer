package de.uzk.markers.interactions;

import de.uzk.edit.markers.MoveArrowEdit;
import de.uzk.edit.markers.ResizeArrowEdit;
import de.uzk.markers.ArrowMarker;
import de.uzk.markers.Marker;

import java.awt.*;
import java.awt.geom.Point2D;

import static de.uzk.Main.workspace;

public class ArrowMarkerModificator implements MarkerModificator {
    private final ArrowMarker marker;
    private DragPoint dragPoint;
    private MoveArrowEdit moveEdit;
    private ResizeArrowEdit resizeEdit;

    public ArrowMarkerModificator(ArrowMarker marker) {
        this.marker = marker;
    }


    @Override
    public void handleRotate(Point mousePos) {
        // noop
    }

    @Override
    public void beginResize() {
        if(dragPoint == null) return;
        resizeEdit = new ResizeArrowEdit(marker, dragPoint == DragPoint.TIP);
    }

    @Override
    public void handleResize(Point mousePos) {

        if (dragPoint == DragPoint.START) {
            Point2D start = marker.getStart();
            resizeEdit.resize(mousePos.x - start.getX(), mousePos.y - start.getY());
            marker.setStart(mousePos);
        }
        else if (dragPoint == DragPoint.TIP) {
            Point2D tip = marker.getTip();
            resizeEdit.resize(mousePos.x -  tip.getX(), mousePos.y - tip.getY());
            marker.setTip(mousePos);
        }

    }

    @Override
    public void finishResize() {
        workspace.getEditManager().registerEdit(resizeEdit);
        resizeEdit = null;
    }

    @Override
    public void beginMove() {
        moveEdit = new MoveArrowEdit(this.marker);
    }

    @Override
    public void handleMove(Point mousePos) {
        Point2D start = marker.getStart();
        Point2D tip = marker.getTip();
        double dx = mousePos.x - start.getX();
        double dy = mousePos.y - start.getY();
        marker.setStart(new Point2D.Double(start.getX() + dx, start.getY() + dy));
        marker.setTip(new Point2D.Double(tip.getX() + dx, tip.getY() + dy));

        moveEdit.move(dx, dy);
    }

    @Override
    public void finishMove() {
        workspace.getEditManager().registerEdit(moveEdit);
        moveEdit = null;
    }

    @Override
    public MarkerInteractionHandler.EditMode checkEditMode(Point mousePos) {
        int distance = Marker.LINE_WIDTH * Marker.LINE_WIDTH;
        if (mousePos.distance(marker.getStart()) <= distance) {
            dragPoint = DragPoint.START;

        } else if (mousePos.distance(marker.getTip()) <= distance) {
            dragPoint = DragPoint.TIP;
        } else {
            dragPoint = null;
        }
        return MarkerInteractionHandler.EditMode.RESIZE;
    }

    @Override
    public Marker getCurrentFocused() {
        return marker;
    }

    private enum DragPoint {
        START, TIP
    }
}

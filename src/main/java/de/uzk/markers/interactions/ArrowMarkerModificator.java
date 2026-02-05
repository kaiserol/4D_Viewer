package de.uzk.markers.interactions;

import de.uzk.edit.markers.MoveArrowEdit;
import de.uzk.edit.markers.ResizeArrowEdit;
import de.uzk.markers.ArrowMarker;
import de.uzk.markers.Marker;

import java.awt.*;

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
            Point start = marker.getStart();
            resizeEdit.resize(mousePos.x - start.x, mousePos.y - start.y);
            marker.setStart(mousePos);
        }
        else if (dragPoint == DragPoint.TIP) {
            Point tip = marker.getTip();
            resizeEdit.resize(mousePos.x -  tip.x, mousePos.y - tip.y);
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

        Point start = marker.getStart();
        Point tip = marker.getTip();
        int dx = mousePos.x - start.x;
        int dy = mousePos.y - start.y;
        marker.setStart(new Point(start.x + dx, start.y + dy));
        marker.setTip(new Point(tip.x + dx, tip.y + dy));

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

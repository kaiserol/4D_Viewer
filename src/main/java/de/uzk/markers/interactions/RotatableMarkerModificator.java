package de.uzk.markers.interactions;

import de.uzk.edit.markers.MoveRotatableEdit;
import de.uzk.edit.markers.RotateMarkerEdit;
import de.uzk.markers.Marker;
import de.uzk.markers.RotatableMarker;
import de.uzk.utils.NumberUtils;

import java.awt.*;
import java.awt.geom.Point2D;

import static de.uzk.Main.workspace;

public abstract class RotatableMarkerModificator<T extends RotatableMarker> implements MarkerModificator {
    protected final T marker;
    protected RotateMarkerEdit edit;
    private MoveRotatableEdit moveEdit = null;

    protected RotatableMarkerModificator(T marker) {
        this.marker = marker;
    }

    @Override
    public void beginRotate() {
        edit = new RotateMarkerEdit(marker);
    }

    @Override
    public void handleRotate(Point mousePos) {
        Point2D center = marker.getCenter();
        // Winkel zwischen Mauszeiger und Mittelpunkt des Markers
        double newAngleRadians = Math.atan2(mousePos.y - center.getY(), mousePos.x - center.getX());
        // 90° Addieren, damit der Dragpunkt (oben in der Mitte) unter dem Mauszeiger bleibt
        int newAngle = NumberUtils.normalizeAngle((int) Math.toDegrees(newAngleRadians) + 90);
        int dTheta = newAngle - marker.getRotation();
        marker.setRotation(newAngle);
        edit.rotate(dTheta);
    }

    @Override
    public void beginMove() {
        moveEdit = new MoveRotatableEdit(marker);
    }

    @Override
    public void handleMove(Point mousePos) {
        double theta = Math.toRadians(marker.getRotation());
        double width = marker.getWidth();
        double height = marker.getHeight();
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);
        double cx = (width * cos - height * sin) / 2;
        double cy = (height * cos + width * sin) / 2;
        double x = mousePos.x + cx;
        double y = mousePos.y + cy;
        Point2D current = marker.getCenter();
        marker.setCenter(new Point2D.Double(x, y));
        moveEdit.move(x - current.getX(), y - current.getY());
    }

    @Override
    public void finishMove() {
        workspace.getEditManager().registerEdit(moveEdit);
        moveEdit = null;
    }

    @Override
    public MarkerInteractionHandler.EditMode checkEditMode(Point mousePos) {
        Point2D rotPoint = marker.getRotatePoint();
        if (mousePos.distance(rotPoint) < Marker.LINE_WIDTH * Marker.LINE_WIDTH) {
            return MarkerInteractionHandler.EditMode.ROTATE;
        } else {
            return MarkerInteractionHandler.EditMode.RESIZE;
        }
    }

    @Override
    public void finishRotate() {
        workspace.getEditManager().registerEdit(edit);
        edit = null;
    }
}

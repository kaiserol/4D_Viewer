package de.uzk.markers.interactions;

import de.uzk.edit.markers.ResizeMarkerEdit;
import de.uzk.markers.Marker;
import de.uzk.markers.ResizableMarker;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import static de.uzk.Main.workspace;

public class ResizableMarkerModificator<T extends ResizableMarker> extends RotatableMarkerModificator<T> {
    private DragPoint dragPoint = null;
    private ResizeMarkerEdit resizeEdit = null;

    public ResizableMarkerModificator(T marker) {
        super(marker);
    }

    @Override
    public void beginResize() {
        resizeEdit = new ResizeMarkerEdit(marker);
    }

    @Override
    public void handleResize(Point mousePos) {
        if (dragPoint == null) return;

        double originalWidth = marker.getWidth();
        double originalHeight = marker.getHeight();
        double originalX = marker.getCenter().getX();
        double originalY = marker.getCenter().getY();

        double x = originalX;
        double y = originalY;

        // Maße des Markers.
        double width = originalWidth;
        double height = originalHeight;

        Point2D derotatedMousePos = derotate(mousePos);
        Point2D dragStart = derotate(marker.getScalePoints()[dragPoint.ordinal()]);

        double dx = (derotatedMousePos.getX() - dragStart.getX());
        double dy = (derotatedMousePos.getY() - dragStart.getY());

        width += dragPoint.x() * dx;
        height += dragPoint.y() * dy;

        double theta = Math.toRadians(marker.getRotation());
        double sin = Math.sin(theta);
        double cos = Math.cos(theta);

        if (dragPoint.isCenter()) {
            // Nur entlang der y-Achse verschieben
            x -= ((dy / 2.) * sin);
            y += ((dy / 2.) * cos);
        } else if (dragPoint.isMiddle()) {
            // Nur entlang der x-Achse Verschieben
            x += ((dx / 2.) * cos);
            y += ((dx / 2.) * sin);
        } else {

            x += ((dx / 2.) * cos - (dy / 2.) * sin);
            y += ((dy / 2.) * cos + (dx / 2.) * sin);
        }

        /*
        Berechne, ob der "tatsächliche" Dragpoint ausgetauscht werden muss, falls eine der Markergrenzen
        "überschritten wurde". Sonst kommt es zu extrem(!) merkwürdigem Verhalten.
        */

        boolean flippedX = false, flippedY = false;
        if (width < 0) {
            width = -width;
            x -= width / 2;
            flippedX = true;
        }
        if (height < 0) {
            height = -height;
            y -= height / 2;
            flippedY = true;
        }

        if (flippedX && flippedY) { // Eine Ecke wurde über ihr gegenüber hinweggezogen
            dragPoint = dragPoint.getOpposite();
        } else if (flippedX) {
            dragPoint = dragPoint.mirrorY();
        } else if (flippedY) {
            dragPoint = dragPoint.mirrorX();
        }

        marker.setSize(width, height);
        marker.setCenter(new Point2D.Double(x, y));
        resizeEdit.resize(width - originalWidth, height - originalHeight, x - originalX, y - originalY);
    }

    @Override
    public void finishResize() {
        if(edit == null) return;
        workspace.getEditManager().registerEdit(resizeEdit);
        resizeEdit = null;
    }

    @Override
    public Marker getCurrentFocused() {
        return marker;
    }

    @Override
    public MarkerInteractionHandler.EditMode checkEditMode(Point mousePos) {
        Point2D[] scalePoints = marker.getScalePoints();
        for (int i = 0; i < scalePoints.length; i++) {
            if (mousePos.distance(scalePoints[i]) < Marker.LINE_WIDTH * Marker.LINE_WIDTH) {
                dragPoint = DragPoint.values()[i];
                return MarkerInteractionHandler.EditMode.RESIZE;
            }
        }

        return super.checkEditMode(mousePos);
    }

    // Hilfsfunktion; Rotiert `p` gegen den Uhrzeigersinn um `selectedMarker.getRotation()` Grad.
    private Point2D derotate(Point2D p) {
        return AffineTransform.getRotateInstance(-Math.toRadians(marker.getRotation())).transform(p, null);
    }

    private enum DragPoint {
        TOP_LEFT, MIDDLE_LEFT, BOTTOM_LEFT, TOP_CENTER, BOTTOM_CENTER, TOP_RIGHT, MIDDLE_RIGHT, BOTTOM_RIGHT;

        public DragPoint getOpposite() {
            return switch (this) {
                case TOP_LEFT -> BOTTOM_RIGHT;
                case TOP_CENTER -> BOTTOM_CENTER;
                case TOP_RIGHT -> BOTTOM_LEFT;
                case MIDDLE_LEFT -> MIDDLE_RIGHT;
                case MIDDLE_RIGHT -> MIDDLE_LEFT;
                case BOTTOM_LEFT -> TOP_RIGHT;
                case BOTTOM_CENTER -> TOP_CENTER;
                case BOTTOM_RIGHT -> TOP_LEFT;
            };
        }

        public DragPoint mirrorX() {
            return switch (this) {
                case TOP_LEFT -> BOTTOM_LEFT;
                case TOP_CENTER -> BOTTOM_CENTER;
                case TOP_RIGHT -> BOTTOM_RIGHT;
                case BOTTOM_LEFT -> TOP_LEFT;
                case BOTTOM_CENTER -> TOP_CENTER;
                case BOTTOM_RIGHT -> TOP_RIGHT;
                default -> this;
            };
        }

        public DragPoint mirrorY() {
            return switch (this) {
                case TOP_LEFT -> TOP_RIGHT;
                case TOP_CENTER -> TOP_CENTER;
                case TOP_RIGHT -> TOP_LEFT;
                case BOTTOM_LEFT -> BOTTOM_RIGHT;
                case BOTTOM_CENTER -> BOTTOM_CENTER;
                case BOTTOM_RIGHT -> BOTTOM_LEFT;
                default -> this;
            };
        }

        public boolean isMiddle() {
            return this == MIDDLE_LEFT || this == MIDDLE_RIGHT;
        }

        public boolean isCenter() {
            return this == TOP_CENTER || this == BOTTOM_CENTER;
        }

        public int x() {
            return switch (this) {
                case TOP_LEFT, MIDDLE_LEFT, BOTTOM_LEFT -> -1;
                case TOP_CENTER, BOTTOM_CENTER -> 0;
                case TOP_RIGHT, MIDDLE_RIGHT, BOTTOM_RIGHT -> 1;
            };
        }

        public int y() {
            return switch (this) {
                case TOP_LEFT, TOP_CENTER, TOP_RIGHT -> -1;
                case MIDDLE_LEFT, MIDDLE_RIGHT -> 0;
                case BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT -> 1;
            };
        }
    }
}

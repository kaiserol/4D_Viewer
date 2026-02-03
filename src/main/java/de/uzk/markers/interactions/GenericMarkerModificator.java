package de.uzk.markers.interactions;

import de.uzk.markers.AbstractMarker;
import de.uzk.markers.GenericMarker;
import de.uzk.utils.NumberUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class GenericMarkerModificator implements MarkerModificator {
    private final GenericMarker marker;
    private DragPoint dragPoint = null;

    public GenericMarkerModificator(GenericMarker marker) {
        this.marker = marker;
    }



    @Override
    public void handleRotate(Point mousePos) {
        Point center = marker.getPos();

        // Winkel zwischen Mauszeiger und Mittelpunkt des Markers
        double newAngleRadians = Math.atan2(mousePos.y - center.y, mousePos.x - center.x);
        // 90° Addieren, damit der Dragpunkt (oben in der Mitte) unter dem Mauszeiger bleibt
        int newAngle = NumberUtils.normalizeAngle((int)Math.toDegrees(newAngleRadians) + 90);
        marker.setRotation(newAngle);
    }

    @Override
    public void handleResize(Point mousePos) {
        if(dragPoint == null) return;
        double x = marker.getPos().x;
        double y = marker.getPos().y;


        // Maße des Markers.
        double width = marker.getSize().width;
        double height = marker.getSize().height;

        mousePos = derotate(mousePos);
        Point dragStart = derotate(marker.getScalePoints()[dragPoint.ordinal()]);

        double dx =  (mousePos.getX() - dragStart.getX());
        double dy =  (mousePos.getY() - dragStart.getY());


        width += dragPoint.x() * dx;
        height += dragPoint.y() * dy;

        double theta =  Math.toRadians(marker.getRotation()) ;
        double sin = Math.sin(theta);
        double cos = Math.cos(theta);



        if(dragPoint.isCenter()) {
            // Nur entlang der y-Achse verschieben
            x -= ( (dy / 2.) * sin);
            y += ((dy/ 2.) * cos);
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
            x -= width/2;
            flippedX = true;
        }
        if (height < 0) {
            height = -height;
            y -= height/2;
            flippedY = true;
        }

        if (flippedX && flippedY) { // Eine Ecke wurde über ihr gegenüber hinweggezogen
            dragPoint = dragPoint.getOpposite();
        } else if(flippedX) {
            dragPoint = dragPoint.mirrorY();
        } else if(flippedY) {
            dragPoint = dragPoint.mirrorX();
        }

        // Rundungsfehler werden bei einfachem Cast sonst schnell visuell sichtbar
        x = Math.round(x);
        y = Math.round(y);
        width = Math.round(width);
        height = Math.round(height);

        marker.setSize(new Dimension((int)width, (int)height));
        marker.setPos(new Point((int)x, (int)y));

    }

    @Override
    public void handleMove(Point mousePos) {
        double theta = Math.toRadians(marker.getRotation());
        Dimension size = marker.getSize();
        double cos =  Math.cos(theta);
        double sin = Math.sin(theta);

        int x = mousePos.x + (int)(size.width * cos - size.height * sin) / 2;
        int y = mousePos.y + (int)(size.height * cos + size.width * sin) / 2;
        marker.setPos(new Point(x, y));
    }

    @Override
    public AbstractMarker getCurrentFocused() { return marker; }

    @Override
    public MarkerInteractionHandler.EditMode checkEditMode(Point mousePos) {
        Point[] scalePoints = marker.getScalePoints();
        for(int i = 0; i < scalePoints.length; i++) {
            if (mousePos.distance(scalePoints[i]) < AbstractMarker.LINE_WIDTH * AbstractMarker.LINE_WIDTH) {
                dragPoint = DragPoint.values()[i];
                return MarkerInteractionHandler.EditMode.RESIZE;
            }
        }

        Point rotPoint = marker.getRotatePoint();
        if (mousePos.distance(rotPoint) < AbstractMarker.LINE_WIDTH * AbstractMarker.LINE_WIDTH) {

            return MarkerInteractionHandler.EditMode.ROTATE;
        } else {
            return MarkerInteractionHandler.EditMode.RESIZE;
        }

    }

    // Hilfsfunktion; Rotiert `p` gegen den Uhrzeigersinn um `selectedMarker.getRotation()` Grad.
    private Point derotate(Point p) {
        Point2D derotated = AffineTransform.getRotateInstance(-Math.toRadians(marker.getRotation())).transform(p, null);
        return new Point((int) derotated.getX(), (int) derotated.getY());
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

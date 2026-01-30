package de.uzk.markers;


import de.uzk.image.ImageEditor;
import org.intellij.lang.annotations.MagicConstant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import static de.uzk.Main.workspace;

/**
 * {@link java.awt.event.MouseAdapter}-Override, der die folgenden Funktionen zur Markerbearbeitung bereitstellt:
 * <ul>
 *  <li>Verschieben von Markern über Mausziehen</li>
 *  <li>Selektieren von Markern durch Doppelklicken</li>
 *  <li>Drehen und Skalieren von selektierten Markern</li>
 * </ul>
 * Andere Markereigenschaften werden vom Nutzer über einen UI-Dialog verändert (siehe {@link de.uzk.gui.marker.MarkerEditor})
 *
 * @see de.uzk.image.ImageEditor
 * @see de.uzk.gui.SensitiveImagePanel
 * */
public class MarkerInteractionHandler extends MouseAdapter {

    private static final int[] DRAG_CURSORS = {Cursor.NW_RESIZE_CURSOR, Cursor.W_RESIZE_CURSOR, Cursor.SW_RESIZE_CURSOR, Cursor.N_RESIZE_CURSOR, Cursor.S_RESIZE_CURSOR, Cursor.NE_RESIZE_CURSOR, Cursor.E_RESIZE_CURSOR, Cursor.SE_RESIZE_CURSOR};

    private final ImageEditor imageEditor;
    private Marker selectedMarker;
    private EditMode editMode = EditMode.NONE;
    private DragPoint dragPoint = null;

    public MarkerInteractionHandler(ImageEditor imageEditor) {
        this.imageEditor = imageEditor;
    }

    //region Maus-Tracking
    //region MouseAdapter Overrides
    @Override
    public void mouseMoved(MouseEvent e) {
        if (editMode == EditMode.RESIZE || editMode == EditMode.ROTATE) {
            checkHoveringDragPoint(e);
        } else {
            checkHoveringMarker(e);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (selectedMarker == null) return;

        if (e.getClickCount() >= 2) {
            editMode = EditMode.RESIZE;
            imageEditor.setFocusedMarker(selectedMarker);
            setCursorAndRerender(e.getComponent(), Cursor.CROSSHAIR_CURSOR);

        } else {
            this.editMode = EditMode.NONE;
            imageEditor.setFocusedMarker(null);
            checkHoveringMarker(e);
        }
        imageEditor.updateImage(false);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (selectedMarker == null) {
            setCursorAndRerender(e.getComponent(), Cursor.DEFAULT_CURSOR);
            return;
        }

        if (editMode == EditMode.NONE) {
            editMode = EditMode.MOVE;
            setCursorAndRerender(e.getComponent(), Cursor.MOVE_CURSOR);
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (editMode == EditMode.MOVE) {
            editMode = EditMode.NONE;
            setCursorAndRerender(e.getComponent(), selectedMarker == null ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (selectedMarker == null) return;
        Point actual = getActualPoint(e.getPoint());
        if (editMode == EditMode.MOVE) {
            handleMove(actual);
        } else if (editMode == EditMode.RESIZE && dragPoint != null) {
            handleResize(actual);
        } else if (editMode == EditMode.ROTATE) {
            handleRotate(actual);
        }
    }
    //endregion

    //region Maustracking-Hilfsmethoden
    private void checkHoveringMarker(MouseEvent e) {
        Component target = e.getComponent();
        Point actual = getActualPoint(e.getPoint());
        for (Marker m : workspace.getMarkers().getMarkersForImage(workspace.getTime())) {
            Shape area = m.getLabelArea(target.getGraphics());
            if (area.contains(actual)) {
                if (selectedMarker == null) {
                    setCursorAndRerender(target, Cursor.HAND_CURSOR);
                    selectedMarker = m;
                }
                return;
            }
        }

        selectedMarker = null;
        setCursorAndRerender(target, Cursor.DEFAULT_CURSOR);
    }

    /** Überprüfe, ob der Mauszeiger sich über einem der Drag-Points des aktuell ausgewählten Markers befindet.
     * Diese Methode geht davon aus, dass momentan ein Marker ausgewählt ist, und berechnet den korrekten <code>EditMode</code>
     * aus dem Punkt, der gehovered wird.
     *
     * @param e Das Event, dessen Position untersucht werden soll
     * */
    @SuppressWarnings("MagicConstant")
    private void checkHoveringDragPoint(MouseEvent e) {
        Component target = e.getComponent();
        Point actual = getActualPoint(e.getPoint());
        Point[] dragPoints = selectedMarker.getScalePoints();

        for (int i = 0; i < dragPoints.length; i++) {

            if (actual.distance(dragPoints[i]) < Marker.LINE_WIDTH * Marker.LINE_WIDTH) {
                setCursorAndRerender(target, DRAG_CURSORS[i]);
                dragPoint = DragPoint.values()[i];
                return;
            }
        }
        dragPoint = null;

        Point rotPoint = selectedMarker.getRotatePoint();
        if (actual.distance(rotPoint) < Marker.LINE_WIDTH * Marker.LINE_WIDTH) {
            setCursorAndRerender(target, Cursor.WAIT_CURSOR);
            editMode = EditMode.ROTATE;
        } else {
            editMode = EditMode.RESIZE;
            setCursorAndRerender(target, Cursor.CROSSHAIR_CURSOR);
        }
    }

    /** Berechnet die "tatsächliche" Position des Mauszeigers.
     * Diese Hilfsmethode ist das Herzstück von MarkerInteractionHandler – sie ermittelt die Position des
     * Mauszeigers relativ zur Zeichenebene, unter Berücksichtigung der Tatsache, dass diese auf verschiedenste
     * Arten skaliert, rotiert usw. wurde.
     *
     * @param event "physische" Position des Mauszeigers
     * @return "tatsächliche" Position des Mauszeigers
    * */
    private Point getActualPoint(Point event) {
        AffineTransform transform = imageEditor.getMarkerTransform();

        try {
            Point2D actual = transform.inverseTransform(event, null);
            return new Point((int) (actual.getX()), (int) (actual.getY()));
        } catch (NoninvertibleTransformException ex) {
            throw new IllegalStateException("Nur bijektive Transformationen (Rotation, Translation, Skalierung) werden verwendet – wie konnte das passieren?", ex);
        }
    }
    //endregion
    //endregion

    /**
     * Setzt den Bearbeitungszustand auf Standardwerte zurück.
     * */
    public void unselectMarker() {
        selectedMarker = null;
        editMode = EditMode.NONE;
    }

    //region Berechnungsmethoden

    /**
     * Berechnet die Richtung, um die der ausgewählte Marker rotiert werden soll, und führt die Rotation durch.
     * Der Winkel wird basierend auf dem Abstand zwischen dem Rotationspunkt und dem Mauszeiger bestimmt.
     *
     * @param mousePos "tatsächliche" Positon des Mauszeigers. Siehe <code>getActualPoint</code>.
     * */
    private void handleRotate(Point mousePos) {
        mousePos = derotate(mousePos);
        int distance = mousePos.x - derotate(selectedMarker.getRotatePoint()).x;
        int direction = distance < 0 ? -1 : 1;
        selectedMarker.rotate(distance / 100 + direction);
        imageEditor.updateImage(false);
    }

    /**
     * Berechnet die Verschiebung, um die der ausgewählte Marker rotiert werden soll, und führt die Verschiebung durch.
     * Der Winkel wird basierend auf der Position des Mauszeigers bestimmt.
     *
     * @param mousePos "tatsächliche" Positon des Mauszeigers. Siehe <code>getActualPoint</code>.
     * */
    private void handleMove(Point mousePos) {
        double theta = Math.toRadians(selectedMarker.getRotation());
        Dimension size = selectedMarker.getSize();
        double cos =  Math.cos(theta);
        double sin = Math.sin(theta);

        int x = mousePos.x + (int)(size.width * cos - size.height * sin) / 2;
        int y = mousePos.y + (int)(size.height * cos + size.width * sin) / 2;
        selectedMarker.setPos(new Point(x, y));
        imageEditor.updateImage(false);
    }

    /**
     * Berechnet  basierend auf der Position des Mauszeigers und des Momentan ausgewählten Dragpoints
     * die Vergrößerung sowie die dafür nötige Verschiebung des Mittelpunktes. Letztere ist nötig, weil
     * die Markerposition dessen Mittelpunkt repräsentiert, das visuelle Zeichnen des Markers jedoch in
     * der oberen linken Ecke beginnt.
     *
     * @param mousePos "tatsächliche" Positon des Mauszeigers. Siehe <code>getActualPoint</code>.
     * */
    private void handleResize(Point mousePos) {
        // Position des Markers
        double x = selectedMarker.getPos().x;
        double y = selectedMarker.getPos().y;


        // Maße des Markers.
        double width = selectedMarker.getSize().width;
        double height = selectedMarker.getSize().height;

        mousePos = derotate(mousePos);
        Point dragStart = derotate(selectedMarker.getScalePoints()[dragPoint.ordinal()]);

        double dx =  (mousePos.getX() - dragStart.getX());
        double dy =  (mousePos.getY() - dragStart.getY());


        width += dragPoint.x() * dx;
        height += dragPoint.y() * dy;

        double theta = Math.toRadians(selectedMarker.getRotation());
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

        selectedMarker.setSize(new Dimension((int)width, (int)height));
        selectedMarker.setPos(new Point((int)x, (int)y));
        imageEditor.updateImage(false);
    }

    //endregion

    //region Sonstige Hilfsmethoden
    private void setCursorAndRerender(Component target, @MagicConstant(valuesFromClass = Cursor.class) int cursorType) {
        if (target.getCursor().getType() == cursorType) return;
        target.setCursor(Cursor.getPredefinedCursor(cursorType));
        // Um die aufrufende Methode nicht durch einen repaint zu blocken (vor allem bei Mehrfachaufrufen)
        SwingUtilities.invokeLater(target::repaint);
    }

    // Hilfsfunktion; Rotiert `p` gegen den Uhrzeigersinn um `selectedMarker.getRotation()` Grad.
    private Point derotate(Point p) {
        Point2D derotated = AffineTransform.getRotateInstance(-Math.toRadians(selectedMarker.getRotation())).transform(p, null);
        return new Point((int) derotated.getX(), (int) derotated.getY());
    }
    //endregion

    //region innere Enums
    private enum EditMode {
        NONE, MOVE, RESIZE, ROTATE
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
    //endregion


}

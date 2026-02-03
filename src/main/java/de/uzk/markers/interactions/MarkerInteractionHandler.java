package de.uzk.markers.interactions;


import de.uzk.image.ImageEditor;
import de.uzk.markers.AbstractMarker;
import de.uzk.markers.ArrowMarker;
import de.uzk.markers.GenericMarker;
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
    private MarkerModificator selectedMarker;
    private EditMode editMode = EditMode.NONE;

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
            imageEditor.setFocusedMarker(selectedMarker.getCurrentFocused());
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
            selectedMarker.handleMove(actual);
            imageEditor.updateImage(false);
        } else if (editMode == EditMode.RESIZE) {
            selectedMarker.handleResize(actual);
            imageEditor.updateImage(false);
        } else if (editMode == EditMode.ROTATE) {
            selectedMarker.handleRotate(actual);
            imageEditor.updateImage(false);
        }
    }
    //endregion

    //region Maustracking-Hilfsmethoden
    private void checkHoveringMarker(MouseEvent e) {
        Component target = e.getComponent();
        Point actual = getActualPoint(e.getPoint());
        for (AbstractMarker m : workspace.getMarkers().getMarkersForImage(workspace.getTime())) {
            Shape area = m.getLabelArea(target.getGraphics());
            if (area.contains(actual)) {
                if (selectedMarker == null) {
                    setCursorAndRerender(target, Cursor.HAND_CURSOR);
                    selectedMarker = m.getSuitableModificator();
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
        Point actual = getActualPoint(e.getPoint());
        editMode = selectedMarker.checkEditMode(actual);
        if(editMode == EditMode.RESIZE) {
            setCursorAndRerender(e.getComponent(), Cursor.HAND_CURSOR);
        } else if (editMode == EditMode.ROTATE) {
            setCursorAndRerender(e.getComponent(), Cursor.MOVE_CURSOR);
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
     * Der Winkel wird basierend auf dem Winkel zwischen dem Markermittelpunkt und dem Mauszeiger bestimmt.
     *
     * @param mousePos "tatsächliche" Positon des Mauszeigers. Siehe <code>getActualPoint</code>.
     * */
    private void handleRotate(Point mousePos) {
        if(selectedMarker instanceof GenericMarker marker) {

        }
    }

    /**
     * Berechnet die Verschiebung, um die der ausgewählte Marker rotiert werden soll, und führt die Verschiebung durch.
     * Der Winkel wird basierend auf der Position des Mauszeigers bestimmt.
     *
     * @param mousePos "tatsächliche" Positon des Mauszeigers. Siehe <code>getActualPoint</code>.
     * */
    private void handleMove(Point mousePos) {

    }

    /**
     * Berechnet  basierend auf der Position des Mauszeigers und des Momentan ausgewählten Dragpoints
     * die Vergrößerung sowie die dafür nötige Verschiebung des Mittelpunktes. Letztere ist nötig, weil
     * die Markerposition dessen Mittelpunkt repräsentiert, das visuelle Zeichnen des Markers jedoch in
     * der oberen linken Ecke beginnt.
     *
     * @param mousePos "tatsächliche" Positon des Mauszeigers. Siehe <code>getActualPoint</code>.
     * */
    private void handleResize(Point mousePos, GenericMarker marker) {
        // Position des Markers

    }

    private void handleResize(Point mousePos, ArrowMarker marker) {
        System.out.println("wha t");
    }

    private void handleResize(Point mousePos, AbstractMarker marker) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //endregion

    //region Sonstige Hilfsmethoden
    private void setCursorAndRerender(Component target, @MagicConstant(valuesFromClass = Cursor.class) int cursorType) {
        if (target.getCursor().getType() == cursorType) return;
        target.setCursor(Cursor.getPredefinedCursor(cursorType));
        // Um die aufrufende Methode nicht durch einen repaint zu blocken (vor allem bei Mehrfachaufrufen)
        SwingUtilities.invokeLater(target::repaint);
    }


    //endregion

    //region innere Enums
    public enum EditMode {
        NONE, MOVE, RESIZE, ROTATE
    }


    //endregion


}

package de.uzk.markers;


import de.uzk.image.ImageEditor;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import static de.uzk.Main.workspace;

public class MarkerEditor extends MouseAdapter {
    private enum EditMode {
        NONE,
        MOVE,
        RESIZE,
    }

    private final ImageEditor imageEditor;
    private Marker selectedMarker;
    private EditMode editMode = EditMode.NONE;

    public MarkerEditor(ImageEditor imageEditor) {
        this.imageEditor = imageEditor;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        checkHoveringMarker(e);
    }

    @Override public void mouseClicked(MouseEvent e) {
        if(selectedMarker != null && e.getClickCount() == 2) {
            editMode = EditMode.RESIZE;
            setCursorAndRerender(e.getComponent(), Cursor.CROSSHAIR_CURSOR);

        } else if(e.getClickCount() == 1) {
            this.editMode = EditMode.NONE;
            checkHoveringMarker(e);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(selectedMarker == null) {
            setCursorAndRerender(e.getComponent(), Cursor.DEFAULT_CURSOR);
            return;
        }

        if(editMode == EditMode.NONE) {
            editMode = EditMode.MOVE;
            setCursorAndRerender(e.getComponent(), Cursor.MOVE_CURSOR);
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(editMode == EditMode.MOVE) {
            editMode = EditMode.NONE;
            setCursorAndRerender(e.getComponent(), selectedMarker == null ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR);
        }
       }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(selectedMarker == null) return;
        if(editMode == EditMode.MOVE) {
            selectedMarker.setX(e.getX());
            selectedMarker.setY(e.getY());
            imageEditor.updateImage();
        }
    }

    private void setCursorAndRerender(Component target, int cursorType) {
        if(target.getCursor().getType() == cursorType) return;
        target.setCursor(Cursor.getPredefinedCursor(cursorType));
        target.repaint();
    }

    private void checkHoveringMarker(MouseEvent e) {
        Component target = e.getComponent();
        Point2D actual;
        try {
            actual = imageEditor.getCurrentTransform().inverseTransform(e.getPoint(), null);
        } catch (NoninvertibleTransformException ex) {
            throw new RuntimeException("Nur bijektive Transformationen (Rotation, Translation, Skalierung) werden verwendet – wie konnte das passieren?", ex);
        }

        for (Marker m : workspace.getMarkers().getMarkersForImage(workspace.getTime())) {
            Dimension labelSize = m.getLabelSize(target.getGraphics());
            if(new Rectangle(new Point(m.getX(), m.getY()), labelSize).contains(actual.getX(), actual.getY())) {
                if(selectedMarker == null) {
                    setCursorAndRerender(target, Cursor.HAND_CURSOR);
                    selectedMarker = m;
                }
                return;
            }
        }
        if(editMode != EditMode.RESIZE) {
            selectedMarker = null;
            setCursorAndRerender(target, Cursor.DEFAULT_CURSOR);
        }

    }
}

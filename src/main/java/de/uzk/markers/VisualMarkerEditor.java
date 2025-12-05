package de.uzk.markers;


import de.uzk.image.ImageEditor;
import org.intellij.lang.annotations.MagicConstant;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import static de.uzk.Main.workspace;

public class VisualMarkerEditor extends MouseAdapter {

    private static final int[] DRAG_CURSORS = {Cursor.NW_RESIZE_CURSOR, Cursor.W_RESIZE_CURSOR, Cursor.SW_RESIZE_CURSOR, Cursor.N_RESIZE_CURSOR, Cursor.S_RESIZE_CURSOR, Cursor.NE_RESIZE_CURSOR, Cursor.E_RESIZE_CURSOR, Cursor.SE_RESIZE_CURSOR};

    private final ImageEditor imageEditor;
    private Marker selectedMarker;
    private EditMode editMode = EditMode.NONE;
    private DragPoint dragPoint = null;

    public VisualMarkerEditor(ImageEditor imageEditor) {
        this.imageEditor = imageEditor;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (editMode == EditMode.RESIZE) {
            checkHoveringDragPoint(e);
        } else {
            checkHoveringMarker(e);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (selectedMarker == null) return;
        if (e.getClickCount() == 2) {
            editMode = EditMode.RESIZE;
            selectedMarker.setResizing(true);
            setCursorAndRerender(e.getComponent(), Cursor.CROSSHAIR_CURSOR);

        } else if (e.getClickCount() == 1) {
            this.editMode = EditMode.NONE;
            selectedMarker.setResizing(false);
            checkHoveringMarker(e);
        }
        imageEditor.updateImage();
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
        }
    }

    private void handleMove(Point mousePos) {
        Dimension size = selectedMarker.getSize();
        int x = mousePos.x + size.width / 2;
        int y = mousePos.y + size.height / 2;
        selectedMarker.setPos(new Point(x, y));
        imageEditor.updateImage();
    }

    private void handleResize(Point mousePos) {
        // Position des Markers
        int mx = selectedMarker.getPos().x;
        int my = selectedMarker.getPos().y;

        // Position des Mauszeigers
        int ex = mousePos.x;
        int ey = mousePos.y;

        // Maße des Markers
        int width = selectedMarker.getSize().width;
        int height = selectedMarker.getSize().height;

        // Breiten- und Höhenwachstum
        int dw = 0;
        int dh = 0;

        // Durch den Resize verursachte Verschiebung des Mittelpunktes
        int dx = 0;
        int dy = 0;


        /*
         * Formel für Achse A mit Größe G:
         * dG = (mA - (G / 2) - eA)
         * dA = (dG / 2) * sign(eA - mA)
         */
        switch (dragPoint) {
            case TOP_LEFT -> {
                dw = (mx - width / 2 - ex);
                dh = (my - height / 2 - ey);
                dx = -dw / 2;
                dy = -dh / 2;
            }
            case MIDDLE_LEFT -> {
                dw = (mx - width / 2 - ex);
                dx = -dw / 2;
            }
            case BOTTOM_LEFT -> {
                dw = (mx - width / 2 - ex);
                dh = (ey - height / 2 - my);
                dx = -dw / 2;
                dy = dh / 2;
            }
            case TOP_CENTER -> {
                dh = (my - height / 2 - ey);
                dy = -dh / 2;
            }
            case BOTTOM_CENTER -> {
                dh = (ey - height / 2 - my);
                dy = dh / 2;
            }

            case TOP_RIGHT -> {
                dw = (ex - width / 2 - mx);
                dh = (my - height / 2 - ey);
                dx = dw / 2;
                dy = -dh / 2;
            }
            case MIDDLE_RIGHT -> {
                dw = (ex - width / 2 - mx);
                dx = dw / 2;
            }

            case BOTTOM_RIGHT -> {
                dw = (ex - width / 2 - mx);
                dh = (ey - height / 2 - my);
                dx = dw / 2;
                dy = dh / 2;
            }

        }

        width += dw;
        height += dh;
        mx += dx;
        my += dy;


        boolean flipped = false;
        if (width < 0) {
            width = -width;
            mx -= width;
            flipped = true;
        }
        if (height < 0) {
            height = -height;
            my -= height;
            flipped = true;
        }
        if (flipped) {
            dragPoint = dragPoint.getOpposite();
        }
        selectedMarker.setSize(new Dimension(width, height));
        selectedMarker.setPos(new Point(mx, my));
        imageEditor.updateImage();
    }

    private void setCursorAndRerender(Component target, @MagicConstant(valuesFromClass = Cursor.class) int cursorType) {
        if (target.getCursor().getType() == cursorType) return;
        target.setCursor(Cursor.getPredefinedCursor(cursorType));
        // Um die aufrufende Methode nicht durch einen repaint zu blocken (vor allem bei Mehrfachaufrufen)
        SwingUtilities.invokeLater(target::repaint);
    }

    private void checkHoveringMarker(MouseEvent e) {
        Component target = e.getComponent();
        Point actual = getActualPoint(e.getPoint());
        for (Marker m : workspace.getMarkers().getMarkersForImage(workspace.getTime())) {
            Rectangle area = m.getLabelArea(target.getGraphics());
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

    @SuppressWarnings("MagicConstant")
    private void checkHoveringDragPoint(MouseEvent e) {
        Component target = e.getComponent();
        Point actual = getActualPoint(e.getPoint());
        Point[] dragPoints = Marker.getScalePoints(selectedMarker.getBounds());

        for (int i = 0; i < dragPoints.length; i++) {
            if (actual.distance(dragPoints[i]) < Marker.LINE_WIDTH * Marker.LINE_WIDTH) {
                setCursorAndRerender(target, DRAG_CURSORS[i]);
                dragPoint = DragPoint.values()[i];
                return;
            }
        }
        dragPoint = null;
        setCursorAndRerender(target, Cursor.CROSSHAIR_CURSOR);
    }

    private Point getActualPoint(Point event) {
        try {
            Point2D actual = imageEditor.getCurrentTransform().inverseTransform(event, null);
            return new Point((int) actual.getX(), (int) actual.getY());
        } catch (NoninvertibleTransformException ex) {
            throw new IllegalStateException("Nur bijektive Transformationen (Rotation, Translation, Skalierung) werden verwendet – wie konnte das passieren?", ex);
        }
    }

    private enum EditMode {
        NONE, MOVE, RESIZE,
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
    }


}

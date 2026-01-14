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
        if (editMode == EditMode.RESIZE || editMode == EditMode.ROTATE) {
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

    private void handleRotate(Point actual) {
        actual = derotate(actual);
        int distance = actual.x - derotate(selectedMarker.getRotatePoint()).x;
        int direction = distance < 0 ? -1 : 1;
        selectedMarker.rotate(distance / 100 + direction);
        imageEditor.updateImage(false);
    }

    private void handleMove(Point mousePos) {
        Dimension size = selectedMarker.getSize();
        int x = mousePos.x + size.width / 2;
        int y = mousePos.y + size.height / 2;
        selectedMarker.setPos(new Point(x, y));
        imageEditor.updateImage(false);
    }

    private void handleResize(Point mousePos) {
        // Position des Markers
        int x = selectedMarker.getPos().x;
        int y = selectedMarker.getPos().y;

        // Maße des Markers
        int width = selectedMarker.getSize().width;
        int height = selectedMarker.getSize().height;

        mousePos = derotate(mousePos);
        Point dragStart = derotate(selectedMarker.getScalePoints()[dragPoint.ordinal()]);

        int dx = (int) (mousePos.getX() - dragStart.getX());
        int dy = (int) (mousePos.getY() - dragStart.getY());


        height += dragPoint.y() * dy;
        y += dragPoint.isTop() ? 0 : dy / 2;

        width += dragPoint.x() * dx;
        x += dragPoint.isCenter() ? 0 : dx / 2;

        boolean flipped = false;
        if (width < 0) {
            width = -width;
            x -= width;
            flipped = true;
        }
        if (height < 0) {
            height = -height;
            y -= height;
            flipped = true;
        }
        if (flipped) {
            dragPoint = dragPoint.getOpposite();
        }
        selectedMarker.setSize(new Dimension(width, height));
        selectedMarker.setPos(new Point(x, y));
        imageEditor.updateImage(false);
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

    private Point getActualPoint(Point event) {
        AffineTransform transform = imageEditor.getMarkerTransform();

        try {
            Point2D actual = transform.inverseTransform(event, null);
            return new Point((int) (actual.getX()), (int) (actual.getY()));
        } catch (NoninvertibleTransformException ex) {
            throw new IllegalStateException("Nur bijektive Transformationen (Rotation, Translation, Skalierung) werden verwendet – wie konnte das passieren?", ex);
        }
    }

    // Hilfsfunktion; Rotiert `p` gegen den Uhrzeigersinn um `selectedMarker.getRotation()` Grad.
    private Point derotate(Point p) {
        Point2D derotated = AffineTransform.getRotateInstance(-Math.toRadians(selectedMarker.getRotation())).transform(p, null);
        return new Point((int) derotated.getX(), (int) derotated.getY());
    }

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

        public boolean isTop() {
            return this == TOP_LEFT || this == TOP_CENTER || this == TOP_RIGHT;
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

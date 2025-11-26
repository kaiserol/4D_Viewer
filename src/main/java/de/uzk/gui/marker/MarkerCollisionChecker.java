package de.uzk.gui.marker;

import de.uzk.markers.Marker;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.function.Consumer;

import static de.uzk.Main.logger;
import static de.uzk.Main.workspace;

public class MarkerCollisionChecker extends MouseAdapter {
    private AffineTransform currentAffineTransform = new AffineTransform();
    private Dimension panelInsets;
    private Dimension imageSize;
    private Consumer<Marker> onMarkerClick;
    private Consumer<Marker> onMarkerBeginHover;
    private Consumer<Marker> onMarkerEndHover;

    // region MouseAdapter overrides
    @Override
    public void mouseClicked(MouseEvent e) {
        Marker collided = getCollision(e);
        if (collided != null && onMarkerClick != null) {
            onMarkerClick.accept(collided);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Marker collided = getCollision(e);
        if (collided != null && onMarkerBeginHover != null) {
            onMarkerBeginHover.accept(collided);
        } else if (onMarkerEndHover != null) {
            onMarkerEndHover.accept(null);
        }
    }
    // endregion

    // region Öffentliche Setter
    public void onMarkerClick(Consumer<Marker> onMarkerCollision) {
        this.onMarkerClick = onMarkerCollision;
    }

    public void onMarkerBeginHover(Consumer<Marker> onMarkerBeginHover) {
        this.onMarkerBeginHover = onMarkerBeginHover;
    }

    public void onMarkerEndHover(Consumer<Marker> onMarkerEndHover) {
        this.onMarkerEndHover = onMarkerEndHover;
    }

    public void updateTransform(AffineTransform transform) {
        currentAffineTransform = transform;
    }

    public void updateInsets(Dimension panelInsets, Dimension imageSize) {
        this.panelInsets = panelInsets;
        this.imageSize = imageSize;
    }
    // endregion

    private Marker getCollision(MouseEvent e) {
        Point p = e.getPoint();
        p.translate(-panelInsets.width, -panelInsets.height);

        if (p.getX() < 0 || p.getY() < 0 ||
            p.getX() > imageSize.width || p.getY() > imageSize.height
        ) {
            // User hat nicht auf das Bild geclickt.
            return null;
        }


        Point2D actual;
        try {
            actual = currentAffineTransform.inverseTransform(p, null);
            logger.debug("User: %s, actual: %s".formatted(p, actual));
        } catch (NoninvertibleTransformException ex) {
            throw new RuntimeException("Nur bijektive Transformationen (Rotation, Translation, Skalierung) werden verwendet – wie konnte das passieren?", ex);
        }
        for (Marker m : workspace.getMarkers().getMarkersForImage(workspace.getTime())) {
            Rectangle label = m.getLabelBounds(((Component) e.getSource()).getGraphics());
            if (label.contains(actual)) {
                return m;
            }

        }
        return null;
    }
}

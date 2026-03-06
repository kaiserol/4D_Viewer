package de.uzk.gui;

import de.uzk.markers.interactions.MarkerInteractionHandler;
import de.uzk.utils.GraphicsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

import static de.uzk.Main.workspace;


/**
 * Ein JPanel, das ein Bild anzeigt und Events verfolgt, welche <i>innerhalb des Bildbereichs</i> auftreten.
 *
 * @see MarkerInteractionHandler
 * @see de.uzk.image.ImageEditor
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class SensitiveImagePanel extends JPanel implements MouseListener, MouseMotionListener {
    private BufferedImage currentImage;
    private double scale; // Um welchen Faktor wurde das Bild skaliert, um auf das JPanel zu passen?
    private Dimension insets;
    private Dimension displaySize;
    private final java.util.List<MouseMotionListener> mouseMotionListeners = new ArrayList<>();
    private final java.util.List<MouseListener> mouseListeners = new ArrayList<>();

    public SensitiveImagePanel() {
        super.addMouseListener(this);
        super.addMouseMotionListener(this);
    }


    public void updateImage(BufferedImage image) {
        currentImage = image;
        repaint();
    }

    public void clear() {
        updateImage(null);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = GraphicsUtils.createHighQualityGraphics2D(g);
        if (currentImage != null) {
            scale = GraphicsUtils.getImageScaleFactor(currentImage, getSize());

            int adjustedImageWidth = (int) (currentImage.getWidth() * scale);
            int adjustedImageHeight = (int) (currentImage.getHeight() * scale);
            displaySize = new Dimension(adjustedImageWidth, adjustedImageHeight);
            insets = new Dimension((getWidth() - adjustedImageWidth) / 2, (getHeight() - adjustedImageHeight) / 2);

            g2d.drawImage(currentImage, insets.width, insets.height    , displaySize.width, displaySize.height, null);
        } else {
            g2d.setColor(UIEnvironment.getBackgroundColor());
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    /**
     * Überprüfe, ob `original` innerhalb des Bildbereiches stattgefunden hat. Falls ja, berechne die Koordinaten des Mauszeigers
     * innerhalb des Bilds, und gib das Ergebnis zurück
     *
     * @param original Das `MouseEvent`, das überprüft werden soll.
     * @return Ein zu original identisches Event mit modifizierten Koordinaten, oder null, wenn das Event außerhalb des Bildbereiches liegt
     */
    private MouseEvent shouldTriggerEvent(MouseEvent original) {
        if(insets == null) return null;
        Point originalPoint = original.getPoint();
        int x = (int) (originalPoint.getX() - insets.width );
        int y = (int) (originalPoint.getY() - insets.height );
        if (x < 0 || x > displaySize.width || y < 0 || y > displaySize.height) {
            setCursor(Cursor.getDefaultCursor());
            return null;
        }
        x = (int) (x / scale);
        y = (int) (y / scale);
        return new MouseEvent(original.getComponent(), original.getID(), original.getWhen(), original.getModifiersEx(), x, y, original.getClickCount(), original.isPopupTrigger(), original.getButton());
    }

    //region Overrides
    @Override
    public synchronized void addMouseMotionListener(MouseMotionListener mouseMotionListener) {
        mouseMotionListeners.add(mouseMotionListener);
    }

    @Override
    public synchronized void addMouseListener(MouseListener mouseListener) {
        mouseListeners.add(mouseListener);
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        if((e = shouldTriggerEvent(e)) != null) {
            MouseEvent finalE = e;
            mouseListeners.forEach(listener -> listener.mouseClicked(finalE));
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if((e = shouldTriggerEvent(e)) != null) {
            MouseEvent finalE = e;
            mouseListeners.forEach(listener -> listener.mousePressed(finalE));
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if((e = shouldTriggerEvent(e)) != null) {
            MouseEvent finalE = e;
            mouseListeners.forEach(listener -> listener.mouseReleased(finalE));
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if((e = shouldTriggerEvent(e)) != null) {
            MouseEvent finalE = e;
            mouseListeners.forEach(listener -> listener.mouseEntered(finalE));
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if((e = shouldTriggerEvent(e)) != null) {
            MouseEvent finalE = e;
            mouseListeners.forEach(listener -> listener.mouseExited(finalE));
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if((e = shouldTriggerEvent(e)) != null) {
            MouseEvent finalE = e;
            mouseMotionListeners.forEach(listener -> listener.mouseDragged(finalE));
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if((e = shouldTriggerEvent(e)) != null) {
            MouseEvent finalE = e;
            mouseMotionListeners.forEach(listener ->
            {listener.mouseMoved(finalE);}
            );
        }
    }
    //endregion
}

package de.uzk.gui;

import de.uzk.utils.GraphicsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class SensitiveImagePanel extends JPanel implements MouseListener, MouseMotionListener {
    private BufferedImage currentImage;
    private double scale;
    private Dimension insets;
    private Dimension displaySize;
    private MouseMotionListener mouseMotionListener;
    private MouseListener mouseListener;

    public SensitiveImagePanel() {
        super.addMouseListener(this);
        super.addMouseMotionListener(this);
    }

    @Override
    public void addMouseMotionListener(MouseMotionListener mouseMotionListener) {
        this.mouseMotionListener = mouseMotionListener;
    }

    @Override
    public void addMouseListener(MouseListener mouseListener) {
        this.mouseListener = mouseListener;
    }

    public void updateImage(BufferedImage image) {
        this.currentImage = image;
        this.repaint();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = GraphicsUtils.createHighQualityGraphics2D(g);
        g2d.clearRect(0, 0, getWidth(), getHeight());
        if(this.currentImage != null) {
            scale = GraphicsUtils.getImageScaleFactor(currentImage, this);

            int adjustedImageWidth = (int) (currentImage.getWidth() * scale);
            int adjustedImageHeight = (int) (currentImage.getHeight() * scale);
            displaySize = new Dimension(adjustedImageWidth, adjustedImageHeight);
            insets = new Dimension((getWidth() - adjustedImageWidth) / 2, (getHeight() - adjustedImageHeight) / 2);

            g2d.drawImage(currentImage, insets.width, insets.height, displaySize.width, displaySize.height, null);
        }
    }

    private void maybeTriggerEvent(MouseEvent original, Consumer<MouseEvent> consumer) {
        Point originalPoint = original.getPoint();
        int x = (int) (originalPoint.getX() - insets.width) ;
        int y = (int) (originalPoint.getY() - insets.height) ;
        if(x < 0 || x > displaySize.width || y < 0 || y > displaySize.height) {
            return;
        }
        x  = (int)(x / scale);
        y = (int)(y / scale);
        consumer.accept(new MouseEvent(original.getComponent(), original.getID(), original.getWhen(), original.getModifiersEx(), x, y, original.getClickCount(), original.isPopupTrigger(), original.getButton()));
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        if(mouseListener != null) maybeTriggerEvent(e, mouseListener::mouseClicked);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (mouseListener != null) maybeTriggerEvent(e, mouseListener::mousePressed);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (mouseListener != null) maybeTriggerEvent(e, mouseListener::mouseReleased);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (mouseListener != null) maybeTriggerEvent(e, mouseListener::mouseEntered);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if (mouseListener != null) maybeTriggerEvent(e, mouseListener::mouseExited);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (mouseMotionListener != null) maybeTriggerEvent(e, mouseMotionListener::mouseDragged);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (mouseMotionListener != null) maybeTriggerEvent(e, mouseMotionListener::mouseMoved);
    }
}

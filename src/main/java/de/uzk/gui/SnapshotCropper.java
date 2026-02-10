package de.uzk.gui;

import de.uzk.utils.GraphicsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class SnapshotCropper extends JPanel {
    private final BufferedImage image;
    private final DragHandler dragHandler;

    private final Rectangle imageCropRect;
    private final double scale;

    public SnapshotCropper(BufferedImage image) {
        this.image = image;
        this.imageCropRect = new Rectangle(0, 0, image.getWidth(), image.getHeight());
        this.dragHandler = new DragHandler();
        this.addMouseMotionListener(dragHandler);
        this.addMouseListener(dragHandler);
        this.scale = GraphicsUtils.getImageScaleFactor(image, new Dimension(800, 600));
    }

    @Override
    public Dimension getPreferredSize() {

        return new Dimension((int) (scale * image.getWidth()), (int) (scale * image.getHeight()));
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.clearRect(0, 0, this.getWidth(), this.getHeight());
        g2d.scale(scale, scale);
        g.drawImage(this.image, 0, 0, this.image.getWidth(), this.image.getHeight(), null);

        drawCropRect(g2d);
        dragHandler.drawResizePoints(g2d);
    }

    private void drawCropRect(Graphics2D g2d) {

        g2d.setColor(new Color(0, 0, 0, 100));
        g2d.fillRect(0, 0, image.getWidth(), imageCropRect.y);
        g2d.fillRect(0, imageCropRect.y, imageCropRect.x, image.getHeight() - imageCropRect.y);
        g2d.fillRect(imageCropRect.x + imageCropRect.width, imageCropRect.y, image.getWidth() - imageCropRect.x - imageCropRect.width, image.getHeight() - imageCropRect.y);
        g2d.fillRect(imageCropRect.x, imageCropRect.y + imageCropRect.height, imageCropRect.width, image.getHeight() - imageCropRect.y - imageCropRect.height);
    }

    public BufferedImage getCroppedImage() {
        return this.image.getSubimage(imageCropRect.x, imageCropRect.y, imageCropRect.width, imageCropRect.height);
    }

    private class DragHandler extends MouseAdapter {
        private final Point[] resizePoints;

        public DragHandler() {
            resizePoints = new Point[2];
            updateResizePoints();
        }

        private void updateResizePoints() {
            resizePoints[0] = imageCropRect.getLocation();
            resizePoints[1] = new Point(imageCropRect.x + imageCropRect.width, imageCropRect.y + imageCropRect.height);
        }

        public void drawResizePoints(Graphics2D g2d) {
            updateResizePoints();
            g2d.setColor(Color.WHITE);
            int stroke = 5;
            g2d.setStroke(new BasicStroke(stroke));
            int x = imageCropRect.x;
            int y = imageCropRect.y;
            int width = imageCropRect.width;
            int height = imageCropRect.height;
            int lx = Math.min(25, width);
            int ly = Math.min(25, height);
            g2d.translate(stroke, stroke);
            g2d.drawLine(x, y, x + lx, y);
            g2d.drawLine(x, y, x, y + ly);
            g2d.translate(-2 * stroke, -2 * stroke);
            g2d.drawLine(x + width, y + height, x + width - lx, y + height);
            g2d.drawLine(x + width, y + height, x + width, y + height - lx);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Point eventPoint = new Point((int) (e.getX() / scale), (int) (e.getY() / scale));
            for (int i = 0; i < 2; i++) {
                if (eventPoint.distance(resizePoints[i]) <= 50) {
                    int width = imageCropRect.width;
                    int height = imageCropRect.height;
                    Point pos = imageCropRect.getLocation();
                    if (i == 0) {
                        pos = new Point(Math.max(eventPoint.x, 0), Math.max(eventPoint.y, 0));
                        width += imageCropRect.x - pos.x;
                        height += imageCropRect.y - pos.y;
                    } else {
                        eventPoint = new Point(Math.min(image.getWidth(), eventPoint.x), Math.min(image.getHeight(), eventPoint.y));
                        width = Math.min(image.getWidth() - pos.x, eventPoint.x - imageCropRect.x);
                        height = Math.min(image.getHeight() - pos.y, eventPoint.y - imageCropRect.y);
                    }
                    if (width > 0 && height > 0) {
                        imageCropRect.width = width;
                        imageCropRect.height = height;
                        imageCropRect.setLocation(pos);
                        updateResizePoints();
                    }
                    break;
                }
            }
            repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            Point eventPoint = new Point((int) (e.getX() / scale), (int) (e.getY() / scale));
            for (Point p : resizePoints) {
                if (p.distance(eventPoint) < 50) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                    repaint();
                    return;
                }
            }
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
}

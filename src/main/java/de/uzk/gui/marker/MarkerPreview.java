package de.uzk.gui.marker;

import de.uzk.markers.Marker;
import de.uzk.utils.GraphicsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class MarkerPreview extends JPanel implements MouseListener, MouseMotionListener {
    private final BufferedImage background;
    private final Marker marker;
    private Point dragStart;
    private final MarkerEditor editor;
    private final Dimension dimension;

    private static final int SHRINK_FACTOR = 2;

    public MarkerPreview(BufferedImage background, Marker marker, MarkerEditor editor) {
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.background = background;
        this.marker = marker;
        this.editor = editor;
        Dimension base = new Dimension(this.background.getWidth(), this.background.getHeight());
        base.width /= SHRINK_FACTOR;
        base.height /= SHRINK_FACTOR;
        this.dimension = base;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        double scaleFactor = GraphicsUtils.getImageScaleFactor(this.background, this);

        int width = (int) (this.background.getWidth() * scaleFactor);
        int height = (int) (this.background.getHeight() * scaleFactor);


        g2d.drawImage(this.background, 0, 0, width, height, null);

        marker.draw(g2d, new Rectangle(0, 0, width, height), scaleFactor);
    }

    @Override
    public Dimension getPreferredSize() {
        return this.dimension;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            Point pos = this.getPointRelativeToImage(e.getPoint());
            this.marker.setX(pos.x);
            this.marker.setY(pos.y);
            this.dragStart = pos;
            this.update();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.dragStart = null;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (this.dragStart != null) {
            Point pos = this.getPointRelativeToImage(e.getPoint());


            Rectangle rect = new Rectangle(this.dragStart);
            rect.add(pos);

            this.marker.setX(rect.x);
            this.marker.setY(rect.y);
            this.marker.setWidth(rect.width);
            this.marker.setHeight(rect.height);
            this.update();
        }
    }

    private void update() {
        this.repaint();
        this.editor.changed();
    }

    private Point getPointRelativeToImage(Point2D pointRelativeToWindow) {
        int x = (int) pointRelativeToWindow.getX() - this.getX();
        int y = (int) pointRelativeToWindow.getY() - this.getY();
        x *= SHRINK_FACTOR; // Sonst wird das Rechteck viel kleiner als eigentlich erwartet
        y *= SHRINK_FACTOR;
        return new Point(x, y);
    }


    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }
}

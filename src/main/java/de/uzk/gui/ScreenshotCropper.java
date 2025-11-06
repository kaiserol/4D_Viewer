package de.uzk.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class ScreenshotCropper extends JPanel {
    private final BufferedImage image;

    private int x;
    private int y;
    private int width;
    private int height;

    public ScreenshotCropper(BufferedImage image) {
        this.image = image;
        this.width = image.getWidth();
        this.height = image.getHeight();
        DragHandler dh = new DragHandler();
        this.addMouseMotionListener(dh);
        this.addMouseListener(dh);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(this.image.getWidth(), this.image.getHeight());
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

    @Override
    protected void paintComponent(Graphics g) {
       Graphics2D g2d = (Graphics2D) g;
       g2d.clearRect(0, 0, this.getWidth(), this.getHeight());
        g.drawImage(this.image, 0, 0, this.image.getWidth(), this.image.getHeight(), null);

        g2d.setColor(new Color(0, 0, 0, 100));

        //Blur-Effekte
        //Oben
        g2d.fillRect(0, 0, this.getWidth(), this.y);

        //Unten
        g2d.fillRect(0, this.y + this.height, this.getWidth(), this.getHeight() - this.y - this.height);

        //Links
        g2d.fillRect(0, this.y, this.x, this.height);

        //Rechts
        g2d.fillRect(this.x + this.width, this.y, this.getWidth() - this.x - this.width, this.height);

        }

    public BufferedImage getCroppedImage() {
        return this.image.getSubimage(this.x, this.y, this.width, this.height);

    }

    private class DragHandler extends MouseAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            width = e.getX() - x;
            height = e.getY() - y;
            repaint();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            x = e.getX();
            y = e.getY();
            repaint();
        }
    }
}

package de.uzk.gui.marker;

import de.uzk.markers.Marker;
import de.uzk.markers.MarkerShape;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;

public class GenericMarkerPreview extends JComponent implements MouseListener {
    private final MarkerShape markerShape;
    private final Color markerColor;
    private Runnable onClick;
    private boolean hovering = false;

    public GenericMarkerPreview(MarkerShape markerShape,  Color markerColor) {
        this.addMouseListener(this);
        this.markerShape = markerShape;
        this.markerColor = markerColor;
        this.setToolTipText("Edit marker");
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        if(hovering) {
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
        }
        g2d.setColor(this.markerColor);
        g2d.setStroke(new BasicStroke(3));
        int x = this.getWidth() / 2 - 25;
        int  y = this.getHeight() / 2 - 25;
        int width = 50;
        int height = 50;
        Shape shape = switch (this.markerShape) {
            case RECTANGLE ->
                new Rectangle(x,y,width,height);
            case ELLIPSE ->
                new Ellipse2D.Double(x,y,width,height);
        };
        g2d.draw(shape);

    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(100,100);
    }

    public void setOnClick(Runnable onClick) {
        this.onClick = onClick;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        this.onClick.run();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        this.repaint();
        this.hovering = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        this.repaint();
        this.hovering = false;
    }
}

package de.uzk.markers;

import de.uzk.markers.interactions.MarkerModificator;
import de.uzk.markers.interactions.TextMarkerModificator;
import de.uzk.utils.ColorUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class TextMarker extends ResizableMarker {
    private boolean widthChanged = false;

    @SuppressWarnings("unused")
    public TextMarker() {
        setLabel("DESERIALIZE");
    }

    public TextMarker(Marker abstractMarker) {
        copyFrom(abstractMarker);
    }

    @Override
    public void setWidth(double width) {
        super.setWidth(width);
        widthChanged = true;
    }

    @Override
    public void setHeight(double height) {
        super.setHeight(height);
        widthChanged = false;
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d = (Graphics2D) g2d.create();
        g2d.setFont(g2d.getFont().deriveFont(Font.PLAIN, calculateFontSize(g2d)));
        g2d.setColor(color);
        g2d.transform(getRotationTransform());
        Rectangle2D rect = getShapeBounds();
        g2d.fill(rect);
        g2d.setColor(ColorUtils.getTextColor(color));
        g2d.drawString(label, (int)rect.getX() , (int)(rect.getY()+ rect.getHeight() * 0.75));
    }

    @Override
    public Marker copy() {
        return new TextMarker(this);
    }

    @Override
    public Shape getLabelArea(Graphics onto) {
        return getShapeBounds();
    }
    @Override
    public void drawDragPoints(Graphics2D g2d) {
        g2d = (Graphics2D) g2d.create();
        g2d.setColor(Color.WHITE);

        Point2D[] scalePoints = getScalePoints();
        for (Point2D point : new Point2D[] { scalePoints[0], scalePoints[2], scalePoints[5], scalePoints[7] }) {
            Shape c = new Ellipse2D.Double(point.getX() - (float) LINE_WIDTH, point.getY() - (float) LINE_WIDTH, 2.0 * LINE_WIDTH, 2.0 * LINE_WIDTH);
            g2d.fill(c);
        }
        drawRotatePoint(g2d);
    }

    @Override
    public MarkerModificator getSuitableModificator() {
        return new TextMarkerModificator(this);
    }

    private AffineTransform calculateFontSize(Graphics2D g2d) {
        snapSize(g2d);
        double oldWidth = g2d.getFontMetrics().stringWidth(label);
        double oldHeight = g2d.getFontMetrics().getHeight();
        return AffineTransform.getScaleInstance(width / oldWidth, height / oldHeight);
    }

    private void snapSize(Graphics2D g2d) {

        double textWidth = g2d.getFontMetrics().stringWidth(label);
        double textHeight = g2d.getFontMetrics().getHeight();

        if(widthChanged) {
            setWidth(height * textWidth / textHeight);
        } else {
            setHeight(width * textHeight / textWidth);
        }
    }
}

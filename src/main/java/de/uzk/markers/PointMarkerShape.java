package de.uzk.markers;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

public enum PointMarkerShape {
    ARROWHEAD, CROSS, DOT;

    public Shape getShape(Point2D center) {
        Path2D path = new Path2D.Double();
        double half = PointMarker.SIZE / 2.;
        double step = PointMarker.SIZE / 10.;
        double halfAfterStep = half - step;
        switch (this) {
            case ARROWHEAD -> {
                path.moveTo(center.getX() - 2 * step, center.getY());
                path.lineTo(center.getX() - half, center.getY() - half);
                path.lineTo(center.getX() + half, center.getY());
                path.lineTo(center.getX() - half , center.getY() + half);
                path.closePath();
            }
            case CROSS -> {
                // Erstes Viertel
                path.moveTo(center.getX() - step, center.getY());
                path.lineTo(center.getX() - half, center.getY() - halfAfterStep);
                path.lineTo(center.getX() - halfAfterStep, center.getY() - half);
                path.lineTo(center.getX(), center.getY() - step);
                // Zweites Viertel
                path.lineTo(center.getX() + halfAfterStep, center.getY() - half);
                path.lineTo(center.getX() + half, center.getY() - halfAfterStep);
                path.lineTo(center.getX() + step, center.getY());
                // Drittes Viertel
                path.moveTo(center.getX() + half, center.getY() + halfAfterStep);
                path.lineTo(center.getX() + halfAfterStep, center.getY() + half);
                path.lineTo(center.getX(), center.getY() + step);
                //Letztes Viertel
                path.lineTo(center.getX() - halfAfterStep, center.getY() + half);
                path.lineTo(center.getX() - half, center.getY() + halfAfterStep);
                path.lineTo(center.getX(), center.getY() - step);

                path.closePath();
            }
            case DOT -> {
                return new Ellipse2D.Double(center.getX() - half / 2, center.getY() - half / 2., half, half);
            }
        }
        return path;
    }
}

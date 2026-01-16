package de.uzk.markers;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.Arrays;

import static de.uzk.config.LanguageHandler.getWord;
import static java.lang.Math.sin;

public enum MarkerShape {
    @JsonEnumDefaultValue
    RECTANGLE,
    ELLIPSE,
    ARROW,
    TRIANGLE;

    public static MarkerShape[] sortedValues() {
        MarkerShape[] values = MarkerShape.values();
        Arrays.sort(values, (mark1, mark2) -> mark1.toString().compareToIgnoreCase(mark2.toString()));
        return values;
    }

    @Override
    public String toString() {
        return switch (this) {
            case RECTANGLE -> getWord("dialog.markers.shape.rectangle");
            case ELLIPSE -> getWord("dialog.markers.shape.ellipse");
            case ARROW -> getWord("dialog.markers.shape.arrow");
            case TRIANGLE -> getWord("dialog.markers.shape.triangle");
        };
    }

    public Shape createShape(Rectangle boundingBox) {
        return switch(this) {
            case RECTANGLE -> boundingBox;
            case ELLIPSE -> new Ellipse2D.Double(boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
            case ARROW -> {
                Path2D path = new Path2D.Double();
                double baseAngle = Math.atan2(boundingBox.height, boundingBox.width);
                double leftAngle = baseAngle + Math.PI / 4;
                double rightAngle = baseAngle - Math.PI / 4;
                double xCorner = boundingBox.getX() + boundingBox.width;
                double yCorner = boundingBox.getY() + boundingBox.height;
                double length = Math.sqrt(boundingBox.getWidth() * boundingBox.getWidth() + boundingBox.getHeight() * boundingBox.getHeight()) / 20;
                path.moveTo(boundingBox.x, boundingBox.y);
                path.lineTo(xCorner, yCorner);
                path.moveTo(xCorner, yCorner);

                path.lineTo(xCorner - length * Math.cos(leftAngle), yCorner - length * sin(leftAngle));
                path.moveTo(xCorner, yCorner);
                  path.lineTo(xCorner - length * Math.cos(rightAngle), yCorner - length * sin(rightAngle));

                 yield path;
            }
            case TRIANGLE -> {
                Path2D path = new Path2D.Double();
                double x = boundingBox.x;
                double y = boundingBox.y;
                double width = boundingBox.width;
                double height = boundingBox.height;

                path.moveTo(x + width /2, y);
                path.lineTo(x + width, y + height);
                path.lineTo(x, y + height);
                path.lineTo(x + width /2, y);
                path.closePath();
                yield path;
            }
        };
    }
}



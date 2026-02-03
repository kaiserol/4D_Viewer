package de.uzk.markers;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.Arrays;

import static de.uzk.config.LanguageHandler.getWord;

public enum GenericMarkerShape {
    @JsonEnumDefaultValue
    RECTANGLE,
    ELLIPSE,
    TRIANGLE;

    public static GenericMarkerShape[] sortedValues() {
        GenericMarkerShape[] values = GenericMarkerShape.values();
        Arrays.sort(values, (mark1, mark2) -> mark1.toString().compareToIgnoreCase(mark2.toString()));
        return values;
    }

    @Override
    public String toString() {
        return switch (this) {
            case RECTANGLE -> getWord("dialog.markers.shape.rectangle");
            case ELLIPSE -> getWord("dialog.markers.shape.ellipse");
            case TRIANGLE -> getWord("dialog.markers.shape.triangle");
        };
    }

    /**
     * Erstellt eine geometrische Figur mit der Form dieser <code>GenericMarkerShape</code> innerhalb
     * des gegebenen Rechtecks.
     *
     * @param boundingBox der Bereich, in den die Form passen soll.
     * @return Die Form, als {@link java.awt.Shape}
     * */
    public Shape createShape(Rectangle boundingBox) {
        return switch(this) {
            case RECTANGLE -> boundingBox;
            case ELLIPSE -> new Ellipse2D.Double(boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);

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



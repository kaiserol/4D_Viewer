package de.uzk.gui.marker;

import de.uzk.markers.Marker;
import de.uzk.markers.ArrowMarker;
import de.uzk.markers.ShapeMarker;
import de.uzk.markers.MarkerShape;

import static de.uzk.config.LanguageHandler.getWord;

public enum MarkerKind {
    SHAPE_RECTANGLE, SHAPE_ELLIPSE, SHAPE_TRIANGLE, ARROW;

    public static MarkerKind fromMarker(Marker marker) {
        if (marker instanceof ArrowMarker) {
            return ARROW;
        } else if (marker instanceof ShapeMarker s) {
            return switch (s.getShape()) {
                case RECTANGLE -> SHAPE_RECTANGLE;
                case ELLIPSE -> SHAPE_ELLIPSE;
                case TRIANGLE -> SHAPE_TRIANGLE;
            };
        }

        throw new UnsupportedOperationException();
    }

    public Marker switchKind(Marker marker) {
        if (this == ARROW) {
            if (marker instanceof ArrowMarker) return marker;
            return new ArrowMarker(marker);
        }
        MarkerShape shape = switch (this) {
            case SHAPE_RECTANGLE -> MarkerShape.RECTANGLE;
            case SHAPE_ELLIPSE -> MarkerShape.ELLIPSE;
            case SHAPE_TRIANGLE -> MarkerShape.TRIANGLE;
            case ARROW -> throw new IllegalStateException("this == ARROW despite prior if branch");
        };
        if (marker instanceof ShapeMarker gen) {
            gen.setShape(shape);
            return gen;
        }
        return new ShapeMarker(marker, shape);
    }

    @Override
    public String toString() {
        return switch (this) {
            case SHAPE_RECTANGLE -> getWord("dialog.markers.shape.rectangle");
            case SHAPE_ELLIPSE -> getWord("dialog.markers.shape.ellipse");
            case SHAPE_TRIANGLE -> getWord("dialog.markers.shape.triangle");
            case ARROW -> getWord("dialog.markers.arrow");
        };
    }
}

package de.uzk.gui.marker;

import de.uzk.markers.AbstractMarker;
import de.uzk.markers.ArrowMarker;
import de.uzk.markers.GenericMarker;
import de.uzk.markers.GenericMarkerShape;

import static de.uzk.config.LanguageHandler.getWord;

public enum MarkerKind {
    SHAPE_RECTANGLE, SHAPE_ELLIPSE, SHAPE_TRIANGLE, ARROW;

    public static MarkerKind fromMarker(AbstractMarker marker) {
        if (marker instanceof ArrowMarker) {
            return ARROW;
        } else if (marker instanceof GenericMarker s) {
            return switch (s.getShape()) {
                case RECTANGLE -> SHAPE_RECTANGLE;
                case ELLIPSE -> SHAPE_ELLIPSE;
                case TRIANGLE -> SHAPE_TRIANGLE;
            };
        }

        throw new UnsupportedOperationException();
    }

    public AbstractMarker switchKind(AbstractMarker marker) {
        if (this == ARROW) {
            if (marker instanceof ArrowMarker) return marker;
            return new ArrowMarker(marker);
        }
        GenericMarkerShape shape = switch (this) {
            case SHAPE_RECTANGLE -> GenericMarkerShape.RECTANGLE;
            case SHAPE_ELLIPSE -> GenericMarkerShape.ELLIPSE;
            case SHAPE_TRIANGLE -> GenericMarkerShape.TRIANGLE;
            case ARROW -> throw new IllegalStateException("this == ARROW despite prior if branch");
        };
        if (marker instanceof GenericMarker gen) {
            gen.setShape(shape);
            return gen;
        }
        return new GenericMarker(marker, shape);
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

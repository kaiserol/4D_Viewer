package de.uzk.gui.marker;

import de.uzk.markers.*;

import static de.uzk.config.LanguageHandler.getWord;

public enum MarkerKind {
    SHAPE_RECTANGLE, SHAPE_ELLIPSE, SHAPE_TRIANGLE, ARROW, POINT_CROSS, POINT_ARROWHEAD, POINT_DOT, TEXT;

    public static MarkerKind fromMarker(Marker marker) {
        if (marker instanceof ArrowMarker) {
            return ARROW;
        } else if(marker instanceof TextMarker) {
            return TEXT;
        }
        else if (marker instanceof PointMarker point) {
            return switch (point.getShape()) {
                case ARROWHEAD -> POINT_ARROWHEAD;
                case CROSS -> POINT_CROSS;
                case DOT -> POINT_DOT;
            };
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
            else if (marker instanceof RotatableMarker rotatable) {
                return new ArrowMarker(rotatable);
            }
            return new ArrowMarker(marker);
        } else if (this == TEXT) {
            if (marker instanceof TextMarker) return marker;
            return new TextMarker(marker);
        }

        MarkerShape shape = null;
        PointMarkerShape pointMarkerShape = null;
        switch (this) {
            case SHAPE_RECTANGLE -> shape = MarkerShape.RECTANGLE;
            case SHAPE_ELLIPSE -> shape = MarkerShape.ELLIPSE;
            case SHAPE_TRIANGLE -> shape = MarkerShape.TRIANGLE;
            case POINT_ARROWHEAD -> pointMarkerShape = PointMarkerShape.ARROWHEAD;
            case POINT_CROSS -> pointMarkerShape = PointMarkerShape.CROSS;
            case POINT_DOT -> pointMarkerShape = PointMarkerShape.DOT;
            default -> throw new IllegalStateException("this == ARROW || this == TEXT despite prior if branch");
        }
        if (shape != null) {
            if (marker instanceof ShapeMarker gen) {
                gen.setShape(shape);
                return gen;
            }
            return new ShapeMarker(marker, shape);
        } else {
            if (marker instanceof PointMarker point) {
                point.setShape(pointMarkerShape);
                return point;
            }
            return new PointMarker(marker, pointMarkerShape);
        }
    }

    @Override
    public String toString() {
        return switch (this) {
            case SHAPE_RECTANGLE -> getWord("dialog.markers.shape.rectangle");
            case SHAPE_ELLIPSE -> getWord("dialog.markers.shape.ellipse");
            case SHAPE_TRIANGLE -> getWord("dialog.markers.shape.triangle");
            case ARROW -> getWord("dialog.markers.arrow");
            case POINT_CROSS -> getWord("dialog.markers.cross");
            case POINT_ARROWHEAD -> getWord("dialog.markers.arrowhead");
            case POINT_DOT -> getWord("dialog.markers.dot");
            case TEXT -> getWord("dialog.markers.text");
        };
    }
}

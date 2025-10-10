package de.uzk.markers;

import static de.uzk.config.LanguageHandler.getWord;

public enum MarkerShape {
    RECTANGLE,
    ELLIPSE;

    @Override
    public String toString() {
        return switch (this) {
            case RECTANGLE -> getWord("dialog.markers.shape.rectangle");
            case ELLIPSE -> getWord("dialog.markers.shape.ellipse");
        };
    }
}



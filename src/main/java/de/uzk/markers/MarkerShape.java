package de.uzk.markers;

import java.util.Arrays;

import static de.uzk.config.LanguageHandler.getWord;

public enum MarkerShape {
    RECTANGLE,
    ELLIPSE;

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
        };
    }
}



package de.uzk.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import static de.uzk.config.LanguageHandler.getWord;

public enum Theme {
    DARK_MODE,
    LIGHT_MODE;

    public boolean isLight() {
        return this == LIGHT_MODE;
    }

    public Theme toggle() {
        Theme[] values = Theme.values();
        int i = (ordinal() + 1) % values.length;
        return values[i];
    }

    @JsonCreator
    public static Theme fromTheme(String theme) {
        if (theme != null) {
            for (Theme t : Theme.values()) {
                boolean sameName = t.name().equalsIgnoreCase(theme);
                if (sameName) return t;
            }
        }
        // Fallback
        return getDefault();
    }

    @JsonValue
    private String jsonName() { return this.name(); }

    public static Theme getDefault() {
        return LIGHT_MODE;
    }

    // TODO: In den Einstellung anders ändern (ComboBox wie Language)
    public static Theme[] sortedValues() {
        Theme[] values = Theme.values();
        java.util.Arrays.sort(values, (theme1, theme2) -> theme1.toString().compareToIgnoreCase(theme2.toString()));
        return values;
    }

    @Override
    public String toString() {
        return switch (this) {
            case DARK_MODE -> getWord("themes.darkMode");
            case LIGHT_MODE -> getWord("themes.lightMode");
        };
    }
}
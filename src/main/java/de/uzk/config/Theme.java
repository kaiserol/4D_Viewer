package de.uzk.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

import static de.uzk.config.LanguageHandler.getWord;

public enum Theme {
    DARK_MODE,
    LIGHT_MODE;

    public boolean isLightMode() {
        return this == LIGHT_MODE;
    }

    @JsonValue
    public String getValue() {
        return switch (this) {
            case DARK_MODE -> "Dark";
            case LIGHT_MODE -> "Light";
        };
    }

    public static Theme getDefault() {
        return LIGHT_MODE;
    }

    @JsonCreator
    public static Theme fromTheme(String newTheme) {
        if (newTheme != null) {
            for (Theme theme : Theme.values()) {
                boolean sameName = theme.name().equalsIgnoreCase(newTheme);
                boolean sameValue = theme.getValue().equalsIgnoreCase(newTheme);
                if (sameName || sameValue) return theme;
            }
        }
        // Fallback
        return getDefault();
    }

    public static Theme[] sortedValues() {
        Theme[] values = Theme.values();
        Arrays.sort(values, (theme1, theme2) -> theme1.toString().compareToIgnoreCase(theme2.toString()));
        return values;
    }

    @Override
    public String toString() {
        return switch (this) {
            case DARK_MODE -> getWord("dialog.settings.theme.darkMode");
            case LIGHT_MODE -> getWord("dialog.settings.theme.lightMode");
        };
    }
}
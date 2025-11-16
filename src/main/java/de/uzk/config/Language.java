package de.uzk.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Locale;

import static de.uzk.config.LanguageHandler.getWord;

public enum Language {
    ENGLISH("English", "en", "UK"),
    GERMAN("German", "de", "DE");

    public boolean isGerman() {
        return this == GERMAN;
    }

    private final String value;
    private final Locale locale;

    Language(String value, String shortForm, String country) {
        if (value == null) throw new NullPointerException("Value is null.");
        if (shortForm == null) throw new NullPointerException("Shortform is null.");
        if (country == null) throw new NullPointerException("Country is null.");
        this.value = value;
        this.locale = new Locale.Builder()
                .setLanguage(shortForm)
                .setRegion(country)
                .build();
    }

    @JsonValue
    public String getValue() {
        return this.value;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public static Language getDefault() {
        return ENGLISH;
    }

    public static Language getSystemDefault() {
        return fromLanguage(Locale.getDefault().getLanguage());
    }

    @JsonCreator
    public static Language fromLanguage(String newLanguage) {
        if (newLanguage != null) {
            for (Language language : Language.values()) {
                boolean sameName = language.name().equalsIgnoreCase(newLanguage);
                boolean sameValue = language.getValue().equalsIgnoreCase(newLanguage);
                if (sameName || sameValue) return language;
            }
        }
        // Fallback
        return getDefault();
    }

    public static Language[] sortedValues() {
        Language[] values = Language.values();
        Arrays.sort(values, (language1, language2) -> language1.toString().compareToIgnoreCase(language2.toString()));
        return values;
    }

    @Override
    public String toString() {
        return switch (this) {
            case ENGLISH -> getWord("language.english");
            case GERMAN -> getWord("language.german");
        };
    }
}
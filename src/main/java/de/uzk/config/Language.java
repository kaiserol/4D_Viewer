package de.uzk.config;

import java.util.Arrays;
import java.util.Locale;

import static de.uzk.config.LanguageHandler.getWord;

public enum Language {
    ENGLISH("en", "UK"),
    GERMAN("de", "DE");

    private final String language;
    private final Locale locale;

    Language(String language, String country) {
        if (language == null) throw new NullPointerException("Language is null.");
        if (country == null) throw new NullPointerException("Country is null.");
        this.language = language;
        this.locale = new Locale.Builder()
                .setLanguage(language)
                .setRegion(country)
                .build();
    }

    public String getLanguage() {
        return this.language;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public static Language fromLanguage(String language) {
        if (language != null) {
            for (Language lang : Language.values()) {
                boolean sameName = lang.name().equalsIgnoreCase(language);
                boolean sameLanguage = lang.getLanguage().equalsIgnoreCase(language);
                if (sameName || sameLanguage) return lang;
            }
        }
        // Fallback
        return getDefault();
    }

    public static Language getDefault() {
        return ENGLISH;
    }

    public static Language getSystemDefault() {
        return fromLanguage(Locale.getDefault().getLanguage());
    }

    public static Language[] sortedValues() {
        Language[] values = Language.values();
        Arrays.sort(values, (lang1, lang2) -> lang1.toString().compareToIgnoreCase(lang2.toString()));
        return values;
    }

    @Override
    public String toString() {
        return switch (this) {
            case ENGLISH -> getWord("languages.english");
            case GERMAN -> getWord("languages.german");
        };
    }
}
package de.uzk.config;

import java.util.Locale;

import static de.uzk.config.LanguageHandler.getWord;

public enum Language {
    ENGLISH(Locale.ENGLISH),
    GERMAN(Locale.GERMAN);

    private final Locale locale;
    private final String name;

    Language(Locale locale) {
        this.locale = locale;
        this.name = this.getLocale().getLanguage();
    }

    public Locale getLocale() {
        return this.locale;
    }

    public String getName() {
        return this.name;
    }

    public static Language byName(String name) {
        for (Language language : Language.values()) {
            if (language.getName().equalsIgnoreCase(name)) return language;
        }
        // fallback to English if no language was found
        return Language.ENGLISH;
    }

    public static Language getSystemDefault() {
        return byName(Locale.getDefault().getLanguage());
    }

    @Override
    public String toString() {
        return switch (this) {
            case ENGLISH -> getWord("languages.english");
            case GERMAN -> getWord("languages.german");
        };
    }
}
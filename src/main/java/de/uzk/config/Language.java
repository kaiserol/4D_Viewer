package de.uzk.config;

import java.util.Locale;

import static de.uzk.config.LanguageHandler.getWord;

// To avoid having to manage locales within the GUI
public enum Language {
    ENGLISH(Locale.ENGLISH),
    GERMAN(Locale.GERMAN);

    private final Locale locale;
    private final String ID;

    Language(Locale locale) {
        this.locale = locale;
        this.ID = this.getLocale().getLanguage();
    }

    public Locale getLocale() {
        return this.locale;
    }

    public String getID() {
        return this.ID;
    }

    public static Language fromID(String ID) {
        for (Language language : Language.values()) {
            if (language.getID().equalsIgnoreCase(ID)) return language;
        }
        // fallback to English if no language was found
        return Language.ENGLISH;
    }

    public static Language getSystemDefault() {
        Locale defaultLocale = Locale.getDefault();
        return Language.fromID(defaultLocale.getLanguage());
    }

    @Override
    public String toString() {
        return switch (this) {
            case ENGLISH -> getWord("languages.english");
            case GERMAN -> getWord("languages.german");
        };
    }
}
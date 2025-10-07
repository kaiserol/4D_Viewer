package de.uzk.handler;

import java.util.Locale;
import java.util.ResourceBundle;

public final class LanguageHandler {
    private static ResourceBundle resources;


    private LanguageHandler() {
    }

    // To avoid having to manage locales within the GUI
    public enum Language {
        ENGLISH,
        GERMAN;


        @Override
        public String toString() {
            return switch (this) {
                case ENGLISH -> getWord("languages.english");
                case GERMAN -> getWord("languages.german");
            };
        }

        public static Language getSystemDefault() {
            Locale defaultLocale = Locale.getDefault();
            return Language.fromId(defaultLocale.getLanguage());
        }

        private Locale getCorrespondingLocale() {
            return switch (this) {
                case ENGLISH -> Locale.ENGLISH;
                case GERMAN -> Locale.GERMAN;
            };
        }

        public String getId() {
            return this.getCorrespondingLocale().getLanguage();
        }

        public static Language fromId(String id) {
            if(id.equals("de")) {
                return Language.GERMAN;
            } else {
                return Language.ENGLISH;
            }
        }
    }

    // Load a different .properties file into memory.
    // This will NOT affect existing UI elements!
    public static void initialize(Language language) {
        // After this, resources won't be null anymore, so getWord won't reset it
        resources = ResourceBundle.getBundle("language", language.getCorrespondingLocale());

    }

    public static String getWord(String attribute) {

        if (resources == null) {
            // We should initialise this from config before anything else
            // Otherwise we might get mixed languages in the UI
            throw new RuntimeException("Tried to use LanguageHandler before initialisation");
        }

        try {
            return resources.getString(attribute);
        } catch (Exception e) {
            return "";
        }
    }
}
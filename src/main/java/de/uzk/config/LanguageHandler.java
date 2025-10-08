package de.uzk.config;

import java.util.ResourceBundle;

public final class LanguageHandler {
    private static ResourceBundle resources;

    private LanguageHandler() {
    }

    // Load a different .properties file into memory.
    // This will NOT affect existing UI elements!
    public static void load(Language language) {
        // After this, resources won't be null anymore, so getWord won't reset it
        resources = ResourceBundle.getBundle("language", language.getLocale());
    }

    public static String getWord(String attribute) {
        try {
            // We should initialize this from config before anything else
            // Otherwise we might get mixed languages in the UI
            if (resources == null) throw new RuntimeException("Tried to use LanguageHandler before initialisation.");
            return resources.getString(attribute);
        } catch (Exception e) {
            return "";
        }
    }
}
package de.uzk.config;

import java.util.ResourceBundle;

import static de.uzk.Main.logger;

public final class LanguageHandler {
    private static ResourceBundle resources;

    private LanguageHandler() {
    }

    // Lädt eine properties-Datei in den Speicher.
    // Dies wirkt sich NICHT auf bereits existierende UI-Elemente aus!
    public static void load(Language language) {
        resources = ResourceBundle.getBundle("language", language.getLocale());
    }

    public static String getWord(String word) {
        try {
            if (resources == null) {
                logger.error("LanguageHandler is not initialized. Searched for word: " + word);
            } else return resources.getString(word);
        } catch (Exception e) {
            logger.error("Could not find word '" + word + "' in language file.");
        }
        return "???";
    }
}
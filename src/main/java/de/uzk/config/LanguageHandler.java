package de.uzk.config;

import java.util.ResourceBundle;

import static de.uzk.Main.logger;

public class LanguageHandler {
    private static ResourceBundle resources;

    private LanguageHandler() {
    }

    // Lädt eine properties-Datei in den Speicher.
    // Dies wirkt sich NICHT auf bereits existierende UI-Elemente aus!
    public static void load(Language language) {
        if (language == null) throw new NullPointerException("Language is null.");
        resources = ResourceBundle.getBundle("language", language.getLocale());
    }

    public static String getWord(String word) {
        if (word == null) throw new NullPointerException("Word is null.");
        try {
            if (resources == null) {
                logger.error(String.format("Failed loading the word for '%s' (Cause: LanguageHandler is not initialized)", word));
            } else return resources.getString(word);
        } catch (Exception e) {
            logger.error(String.format("Failed loading the word for '%s' (Cause: Not available in the language package)", word));
        }
        return "???";
    }
}
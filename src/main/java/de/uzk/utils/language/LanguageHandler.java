package de.uzk.utils.language;

import java.util.ResourceBundle;

public final class LanguageHandler {
    private static ResourceBundle resources;

    private LanguageHandler() {
    }

    public static String getWord(String attribute) {
        if (resources == null) {
            resources = ResourceBundle.getBundle("language");
        }

        try {
            return resources.getString(attribute);
        } catch (Exception e) {
            return "";
        }
    }
}
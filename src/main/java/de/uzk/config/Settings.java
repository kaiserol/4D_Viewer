package de.uzk.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;

import static de.uzk.utils.PathManager.*;

public class Settings {
    // Einstellungen
    private Language language;
    private Theme theme;
    private int fontSize;
    private boolean confirmExit;

    // Default-Konstanten
    private static final Language DEFAULT_LANGUAGE = Language.getSystemDefault();
    private static final Theme DEFAULT_THEME = Theme.getDefault();
    public static final int DEFAULT_FONT_SIZE = 14;
    private static final boolean DEFAULT_CONFIRM_EXIT = true;

    // MinMax Konstanten
    public static final int MIN_FONT_SIZE = 8;
    public static final int MAX_FONT_SIZE = 20;

    @JsonCreator
    public Settings(
        @JsonProperty("language") Language language,
        @JsonProperty("theme") Theme theme,
        @JsonProperty("fontSize") int fontSize,
        @JsonProperty("confirmExit") boolean confirmExit
    ) {
        this.setLanguage(language);
        this.setTheme(theme);
        this.setFontSize(fontSize);
        this.setConfirmExit(confirmExit);
    }

    public Language getLanguage() {
        return this.language;
    }

    public boolean setLanguage(Language language) {
        if (language != null) {
            if (this.language == language) return false;
            this.language = language;
        } else {
            // Setzt den Defaultwert, wenn der Wert null ist
            if (this.language != null) return false;
            this.language = DEFAULT_LANGUAGE;
        }
        LanguageHandler.load(language);
        return true;
    }

    public Theme getTheme() {
        return this.theme;
    }

    public boolean setTheme(Theme theme) {
        if (theme != null) {
            if (this.theme == theme) return false;
            this.theme = theme;
        } else {
            // Setzt den Defaultwert, wenn der Wert null ist
            if (this.theme != null) return false;
            this.theme = DEFAULT_THEME;
        }
        return true;
    }

    public int getFontSize() {
        return fontSize;
    }

    public boolean setFontSize(int fontSize) {
        if (MIN_FONT_SIZE <= fontSize && fontSize <= MAX_FONT_SIZE) {
            if (this.fontSize == fontSize) return false;
            this.fontSize = fontSize;
        } else {
            // Setzt den Defaultwert, wenn der Wert nicht innerhalb der MinMax-Grenzen liegt
            if (MIN_FONT_SIZE <= this.fontSize && this.fontSize <= MAX_FONT_SIZE) return false;
            this.fontSize = DEFAULT_FONT_SIZE;
        }
        return true;
    }

    public boolean isConfirmExit() {
        return confirmExit;
    }

    public void setConfirmExit(boolean confirmExit) {
        if (this.confirmExit == confirmExit) return;
        this.confirmExit = confirmExit;
    }

    public void save() {
        Path file = resolveConfigPath(SETTINGS_FILE_NAME);
        saveFile(file, this);
    }

    public static Settings load() {
        Path file = resolveConfigPath(SETTINGS_FILE_NAME);

        Object object = loadFile(file, Settings.class);
        if (object instanceof Settings settings) return settings;
        else return getDefault();
    }

    private static Settings getDefault() {
        return new Settings(
            DEFAULT_LANGUAGE,
            DEFAULT_THEME,
            DEFAULT_FONT_SIZE,
            DEFAULT_CONFIRM_EXIT
        );
    }
}

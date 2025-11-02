package de.uzk.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.nio.file.Path;

import static de.uzk.utils.AppPath.*;

public class Settings {
    // Einstellungen
    private Language language;
    private Theme theme;
    private int fontSize;
    private boolean confirmExit;

    // Default-Konstanten
    public static final Language DEFAULT_LANGUAGE = Language.getSystemDefault();
    public static final Theme DEFAULT_THEME = Theme.getDefault();
    public static final int DEFAULT_FONT_SIZE = 16;
    public static final boolean DEFAULT_CONFIRM_EXIT = true;

    // MinMax Konstanten
    public static final int MIN_FONT_SIZE = 8;
    public static final int MAX_FONT_SIZE = 24;

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
        if (this.language == language && this.language != null) return false;
        this.language = (language != null) ? language : DEFAULT_LANGUAGE;
        LanguageHandler.load(language);
        return true;
    }

    public Theme getTheme() {
        return this.theme;
    }

    public boolean setTheme(Theme theme) {
        if (this.theme == theme && this.theme != null) return false;
        this.theme = (theme != null) ? theme : DEFAULT_THEME;
        return true;
    }

    public int getFontSize() {
        return fontSize;
    }

    public boolean setFontSize(int fontSize) {
        if (this.fontSize == fontSize) return false;
        this.fontSize = (fontSize >= MIN_FONT_SIZE && fontSize <= MAX_FONT_SIZE) ? fontSize : DEFAULT_FONT_SIZE;
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
        Path jsonPath = getAppPath(Path.of(SETTINGS_FILE_NAME));
        saveJson(jsonPath, this);
    }

    public static Settings load() {
        Path jsonPath = getAppPath(Path.of(SETTINGS_FILE_NAME));

        Object obj = loadJson(jsonPath, Settings.class);
        if (obj instanceof Settings settings) return settings;
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

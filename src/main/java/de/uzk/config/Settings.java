package de.uzk.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import de.uzk.Main;
import de.uzk.utils.AppPath;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;

import static de.uzk.Main.logger;

public class Settings {
    // Einstellungen
    @JsonUnwrapped
    private Language language;
    @JsonUnwrapped
    private Theme theme;
    private int fontSize;
    private boolean confirmExit;

    // Default-Konstanten
    private static final Language DEFAULT_LANGUAGE = Language.getSystemDefault();
    private static final Theme DEFAULT_THEME = Theme.getDefault();
    public static final int DEFAULT_FONT_SIZE = 16;
    private static final boolean DEFAULT_CONFIRM_EXIT = true;

    // MinMax Konstanten
    public static final int MIN_FONT_SIZE = 8;
    public static final int MAX_FONT_SIZE = 24;

    // Pfad der Einstellungsdatei
    private static final Path SETTINGS_FILE_NAME = AppPath.VIEWER_HOME_DIRECTORY.resolve("settings.json");

    // Nur Konstanten vom primitiven Datentyp können als Default-Werte verwendet werden (inklusive Strings)
    @JsonCreator
    public Settings(
            @JsonProperty(value = "language") Language language,
            @JsonProperty(value = "theme") Theme theme,
            @JsonProperty(value = "fontSize", defaultValue = DEFAULT_FONT_SIZE + "") int fontSize,
            @JsonProperty(value = "confirmExit", defaultValue = DEFAULT_CONFIRM_EXIT + "") boolean confirmExit
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
        logger.info("Saving settings.json ...");
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(SETTINGS_FILE_NAME, this);
        } catch (JacksonException e) {
            Main.logger.error("Couldn't save settings.json: " + e.getMessage());
        }
    }

    public static Settings load() {
        logger.info("Loading settings.json ...");
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(SETTINGS_FILE_NAME, Settings.class);
        } catch (JacksonException e) {
            logger.error("Couldn't load settings.json: " + e.getMessage());
        }
        return getDefault();
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

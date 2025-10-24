package de.uzk.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.uzk.Main;
import de.uzk.utils.AppPath;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Locale;

import static de.uzk.Main.logger;

public class Settings {
    // MinMax Konstanten
    public static final int MIN_FONT_SIZE = 10;
    public static final int MAX_FONT_SIZE = 22;

    // Default-Konstanten
    private static final Language DEFAULT_LANGUAGE = Language.getSystemDefault();
    private static final Theme DEFAULT_THEME = Theme.getDefault();
    public static final int DEFAULT_FONT_SIZE = 16;
    private static final boolean DEFAULT_CONFIRM_EXIT = true;

    // Verzeichnisse
    private static final Path SETTINGS_FILE_NAME = AppPath.VIEWER_HOME_DIRECTORY.resolve("settings.json");

    // Einstellungen
    private Language language;
    private Theme theme;
    private int fontSize;
    private boolean confirmExit;

    // Nur Konstanten vom primitiven Datentyp können als Default-Werte verwendet werden (inklusive Strings)
    @JsonCreator
    public Settings(
            @JsonProperty(value = "language") String language,
            @JsonProperty(value = "theme") String theme,
            @JsonProperty(value = "fontSize", defaultValue = DEFAULT_FONT_SIZE + "") int fontSize,
            @JsonProperty(value = "confirmExit", defaultValue = DEFAULT_CONFIRM_EXIT + "") boolean confirmExit
    ) {
        this.setTheme(Theme.fromTheme(theme));
        this.setLanguage(Language.fromLanguage(language));
        this.setFontSize(fontSize);
        this.setConfirmExit(confirmExit);
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        if (this.language == language) return;
        this.language = (language != null) ? language : DEFAULT_LANGUAGE;
        Locale.setDefault(this.language.getLocale());
        JComponent.setDefaultLocale(this.language.getLocale());
        LanguageHandler.load(this.language);
    }

    public Theme getTheme() {
        return this.theme;
    }

    public void setTheme(Theme theme) {
        this.theme = (theme != null) ? theme : DEFAULT_THEME;
    }

    public void toggleTheme() {
        this.theme = this.theme.toggle();
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = (fontSize >= MIN_FONT_SIZE && fontSize <= MAX_FONT_SIZE) ? fontSize : DEFAULT_FONT_SIZE;
    }

    public boolean isConfirmExit() {
        return confirmExit;
    }

    public void setConfirmExit(boolean confirmExit) {
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
                DEFAULT_LANGUAGE.name(),
                DEFAULT_THEME.name(),
                DEFAULT_FONT_SIZE,
                DEFAULT_CONFIRM_EXIT
        );
    }
}

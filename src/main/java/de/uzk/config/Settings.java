package de.uzk.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.uzk.Main;
import de.uzk.image.ImageFileNameExtension;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Locale;

import static de.uzk.Main.logger;
import static de.uzk.Main.operationSystem;

// TODO: Validate
public class Settings {
    private static final Path SETTINGS_FILE_NAME = operationSystem.getDirectoryPath(false).resolve("settings.json");

    public static final int MIN_FONT_SIZE = 10;
    public static final int DEFAULT_FONT_SIZE = 16;
    public static final int MAX_FONT_SIZE = 22;

    // Einstellungen
    private Language language;
    private Theme theme;
    private int fontSize;
    private boolean confirmExit;
    private ImageFileNameExtension fileNameExt;

    private Settings() {
        this.setTheme(Theme.LIGHT_MODE);
        this.setFontSize(DEFAULT_FONT_SIZE);
        this.setConfirmExit(true);
        this.setLanguage(Language.getSystemDefault());
        this.setFileNameExt(ImageFileNameExtension.getDefault());
    }

    @JsonCreator
    public Settings(
            @JsonProperty(value = "language", defaultValue = "ENGLISH") String language,
            @JsonProperty(value = "theme", defaultValue = "LIGHT_MODE") String theme,
            @JsonProperty(value = "fontSize", defaultValue = "16") int fontSize,
            @JsonProperty(value = "confirmExit", defaultValue = "true") boolean confirmExit,
            @JsonProperty(value = "fileNameExt", defaultValue = "JPEG")  String fileNameExt
    ) {
        this.setTheme(Theme.fromName(theme));
        this.setLanguage(Language.fromLanguage(language));
        this.setFontSize(fontSize);
        this.setConfirmExit(confirmExit);
        this.setFileNameExt(ImageFileNameExtension.fromExtension(fileNameExt));
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        if (language == null || this.language == language) return;

        this.language = language;
        Locale.setDefault(language.getLocale());
        JComponent.setDefaultLocale(language.getLocale());
        LanguageHandler.load(language);
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public int getFontSize() {
        return fontSize;
    }

    public boolean setFontSize(int fontSize) {
        if (fontSize < MIN_FONT_SIZE || fontSize > MAX_FONT_SIZE) {
            if (this.fontSize < MIN_FONT_SIZE) {
                this.fontSize = DEFAULT_FONT_SIZE;
            }
            return false;
        }
        this.fontSize = fontSize;
        return true;
    }

    public boolean isConfirmExit() {
        return confirmExit;
    }

    public void setConfirmExit(boolean confirmExit) {
        this.confirmExit = confirmExit;
    }

    public void save() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(SETTINGS_FILE_NAME, this);
        } catch (JacksonException e) {
            Main.logger.error("Couldn't save settings.json: " + e.getMessage());
        }
    }

    public static Settings load() {
        try  {
            ObjectMapper mapper = new ObjectMapper();
            Settings settings = mapper.readValue(SETTINGS_FILE_NAME, Settings.class);
            return settings;
        } catch (JacksonException e) {
            logger.error("Couldn't load settings.json: " + e.getMessage());
        }
        return new Settings();
    }

    public void toggleTheme() {
        this.theme = this.theme.opposite();
    }

    public ImageFileNameExtension getFileNameExt() {
        return fileNameExt;
    }

    public void setFileNameExt(ImageFileNameExtension fileNameExt) {
        this.fileNameExt = fileNameExt != null ? fileNameExt : ImageFileNameExtension.getDefault();
    }
}

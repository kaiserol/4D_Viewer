package de.uzk.config;

import com.fasterxml.jackson.annotation.JsonSetter;
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

    public Settings() {
        this.setTheme(Theme.LIGHT_MODE);
        this.setLanguage(Language.getSystemDefault());
        this.setFontSize(16);
        this.setConfirmExit(true);
        this.fileNameExt = ImageFileNameExtension.getDefault();

    }

    public Language getLanguage() {
        return language;
    }

    @JsonSetter("language")
    private void setLanguage(String language) {
        this.setLanguage(Language.fromLanguage(language));
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

    @JsonSetter("theme")
    private void setTheme(String theme) {
        this.setTheme(Theme.fromName(theme));
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public int getFontSize() {
        return fontSize;
    }

    @JsonSetter("fontSize")
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
            return mapper.readValue(SETTINGS_FILE_NAME, Settings.class);
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
        this.fileNameExt = fileNameExt;
    }

    public void setFileNameExt(String extension) {
        ImageFileNameExtension temp = ImageFileNameExtension.fromExtension(extension);
        this.fileNameExt = temp != null ? temp : ImageFileNameExtension.getDefault();
    }
}

package de.uzk.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.uzk.io.PathManager;
import de.uzk.utils.NumberUtils;

import java.nio.file.Path;
import java.util.Objects;

public class Settings {
    // Einstellungen
    private Language language;
    private InitialDirectory initialDirectory;
    private Theme theme;
    private int fontSize;
    private boolean confirmExit;
    private Path screenshotDirectory;

    // Default-Konstanten
    private static final Language DEFAULT_LANGUAGE = Language.getSystemDefault();
    private static final Theme DEFAULT_THEME = Theme.getDefault();
    private static final InitialDirectory DEFAULT_INITIAL_DIRECTORY = InitialDirectory.ROOT;
    public static final int DEFAULT_FONT_SIZE = 14;
    private static final boolean DEFAULT_CONFIRM_EXIT = true;
    private static final Path DEFAULT_SCREENSHOT_DIRECTORY = PathManager.DEFAULT_SNAPSHOTS_DIRECTORY;

    // MinMax-Konstanten
    public static final int MIN_FONT_SIZE = 8;
    public static final int MAX_FONT_SIZE = 20;

    @JsonCreator
    public Settings(
        @JsonProperty("language") Language language,
        @JsonProperty("theme") Theme theme,
        @JsonProperty("fontSize") int fontSize,
        @JsonProperty("confirmExit") boolean confirmExit,
        @JsonProperty("initialDirectory") InitialDirectory initialDirectory,
        @JsonProperty("screenshotDirectory")  Path screenshotDirectory
     ) {
        this.setLanguage(language);
        this.setTheme(theme);
        this.setFontSize(fontSize);
        this.setConfirmExit(confirmExit);
        this.setInitialDirectory(initialDirectory);
        this.setScreenshotDirectory(screenshotDirectory);
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

    public InitialDirectory getInitialDirectory() {
        return this.initialDirectory;
    }

    public boolean setInitialDirectory(InitialDirectory initialDirectory) {
        if (initialDirectory != null) {
            if (this.initialDirectory == initialDirectory) return false;
            this.initialDirectory = initialDirectory;
        } else {
            if (this.initialDirectory != null) return false;
            this.initialDirectory = DEFAULT_INITIAL_DIRECTORY;
        }
        return true;
    }

    public int getFontSize() {
        return fontSize;
    }

    public boolean setFontSize(int fontSize) {
        if (NumberUtils.valueInRange(fontSize, MIN_FONT_SIZE, MAX_FONT_SIZE)) {
            if (this.fontSize == fontSize) return false;
            this.fontSize = fontSize;
        } else {
            // Setzt den Defaultwert, wenn der Wert nicht innerhalb der MinMax-Grenzen liegt
            if (NumberUtils.valueInRange(this.fontSize, MIN_FONT_SIZE, MAX_FONT_SIZE)) return false;
            this.fontSize = DEFAULT_FONT_SIZE;
        }
        return true;
    }

    public boolean isConfirmExit() {
        return confirmExit;
    }

    public boolean setConfirmExit(boolean confirmExit) {
        if (this.confirmExit == confirmExit) return false;
        this.confirmExit = confirmExit;
        return true;
    }

    public Path getScreenshotDirectory() {
        return screenshotDirectory;
    }

    public boolean setScreenshotDirectory(Path screenshotDirectory) {
        if(screenshotDirectory != null) {
            if(Objects.equals(this.screenshotDirectory, screenshotDirectory)) return false;
            this.screenshotDirectory = screenshotDirectory;
        } else {
            if(this.screenshotDirectory != null) return false;
            this.screenshotDirectory = DEFAULT_SCREENSHOT_DIRECTORY;
        }
        return true;
    }

    public void save() {
        Path filePath = PathManager.resolveConfigPath(PathManager.SETTINGS_FILE_NAME);
        PathManager.save(filePath, this);
    }

    public static Settings load() {
        Path filePath = PathManager.resolveConfigPath(PathManager.SETTINGS_FILE_NAME);

        Object object = PathManager.load(filePath, Settings.class);
        if (object instanceof Settings settings) return settings;
        else return getDefault();
    }

    private static Settings getDefault() {
        return new Settings(
            DEFAULT_LANGUAGE,
            DEFAULT_THEME,
            DEFAULT_FONT_SIZE,
            DEFAULT_CONFIRM_EXIT,
            DEFAULT_INITIAL_DIRECTORY,
            DEFAULT_SCREENSHOT_DIRECTORY
        );
    }
}

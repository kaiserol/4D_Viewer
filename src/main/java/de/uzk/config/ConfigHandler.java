package de.uzk.config;

import de.uzk.gui.GuiUtils;
import de.uzk.image.ImageFileNameExtension;
import de.uzk.utils.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import static de.uzk.Main.imageFileHandler;
import static de.uzk.Main.logger;

public class ConfigHandler {
    // Einstellungen Konstanten
    public static final Language DEFAULT_LANGUAGE = Language.getSystemDefault();
    public static final Theme DEFAULT_THEME = Theme.LIGHT_MODE;
    public static final int MIN_FONT_SIZE = 12;
    public static final int DEFAULT_FONT_SIZE = 18;
    public static final int MAX_FONT_SIZE = 22;
    public static final boolean DEFAULT_CONFIRM_EXIT = true;

    // Dateien / Verzeichnisse
    private static final File CONFIG_FILE = new File("config.properties");
    private static final File SCREENSHOT_DIRECTORY = new File("screenshots");

    // Bild Konstanten
    private static final ImageFileNameExtension DEFAULT_IMAGE_FILE_NAME_EXTENSION = ImageFileNameExtension.getDefault();
    private static final String DEFAULT_IMAGE_FILE_NAME_TIME_SEP = "X";
    private static final String DEFAULT_IMAGE_FILE_NAME_LEVEL_SEP = "L";
    private static final boolean DEFAULT_IMAGE_MIRROR_X = false;
    private static final boolean DEFAULT_IMAGE_MIRROR_Y = false;
    private static final int DEFAULT_IMAGE_ROTATION = 0;

    // Bild Bewegungen Konstanten
    private static final double DEFAULT_SHIFT_TIME_UNIT = 30.0; // 30 Sekunden
    private static final double DEFAULT_SHIFT_LEVEL_UNIT = 1.0; // 1 Mikrometer

    // Einstellungen Eigenschaften
    private Language language;
    private Theme theme;
    private int fontSize;
    private boolean confirmExit;

    // Eigenschaften
    private final SimpleDateFormat dateFormat;
    private final String dateFormatPattern;

    public ConfigHandler() {
        this.dateFormat = new SimpleDateFormat("yyyy-dd-MM");
        this.dateFormatPattern = "\\d{4}-\\d{2}-\\d{2}";
    }

    public Language getLanguage() {
        return this.language;
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

    public void setTheme(String newTheme) {
        try {
            this.theme = Theme.fromName(newTheme);
        } catch (IllegalArgumentException ignore) {
            if (this.theme == null) this.theme = DEFAULT_THEME;
        }
    }

    public void toggleTheme() {
        this.theme = this.theme.opposite();
    }

    public int getFontSize() {
        return fontSize;
    }

    public boolean setFontSize(int fontSize) {
        if (fontSize < MIN_FONT_SIZE || fontSize > MAX_FONT_SIZE) {
            if (this.fontSize == 0) this.fontSize = DEFAULT_FONT_SIZE;
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

    public String loadConfig() {
        logger.info("Reading config ...");
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(CONFIG_FILE)) {
            properties.load(fileInputStream);
            return readProperties(properties);
        } catch (IOException ignored) {
            readDefaultProperties();
        }
        return null;
    }

    public void saveConfig() {
        logger.info("Storing config ...");
        Properties properties = new Properties();
        try {
            if (!CONFIG_FILE.exists() && !CONFIG_FILE.createNewFile()) {
                logger.error("The config was already created.");
            }
        } catch (IOException e) {
            logger.logException(e);
            return;
        }

        // Kommentare hinzufügen
        boolean isGerman = language == Language.GERMAN;
        SimpleDateFormat dateTimeFormat = language == Language.GERMAN ?
                new SimpleDateFormat("EEEE, dd. MMMM yyyy HH:mm:ss 'Uhr'", Locale.GERMAN) :
                new SimpleDateFormat("EEEE, MMMM d, yyyy, h:mm:ss a", Locale.ENGLISH);

        String description = isGerman ? "#Konfiguration der 4D-Viewer App" : "#Configuration of the 4D-Viewer App";
        String dateTime = (isGerman ? "#Stand" : "#As of") + ": " + dateTimeFormat.format(new Date());

        try (PrintWriter writer = new PrintWriter(new FileWriter(CONFIG_FILE.getAbsolutePath()))) {
            writer.println(description);
            writer.println(dateTime);
            saveProperties(properties);

            // Eigenschaften sortiert abspeichern
            String[] sortedKeys = properties.stringPropertyNames().stream().sorted().toArray(String[]::new);
            for (String key : sortedKeys) {
                writer.println(key + "=" + properties.getProperty(key));
            }
        } catch (IOException e) {
            logger.logException(e);
        }
    }

    private void readDefaultProperties() {
        // Einstellungen Eigenschaften: Sprache muss sofort initialisiert werden
        this.setLanguage(DEFAULT_LANGUAGE);
        this.setTheme(DEFAULT_THEME.name());
        this.setFontSize(DEFAULT_FONT_SIZE);
        this.setConfirmExit(DEFAULT_CONFIRM_EXIT);

        // Bild Eigenschaften
        imageFileHandler.setImageFileNameExtension(DEFAULT_IMAGE_FILE_NAME_EXTENSION.name());
        imageFileHandler.setImageFileNameTimeSep(DEFAULT_IMAGE_FILE_NAME_TIME_SEP);
        imageFileHandler.setImageFileNameLevelSep(DEFAULT_IMAGE_FILE_NAME_LEVEL_SEP);
        imageFileHandler.setImageMirrorX(DEFAULT_IMAGE_MIRROR_X);
        imageFileHandler.setImageMirrorY(DEFAULT_IMAGE_MIRROR_Y);
        imageFileHandler.setImageRotation(DEFAULT_IMAGE_ROTATION);

        // Bild Bewegungen Eigenschaften
        imageFileHandler.setShiftTimeUnit(DEFAULT_SHIFT_TIME_UNIT);
        imageFileHandler.setShiftLevelUnit(DEFAULT_SHIFT_LEVEL_UNIT);
    }

    private String readProperties(Properties properties) {
        // Einstellungen Eigenschaften: Sprache muss sofort initialisiert werden
        this.setLanguage(Language.fromName(loadString(properties, "Settings.Language", DEFAULT_LANGUAGE.getName())));
        this.setTheme(loadString(properties, "Settings.Theme", DEFAULT_THEME.name()));
        this.setFontSize(loadInteger(properties, "Settings.FontSize", DEFAULT_FONT_SIZE));
        this.setConfirmExit(loadBoolean(properties, "Settings.ConfirmExit", DEFAULT_CONFIRM_EXIT));

        // Bild Eigenschaften
        imageFileHandler.setImageFileNameExtension(loadString(properties, "ImageFileNameExtension", DEFAULT_IMAGE_FILE_NAME_EXTENSION.name()));
        imageFileHandler.setImageFileNameTimeSep(loadString(properties, "ImageFileNameTimeSep", DEFAULT_IMAGE_FILE_NAME_TIME_SEP));
        imageFileHandler.setImageFileNameLevelSep(loadString(properties, "ImageFileNameLevelSep", DEFAULT_IMAGE_FILE_NAME_LEVEL_SEP));
        imageFileHandler.setImageMirrorX(loadBoolean(properties, "ImageMirrorX", DEFAULT_IMAGE_MIRROR_X));
        imageFileHandler.setImageMirrorY(loadBoolean(properties, "ImageMirrorY", DEFAULT_IMAGE_MIRROR_Y));
        imageFileHandler.setImageRotation(loadInteger(properties, "ImageRotation", DEFAULT_IMAGE_ROTATION));

        // Bild Bewegungen Eigenschaften
        imageFileHandler.setShiftTimeUnit(loadDouble(properties, "ShiftTimeUnit", DEFAULT_SHIFT_TIME_UNIT));
        imageFileHandler.setShiftLevelUnit(loadDouble(properties, "ShiftLevelUnit", DEFAULT_SHIFT_LEVEL_UNIT));

        // Bilderverzeichnis zurückgeben
        return loadString(properties, "ImageFilesDirectory", null);
    }

    private void saveProperties(Properties properties) {
        // Einstellungen Eigenschaften
        properties.setProperty("Settings.Language", getLanguage().getName());
        properties.setProperty("Settings.Theme", String.valueOf(getTheme()));
        properties.setProperty("Settings.FontSize", String.valueOf(getFontSize()));
        properties.setProperty("Settings.ConfirmExit", String.valueOf(isConfirmExit()));

        // Bild Eigenschaften
        properties.setProperty("ImageFilesDirectory", imageFileHandler.getImageFilesDirectoryPath());
        properties.setProperty("ImageFileNameExtension", imageFileHandler.getImageFileNameExtension().name());
        properties.setProperty("ImageFileNameTimeSep", imageFileHandler.getImageFileNameTimeSep());
        properties.setProperty("ImageFileNameLevelSep", imageFileHandler.getImageFileNameLevelSep());
        properties.setProperty("ImageMirrorX", String.valueOf(imageFileHandler.isImageMirrorX()));
        properties.setProperty("ImageMirrorY", String.valueOf(imageFileHandler.isImageMirrorY()));
        properties.setProperty("ImageRotation", String.valueOf(imageFileHandler.getImageRotation()));

        // Bild Bewegungen Eigenschaften
        properties.setProperty("ShiftTimeUnit", String.valueOf(imageFileHandler.getShiftTimeUnit()));
        properties.setProperty("ShiftLevelUnit", String.valueOf(imageFileHandler.getShiftLevelUnit()));
    }

    private String loadString(Properties properties, String property, String defaultValue) {
        String value = properties.getProperty(property);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

    private boolean loadBoolean(Properties properties, String property, boolean defaultValue) {
        String value = properties.getProperty(property);
        if ("true".equalsIgnoreCase(value)) return true;
        else if ("false".equalsIgnoreCase(value)) return false;
        return defaultValue;
    }

    private Integer loadInteger(Properties properties, String property, Integer defaultValue) {
        String value = properties.getProperty(property);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }

    private Double loadDouble(Properties properties, String property, Double defaultValue) {
        String value = properties.getProperty(property);
        try {
            return Double.parseDouble(value);
        } catch (NullPointerException | NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean saveScreenshot(BufferedImage originalImage) {
        try {
            if (SCREENSHOT_DIRECTORY.isDirectory() || SCREENSHOT_DIRECTORY.mkdirs()) {
                String date = this.dateFormat.format(new Date());
                int count = getNextScreenshotIndex(date);
                String fileName = String.format("%s(%d)_%s", date, count, imageFileHandler.getImageFile().getName());
                File saveFile = new File(SCREENSHOT_DIRECTORY.getAbsolutePath() + StringUtils.FILE_SEP + fileName);

                BufferedImage edited = GuiUtils.getEditedImage(originalImage, true);
                ImageIO.write(edited, imageFileHandler.getImageFileNameExtension().getType(), saveFile);
                logger.info("Saved screenshot: '" + saveFile.getAbsolutePath() + "'.");
                return true;
            }
        } catch (IOException e) {
            logger.logException(e);
        }
        return false;
    }

    private int getNextScreenshotIndex(String date) {
        int index = 1;
        if (SCREENSHOT_DIRECTORY.isDirectory()) {
            File[] files = SCREENSHOT_DIRECTORY.listFiles();
            if (files == null) return index;

            String fileNamePattern = date + "\\(\\d+\\)_" + imageFileHandler.getFileNamePattern();
            for (File file : files) {
                String filename = file.getName();
                if (filename.matches(fileNamePattern)) {
                    int indexStart = filename.indexOf("(") + 1;
                    int indexEnd = filename.indexOf(")");

                    int count = Integer.parseInt(filename.substring(indexStart, indexEnd)) + 1;
                    if (count > index) index = count;
                }
            }
        }
        return index;
    }

    public int getScreenshotCount() {
        int count = 0;
        if (SCREENSHOT_DIRECTORY.isDirectory()) {
            File[] files = SCREENSHOT_DIRECTORY.listFiles();
            if (files == null) return count;

            String filePattern = this.dateFormatPattern + "\\(\\d+\\)_" + imageFileHandler.getFileNamePattern();
            for (File file : files) {
                String filename = file.getName();
                if (filename.matches(filePattern)) count++;
            }
        }
        return count;
    }

    // theme settings
    public enum Theme {
        DARK_MODE, LIGHT_MODE;

        public boolean isLight() {
            return this == LIGHT_MODE;
        }

        public Theme opposite() {
            return isLight() ? DARK_MODE : LIGHT_MODE;
        }

        public static Theme fromName(String name) {
            if (name == null) return null;
            try {
                for (Theme theme : Theme.values()) {
                    if (theme.name().equalsIgnoreCase(name)) return theme;
                }
            } catch (Exception ignore) {
            }
            return null;
        }
    }
}

package de.uzk.config;

import de.uzk.gui.GuiUtils;
import de.uzk.image.ImageDetails;
import de.uzk.image.ImageFile;
import de.uzk.image.ImageFileConstants;
import de.uzk.image.ImageType;
import de.uzk.utils.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import static de.uzk.Main.imageHandler;
import static de.uzk.Main.logger;
import static de.uzk.image.ImageFileConstants.DEFAULT_IMAGE_TYPE;
import static de.uzk.utils.NumberUtils.isDouble;
import static de.uzk.utils.NumberUtils.isInteger;

public class ConfigHandler {
    // TODO: DEFAULT_ASK_AGAIN_CLOSING_WINDOW muss über die Einstellugen aktivierbar und wieder nicht sein
    public static final boolean DEFAULT_ASK_AGAIN_CLOSING_WINDOW = true;
    public static final Theme DEFAULT_THEME = Theme.LIGHT_MODE;
    public static final int MIN_FONT_SIZE = 12;
    public static final int DEFAULT_FONT_SIZE = 18;
    public static final int MAX_FONT_SIZE = 22;
    public static final Language SYSTEM_LANGUAGE = Language.getSystemDefault();
    private static final File CONFIG_FILE = new File("config.cfg");
    private static final File SCREENSHOT_FOLDER = new File("screenshots");

    private static final String DEFAULT_SEP_TIME = "X";
    private static final String DEFAULT_SEP_LEVEL = "L";
    private static final boolean DEFAULT_IMAGE_MIRROR_X = false;
    private static final boolean DEFAULT_IMAGE_MIRROR_Y = false;
    private static final int DEFAULT_IMAGE_ROTATION = 0;
    private static final double DEFAULT_TIME_UNIT = 30.0;
    private static final double DEFAULT_LEVEL_UNIT = 1;

    private final SimpleDateFormat dateFormat;
    private boolean askAgainClosingWindow;
    private Theme theme;
    private int fontSize;
    private Language language;

    public ConfigHandler() {
        this.dateFormat = new SimpleDateFormat("yyyy-dd-MM");
    }

    public boolean isAskAgainClosingWindow() {
        return askAgainClosingWindow;
    }

    public void setAskAgainClosingWindow(boolean askAgainClosingWindow) {
        this.askAgainClosingWindow = askAgainClosingWindow;
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(String newTheme) {
        try {
            this.theme = Theme.valueOf(newTheme);
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

    public Language getLanguage() {
        return this.language;
    }

    public void setLanguage(Language language) {
        if (language == null) return;

        this.language = language;
        Locale.setDefault(language.getLocale());
        JComponent.setDefaultLocale(language.getLocale());
        LanguageHandler.load(language);
    }

    public boolean saveScreenshot(BufferedImage originalImage) {
        try {
            if (SCREENSHOT_FOLDER.isDirectory() || SCREENSHOT_FOLDER.mkdir()) {
                String date = dateFormat.format(new Date());
                int count = getNextScreenshotIndex(date);
                String fileName = String.format("%s(%d)_%s", date, count, imageHandler.getCurrentImage().getFileName());
                File saveFile = new File(SCREENSHOT_FOLDER.getAbsolutePath() + StringUtils.FILE_SEP + fileName);

                BufferedImage edited = GuiUtils.getEditedImage(originalImage, imageHandler.getImageDetails(), true);
                ImageIO.write(edited, imageHandler.getImageDetails().getImageType().getType(), saveFile);
                logger.info("Saved screenshot: " + saveFile.getAbsolutePath());
                return true;
            }
        } catch (IOException e) {
            logger.logException(e);
        }
        return false;
    }

    private int getNextScreenshotIndex(String date) {
        int index = 1;
        if (SCREENSHOT_FOLDER.isDirectory()) {
            File[] files = SCREENSHOT_FOLDER.listFiles();
            if (files == null) return index;

            String filePattern = date + "\\(\\d+\\)_" + ImageFile.getImageNamePattern(imageHandler.getImageDetails());
            for (File file : files) {
                String filename = file.getName();
                if (filename.matches(filePattern)) {
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
        if (SCREENSHOT_FOLDER.isDirectory()) {
            File[] files = SCREENSHOT_FOLDER.listFiles();
            if (files == null) return count;

            String filePattern = "\\d{4}-\\d{2}-\\d{2}\\(\\d+\\)_" + ImageFile.getImageNamePattern(imageHandler.getImageDetails());
            for (File file : files) {
                String filename = file.getName();
                if (filename.matches(filePattern)) count++;
            }
        }
        return count;
    }

    public void loadConfig() {
        logger.info("Reading config...");
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(CONFIG_FILE)) {
            properties.load(fileInputStream);
            readProperties(properties);
        } catch (IOException ignored) {
            readDefaultProperties();
        }
    }

    public void saveConfig() {
        logger.info("Storing config...");
        Properties properties = new Properties();
        try {
            if (!CONFIG_FILE.isDirectory() && !CONFIG_FILE.createNewFile()) {
                logger.error("The config was already created.");
            }
        } catch (IOException e) {
            logger.logException(e);
        }

        try (FileOutputStream outputStream = new FileOutputStream(CONFIG_FILE)) {
            saveProperties(properties);
            properties.store(outputStream, "Config");
        } catch (IOException e) {
            logger.logException(e);
        }
    }

    private void readDefaultProperties() {
        // imageDetails
        imageHandler.setImageDetails(new ImageDetails(
                DEFAULT_SEP_TIME,
                DEFAULT_SEP_LEVEL,
                DEFAULT_IMAGE_TYPE,
                DEFAULT_IMAGE_MIRROR_X,
                DEFAULT_IMAGE_MIRROR_Y,
                DEFAULT_IMAGE_ROTATION));

        // units
        imageHandler.setTimeUnit(DEFAULT_TIME_UNIT);
        imageHandler.setLevelUnit(DEFAULT_LEVEL_UNIT);

        // askAgainClosingWindow, theme, font size, language
        this.askAgainClosingWindow = DEFAULT_ASK_AGAIN_CLOSING_WINDOW;
        this.theme = DEFAULT_THEME;
        this.fontSize = DEFAULT_FONT_SIZE;
        this.setLanguage(SYSTEM_LANGUAGE);
    }

    private void readProperties(Properties properties) {
        // imageFolder
        imageHandler.setImageFolder(loadImageFolder(properties));

        // imageDetails
        String sepTime = loadString(properties, "SepTime", DEFAULT_SEP_TIME);
        String sepLevel = loadString(properties, "SepLevel", DEFAULT_SEP_LEVEL);
        ImageType imageType = loadImageType(properties);
        boolean mirrorX = loadBoolean(properties, "MirrorX", DEFAULT_IMAGE_MIRROR_X);
        boolean mirrorY = loadBoolean(properties, "MirrorY", DEFAULT_IMAGE_MIRROR_Y);
        int rotation = loadNumber(properties, "Rotation", DEFAULT_IMAGE_ROTATION).intValue();
        imageHandler.setImageDetails(new ImageDetails(sepTime, sepLevel, imageType, mirrorX, mirrorY, rotation));

        // units
        imageHandler.setTimeUnit(loadNumber(properties, "TimeUnit", DEFAULT_TIME_UNIT).doubleValue());
        imageHandler.setLevelUnit(loadNumber(properties, "LevelUnit", DEFAULT_LEVEL_UNIT).doubleValue());

        // askAgainClosingWindow, theme, font size, language
        this.askAgainClosingWindow = loadBoolean(properties, "AskAgainClosingWindow", DEFAULT_ASK_AGAIN_CLOSING_WINDOW);
        this.setTheme(loadString(properties, "Theme", DEFAULT_THEME.name()));
        this.setFontSize(loadNumber(properties, "FontSize", DEFAULT_FONT_SIZE).intValue());
        this.setLanguage(Language.byName(loadString(properties, "Language", SYSTEM_LANGUAGE.getName())));
    }

    private void saveProperties(Properties properties) {
        // imageFolder
        properties.setProperty("ImageFolder", imageHandler.getImageDir());

        // imageDetails
        properties.setProperty("SepTime", imageHandler.getImageDetails().getSepTime());
        properties.setProperty("SepLevel", imageHandler.getImageDetails().getSepLevel());
        properties.setProperty("ImageType", imageHandler.getImageDetails().getImageType().getTypeDescription());
        properties.setProperty("MirrorX", String.valueOf(imageHandler.getImageDetails().isMirrorX()));
        properties.setProperty("MirrorY", String.valueOf(imageHandler.getImageDetails().isMirrorY()));
        properties.setProperty("Rotation", String.valueOf(imageHandler.getImageDetails().getRotation()));

        // units
        properties.setProperty("TimeUnit", String.valueOf(imageHandler.getTimeUnit()));
        properties.setProperty("LevelUnit", String.valueOf(imageHandler.getLevelUnit()));

        // askAgainClosingWindow, theme, font size, language
        properties.setProperty("AskAgainClosingWindow", String.valueOf(isAskAgainClosingWindow()));
        properties.setProperty("Theme", String.valueOf(getTheme()));
        properties.setProperty("FontSize", String.valueOf(getFontSize()));
        properties.setProperty("Language", getLanguage().getName());
    }

    private File loadImageFolder(Properties properties) {
        String result = loadString(properties, "ImageFolder", null);
        if (result != null) {
            File file = new File(result);
            if (file.isDirectory()) return file;
        }
        return null;
    }

    private ImageType loadImageType(Properties properties) {
        String result = loadString(properties, "ImageType", null);
        if (result != null) {
            // compares descriptions of image types
            ImageType imageType = ImageFileConstants.getImageType(result);
            if (imageType != null) return imageType;
        }
        return DEFAULT_IMAGE_TYPE;
    }

    private String loadString(Properties properties, String property, String defaultValue) {
        String result = properties.getProperty(property);
        return (result == null || result.isEmpty()) ? defaultValue : result;
    }

    private boolean loadBoolean(Properties properties, String property, boolean defaultValue) {
        String bool = properties.getProperty(property);
        if ("true".equalsIgnoreCase(bool)) return true;
        else if ("false".equalsIgnoreCase(bool)) return false;
        return defaultValue;
    }

    private Number loadNumber(Properties properties, String property, Number defaultValue) {
        try {
            if (isInteger(defaultValue)) return Integer.parseInt(properties.getProperty(property));
            else if (isDouble(defaultValue)) return Double.parseDouble(properties.getProperty(property));
        } catch (NumberFormatException | NullPointerException ignored) {
            return defaultValue;
        }
        return defaultValue;
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
    }
}

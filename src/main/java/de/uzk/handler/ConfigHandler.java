package de.uzk.handler;

import de.uzk.utils.GuiUtils;
import de.uzk.utils.SystemConstants;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import static de.uzk.Main.imageHandler;
import static de.uzk.Main.logger;
import static de.uzk.handler.ImageFileConstants.*;
import static de.uzk.utils.NumberUtils.isDouble;
import static de.uzk.utils.NumberUtils.isInteger;
import static de.uzk.utils.SystemConstants.CONFIG_FILE;
import static de.uzk.utils.SystemConstants.SCREENSHOT_FOLDER;

public class ConfigHandler {
    public static final boolean DEFAULT_ASK_AGAIN_CLOSING_WINDOW = true;
    public static final Theme DEFAULT_THEME = Theme.LIGHT_MODE;
    public static final int MIN_FONT_SIZE = 10;
    public static final int DEFAULT_FONT_SIZE = 16;
    public static final int MAX_FONT_SIZE = 22;
    public static final LanguageHandler.Language SYSTEM_LANGUAGE = LanguageHandler.Language.getSystemDefault();



    private final SimpleDateFormat dateFormat;
    private boolean askAgainClosingWindow;
    private Theme theme;
    private int fontSize;
    private LanguageHandler.Language language;

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

    public void setFontSize(int newFontSize) {
        this.fontSize = newFontSize;
    }

    public LanguageHandler.Language getLanguage() {
        return this.language;
    }

    public void setLanguage(LanguageHandler.Language language) {
        this.language = language;
        LanguageHandler.initialize(language);
    }

    private void createConfigIfNeeded() {
        try {
            if (!CONFIG_FILE.exists() && !CONFIG_FILE.createNewFile()) {
                logger.error("The config was already created.");
            }
        } catch (IOException e) {
            logger.logException(e);
        }
    }

    public void loadConfig() {
        logger.info("Reading config...");
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(CONFIG_FILE)) {
            properties.load(fileInputStream);
            readProperties(properties);
        } catch (IOException ignored) {
            setDefaultValues();
        }
    }

    private void setDefaultValues() {
        // language. needs to be initialized first since the default imageDetails need translated text
        this.setLanguage(SYSTEM_LANGUAGE);
        // imageDetails
        imageHandler.setImageDetails(new ImageDetails(DEFAULT_SEP_TIME, DEFAULT_SEP_LEVEL, DEFAULT_IMAGE_TYPE,
                DEFAULT_MIRROR_IMAGE, DEFAULT_MIRROR_IMAGE, DEFAULT_IMAGE_ROTATION));

        // units
        imageHandler.setTimeUnit(DEFAULT_TIME_UNIT);
        imageHandler.setLevelUnit(DEFAULT_LEVEL_UNIT);

        // askAgainClosingWindow, theme, font size
        this.askAgainClosingWindow = DEFAULT_ASK_AGAIN_CLOSING_WINDOW;
        this.theme = DEFAULT_THEME;
        this.fontSize = DEFAULT_FONT_SIZE;

    }

    private void readProperties(Properties properties) {

        //language
        //Needs to be initialized first since others (e.g. imageDetails) need correct labels
        String languageName = loadString(properties, "Language", SYSTEM_LANGUAGE.getId());
        LanguageHandler.Language language = LanguageHandler.Language.fromId(languageName);
        setLanguage(language);
        logger.info("Found language setting '"+languageName+"', selecting language " + language);

        // folderDir
        imageHandler.setImageFolder(loadImageFolder(properties));

        // imageDetails
        String sepTime = loadString(properties, "SepTime", DEFAULT_SEP_TIME);
        String sepLevel = loadString(properties, "SepLevel", DEFAULT_SEP_LEVEL);
        ImageType imageType = loadImageType(properties);
        boolean mirrorX = loadBoolean(properties, "MirrorX", DEFAULT_MIRROR_IMAGE);
        boolean mirrorY = loadBoolean(properties, "MirrorY", DEFAULT_MIRROR_IMAGE);
        int rotation = loadNumber(properties, "Rotation", DEFAULT_IMAGE_ROTATION).intValue();
        imageHandler.setImageDetails(new ImageDetails(sepTime, sepLevel, imageType, mirrorX, mirrorY, rotation));

        // units
        imageHandler.setTimeUnit(loadNumber(properties, "TimeUnit", DEFAULT_TIME_UNIT).doubleValue());
        imageHandler.setLevelUnit(loadNumber(properties, "LevelUnit", DEFAULT_LEVEL_UNIT).doubleValue());

        // askAgainClosingWindow
        this.askAgainClosingWindow = loadBoolean(properties, "AskAgainClosingWindow", DEFAULT_ASK_AGAIN_CLOSING_WINDOW);
        // theme
        setTheme(loadString(properties, "Theme", DEFAULT_THEME.name()));
        // font size
        int tempFontSize = loadNumber(properties, "FontSize", -1).intValue();
        boolean legalFontSize = tempFontSize >= MIN_FONT_SIZE && tempFontSize <= MAX_FONT_SIZE;
        setFontSize(legalFontSize ? tempFontSize : DEFAULT_FONT_SIZE);

    }

    public void saveConfig() {
        createConfigIfNeeded();
        final Properties cfg = new Properties();

        logger.info("Storing config...");
        try (FileInputStream inputStream = new FileInputStream(CONFIG_FILE)) {
            cfg.load(inputStream);
            cfg.setProperty("ImageFolder", imageHandler.getImageDir());
            cfg.setProperty("SepTime", imageHandler.getImageDetails().getSepTime());
            cfg.setProperty("SepLevel", imageHandler.getImageDetails().getSepLevel());
            cfg.setProperty("ImageType", imageHandler.getImageDetails().getImageType().getTypeDescription());
            cfg.setProperty("MirrorX", String.valueOf(imageHandler.getImageDetails().isMirrorX()));
            cfg.setProperty("MirrorY", String.valueOf(imageHandler.getImageDetails().isMirrorY()));
            cfg.setProperty("Rotation", String.valueOf(imageHandler.getImageDetails().getRotation()));
            cfg.setProperty("TimeUnit", String.valueOf(imageHandler.getTimeUnit()));
            cfg.setProperty("LevelUnit", String.valueOf(imageHandler.getLevelUnit()));
            cfg.setProperty("AskAgainClosingWindow", String.valueOf(isAskAgainClosingWindow()));
            cfg.setProperty("Theme", String.valueOf(getTheme()));
            cfg.setProperty("FontSize", String.valueOf(getFontSize()));
            cfg.setProperty("Language", getLanguage().getId());
        } catch (IOException e) {
            logger.logException(e);
        }

        try (FileOutputStream outputStream = new FileOutputStream(CONFIG_FILE)) {
            cfg.store(outputStream, "Config");
        } catch (IOException e) {
            logger.logException(e);
        }
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
            if (imageType != null) {
                return imageType;
            }
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

    // screenshots
    public String saveScreenshot(BufferedImage originalImage) {
        if (originalImage != null) {
            // Format the date using the SimpleDateFormat
            String formattedDate = dateFormat.format(new Date());
            int screenshotCount = getScreenshots(formattedDate) + 1;
            String filename = getScreenshotName(formattedDate, screenshotCount, imageHandler.getCurrentImage());

            File saveFile = new File(SCREENSHOT_FOLDER.getAbsolutePath() + SystemConstants.FILE_SEP + filename);
            boolean savedImage = saveScreenshot(saveFile, originalImage);
            if (savedImage) return saveFile.getAbsolutePath();
        }
        return null;
    }

    private boolean saveScreenshot(File saveFile, BufferedImage originalImage) {
        try {
            if (!SCREENSHOT_FOLDER.exists() && !SCREENSHOT_FOLDER.mkdir()) {
                logger.error("Could not create screenshot folder.");
                return false;
            }

            if (saveFile.createNewFile()) {
                BufferedImage editedImage = GuiUtils.getEditedImage(originalImage, imageHandler.getImageDetails(), true);
                ImageIO.write(editedImage, imageHandler.getImageDetails().getImageType().getType(), saveFile);
                return true;
            }
        } catch (IOException e) {
            logger.logException(e);
            try {
                Files.delete(saveFile.toPath());
            } catch (IOException ex) {
                logger.logException(ex);
            }
        }
        return false;
    }

    public int getScreenshots() {
        return getScreenshots(dateFormat.format(new Date()));
    }

    private int getScreenshots(String formattedDate) {
        int maxCount = 0;
        if (SCREENSHOT_FOLDER.exists()) {
            File[] files = SCREENSHOT_FOLDER.listFiles(File::isFile);
            if (files != null) {
                String pattern = getScreenshotPattern(formattedDate, imageHandler.getImageDetails());
                for (File file : files) {
                    int count = getScreenshotCount(file, pattern);
                    if (count > maxCount) maxCount = count;
                }
            }
        }
        return maxCount;
    }

    private int getScreenshotCount(File file, String pattern) {
        if (file != null && file.exists()) {
            String filename = file.getName();

            // compare pattern
            if (filename.matches(pattern)) {
                int indexStart = filename.indexOf("(") + 1;
                int indexEnd = filename.indexOf(")");

                return Integer.parseInt(filename.substring(indexStart, indexEnd));
            }
        }
        return 0;
    }

    private String getScreenshotPattern(String date, ImageDetails imageDetails) {
        String imageNamePattern = ImageFile.getImageNamePattern(imageDetails);
        return date + "\\([0-9]+\\)_" + imageNamePattern;
    }

    private String getScreenshotName(String date, int screenshot, ImageFile imageFile) {
        return date + "("+ screenshot + ")_" + imageFile.getFileName();
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

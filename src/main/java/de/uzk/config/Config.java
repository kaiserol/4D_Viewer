package de.uzk.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.uzk.image.ImageFileType;
import de.uzk.utils.PathManager;

import java.nio.file.Path;

import static de.uzk.utils.PathManager.CONFIG_FILE_NAME;
import static de.uzk.utils.PathManager.resolveProjectPath;

public class Config {
    // Konfigurationen
    private ImageFileType imageFileType;
    private String timeSep;
    private String levelSep;
    private double timeUnit;
    private double levelUnit;
    private boolean mirrorX;
    private boolean mirrorY;
    private int brightness;
    private int contrast;
    private int zoom;
    private int rotation;

    // Default-Konstanten
    private static final ImageFileType DEFAULT_IMAGE_FILE_TYPE = ImageFileType.getDefault();
    private static final String DEFAULT_TIME_SEP = "X";
    private static final String DEFAULT_LEVEL_SEP = "L";
    private static final double DEFAULT_TIME_UNIT = 30.0;
    private static final double DEFAULT_LEVEL_UNIT = 1.0;

    private static final boolean DEFAULT_MIRROR_X = false;
    private static final boolean DEFAULT_MIRROR_Y = false;

    private static final int DEFAULT_BRIGHTNESS = 100;
    private static final int DEFAULT_CONTRAST = 100;
    private static final int DEFAULT_ZOOM = 100;
    private static final int DEFAULT_ROTATION = 0;

    // MinMax Konstanten
    public static final double MIN_TIME_UNIT = 1;
    public static final double MAX_TIME_UNIT = 600;
    public static final double MIN_LEVEL_UNIT = 0.1;
    public static final double MAX_LEVEL_UNIT = 1000;

    public static final int MIN_BRIGHTNESS = 0;
    public static final int MAX_BRIGHTNESS = 200;
    public static final int MIN_CONTRAST = 0;
    public static final int MAX_CONTRAST = 200;
    public static final int MIN_ZOOM = 50;
    public static final int MAX_ZOOM = 200;
    public static final int MIN_ROTATION = 0;
    public static final int MAX_ROTATION = 359;

    @JsonCreator
    public Config(
        @JsonProperty("imageFileType") ImageFileType imageFileType,
        @JsonProperty("timeSep") String timeSep,
        @JsonProperty("levelSep") String levelSep,
        @JsonProperty("timeUnit") double timeUnit,
        @JsonProperty("levelUnit") double levelUnit,
        @JsonProperty("mirrorX") boolean mirrorX,
        @JsonProperty("mirrorY") boolean mirrorY,
        @JsonProperty("brightness") int brightness,
        @JsonProperty("contrast") int contrast,
        @JsonProperty("zoom") int zoom,
        @JsonProperty("rotation") int rotation
    ) {
        this.setImageFileType(imageFileType);
        this.setTimeSep(timeSep);
        this.setLevelSep(levelSep);
        this.setTimeUnit(timeUnit);
        this.setLevelUnit(levelUnit);

        this.setMirrorX(mirrorX);
        this.setMirrorY(mirrorY);

        this.setBrightness(brightness);
        this.setContrast(contrast);
        this.setZoom(zoom);
        this.setRotation(rotation);
    }

    public ImageFileType getImageFileType() {
        return this.imageFileType;
    }

    public void setImageFileType(ImageFileType imageFileType) {
        if (imageFileType != null) {
            this.imageFileType = imageFileType;
        } else {
            // Setzt den Defaultwert, wenn der Wert null ist
            if (this.imageFileType != null) return;
            this.imageFileType = DEFAULT_IMAGE_FILE_TYPE;
        }
    }

    public String getTimeSep() {
        return this.timeSep;
    }

    public void setTimeSep(String timeSep) {
        if (timeSep != null && !timeSep.isBlank()) {
            this.timeSep = timeSep;
        } else {
            // Setzt den Defaultwert, wenn der Wert null ist
            if (this.timeSep != null) return;
            this.timeSep = DEFAULT_TIME_SEP;
        }
    }

    public String getLevelSep() {
        return this.levelSep;
    }

    public void setLevelSep(String levelSep) {
        if (levelSep != null && !levelSep.isBlank()) {
            this.levelSep = levelSep;
        } else {
            // Setzt den Defaultwert, wenn der Wert null ist
            if (this.levelSep != null) return;
            this.levelSep = DEFAULT_LEVEL_SEP;
        }
    }

    public double getTimeUnit() {
        return this.timeUnit;
    }

    public void setTimeUnit(double timeUnit) {
        if (MIN_TIME_UNIT <= timeUnit && timeUnit <= MAX_TIME_UNIT) {
            this.timeUnit = timeUnit;
        } else {
            // Setzt den Defaultwert, wenn der Wert nicht innerhalb der MinMax-Grenzen liegt
            if (MIN_TIME_UNIT <= this.timeUnit && this.timeUnit <= MAX_TIME_UNIT) return;
            this.timeUnit = DEFAULT_TIME_UNIT;
        }
    }

    public double getLevelUnit() {
        return this.levelUnit;
    }

    public void setLevelUnit(double levelUnit) {
        if (MIN_LEVEL_UNIT <= levelUnit && levelUnit <= MAX_LEVEL_UNIT) {
            this.levelUnit = levelUnit;
        } else {
            // Setzt den Defaultwert, wenn der Wert nicht innerhalb der MinMax-Grenzen liegt
            if (MIN_LEVEL_UNIT <= this.levelUnit && this.levelUnit <= MAX_LEVEL_UNIT) return;
            this.levelUnit = DEFAULT_LEVEL_UNIT;
        }
    }

    public boolean isMirrorX() {
        return this.mirrorX;
    }

    public void setMirrorX(boolean mirrorX) {
        this.mirrorX = mirrorX;
    }

    public boolean isMirrorY() {
        return this.mirrorY;
    }

    public void setMirrorY(boolean mirrorY) {
        this.mirrorY = mirrorY;
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        if (MIN_BRIGHTNESS <= brightness && brightness <= MAX_BRIGHTNESS) {
            this.brightness = brightness;
        } else {
            // Setzt den Defaultwert, wenn der Wert nicht innerhalb der MinMax-Grenzen liegt
            if (MIN_BRIGHTNESS <= this.brightness && this.brightness <= MAX_BRIGHTNESS) return;
            this.brightness = DEFAULT_BRIGHTNESS;
        }
    }

    public int getContrast() {
        return contrast;
    }

    public void setContrast(int contrast) {
        if (MIN_CONTRAST <= contrast && contrast <= MAX_CONTRAST) {
            this.contrast = contrast;
        } else {
            // Setzt den Defaultwert, wenn der Wert nicht innerhalb der MinMax-Grenzen liegt
            if (MIN_CONTRAST <= this.contrast && this.contrast <= MAX_CONTRAST) return;
            this.contrast = DEFAULT_CONTRAST;
        }
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        if (MIN_ZOOM <= zoom && zoom <= MAX_ZOOM) {
            this.zoom = zoom;
        } else {
            // Setzt den Defaultwert, wenn der Wert nicht innerhalb der MinMax-Grenzen liegt
            if (MIN_ZOOM <= this.zoom && this.zoom <= MAX_ZOOM) return;
            this.zoom = DEFAULT_ZOOM;
        }
    }

    public int getRotation() {
        return this.rotation;
    }

    public void setRotation(int rotation) {
        if (MIN_ROTATION <= rotation && rotation <= MAX_ROTATION) {
            this.rotation = rotation;
        } else {
            // Setzt den Defaultwert, wenn der Wert nicht innerhalb der MinMax-Grenzen liegt
            if (MIN_ROTATION <= this.rotation && this.rotation <= MAX_ROTATION) return;
            this.rotation = DEFAULT_ROTATION;
        }
    }

    public void save() {
        Path filePath = resolveProjectPath(CONFIG_FILE_NAME);
        PathManager.save(filePath, this);
    }

    public static Config load() {
        Path filePath = resolveProjectPath(CONFIG_FILE_NAME);

        Object object = PathManager.load(filePath, Config.class);
        if (object instanceof Config config) return config;
        else return getDefault();
    }

    public static Config getDefault() {
        return new Config(
            DEFAULT_IMAGE_FILE_TYPE,
            DEFAULT_TIME_SEP,
            DEFAULT_LEVEL_SEP,
            DEFAULT_TIME_UNIT,
            DEFAULT_LEVEL_UNIT,
            DEFAULT_MIRROR_X,
            DEFAULT_MIRROR_Y,
            DEFAULT_BRIGHTNESS,
            DEFAULT_CONTRAST,
            DEFAULT_ZOOM,
            DEFAULT_ROTATION
        );
    }
}

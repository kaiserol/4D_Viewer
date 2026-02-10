package de.uzk.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.uzk.image.ImageFileType;
import de.uzk.io.PathManager;
import de.uzk.utils.NumberUtils;

import java.nio.file.Path;

public class Config {
    // MinMax-Konstanten
    public static final double MIN_TIME_UNIT = 1;
    public static final double MAX_TIME_UNIT = 600;
    public static final double MIN_LEVEL_UNIT = 0.1;
    public static final double MAX_LEVEL_UNIT = 1000;
    public static final int MIN_BRIGHTNESS = 0;
    public static final int MAX_BRIGHTNESS = 200;
    public static final int MIN_CONTRAST = 0;
    public static final int MAX_CONTRAST = 200;
    public static final int MIN_ZOOM = 100;
    public static final int MAX_ZOOM = 500;
    public static final int MIN_ROTATION = 0;
    public static final int MAX_ROTATION = 359;
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

    public static Config load() {
        Path filePath = PathManager.resolveProjectPath(PathManager.CONFIG_FILE_NAME);

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

    public boolean setTimeUnit(double timeUnit) {
        if (NumberUtils.valueInRange(timeUnit, MIN_TIME_UNIT, MAX_TIME_UNIT)) {
            if (this.timeUnit == timeUnit) return false;
            this.timeUnit = timeUnit;
        } else {
            // Setzt den Defaultwert, wenn der Wert nicht innerhalb der MinMax-Grenzen liegt
            if (NumberUtils.valueInRange(this.timeUnit, MIN_TIME_UNIT, MAX_TIME_UNIT)) return false;
            this.timeUnit = DEFAULT_TIME_UNIT;
        }
        return true;
    }

    public double getLevelUnit() {
        return this.levelUnit;
    }

    public boolean setLevelUnit(double levelUnit) {
        if (NumberUtils.valueInRange(levelUnit, MIN_LEVEL_UNIT, MAX_LEVEL_UNIT)) {
            if (this.levelUnit == levelUnit) return false;
            this.levelUnit = levelUnit;
        } else {
            // Setzt den Defaultwert, wenn der Wert nicht innerhalb der MinMax-Grenzen liegt
            if (NumberUtils.valueInRange(this.levelUnit, MIN_LEVEL_UNIT, MAX_LEVEL_UNIT)) return false;
            this.levelUnit = DEFAULT_LEVEL_UNIT;
        }
        return true;
    }

    public boolean isMirrorX() {
        return this.mirrorX;
    }

    public boolean setMirrorX(boolean mirrorX) {
        if (this.mirrorX == mirrorX) return false;
        this.mirrorX = mirrorX;
        return true;
    }

    public boolean isMirrorY() {
        return this.mirrorY;
    }

    public boolean setMirrorY(boolean mirrorY) {
        if (this.mirrorY == mirrorY) return false;
        this.mirrorY = mirrorY;
        return true;
    }

    public int getBrightness() {
        return brightness;
    }

    public boolean setBrightness(int brightness) {
        if (NumberUtils.valueInRange(brightness, MIN_BRIGHTNESS, MAX_BRIGHTNESS)) {
            if (this.brightness == brightness) return false;
            this.brightness = brightness;
        } else {
            // Setzt den Defaultwert, wenn der Wert nicht innerhalb der MinMax-Grenzen liegt
            if (NumberUtils.valueInRange(this.brightness, MIN_BRIGHTNESS, MAX_BRIGHTNESS)) return false;
            this.brightness = DEFAULT_BRIGHTNESS;
        }
        return true;
    }

    public int getContrast() {
        return contrast;
    }

    public boolean setContrast(int contrast) {
        if (NumberUtils.valueInRange(contrast, MIN_CONTRAST, MAX_CONTRAST)) {
            if (this.contrast == contrast) return false;
            this.contrast = contrast;
        } else {
            // Setzt den Defaultwert, wenn der Wert nicht innerhalb der MinMax-Grenzen liegt
            if (NumberUtils.valueInRange(this.contrast, MIN_CONTRAST, MAX_CONTRAST)) return false;
            this.contrast = DEFAULT_CONTRAST;
        }
        return true;
    }

    public int getZoom() {
        return zoom;
    }

    public boolean setZoom(int zoom) {
        if (NumberUtils.valueInRange(zoom, MIN_ZOOM, MAX_ZOOM)) {
            if (this.zoom == zoom) return false;
            this.zoom = zoom;
        } else {
            // Setzt den Defaultwert, wenn der Wert nicht innerhalb der MinMax-Grenzen liegt
            if (NumberUtils.valueInRange(this.zoom, MIN_ZOOM, MAX_ZOOM)) return false;
            this.zoom = DEFAULT_ZOOM;
        }
        return true;
    }

    public int getRotation() {
        return this.rotation;
    }

    public boolean setRotation(int rotation) {
        if (NumberUtils.valueInRange(rotation, MIN_ROTATION, MAX_ROTATION)) {
            if (this.rotation == rotation) return false;
            this.rotation = rotation;
        } else {
            // Setzt den Defaultwert, wenn der Wert nicht innerhalb der MinMax-Grenzen liegt
            if (NumberUtils.valueInRange(this.rotation, MIN_ROTATION, MAX_ROTATION)) return false;
            this.rotation = DEFAULT_ROTATION;
        }
        return true;
    }

    public void save() {
        Path filePath = PathManager.resolveProjectPath(PathManager.CONFIG_FILE_NAME);
        PathManager.save(filePath, this);
    }
}

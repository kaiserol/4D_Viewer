package de.uzk.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.uzk.image.ImageFileType;

import java.nio.file.Path;
import java.util.Objects;

import static de.uzk.utils.PathManager.*;

public class Config {
    // Konfigurationen
    private ImageFileType imageFileType;
    private String timeSep;
    private String levelSep;
    private double timeUnit;
    private double levelUnit;
    private boolean mirrorX;
    private boolean mirrorY;
    private int rotation;
    private int zoom;
    private int contrast;
    private int brightness;

    // Default-Konstanten
    public static final ImageFileType DEFAULT_IMAGE_FILE_TYPE = ImageFileType.getDefault();
    public static final String DEFAULT_TIME_SEP = "X";
    public static final String DEFAULT_LEVEL_SEP = "L";
    public static final double DEFAULT_TIME_UNIT = 30.0;
    public static final double DEFAULT_LEVEL_UNIT = 1.0;
    public static final boolean DEFAULT_MIRROR_X = false;
    public static final boolean DEFAULT_MIRROR_Y = false;
    public static final int DEFAULT_ROTATION = 0;
    private static final int DEFAULT_ZOOM = 100;
    private static final int DEFAULT_CONTRAST = 100;
    private static final int DEFAULT_BRIGHTNESS = 100;

    // MinMax Konstanten
    public static final double MAX_TIME_UNIT = 600;
    public static final double MAX_LEVEL_UNIT = 1000;
    public static final int MAX_ROTATION = 359;
    public static final int MIN_CONTRAST = 1;
    public static final int MAX_CONTRAST = 200;
    public static final int MIN_BRIGHTNESS = 1;
    public static final int MAX_BRIGHTNESS = 200;
    public static final int MIN_ZOOM = 50;
    public static final int MAX_ZOOM = 200;

    @JsonCreator
    public Config(
        @JsonProperty("imageFileType") ImageFileType imageFileType,
        @JsonProperty("timeSep") String timeSep,
        @JsonProperty("levelSep") String levelSep,
        @JsonProperty("timeUnit") double timeUnit,
        @JsonProperty("levelUnit") double levelUnit,
        @JsonProperty("mirrorX") boolean mirrorX,
        @JsonProperty("mirrorY") boolean mirrorY,
        @JsonProperty("rotation") int rotation,
        @JsonProperty("zoom") int zoom,
        @JsonProperty("contrast") int contrast,
        @JsonProperty("brightness") int brightness
    ) {
        this.setImageFileType(imageFileType);
        this.setTimeSep(timeSep);
        this.setLevelSep(levelSep);
        this.setTimeUnit(timeUnit);
        this.setLevelUnit(levelUnit);
        this.setMirrorX(mirrorX);
        this.setMirrorY(mirrorY);
        this.setRotation(rotation);
        this.setZoom(zoom);
        this.setContrast(contrast);
        this.setBrightness(brightness);
    }

    public ImageFileType getImageFileType() {
        return this.imageFileType;
    }

    public void setImageFileType(ImageFileType imageFileType) {
        if (this.imageFileType == imageFileType && this.imageFileType != null) return;
        this.imageFileType = (imageFileType != null) ? imageFileType : DEFAULT_IMAGE_FILE_TYPE;
    }

    public String getTimeSep() {
        return this.timeSep;
    }

    public void setTimeSep(String timeSep) {
        if (Objects.equals(this.timeSep, timeSep) && this.timeSep != null) return;
        this.timeSep = (timeSep != null && !timeSep.isBlank()) ? timeSep : DEFAULT_TIME_SEP;
    }

    public String getLevelSep() {
        return this.levelSep;
    }

    public void setLevelSep(String levelSep) {
        if (Objects.equals(this.levelSep, levelSep) && this.levelSep != null) return;
        this.levelSep = (levelSep != null && !levelSep.isBlank()) ? levelSep : DEFAULT_LEVEL_SEP;
    }

    public double getTimeUnit() {
        return this.timeUnit;
    }

    public void setTimeUnit(double timeUnit) {
        if (this.timeUnit == timeUnit) return;
        this.timeUnit = (timeUnit >= 0 && timeUnit <= MAX_TIME_UNIT) ? timeUnit : DEFAULT_TIME_UNIT;
    }

    public double getLevelUnit() {
        return this.levelUnit;
    }

    public void setLevelUnit(double levelUnit) {
        if (this.levelUnit == levelUnit) return;
        this.levelUnit = (levelUnit >= 0 && levelUnit <= MAX_LEVEL_UNIT) ? levelUnit : DEFAULT_LEVEL_UNIT;
    }

    public boolean isMirrorX() {
        return this.mirrorX;
    }

    public void setMirrorX(boolean mirrorX) {
        if (this.mirrorX == mirrorX) return;
        this.mirrorX = mirrorX;
    }

    public boolean isMirrorY() {
        return this.mirrorY;
    }

    public void setMirrorY(boolean mirrorY) {
        if (this.mirrorY == mirrorY) return;
        this.mirrorY = mirrorY;
    }

    public int getRotation() {
        return this.rotation;
    }

    public void setRotation(int rotation) {
        if (this.rotation == rotation) return;
        this.rotation = (rotation >= 0 && rotation <= MAX_ROTATION) ? rotation : DEFAULT_ROTATION;
    }

    public void save() {
        Path jsonFile = resolveInAppProjectsPath(Path.of(CONFIG_FILE_NAME));
        saveJson(jsonFile, this);
    }

    public static Config load() {
        Path jsonFile = resolveInAppProjectsPath(Path.of(CONFIG_FILE_NAME));

        Object obj = loadJson(jsonFile, Config.class);
        if (obj instanceof Config config) return config;
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
                DEFAULT_ROTATION,
                DEFAULT_ZOOM,
                DEFAULT_CONTRAST,
                DEFAULT_BRIGHTNESS
        );
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        if(MIN_ZOOM <= zoom && zoom <= MAX_ZOOM) {
            this.zoom = zoom;
        } else {
            this.zoom = DEFAULT_ZOOM;
        }
    }

    public int getContrast() {
        return contrast;
    }

    public void setContrast(int contrast) {
        if(MIN_CONTRAST <= contrast && contrast <= MAX_CONTRAST) {
            this.contrast = contrast;
        } else {
            this.contrast = DEFAULT_CONTRAST;
        }
    }

    public int getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        if(MIN_BRIGHTNESS <= brightness && brightness <= MAX_BRIGHTNESS) {
            this.brightness = brightness;
        }
    }
}

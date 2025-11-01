package de.uzk.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import de.uzk.image.ImageFileType;

import java.nio.file.Path;
import java.util.Objects;

import static de.uzk.utils.AppPath.*;

public class Config {
    // Konfigurationen
    @JsonUnwrapped
    private ImageFileType imageFileType;
    private String timeSep;
    private String levelSep;
    private double timeUnit;
    private double levelUnit;
    private boolean mirrorX;
    private boolean mirrorY;
    private int rotation;

    // Default-Konstanten
    private static final ImageFileType DEFAULT_IMAGE_FILE_TYPE = ImageFileType.getDefault();
    private static final String DEFAULT_TIME_SEP = "X";
    private static final String DEFAULT_LEVEL_SEP = "L";
    private static final double DEFAULT_TIME_UNIT = 30.0;
    private static final double DEFAULT_LEVEL_UNIT = 1.0;
    private static final boolean DEFAULT_MIRROR_X = false;
    private static final boolean DEFAULT_MIRROR_Y = false;
    private static final int DEFAULT_ROTATION = 0;

    // MinMax Konstanten
    public static final double MAX_TIME_UNIT = 600;
    public static final double MAX_LEVEL_UNIT = 1000;
    public static final int MAX_ROTATION = 359;

    // Nur Konstanten vom primitiven Datentyp können als Default-Werte verwendet werden (inklusive Strings)
    @JsonCreator
    public Config(
            @JsonProperty(value = "imageFileType") ImageFileType imageFileType,
            @JsonProperty(value = "timeSep", defaultValue = DEFAULT_TIME_SEP) String timeSep,
            @JsonProperty(value = "levelSep", defaultValue = DEFAULT_LEVEL_SEP) String levelSep,
            @JsonProperty(value = "timeUnit", defaultValue = DEFAULT_TIME_UNIT + "") double timeUnit,
            @JsonProperty(value = "levelUnit", defaultValue = DEFAULT_LEVEL_UNIT + "") double levelUnit,
            @JsonProperty(value = "mirrorX", defaultValue = DEFAULT_MIRROR_X + "") boolean mirrorX,
            @JsonProperty(value = "mirrorY", defaultValue = DEFAULT_MIRROR_Y + "") boolean mirrorY,
            @JsonProperty(value = "rotation", defaultValue = DEFAULT_ROTATION + "") int rotation
    ) {
        this.setImageFileType(imageFileType);
        this.setTimeSep(timeSep);
        this.setLevelSep(levelSep);
        this.setTimeUnit(timeUnit);
        this.setLevelUnit(levelUnit);
        this.setMirrorX(mirrorX);
        this.setMirrorY(mirrorY);
        this.setRotation(rotation);
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
        Path jsonPath = getAppProjectPath(Path.of(CONFIG_FILE_NAME));
        saveJson(jsonPath, this);
    }

    public static Config load() {
        Path jsonPath = getAppProjectPath(Path.of(CONFIG_FILE_NAME));

        Object obj = loadJson(jsonPath, Config.class);
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
                DEFAULT_ROTATION
        );
    }
}

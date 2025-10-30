package de.uzk.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.uzk.image.ImageFileType;
import de.uzk.utils.AppPath;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

import static de.uzk.Main.logger;

public class Config {
    // MinMax Konstanten
    public static final double MAX_TIME_UNIT = 600;
    public static final double MAX_LEVEL_UNIT = 1000;
    public static final int MAX_ROTATION = 359;

    // Default-Konstanten
    private static final ImageFileType DEFAULT_IMAGE_FILE_TYPE = ImageFileType.getDefault();
    private static final String DEFAULT_TIME_SEP = "X";
    private static final String DEFAULT_LEVEL_SEP = "L";
    private static final double DEFAULT_TIME_UNIT = 30.0;
    private static final double DEFAULT_LEVEL_UNIT = 1.0;
    private static final boolean DEFAULT_MIRROR_X = false;
    private static final boolean DEFAULT_MIRROR_Y = false;
    private static final int DEFAULT_ROTATION = 0;
    private static final String CONFIG_FILE_NAME = "config.json";

    // Konfigurationen
    private ImageFileType imageFileType;
    private String timeSep;
    private String levelSep;
    private double timeUnit;
    private double levelUnit;
    private boolean mirrorX;
    private boolean mirrorY;
    private int rotation;


    // Nur Konstanten vom primitiven Datentyp können als Default-Werte verwendet werden (inklusive Strings)
    @JsonCreator
    public Config(
            @JsonProperty(value = "imageFileType") String imageFileType,
            @JsonProperty(value = "timeSep", defaultValue = DEFAULT_TIME_SEP) String timeSep,
            @JsonProperty(value = "levelSep", defaultValue = DEFAULT_LEVEL_SEP) String levelSep,
            @JsonProperty(value = "timeUnit", defaultValue = DEFAULT_TIME_UNIT + "") double timeUnit,
            @JsonProperty(value = "levelUnit", defaultValue = DEFAULT_LEVEL_UNIT + "") double levelUnit,
            @JsonProperty(value = "mirrorX", defaultValue = DEFAULT_MIRROR_X + "") boolean mirrorX,
            @JsonProperty(value = "mirrorY", defaultValue = DEFAULT_MIRROR_Y + "") boolean mirrorY,
            @JsonProperty(value = "rotation", defaultValue = DEFAULT_ROTATION + "") int rotation
            ) {
        this.setImageFileType(ImageFileType.fromExtension(imageFileType));
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
        this.imageFileType = (imageFileType != null) ? imageFileType : DEFAULT_IMAGE_FILE_TYPE;
    }

    public String getTimeSep() {
        return this.timeSep;
    }

    public void setTimeSep(String timeSep) {
        this.timeSep = (timeSep != null && !timeSep.isBlank()) ? timeSep : DEFAULT_TIME_SEP;
    }

    public String getLevelSep() {
        return this.levelSep;
    }

    public void setLevelSep(String levelSep) {
        this.levelSep = (levelSep != null && !levelSep.isBlank()) ? levelSep : DEFAULT_LEVEL_SEP;
    }

    public double getTimeUnit() {
        return this.timeUnit;
    }

    public void setTimeUnit(double timeUnit) {
        this.timeUnit = (timeUnit >= 0 && timeUnit <= MAX_TIME_UNIT) ? timeUnit : DEFAULT_TIME_UNIT;
    }

    public double getLevelUnit() {
        return this.levelUnit;
    }

    public void setLevelUnit(double levelUnit) {
        this.levelUnit = (levelUnit >= 0 && levelUnit <= MAX_LEVEL_UNIT) ? levelUnit : DEFAULT_LEVEL_UNIT;
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

    public int getRotation() {
        return this.rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = (rotation >= 0 && rotation <= MAX_ROTATION) ? rotation : DEFAULT_ROTATION;
    }

    public void save(Path folderName) {
        Path location = AppPath.VIEWER_HOME_DIRECTORY.resolve(folderName).resolve(CONFIG_FILE_NAME);
        logger.info("Loading config under '" + location.toAbsolutePath() + "' ...");
        try {
            if(!Files.exists(location)) {
                Files.createDirectories(location.getParent());
            }
            new ObjectMapper().writeValue(location, this);
        } catch (Exception e) {
            logger.error("Failed to save config: " + e.getMessage());
        }
    }

    public static Config load(Path folderName) {
        Path directory = AppPath.VIEWER_HOME_DIRECTORY.resolve(folderName).resolve(CONFIG_FILE_NAME);
        logger.info("Loading config from '" + directory.toAbsolutePath() + "' ...");
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(directory, Config.class);
        } catch (Exception e) {
            logger.error("Failed to load config: " + e.getMessage());
        }
        return getDefault();
    }

    // TODO: TimeSep, LevelSep müssen veränderbar sein (entweder über settings, dann muss es aber auch in settings als Attribut geführt werden. oder es wird vor dem Öffnen des JFileDialog abgefragt (Freiwillig))
    // Das abspeichern von TimeSep und LevelSep macht sonst keinen sinn, weil es ja nicht
    // veränderbar ist und wäre somit bislang in settings besser aufgehoben ändern zu können)
    public static Config getDefault() {
        return new Config(
                DEFAULT_IMAGE_FILE_TYPE.name(),
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

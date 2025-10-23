package de.uzk.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.GsonBuilder;
import de.uzk.markers.Marker;
import de.uzk.markers.MarkerMapping;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static de.uzk.Main.logger;
import static de.uzk.Main.operationSystem;

public class Config {
    // MinMax Konstanten
    public static final double MAX_TIME_UNIT = 600;
    public static final double MAX_LEVEL_UNIT = 1000;
    public static final int MAX_ROTATION = 359;

    // Default-Konstanten
    private static final String DEFAULT_TIME_SEP = "X";
    private static final String DEFAULT_LEVEL_SEP = "L";
    private static final double DEFAULT_TIME_UNIT = 30.0;
    private static final double DEFAULT_LEVEL_UNIT = 1.0;
    private static final boolean DEFAULT_MIRROR_X = false;
    private static final boolean DEFAULT_MIRROR_Y = false;
    private static final int DEFAULT_ROTATION = 0;

    // Primitive Datentypen
    private String timeSep;
    private String levelSep;
    private double timeUnit;
    private double levelUnit;
    private boolean mirrorX;
    private boolean mirrorY;
    private int rotation;

    // Komplexe Datentypen
    private List<MarkerMapping> markers;

    @JsonCreator
    public Config(
            @JsonProperty(value = "timeSep", defaultValue = DEFAULT_TIME_SEP) String timeSep,
            @JsonProperty(value = "levelSep", defaultValue = DEFAULT_LEVEL_SEP) String levelSep,
            @JsonProperty(value = "timeUnit", defaultValue = DEFAULT_TIME_UNIT + "") double timeUnit,
            @JsonProperty(value = "levelUnit", defaultValue = DEFAULT_LEVEL_UNIT + "") double levelUnit,
            @JsonProperty(value = "mirrorX", defaultValue = DEFAULT_MIRROR_X + "") boolean mirrorX,
            @JsonProperty(value = "mirrorY", defaultValue = DEFAULT_MIRROR_Y + "") boolean mirrorY,
            @JsonProperty(value = "rotation", defaultValue = DEFAULT_ROTATION + "") int rotation,
            @JsonProperty(value = "markers", defaultValue = "[]") List<MarkerMapping> markers
    ) {
        this.setTimeSep(timeSep);
        this.setLevelSep(levelSep);
        this.setTimeUnit(timeUnit);
        this.setLevelUnit(levelUnit);
        this.setMirrorX(mirrorX);
        this.setMirrorY(mirrorY);
        this.setRotation(rotation);
        this.setMarkers(markers);
    }

    private Config() {
        this(
                DEFAULT_TIME_SEP,
                DEFAULT_LEVEL_SEP,
                DEFAULT_TIME_UNIT,
                DEFAULT_LEVEL_UNIT,
                DEFAULT_MIRROR_X,
                DEFAULT_MIRROR_Y,
                DEFAULT_ROTATION,
                new ArrayList<>()
        );
    }

    public String getTimeSep() {
        return this.timeSep;
    }

    public void setTimeSep(String timeSep) {
        this.timeSep = (timeSep == null || timeSep.isBlank()) ? DEFAULT_TIME_SEP : timeSep;
    }

    public String getLevelSep() {
        return this.levelSep;
    }

    public void setLevelSep(String levelSep) {
        this.levelSep = (levelSep == null || levelSep.isBlank()) ? DEFAULT_LEVEL_SEP : levelSep;
    }

    public double getTimeUnit() {
        return this.timeUnit;
    }

    public void setTimeUnit(double timeUnit) {
        this.timeUnit = (timeUnit <= 0 || timeUnit >= MAX_TIME_UNIT) ? DEFAULT_TIME_UNIT : timeUnit;
    }

    public double getLevelUnit() {
        return this.levelUnit;
    }

    public void setLevelUnit(double levelUnit) {
        this.levelUnit = (levelUnit <= 0 || levelUnit > MAX_LEVEL_UNIT) ? DEFAULT_LEVEL_UNIT : levelUnit;
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
        this.rotation = (rotation <= 0 || rotation > MAX_ROTATION) ? DEFAULT_ROTATION : rotation;
    }

    public List<MarkerMapping> getMarkers() {
        return this.markers;
    }

    public void setMarkers(List<MarkerMapping> markers) {
        if (markers == null) markers = new ArrayList<>();
        else markers.removeIf((Predicate<? super MarkerMapping>) m -> m == null || m.getMarker() == null);
        this.markers = markers;
    }

    public void addMarker(Marker marker, int image) {
        this.markers.add(new MarkerMapping(marker, image, image));
    }

    public void save(String fileName) {
        logger.info("Storing Config File ...");
        if (fileName != null && !fileName.isBlank()) {
            Path location = operationSystem.getDirectoryPath(true).resolve(fileName);
            try (BufferedWriter out = Files.newBufferedWriter(location)) {
                out.write(new GsonBuilder().setPrettyPrinting().create().toJson(this));
            } catch (IOException e) {
                logger.error("Failed to store config: " + e.getMessage());
            }
        }
    }

    public static Config load(String fileName) {
        logger.info("Loading Config File ...");
        if (fileName != null && !fileName.isBlank()) {
            Path location = operationSystem.getDirectoryPath(true).resolve(fileName);
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(location, Config.class);
            } catch (JacksonException e) {
                logger.error("Failed to load config: " + e.getMessage());
            }
        }
        return new Config();
    }
}

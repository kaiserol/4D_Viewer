package de.uzk.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.uzk.markers.Marker;
import de.uzk.markers.MarkerMapping;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static de.uzk.Main.logger;
import static de.uzk.Main.operationSystem;

public class Config {
    // Konstanten
    public static final double MAX_TIME_UNIT = 600;
    public static final double MAX_LEVEL_UNIT = 1000;
    public static final int MAX_ROTATION = 359;

    // Default-Konstanten
    private static final String DEFAULT_TIME_SEP = "X";
    private static final String DEFAULT_LEVEL_SEP = "L";
    private static final String DEFAULT_TIME_UNIT = "30.0";
    private static final String DEFAULT_LEVEL_UNIT = "1.0";
    private static final String DEFAULT_MIRROR_X = "false";
    private static final String DEFAULT_MIRROR_Y = "false";
    private static final String DEFAULT_ROTATION = "0";

    // Primitive Datentypen & String
    private String timeSep;
    private String levelSep = "L";
    private double timeUnit;
    private double levelUnit;
    private boolean mirrorX;
    private boolean mirrorY;
    private int rotation;

    @JsonCreator
    public Config(
            @JsonProperty(value = "timeSep", defaultValue = DEFAULT_TIME_SEP) String timeSep,
            @JsonProperty(value = "levelSep", defaultValue = DEFAULT_LEVEL_SEP) String levelSep,
            @JsonProperty(value = "timeUnit", defaultValue = DEFAULT_TIME_UNIT) double timeUnit,
            @JsonProperty(value = "levelUnit", defaultValue = DEFAULT_LEVEL_UNIT) double levelUnit,
            @JsonProperty(value = "mirrorX", defaultValue = DEFAULT_MIRROR_X) boolean mirrorX,
            @JsonProperty(value = "mirrorY", defaultValue = DEFAULT_MIRROR_Y) boolean mirrorY,
            @JsonProperty(value = "rotation",  defaultValue = DEFAULT_ROTATION) String rotation


            ) {
        this.setTimeSep(timeSep);
        this.setLevelSep(levelSep);
        this.setTimeUnit(timeUnit);
        this.setLevelUnit(levelUnit);
        this.setMirrorX(mirrorX);
        this.setMirrorY(mirrorY);
        this.setRotation(Integer.parseInt(rotation));
    }

    // Komplexe Datentypen
    private List<MarkerMapping> markers;

    private Config() {
        this.markers = new ArrayList<>();
    }

    public String getTimeSep() {
        return this.timeSep;
    }

    public void setTimeSep(String timeSep) {
        if(timeSep == null || timeSep.isBlank()) {
            timeSep = DEFAULT_TIME_SEP;
        }
        this.timeSep = timeSep;
    }

    public String getLevelSep() {
        return this.levelSep;
    }

    public void setLevelSep(String levelSep) {

        this.levelSep = levelSep;
    }

    public double getTimeUnit() {
        return this.timeUnit;
    }

    public void setTimeUnit(double timeUnit) {
        if(timeUnit <= 0 ||  timeUnit >= MAX_TIME_UNIT) {
            timeUnit = Double.parseDouble(DEFAULT_TIME_UNIT);
        }
        this.timeUnit = timeUnit;
    }

    public double getLevelUnit() {
        return this.levelUnit;
    }

    public void setLevelUnit(double levelUnit) {
        if(levelUnit <= 0 || levelUnit > MAX_LEVEL_UNIT) {
            levelUnit = Double.parseDouble(DEFAULT_LEVEL_UNIT);
        }
        this.levelUnit = levelUnit;
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
        if(rotation <= 0 || rotation > MAX_ROTATION) {
            rotation = Integer.parseInt(DEFAULT_ROTATION);
        }
        this.rotation = rotation;
    }

    public List<MarkerMapping> getMarkers(int time) {
        return this.markers.stream().filter(m -> m.shouldRender(time)).toList();
    }

    public List<MarkerMapping> getMarkers() {
        return this.markers;
    }

    public void addMarker(Marker marker, int image) {
        this.addMarker(marker, image, image);
    }

    public void addMarker(Marker marker, int from, int to) {
        this.markers.add(new MarkerMapping(marker, from, to));
    }



    private void validate() {
        if (timeSep == null || timeSep.isBlank()) timeSep = "X";
        if (levelSep == null || levelSep.isBlank()) levelSep = "L";
        // Entferne null-Einträge in Markern
        if (this.markers == null) this.markers = new ArrayList<>();
        markers.removeIf(m -> m == null || m.getMarker() == null);
    }

    public void save(String fileName) {
        Path location = operationSystem.getDirectoryPath(true).resolve(fileName);
        try (BufferedWriter out = Files.newBufferedWriter(location)) {
            out.write(new GsonBuilder().setPrettyPrinting().create().toJson(this));
        } catch (IOException e) {
            logger.logException(e);
        }
    }

    public static Config load(String fileName) {
        if(fileName == null || fileName.isBlank()) return new Config();
        Path location = operationSystem.getDirectoryPath(true).resolve(fileName);

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(location, Config.class);
        } catch (JacksonException e) {
            logger.error("Failed to load config: " + e.getMessage());
            return new Config();
        }
    }
}

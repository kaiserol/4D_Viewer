package de.uzk.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.uzk.Main;
import de.uzk.markers.Marker;
import de.uzk.markers.MarkerMapping;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static de.uzk.Main.operationSystem;
import static de.uzk.Main.settings;

public class Config {
    private static final Path CONFIG_FILE_NAME = Path.of("config.json");

    private String timeSep = "X";
    private String levelSep = "L";
    private double timeUnit = 30.0; //30s
    private double levelUnit = 1.0; //1 um
    private boolean mirrorX = false;
    private boolean mirrorY = false;
    private int rotation = 0;
    private List<MarkerMapping> markers = new ArrayList<>();

    public String getTimeSep() {
        return this.timeSep;
    }

    public void setTimeSep(String timeSep) {
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
        this.timeUnit = timeUnit;
    }

    public double getLevelUnit() {
        return this.levelUnit;
    }

    public void setLevelUnit(double levelUnit) {
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

    public void save(Path fileName) {
        Path location = operationSystem.getDataDirectory().resolve(fileName);
        try (BufferedWriter out = Files.newBufferedWriter(location)) {
            out.write(new GsonBuilder().setPrettyPrinting().create().toJson(this));
        } catch (IOException e) {
            Main.logger.error("Couldn't save " + location.getFileName().toString() +": " + e.getMessage());
        }
    }

    public static Config load(Path fileName) {
        return load(fileName, true);
    }

    public static Config load(Path fileName, boolean fallback) {
        Path location = operationSystem.getDataDirectory().resolve(fileName);
        try (BufferedReader in = Files.newBufferedReader(location)) {
            Gson gson = new Gson();
            return gson.fromJson(in, Config.class);
        } catch (IOException e) {
            if (fallback && settings.getLastHistory() != null) {
                Path last = Path.of(settings.getLastHistory().getFileName() + ".json");
                return Config.load(last, false);
            }
            return new Config();
        }
    }
}

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

import static de.uzk.Main.history;
import static de.uzk.Main.operationSystem;

public class Config {
    // Primitive Datentypen & String
    private String timeSep;
    private String levelSep;
    private double timeUnit;
    private double levelUnit;
    private boolean mirrorX;
    private boolean mirrorY;
    private int rotation;

    // Komplexe Datentypen
    private final List<MarkerMapping> markers;

    private Config() {
        setTimeSep("X");
        setLevelSep("L");
        setTimeUnit(30.0);
        setLevelUnit(1.0);
        setMirrorX(false);
        setMirrorY(false);
        setRotation(0);

        // Liste initialisieren
        this.markers = new ArrayList<>();
    }

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
        Path location = operationSystem.getDirectoryPath(true).resolve(fileName);
        try (BufferedWriter out = Files.newBufferedWriter(location)) {
            out.write(new GsonBuilder().setPrettyPrinting().create().toJson(this));
        } catch (IOException e) {
            Main.logger.error("Couldn't save " + location.getFileName().toString() + ": " + e.getMessage());
        }
    }

    public static Config load(Path fileName) {
        return load(fileName, true);
    }

    public static Config load(Path fileName, boolean fallback) {
        Path location = operationSystem.getDirectoryPath(true).resolve(fileName);
        try (BufferedReader in = Files.newBufferedReader(location)) {
            Gson gson = new Gson();
            return gson.fromJson(in, Config.class);
        } catch (IOException e) {
            if (fallback && !history.isEmpty()) {
                Path last = Path.of(history.last().getFileName() + ".json");
                return Config.load(last, false);
            }
            return new Config();
        }
    }
}

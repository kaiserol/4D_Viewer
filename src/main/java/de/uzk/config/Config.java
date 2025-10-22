package de.uzk.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.uzk.Main;
import de.uzk.image.ImageFileNameExtension;
import de.uzk.markers.MarkerHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.uzk.Main.settings;

public class Config implements Serializable {
    private static final Path CONFIG_FILE_NAME = Path.of("config.json");

    private String timeSep = "X";
    private String levelSep = "L";
    private double timeUnit = 30.0; //30s
    private double levelUnit = 1.0; //1 um
    private boolean mirrorX = false;
    private boolean mirrorY = false;
    private int rotation = 0;




    public String getTimeSep() {
        return timeSep;
    }

    public void setTimeSep(String timeSep) {
        this.timeSep = timeSep;
    }

    public String getLevelSep() {
        return levelSep;
    }

    public void setLevelSep(String levelSep) {
        this.levelSep = levelSep;
    }

    public double getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(double timeUnit) {
        this.timeUnit = timeUnit;
    }

    public double getLevelUnit() {
        return levelUnit;
    }

    public void setLevelUnit(double levelUnit) {
        this.levelUnit = levelUnit;
    }

    public boolean isMirrorX() {
        return mirrorX;
    }

    public void setMirrorX(boolean mirrorX) {
        this.mirrorX = mirrorX;
    }

    public boolean isMirrorY() {
        return mirrorY;
    }

    public void setMirrorY(boolean mirrorY) {
        this.mirrorY = mirrorY;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public void save(Path location) {
        try(BufferedWriter out = Files.newBufferedWriter(location.resolve(CONFIG_FILE_NAME))) {
            out.write(new GsonBuilder().setPrettyPrinting().create().toJson(this));

        } catch(IOException e) {
            Main.logger.error("Couldn't save settings.json");
        }
    }

    public static Config load(Path location) {
        return load(location, true);
    }

    public static Config load(Path location, boolean fallback) {
        try(BufferedReader in = Files.newBufferedReader(location.resolve(CONFIG_FILE_NAME))) {
            Gson gson = new Gson();
            return gson.fromJson(in, Config.class);

        } catch (IOException e) {

           if(fallback && settings.getLastHistory() != null) {
               return Config.load(settings.getLastHistory(), false);
           }

           return new Config();
        }
    }
}

package de.uzk.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.uzk.Main;
import de.uzk.image.ImageFileNameExtension;
import jdk.jshell.spi.ExecutionControl;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Settings {
    private static final Path SETTINGS_FILE_NAME = Path.of("settings.json");

    public static final int MIN_FONT_SIZE = 10;
    public static final int DEFAULT_FONT_SIZE = 16;
    public static final int MAX_FONT_SIZE = 22;

    private Language language;
    private Theme theme;
    private int fontSize;
    private boolean confirmExit;
    @JsonAdapter(HistoryAdapter.class)
    private List<Path> history;
    private ImageFileNameExtension fileNameExt;



    public Settings() {
        this.setTheme(Theme.LIGHT_MODE);
        this.setLanguage(Language.getSystemDefault());
        this.setFontSize(16);
        this.setConfirmExit(true);
        this.fileNameExt = ImageFileNameExtension.getDefault();
        this.history = new ArrayList<>();
    }


    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        if (language == null || this.language == language) return;

        this.language = language;
        Locale.setDefault(language.getLocale());
        JComponent.setDefaultLocale(language.getLocale());
        LanguageHandler.load(language);
    }

    public Theme getTheme() {
        return theme;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public int getFontSize() {
        return fontSize;
    }

    public boolean setFontSize(int fontSize) {
        if(fontSize < MIN_FONT_SIZE ||  fontSize > MAX_FONT_SIZE) {
            if(this.fontSize < MIN_FONT_SIZE) {
                this.fontSize = DEFAULT_FONT_SIZE;
            }
            return  false;
        }
        this.fontSize = fontSize;
        return true;
    }

    public boolean isConfirmExit() {
        return confirmExit;
    }

    public void setConfirmExit(boolean confirmExit) {
        this.confirmExit = confirmExit;
    }

    public Path getLastHistory() {
       if(this.history.isEmpty()) {
           return null;
       }
        return this.history.get(this.history.size() - 1);
    }

    public void pushHistory(Path opened) {
        if(!opened.equals(this.getLastHistory())) {
            history.add(opened);
        }
    }



    public void save() {
        try  (BufferedWriter bw = Files.newBufferedWriter(SETTINGS_FILE_NAME)) {
            bw.write(new GsonBuilder().setPrettyPrinting().create().toJson(this));
        } catch (IOException e) {
            Main.logger.error("Couldn't save settings.json");
        }
    }

    public static Settings load()  {
        try (BufferedReader br = Files.newBufferedReader(SETTINGS_FILE_NAME)) {
            Gson gson = new Gson();
            Settings result = gson.fromJson(br, Settings.class);
            if(result == null) {
                return new Settings();
            } else {
                return result;
            }
        } catch(Exception e) {
            return new Settings();
        }
    }

    public void toggleTheme() {
        if(this.theme == Theme.LIGHT_MODE) {
            this.setTheme(Theme.DARK_MODE);
        }  else {
            this.setTheme(Theme.LIGHT_MODE);
        }
    }

    public ImageFileNameExtension getFileNameExt() {
        return fileNameExt;
    }

    public void setFileNameExt(ImageFileNameExtension fileNameExt) {
        this.fileNameExt = fileNameExt;
    }

    public void setFileNameExt(String extension) {
        ImageFileNameExtension temp = ImageFileNameExtension.fromExtension(extension);
        this.fileNameExt = temp != null ? temp : ImageFileNameExtension.getDefault();

    }

    private static class HistoryAdapter extends TypeAdapter<List<Path>> {

        @Override
        public void write(JsonWriter jsonWriter, List<Path> paths) throws IOException {
            jsonWriter.beginArray();
            for(Path path : paths) {
                jsonWriter.value(path.toAbsolutePath().normalize().toString());
            }
            jsonWriter.endArray();
        }

        @Override
        public List<Path> read(JsonReader jsonReader) throws IOException {
            List<Path> result = new ArrayList<>();
             jsonReader.beginArray();
             while(jsonReader.hasNext()) {
                 result.add(Path.of(jsonReader.nextString()));
             }
             jsonReader.endArray();
            return result;
        }
    }
}

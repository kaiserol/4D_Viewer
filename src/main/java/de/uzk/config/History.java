package de.uzk.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static de.uzk.utils.PathManager.*;

public class History {
    // Historie
    private final LinkedList<Path> history;

    private History(List<Path> history) {
        this.history = new LinkedList<>();
        if (history != null) {
            // Pfade "manuell" mit Überprüfung hinzufügen
            for (Path path : history) add(path);
        }
    }

    public boolean isEmpty() {
        return history.isEmpty();
    }

    public void add(Path directory) {
        if (directory == null) return;
        if (directory.toString().trim().isEmpty()) return;

        // Normalisiere den Pfad für konsistente Vergleiche
        Path normalized = directory.normalize();

        // Wenn der Pfad existiert und kein Verzeichnis ist, ablehnen
        if (Files.exists(normalized) && !Files.isDirectory(normalized)) return;

        // Duplikate vermeiden
        if (this.history.contains(normalized)) return;

        // Am Ende hinzufügen
        this.history.add(normalized);
    }

    public Path getLast() {
        if (isEmpty()) return null;
        return this.history.getLast();
    }

    public void save() {
        Path file = resolveConfigPath(HISTORY_FILE_NAME);
        List<String> lines = this.history.stream().map(Path::toString).toList();
        saveFile(file, lines);
    }

    public static History load() {
        Path file = resolveConfigPath(HISTORY_FILE_NAME);

        Object object = loadFile(file, History.class);
        if (object instanceof List<?> list) {
            List<Path> lines = list.stream().map(line -> Path.of(line == null ? "" : String.valueOf(line))).toList();
            return new History(lines);
        }
        return getDefault();
    }

    public static History getDefault() {
        return new History(null);
    }
}

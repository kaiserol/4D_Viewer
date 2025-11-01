package de.uzk.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static de.uzk.utils.AppPath.*;

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
        Path filePath = getAppPath(Path.of(HISTORY_FILE_NAME));
        List<String> lines = this.history.stream().map(Path::toString).toList();
        saveFile(filePath, lines);
    }

    public static History load() {
        Path filePath = getAppPath(Path.of(HISTORY_FILE_NAME));

        List<String> lines = loadFile(filePath);
        if (lines == null) return new History(null);
        return new History(lines.stream().map(Path::of).toList());
    }
}

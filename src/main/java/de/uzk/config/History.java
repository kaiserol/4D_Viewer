package de.uzk.config;

import de.uzk.utils.AppPath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static de.uzk.Main.logger;

public class History {
    // Pfade
    private static final Path HISTORY_PATH = AppPath.VIEWER_HOME_DIRECTORY.resolve("history");
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
        if (directory.toAbsolutePath().toString().trim().isEmpty()) return;

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
        try {
            List<String> lines = this.history.stream().map(Path::toString).toList();
            Files.write(HISTORY_PATH, lines);
        } catch (IOException e) {
            logger.error("Failed to save history: " + e.getMessage());
        }
    }

    public static History load() {
        try {
            List<Path> history = Files.readAllLines(HISTORY_PATH).stream()
                    .map(Path::of)
                    .toList();
            return new History(history);
        } catch (IOException e) {
            return new History(null);
        }
    }
}

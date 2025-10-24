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
        this.history = (history != null) ? new LinkedList<>(history) : new LinkedList<>();
    }

    public boolean isEmpty() {
        return history.isEmpty();
    }

    public void add(Path directory) {
        if (directory == null) return;
        if (!Files.isDirectory(directory) || this.history.contains(directory)) return;

        // Am Ende hinzufügen
        this.history.add(directory);
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
            List<Path> history = Files.readAllLines(HISTORY_PATH).stream().map(Path::of).filter(Files::isDirectory).toList();
            return new History(history);
        } catch (IOException e) {
            return new History(null);
        }
    }
}

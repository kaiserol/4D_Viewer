package de.uzk.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import static de.uzk.Main.operationSystem;

public class History {
    // Pfade
    private static final Path HISTORY_PATH = operationSystem.getDirectory(false).resolve("history");

    // Historie
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

    // TODO: Verbessere save / load (muss ins home Verzeichni...)
    public void save() {
       // In eine normale Datei schreiben
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

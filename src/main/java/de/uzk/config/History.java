package de.uzk.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static de.uzk.Main.operationSystem;

// TODO: Validate
public class History {
    private static final Path HISTORY_PATH = operationSystem.getDirectoryPath(true).resolve("history");
    private final List<Path> history;

    private History(List<Path> history) {
        this.history = new LinkedList<>(history);
    }

    public static History load() {
        try {
            List<Path> history = Files.readAllLines(HISTORY_PATH).stream().map(Path::of).toList();
            return new History(history);
        } catch (IOException e) {
            return new History(new ArrayList<>());
        }
    }

    public boolean isEmpty() {
        return history.isEmpty();
    }

    public Path last() {
        if (!this.isEmpty()) {
            return this.history.get(this.history.size() - 1);
        } else {
            return null;
        }
    }

    public void open(Path path) {
        this.history.remove(path); // Keine doppelten Einträge
        this.history.add(path); // Ganz an den anfang
    }
}

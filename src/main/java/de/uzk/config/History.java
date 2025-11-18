package de.uzk.config;

import de.uzk.io.PathManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class History {
    // Historie
    private final LinkedList<Path> history;

    private History(List<Path> history) {
        this.history = new LinkedList<>();
        if (history != null) {
            // Wenn Pfade existieren, müssen sie vom Typ Verzeichnis sein, um hinzugefügt zu werden
            for (Path path : history) this.add(path);
        }
    }

    public boolean isEmpty() {
        return history.isEmpty();
    }

    public void add(Path imagesDirectory) {
        if (imagesDirectory == null) return;
        if (imagesDirectory.toString().isBlank()) return;

        // Normalisiere den Pfad für konsistente Vergleiche
        Path normalized = imagesDirectory.normalize().toAbsolutePath();

        // Wenn der Pfad existiert und kein Verzeichnis ist, ablehnen
        if (Files.exists(normalized) && !Files.isDirectory(normalized)) return;

        // Falls bereits vorhanden, dann löschen
        this.history.remove(normalized);

        // Am Ende hinzufügen
        this.history.add(normalized);
    }

    public Path getLastIfExists() {
        if (isEmpty()) return null;
        Path path = this.history.getLast();
        if (Files.isDirectory(path)) return path;
        return null;
    }

    public List<Path> getAll() {
        return this.history;
    }

    public void save() {
        Path filePath = PathManager.resolveConfigPath(PathManager.HISTORY_FILE_NAME);
        List<String> lines = this.history.stream()
            .map(p -> p.toAbsolutePath().toString())
            .toList();
        PathManager.save(filePath, lines);
    }

    public static History load() {
        Path filePath = PathManager.resolveConfigPath(PathManager.HISTORY_FILE_NAME);

        Object object = PathManager.load(filePath, History.class);
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

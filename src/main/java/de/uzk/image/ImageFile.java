package de.uzk.image;


import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class ImageFile implements Comparable<ImageFile> {
    private final Path filePath;
    private final String name;
    private final int time;
    private final int level;

    public ImageFile(Path filePath, int time, int level) {
        if (filePath == null) throw new NullPointerException("Path is null.");
        if (time < 0) throw new IllegalArgumentException("Time must be greater than 0.");
        if (level < 0) throw new IllegalArgumentException("Level must be greater than 0.");
        this.filePath = filePath;
        name = filePath.getFileName().toString();
        this.time = time;
        this.level = level;
    }

    public Path getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return name;
    }

    public int getTime() {
        return time;
    }

    public int getLevel() {
        return level;
    }

    public boolean exists() {
        return Files.exists(filePath);
    }

    @Override
    public int compareTo(@NotNull ImageFile imageFile) {
        if (time != imageFile.time) return Integer.compare(time, imageFile.time);
        if (level != imageFile.level) return Integer.compare(level, imageFile.level);
        return filePath.getFileName().compareTo(imageFile.filePath.getFileName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageFile imageFile = (ImageFile) o;
        return time == imageFile.time && level == imageFile.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, level);
    }

    @Override
    public String toString() {
        return name;
    }
}

package de.uzk.image;


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
        this.name = filePath.getFileName().toString();
        this.time = time;
        this.level = level;
    }

    public Path getFilePath() {
        return this.filePath;
    }

    public String getFileName() {
        return this.name;
    }

    public int getTime() {
        return this.time;
    }

    public int getLevel() {
        return this.level;
    }

    public boolean exists() {
        return Files.exists(this.filePath);
    }

    @Override
    public int compareTo(ImageFile imageFile) {
        if (this.time != imageFile.time) return Integer.compare(this.time, imageFile.time);
        if (this.level != imageFile.level) return Integer.compare(this.level, imageFile.level);
        return filePath.getFileName().compareTo(imageFile.filePath.getFileName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImageFile imageFile = (ImageFile) o;
        return this.time == imageFile.time && this.level == imageFile.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.time, this.level);
    }

    @Override
    public String toString() {
        return this.name;
    }
}

package de.uzk.image;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class ImageFile implements Comparable<ImageFile> {
    private final Path path;
    private final String name;
    private final int time;
    private final int level;

    public ImageFile(Path path, int time, int level) {
        if (path == null) throw new NullPointerException("Path is null.");
        this.path = path;
        this.name = path.getFileName().toString();

        if (time < 0) throw new IllegalArgumentException("Time must be greater than 0.");
        if (level < 0) throw new IllegalArgumentException("Level must be greater than 0.");
        this.time = time;
        this.level = level;
    }

    public Path getPath() {
        return this.path;
    }

    public String getName() {
        return this.name;
    }

    public boolean exists() {
        return Files.exists(this.path);
    }

    public int getTime() {
        return this.time;
    }

    public int getLevel() {
        return this.level;
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
    public int compareTo(ImageFile imageFile) {
        if (this.time != imageFile.time) return Integer.compare(this.time, imageFile.time);
        if (this.level != imageFile.level) return Integer.compare(this.level, imageFile.level);
        return name.compareToIgnoreCase(imageFile.name);
    }

    @Override
    public String toString() {
        return this.name;
    }
}

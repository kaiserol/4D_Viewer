package de.uzk.image;


import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class ImageFile implements Comparable<ImageFile> {
    private final Path file;
    private final String name;
    private final int time;
    private final int level;

    public ImageFile(Path file, int time, int level) {
        if (file == null) throw new NullPointerException("File is null.");
        this.file = file;
        this.name = file.getFileName().toString();

        if (time < 0) throw new IllegalArgumentException("Time must be greater than 0.");
        if (level < 0) throw new IllegalArgumentException("Level must be greater than 0.");
        this.time = time;
        this.level = level;
    }

    public Path getFile() {
        return this.file;
    }

    public String getName() {
        return this.name;
    }

    public boolean exists() {
        return Files.exists(this.file);
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

package de.uzk.handler;

import de.uzk.utils.StringUtils;

import java.io.File;
import java.util.Objects;

public class ImageFile implements Comparable<ImageFile> {
    private final ImageDetails imageDetails;
    private final int time;
    private final int level;
    private final File file;
    private final String fileName;
    private final String fileExtension;

    public ImageFile(ImageDetails imageDetails, File file, String fileName) {
        this.imageDetails = imageDetails;

        this.file = file;
        this.fileName = fileName;
        this.fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1);
        this.time = parseTime();
        this.level = parseLevel();
    }

    private static String getNamePattern(ImageDetails imageDetails) {
        return imageDetails.getSepTime() + "[0-9]+" + imageDetails.getSepLevel() + "[0-9]+";
    }

    private static String getFileExtensions(ImageDetails imageDetails) {
        return "\\.(?i)\\b" + StringUtils.formatArray(imageDetails.getImageType().getFileExtensions(),
                "|", '(', ')') + "\\b";
    }

    public static String getImageNamePattern(ImageDetails imageDetails) {
        return getNamePattern(imageDetails) + getFileExtensions(imageDetails);
    }

    private int parseTime() {
        int index = fileName.indexOf(this.imageDetails.getSepTime());
        int index2 = fileName.lastIndexOf(this.imageDetails.getSepLevel());

        String timeString = fileName.substring(index + 1, index2);
        return Integer.parseInt(timeString);
    }

    private int parseLevel() {
        int index = fileName.lastIndexOf(this.imageDetails.getSepLevel());
        int index2 = fileName.lastIndexOf(fileExtension) - 1;
        String levelString = fileName.substring(index + 1, index2);
        return Integer.parseInt(levelString);
    }

    public int getTime() {
        return time;
    }

    public int getLevel() {
        return level;
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public String toString() {
        return fileName;
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
    public int compareTo(ImageFile imageFile) {
        if (time != imageFile.time) return Integer.compare(time, imageFile.time);
        return Integer.compare(level, imageFile.level);
    }
}

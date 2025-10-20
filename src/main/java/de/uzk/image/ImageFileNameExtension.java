package de.uzk.image;

import de.uzk.utils.StringUtils;

import java.util.Arrays;

import static de.uzk.config.LanguageHandler.getWord;

public enum ImageFileNameExtension {
    GIF("GIF", "gif"),
    JPEG("JPEG", "jpg", "jpeg"),
    PNG("PNG", "png"),
    TIFF("TIFF", "tif", "tiff");

    // Typ der Datei
    private final String type;

    // Zugehörige Dateiendungen
    private final String[] extensions;

    ImageFileNameExtension(String type, String... extensions) {
        this.type = type;
        this.extensions = extensions;
    }

    public String getType() {
        return this.type;
    }

    public String getDescription() {
        return this.type + "-" + getWord("file.images");
    }

    public String getFullDescription() {
        String[] formattedExtensions = Arrays.stream(this.extensions)
                .map(ext -> "*." + ext)
                .toArray(String[]::new);

        return getDescription() + " " +
                StringUtils.formatArray(formattedExtensions, ", ", '(', ')');
    }

    public String[] getExtensions() {
        return Arrays.copyOf(this.extensions, this.extensions.length);
    }

    public boolean matches(String extension) {
        if (extension == null) return false;
        return Arrays.stream(this.extensions)
                .anyMatch(ext -> ext.equalsIgnoreCase(extension));
    }

    public static ImageFileNameExtension fromExtension(String extension) {
        if (extension != null) {
            for (ImageFileNameExtension ext : values()) {
                if (ext.matches(extension)) return ext;
            }
        }
        return null;
    }

    public static ImageFileNameExtension getDefault() {
        return JPEG;
    }

    public static ImageFileNameExtension[] sortedValues() {
        ImageFileNameExtension[] values = values();
        Arrays.sort(values, (ext1, ext2) -> ext1.toString().compareToIgnoreCase(ext2.toString()));
        return values;
    }

    @Override
    public String toString() {
        return getDescription();
    }
}

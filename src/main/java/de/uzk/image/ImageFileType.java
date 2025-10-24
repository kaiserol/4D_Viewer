package de.uzk.image;

import de.uzk.utils.StringUtils;

import java.util.Arrays;

import static de.uzk.config.LanguageHandler.getWord;

public enum ImageFileType {
    GIF("GIF", "gif"),
    JPEG("JPEG", "jpg", "jpeg"),
    PNG("PNG", "png"),
    TIFF("TIFF", "tif", "tiff");

    // Typ der Datei
    private final String type;

    // Zugehörige Dateiendungen
    private final String[] extensions;

    ImageFileType(String type, String... extensions) {
        this.type = type;
        this.extensions = extensions;
    }

    public String getType() {
        return this.type;
    }

    public String[] getExtensions() {
        return Arrays.copyOf(this.extensions, this.extensions.length);
    }

    public String getDescription() {
        return this.type + "-" + getWord("file.images");
    }

    public String getFullDescription() {
        String[] formattedExtensions = Arrays.stream(this.extensions).
                map(ext -> "*." + ext).
                toArray(String[]::new);

        return getDescription() + " " + StringUtils.formatArray(formattedExtensions, ", ", '(', ')');
    }

    public static ImageFileType fromExtension(String extension) {
        if (extension != null) {
            for (ImageFileType type : ImageFileType.values()) {
                boolean sameName = type.name().equalsIgnoreCase(extension);
                boolean hasSameExtension = Arrays.stream(type.extensions).anyMatch(ext -> ext.equalsIgnoreCase(extension));
                if (sameName || hasSameExtension) return type;
            }
        }
        return getDefault();
    }

    public static ImageFileType getDefault() {
        return JPEG;
    }

    public static ImageFileType[] sortedValues() {
        ImageFileType[] values = ImageFileType.values();
        Arrays.sort(values, (type1, type2) -> type1.toString().compareToIgnoreCase(type2.toString()));
        return values;
    }

    @Override
    public String toString() {
        return getDescription();
    }
}

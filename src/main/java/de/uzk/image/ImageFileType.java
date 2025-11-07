package de.uzk.image;

import com.fasterxml.jackson.annotation.*;
import de.uzk.utils.StringUtils;

import java.util.Arrays;

import static de.uzk.config.LanguageHandler.getWord;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ImageFileType {
    GIF("gif", "gif"),
    JPEG("jpeg", "jpg", "jpeg"),
    PNG("png", "png"),
    TIFF("tiff", "tif", "tiff");

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

    @JsonIgnore
    public String getDescription() {
        return this.type + "-" + getWord("file.images");
    }

    @JsonIgnore
    public String getFullDescription() {
        String[] formattedExtensions = Arrays.stream(this.extensions).
                map(ext -> "*." + ext).
                toArray(String[]::new);

        return getDescription() + " " + StringUtils.formatArray(formattedExtensions, ", ", '(', ')');
    }

    public static ImageFileType getDefault() {
        return JPEG;
    }

    @JsonCreator
    public static ImageFileType fromType(@JsonProperty("type") String newType) {
        if (newType != null) {
            for (ImageFileType type : ImageFileType.values()) {
                boolean sameName = type.name().equalsIgnoreCase(newType);
                boolean sameType = type.getType().equalsIgnoreCase(newType);
                if (sameName || sameType) return type;
            }
        }
        // Fallback
        return getDefault();
    }

    public static ImageFileType fromExtension(String extension) {
        if (extension != null) {
            for (ImageFileType type : ImageFileType.values()) {
                boolean hasSameExtension = Arrays.stream(type.extensions).anyMatch(ext -> ext.equalsIgnoreCase(extension));
                if (hasSameExtension) return type;
            }
        }
        // Fallback
        return getDefault();
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

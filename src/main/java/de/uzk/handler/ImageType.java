package de.uzk.handler;

import de.uzk.utils.StringUtils;

import static de.uzk.handler.LanguageHandler.getWord;

public class ImageType {
    private static final String FILES = getWord("file.files");
    private String type;
    private String[] fileExtensions;

    public ImageType(String type, String... fileExtensions) {
        setType(type);
        setFileExtensions(fileExtensions);
    }

    private void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getTypeDescription() {
        return type + " " + FILES;
    }

    private void setFileExtensions(String[] types) {
        this.fileExtensions = types;
    }

    public String[] getFileExtensions() {
        return fileExtensions;
    }

    public String getFileExtensionsDescription() {
        return StringUtils.formatArray(this.fileExtensions, ", ", '(', ')');
    }

    @Override
    public String toString() {
        return getTypeDescription() + " " + getFileExtensionsDescription();
    }
}

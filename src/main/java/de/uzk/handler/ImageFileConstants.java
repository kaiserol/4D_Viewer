package de.uzk.handler;

import de.uzk.utils.StringUtils;

public final class ImageFileConstants {
    public static final ImageType[] IMAGE_TYPES = new ImageType[]{
            new ImageType("JPEG", "jpg", "jpeg"),
            new ImageType("TIFF", "tif", "tiff"),
            new ImageType("PNG", "png")};
    public static final ImageType DEFAULT_IMAGE_TYPE = IMAGE_TYPES[0];
    public static final String DEFAULT_SEP_TIME = "X";
    public static final String DEFAULT_SEP_LEVEL = "L";
    public static final boolean DEFAULT_MIRROR_IMAGE = false;
    public static final int DEFAULT_IMAGE_ROTATION = 0;
    public static final double DEFAULT_TIME_UNIT = 30.0;
    public static final double DEFAULT_LEVEL_UNIT = 1;
    public static final int DEFAULT_PIN_TIME = -1;

    private ImageFileConstants() {
    }

    public static ImageType getImageType(String type) {
        if (type != null && type.length() > 1) {
            for (ImageType imageType : IMAGE_TYPES) {
                String cleanType = StringUtils.splitTextInWords(imageType.getTypeDescription())[0];
                if (imageType.getTypeDescription().equalsIgnoreCase(type) ||
                        cleanType.equalsIgnoreCase(type)) {
                    return imageType;
                }
            }
        }
        return null;
    }
}

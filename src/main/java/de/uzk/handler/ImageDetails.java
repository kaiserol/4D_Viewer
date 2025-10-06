package de.uzk.handler;

public class ImageDetails {
    private final String sepTime;
    private final String sepLevel;
    private ImageType imageType;
    private boolean mirrorX;
    private boolean mirrorY;
    private int rotation;

    public ImageDetails(String sepTime, String sepLevel, ImageType imageType, boolean mirrorX, boolean mirrorY, int rotation) {
        this.sepTime = sepTime;
        this.sepLevel = sepLevel;
        this.imageType = imageType;
        this.mirrorX = mirrorX;
        this.mirrorY = mirrorY;
        this.rotation = rotation;
    }

    public String getSepTime()  {
        return sepTime;
    }

    public String getSepLevel() {
        return sepLevel;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    public boolean isMirrorX() {
        return mirrorX;
    }

    public void setMirrorX(boolean mirrorX) {
        this.mirrorX = mirrorX;
    }

    public boolean isMirrorY() {
        return mirrorY;
    }

    public void setMirrorY(boolean mirrorY) {
        this.mirrorY = mirrorY;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }
}

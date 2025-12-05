package de.uzk.image;

import de.uzk.config.Config;
import de.uzk.io.ImageLoader;
import de.uzk.markers.Marker;
import de.uzk.utils.GraphicsUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.nio.file.Path;
import java.util.function.Consumer;

import static de.uzk.Main.logger;
import static de.uzk.Main.workspace;

public class ImageEditor {
    private BufferedImage currentImage;
    private BufferedImage cache;
    private AffineTransform imageTransform = new AffineTransform();
    private AffineTransform markerTransform = new AffineTransform();
    private Consumer<BufferedImage> newImageConsumer;
    private final Rectangle zoomRect = new Rectangle(0, 0);

    // region Getter
    public BufferedImage getCurrentImage() {
        return currentImage;
    }

    public AffineTransform getMarkerTransform() {
        return markerTransform;
    }

    //endregion

    //region public Methoden für Bildupdates
    public void onNewImageAvailable(Consumer<BufferedImage> listener) {
        this.newImageConsumer = listener;
    }

    public void updateImage(boolean needsFullRedraw) {
        needsFullRedraw |= cache == null;
        this.currentImage = null;
        if (!workspace.isLoaded()) return;
        BufferedImage newImage = null;
        if (needsFullRedraw){
            Path imagePath = workspace.getCurrentImageFile().getFilePath();
            newImage = ImageLoader.openImage(imagePath, false);
            if (newImage != null) {
                recalculateTransform(newImage);
                calculateRescaleOp().filter(newImage, newImage);

            } else {
                logger.error("Failed to load image: " + imagePath);
                return;
            }
        }
        currentImage = redraw(newImage);
        newImageAvailable();
    }

    //endregion

    //region Private Helfermethoden – für Bild-Updates

    // Einige Operationen (Zoom, Marker) nur durch Erstellen eines neuen Bildes umsetzbar
    private BufferedImage redraw(BufferedImage newBase) {

        if(newBase != null) {
            updateBase(newBase);
        }

        BufferedImage result = new BufferedImage(cache.getWidth(), cache.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = GraphicsUtils.createHighQualityGraphics2D(result.getGraphics());
        g2d.drawImage(cache, 0, 0, null);

        java.util.List<Marker> markers = workspace.getMarkers().getMarkersForImage(workspace.getTime());
        g2d.transform(markerTransform);

        for (Marker marker : markers) {
            marker.draw(g2d);
        }
        g2d.dispose();
        return result;

    }

    private void updateBase(BufferedImage image) {

        cache = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = GraphicsUtils.createHighQualityGraphics2D(cache.getGraphics());

        g2d.transform(imageTransform);
        g2d.drawImage(image, zoomRect.x, zoomRect.y, zoomRect.width, zoomRect.height, null);
        g2d.dispose();
    }

    private RescaleOp calculateRescaleOp() {
        float offset = 128 * ((workspace.getConfig().getBrightness() - 100) / 100f);
        float scale = workspace.getConfig().getContrast() / 100f;

        return new RescaleOp(scale, offset, null);
    }

    private double recalculateZoom(BufferedImage source) {
        double scale = (workspace.getConfig().getZoom()) / 100.;
        zoomRect.width = (int) (source.getWidth() * scale);
        zoomRect.height = (int) (source.getHeight() * scale);
        zoomRect.x = (source.getWidth() - zoomRect.width) / 2;
        zoomRect.y = (source.getHeight() - zoomRect.height) / 2;
        return scale;
    }

    private void recalculateTransform(BufferedImage image) {
        Config config = workspace.getConfig();

        int width = image.getWidth();
        int height = image.getHeight();
        double radians = Math.toRadians(config.getRotation());
        boolean mirrorX = config.isMirrorX();
        boolean mirrorY = config.isMirrorY();

        AffineTransform at = new AffineTransform();


        // Mirror
        at.scale(mirrorX ? -1 : 1, mirrorY ? -1 : 1);
        at.translate(mirrorX ? -width : 0, mirrorY ? -height : 0);

        // Rotate
        at.rotate(radians, width / 2.0, height / 2.0);

        imageTransform = at;
        markerTransform = new AffineTransform(at);


        double scale = recalculateZoom(image);
        markerTransform.translate(zoomRect.x, zoomRect.y);
        markerTransform.scale(scale, scale);


    }


    private void newImageAvailable() {
        if (this.newImageConsumer != null) this.newImageConsumer.accept(this.currentImage);
    }

    //endregion
}

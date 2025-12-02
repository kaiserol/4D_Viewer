package de.uzk.image;

import de.uzk.config.Config;
import de.uzk.io.ImageLoader;
import de.uzk.markers.Marker;
import de.uzk.utils.GraphicsUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import static de.uzk.Main.workspace;

public class ImageEditor {
    private BufferedImage currentImage;
    private AffineTransform currentTransform = new AffineTransform();
    private Consumer<BufferedImage> newImageConsumer;

    // region Getter
    public BufferedImage getCurrentImage() {
        return currentImage;
    }

    public AffineTransform getCurrentTransform() {
        return currentTransform;
    }

    //endregion

    //region public Methoden für Bildupdates
    public void onNewImageAvailable(Consumer<BufferedImage> listener) {
        this.newImageConsumer = listener;
    }

    public void updateImage() {
         this.currentImage = null;
        if (workspace.isLoaded()) {
            Path imagePath = workspace.getCurrentImageFile().getFilePath();
            currentImage = ImageLoader.openImage(imagePath, false);
            List<Marker> markers = workspace.getMarkers().getMarkersForImage(workspace.getTime());
            if (currentImage != null) {
                this.recalculateTransform();
                AffineTransformOp at = new AffineTransformOp(currentTransform, AffineTransformOp.TYPE_BILINEAR);
                currentImage = at.filter(currentImage, null);
                calculateRescaleOp().filter(currentImage, currentImage);
                currentImage = redraw(currentImage, markers);
                newImageAvailable();
            }
        }
    }

    //endregion

    //region Private Helfermethoden – für Bild-Updates

    // Einige Operationen (Zoom, Marker) nur durch Erstellen eines neuen Bildes umsetzbar
    private BufferedImage redraw(BufferedImage to, List<Marker> markers) {
        double zoomFactor = workspace.getConfig().getZoom() / 100.;
        Rectangle scaled = GraphicsUtils.scaleInsetsAndSize(to.getWidth(), to.getHeight(), zoomFactor);

        BufferedImage result = new BufferedImage(to.getWidth(), to.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = GraphicsUtils.createHighQualityGraphics2D(result.getGraphics());
        g2d.drawImage(to, scaled.x, scaled.y, scaled.width, scaled.height, null);
        g2d.transform(currentTransform);
        for (Marker marker : markers) {
            marker.draw(g2d, new Rectangle(scaled.x, scaled.y, scaled.width, scaled.height));
        }
        g2d.dispose();
        return result;

    }

    private RescaleOp calculateRescaleOp() {
        float offset = 128 * ((workspace.getConfig().getBrightness() - 100) / 100f);
        float scale = workspace.getConfig().getContrast() / 100f;

        return new RescaleOp(scale, offset, null);
    }

    private void recalculateTransform() {
        if(this.currentImage == null) return;
        Config config = workspace.getConfig();

        int width = currentImage.getWidth();
        int height = currentImage.getHeight();
        double radians = Math.toRadians(config.getRotation());
        boolean mirrorX = config.isMirrorX();
        boolean mirrorY = config.isMirrorY();
        double zoom = (config.getZoom()) / 100.;


        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));
        int newWidth = (int) Math.floor(width * cos + height * sin);
        int newHeight = (int) Math.floor(height * cos + width * sin);

        AffineTransform at = new AffineTransform();


        // Mirror
        at.scale(mirrorX ? -1 : 1, mirrorY ? -1 : 1);
        at.translate(mirrorX ? -width : 0, mirrorY ? -height : 0);

        // Rotate
        at.rotate(radians, width / 2.0, height / 2.0);
        at.translate((newWidth - width) / 2.0, (newHeight - height) / 2.0);

        // Zoom part I
        // visuell geschieht der "zoom" erst in redraw()
        at.scale(zoom, zoom);

        this.currentTransform = at;
    }


    private void newImageAvailable() {
        if(this.newImageConsumer != null) this.newImageConsumer.accept(this.currentImage);
    }

    //endregion
}

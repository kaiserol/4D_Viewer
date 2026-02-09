package de.uzk.image;

import de.uzk.config.Config;
import de.uzk.io.ImageLoader;
import de.uzk.markers.Marker;
import de.uzk.markers.interactions.MarkerInteractionHandler;
import de.uzk.utils.GraphicsUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.nio.file.Path;
import java.util.function.Consumer;

import static de.uzk.Main.logger;
import static de.uzk.Main.workspace;

/**
 * Zuständig für Bildbearbeitung und die Erstellung sowie das Caching von {@link BufferedImage}s.
 * @see de.uzk.gui.SensitiveImagePanel
 * @see MarkerInteractionHandler
 * */
public class ImageEditor {
    private BufferedImage currentImage;
    private BufferedImage cache;
    // Der Marker, der vom Nutzer aktuell skaliert/rotiert wird, falls vorhanden.
    private Marker focusedMarker;
    // Transformationen, die auf das Bild angewendet werden.
    private AffineTransform imageTransform = new AffineTransform();
    // Transformationen, die auf Marker angewendet werden.
    private AffineTransform markerTransform = new AffineTransform();
    private Consumer<BufferedImage> newImageConsumer;
    //Der "tatsächliche" Zeichenbereich für das Bild nach Zoom und Skalierung auf die Größe des ImagePanels
    private final Rectangle drawArea = new Rectangle(0, 0);

    // region Getter
    public BufferedImage getCurrentImage() {
        return currentImage;
    }

    /**
     * @return Die affine Transformation, die auf Marker angewendet wird.
     * Diese ist u.a. relevant für Koordinatenberechnungen, weswegen sie `public` ist.
     * */
    public AffineTransform getMarkerTransform() {
        return markerTransform;
    }

    //endregion

    //region public Methoden für Bildupdates
    /**
     * Setzt einen Eventhandler, der aufgerufen wird, sobald ein neues, fertig bearbeitetes Bild verfügbar ist.
     * Momentan wird nur ein Handler gespeichert, d.h. mehrere Aufrufe dieser Methode überschreiben vorherig
     * registrierte Handler.
     *
     * @param listener ein Consumer, der mit jedem neuen Bild aufgerufen werden soll.
     * */
    public void onNewImageAvailable(Consumer<BufferedImage> listener) {
        this.newImageConsumer = listener;
    }

    /**
     * Legt fest, welcher Marker als "fokussiert" dargestellt werden soll, ergo welcher Marker gerade
     * vom Nutzer rotiert/skaliert wird. Diese Methode bewirkt nur einen <b>rein visuellen</b> Effekt,
     * die Markerbearbeitung findet in {@link MarkerInteractionHandler} statt.
     *
     * @param marker der Marker, der fokussiert werden soll, oder <code>null</code>.
     * */
    public void setFocusedMarker(Marker marker) {
        this.focusedMarker = marker;
    }

    /**
     * Lädt und bearbeitet das richtige Bild und ruft anschließend den über {@link #onNewImageAvailable} festgelegten
     * Callback auf.
     * @param needsFullRedraw Wenn <code>false</code>, wird nicht das gesamte Bild neu gezeichnet, sondern nur Marker.
     *                        Hilfreich, um unnötige Berechnungen desselben Bildes zu vermeiden.
     * */
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

    public void clear() {
        currentImage = null;
        cache = null;
    }

    /**
     * Überprüft, ob der fokussierte Marker noch existiert.
     * @return Existiert der bisher fokussierte Marker noch?
     * */
    public boolean focusedStillExists() {
        boolean focusedExists = workspace.getMarkers().getMarkersForImage(workspace.getTime()).contains(focusedMarker);
        if(!focusedExists) {
            focusedMarker = null;
        }

        return !focusedExists;
    }

    //endregion

    //region Private Helfermethoden – für Bild-Updates

    /**
     * Wendet alle transformationen an und zeichnet Marker auf das aktuelle Bild im Cache, nachdem dieser
     * eventuell durch ein anderes <code>BufferedImage</code> ersetzt wurde.
     *
     * @param newBase ein neues Bild, falls vorhanden; Wenn <code>null</code>, wird der Cache verwendet.
     *
     * @return Das bearbeitete Bild.
     * */
    private BufferedImage redraw(BufferedImage newBase) {

        if(newBase != null) {
            updateBase(newBase);
        }

        BufferedImage result = new BufferedImage(cache.getWidth(), cache.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = GraphicsUtils.createHighQualityGraphics2D(result.getGraphics());
        g2d.drawImage(cache, 0, 0, null);

        java.util.List<Marker> markers = workspace.getMarkers().getMarkersForImage(workspace.getTime());
        g2d.transform(markerTransform);

        boolean focusedMarkerStillExists = false;
        for (Marker marker : markers) {
            marker.draw(g2d);
            focusedMarkerStillExists |= marker == focusedMarker;
        }
        if(focusedMarker != null && focusedMarkerStillExists) {
            // Wenn mehrere Marker überlappen, sollten die Eckpunkte in der obersten Ebene liegen
            focusedMarker.drawDragPoints(g2d);
        } else {
            focusedMarker = null;
        }
        g2d.dispose();
        return result;

    }

    /**
     * Skaliert das gegebene Bild auf die Größe des Imagepanels und speichert es im Cache.
     * */
    private void updateBase(BufferedImage image) {
        cache = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = GraphicsUtils.createHighQualityGraphics2D(cache.getGraphics());

        g2d.transform(imageTransform);
        g2d.drawImage(image, drawArea.x, drawArea.y, drawArea.width, drawArea.height, null);
        g2d.dispose();
    }

    private RescaleOp calculateRescaleOp() {
        float offset = 128 * ((workspace.getConfig().getBrightness() - 100) / 100f);
        float scale = workspace.getConfig().getContrast() / 100f;

        return new RescaleOp(scale, offset, null);
    }

    /**
     * Berechnet den Zeichenbereich für das gegebene Bild neu
     * */
    private double recalculateZoom(BufferedImage source) {
        double scale = (workspace.getConfig().getZoom()) / 100.;
        drawArea.width = (int) (source.getWidth() * scale);
        drawArea.height = (int) (source.getHeight() * scale);
        drawArea.x = (source.getWidth() - drawArea.width) / 2;
        drawArea.y = (source.getHeight() - drawArea.height) / 2;
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
        markerTransform.translate(drawArea.x, drawArea.y);
        markerTransform.scale(scale, scale);


    }


    private void newImageAvailable() {
        if (this.newImageConsumer != null) this.newImageConsumer.accept(this.currentImage);
    }

    //endregion
}

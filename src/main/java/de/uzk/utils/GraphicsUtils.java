package de.uzk.utils;

import de.uzk.markers.Marker;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.List;

import static de.uzk.Main.workspace;

// TODO: Verbessere die Methoden
public final class GraphicsUtils {

    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private GraphicsUtils() {
        // Verhindert die Instanziierung dieser Klasse
    }

    public static BufferedImage getEditedImage(BufferedImage image, boolean transparentBackground, java.util.List<Marker> appliedMarkers) {
        int imageType = transparentBackground ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        float offset = 128 * ((workspace.getConfig().getBrightness() - 100) / 100f);
        float scale = workspace.getConfig().getContrast() / 100f;

        BufferedImage transformed = transformImage(image, imageType, workspace.getConfig().getRotation(), workspace.getConfig().isMirrorX(), workspace.getConfig().isMirrorY(), appliedMarkers);

        new RescaleOp(scale, offset, null).filter(transformed, transformed);

        return transformed;
    }

    public static BufferedImage transformImage(BufferedImage image, int imageType, int rotation, boolean mirrorX, boolean mirrorY, List<Marker> appliedMarkers) {
        if (appliedMarkers == null) appliedMarkers = new ArrayList<>();
        if (!mirrorX && !mirrorY && rotation % 360 == 0 && appliedMarkers.isEmpty()) return image;

        int width = image.getWidth();
        int height = image.getHeight();
        double radians = Math.toRadians(rotation);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));
        int newWidth = (int) Math.floor(width * cos + height * sin);
        int newHeight = (int) Math.floor(height * cos + width * sin);


        BufferedImage transformedImage = new BufferedImage(newWidth, newHeight, imageType);

        Graphics2D g2d = createHighQualityGraphics2D(transformedImage.getGraphics());
        AffineTransform at = new AffineTransform();


        // Mirror
        at.scale(mirrorX ? -1 : 1, mirrorY ? -1 : 1);
        at.translate(mirrorX ? -width : 0, mirrorY ? -height : 0);


        // Rotate
        at.translate((newWidth - width) / 2.0, (newHeight - height) / 2.0);
        at.rotate(radians, width / 2.0, height / 2.0);

        new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR).filter(image, image);

        g2d.drawRenderedImage(image, null);

        for (Marker marker : appliedMarkers) {
            marker.draw(g2d, new Rectangle(0, 0, newWidth, newHeight), 1.0);
        }

        g2d.dispose();

        return transformedImage;
    }

    /**
     * Wandelt das übergebene {@link BufferedImage} ins RGB-Format um.
     * Diese Methode tut nichts, wenn `image.getType() == BufferedImage.TYPE_INT_RGB`.
     *
     * @param image Bild, welches transformiert werden soll.
     * */
    public static BufferedImage transformToRGB(BufferedImage image) {
        if(image.getType() == BufferedImage.TYPE_INT_RGB) {
            // Hintergrund ist bereits intransparent
            return image;
        }
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = createHighQualityGraphics2D(newImage.getGraphics());
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return newImage;
    }

    public static void drawCenteredText(Graphics2D g2D, String text, Container container) {
        FontMetrics metrics = g2D.getFontMetrics(g2D.getFont());
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getHeight();

        int x = (container.getWidth() - textWidth) / 2;
        int y = (container.getHeight() + textHeight) / 2;
        g2D.drawString(text, x, y);
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    public static Graphics2D createHighQualityGraphics2D(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        return g2d;
    }

    public static double getImageScaleFactor(BufferedImage image, Container container) {
        int imgWidth = image.getWidth(null);
        int imgHeight = image.getHeight(null);

        double scaleWidth = (double) container.getWidth() / imgWidth;
        double scaleHeight = (double) container.getHeight() / imgHeight;
        return Math.min(scaleWidth, scaleHeight);
    }
}

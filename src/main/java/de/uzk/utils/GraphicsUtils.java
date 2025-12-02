package de.uzk.utils;

import java.awt.*;
import java.awt.image.BufferedImage;

// TODO: Verbessere die Methoden
public final class GraphicsUtils {

    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private GraphicsUtils() {
        // Verhindert die Instanziierung dieser Klasse
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

    public static Rectangle scaleInsetsAndSize(int width, int height, double scaleFactor) {
        int newWidth = (int) (width * scaleFactor);
        int newHeight = (int) (height * scaleFactor);
        int insetX = (width - newWidth) / 2;
        int insetY = (height - newHeight) / 2;
        return new Rectangle(insetX, insetY, newWidth, newHeight);
    }
}

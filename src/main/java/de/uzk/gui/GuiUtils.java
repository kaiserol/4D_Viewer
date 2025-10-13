package de.uzk.gui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.util.SystemInfo;
import de.uzk.config.ConfigHandler;
import de.uzk.image.ImageDetails;
import de.uzk.utils.NumberUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import static de.uzk.Main.config;
import static de.uzk.config.LanguageHandler.getWord;

public final class GuiUtils {
    public static final Color COLOR_BLUE = new Color(0, 122, 255);
    public static final Color COLOR_GREEN = new Color(8, 166, 52);
    public static final Color COLOR_YELLOW = new Color(252, 204, 78);
    public static final Color COLOR_RED = new Color(255, 86, 86);
    public static final String COMP_DISABLED = "DISABLED";
    public static final String SLIDER_DRAGGED = "DRAGGING";
    private static Color borderColor;
    private static Font font;

    private GuiUtils() {
    }

    private static FlatLaf getDarkMode() {
        return new FlatMacDarkLaf();
    }

    private static FlatLaf getLightMode() {
        return new FlatMacLightLaf();
    }

    public static void initFlatLaf() {
        FlatLaf.setup(config.getTheme().isLight() ? getLightMode() : getDarkMode());
        borderColor = config.getTheme().isLight() ? Color.LIGHT_GRAY : Color.DARK_GRAY;

        if (SystemInfo.isMacOS) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", getWord("app.name"));
            System.setProperty("apple.awt.application.appearance", "system");
        }

        if (SystemInfo.isLinux) {
            JFrame.setDefaultLookAndFeelDecorated(true);
            JDialog.setDefaultLookAndFeelDecorated(true);
        }

        boolean screenshotsMode = Boolean.parseBoolean(System.getProperty("flatlaf.demo.screenshotsMode"));
        if (screenshotsMode && !SystemInfo.isJava_9_orLater && System.getProperty("flatlaf.uiScale") == null) {
            System.setProperty("flatlaf.uiScale", "2x");
        }

        // focus
        UIManager.put("Component.focusWidth", 0);
        UIManager.put("Component.innerFocusWidth", 0);

        // rounded elements
        UIManager.put("Button.arc", 5);
        UIManager.put("Component.arc", 5);
        UIManager.put("TextComponent.arc", 5);
        UIManager.put("ProgressBar.arc", 5);

        // TabbedPane
        UIManager.put("TabbedPane.contentSeparatorHeight", 2);
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("TabbedPane.contentAreaColor", borderColor);
        UIManager.put("TabbedPane.selectedBackground", UIManager.getColor("TabbedPane.buttonHoverBackground").brighter());

        // ScrollBar
        UIManager.put("Component.arrowType", "chevron");
        UIManager.put("ScrollBar.showButtons", true);
        UIManager.put("ScrollBar.trackArc", 999);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 15);

        // track and thumb
        UIManager.put("ScrollBar.trackInsets", new Insets(0, 0, 0, 0));
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));

        // scrollbar color
        Color bgBrighter = UIManager.getColor("ScrollBar.background").brighter();
        UIManager.put("ScrollBar.track", bgBrighter);
        UIManager.put("ScrollBar.hoverTrackColor", bgBrighter);

        // SplitPane
        UIManager.put("SplitPaneDivider.gripDotCount", 3);
        UIManager.put("SplitPaneDivider.gripDotSize", 3);
        UIManager.put("SplitPaneDivider.gripGap", 3);
        UIManager.put("SplitPane.dividerSize", 10);
        UIManager.put("SplitPane.supportsOneTouchButtons", false);

        // show shortcuts in the 'menubar'
        UIManager.put("Component.hideMnemonics", false);
        UIManager.put("OptionPane.showIcon", true);

        Icons.updateSVGIcons();
        font = UIManager.getFont("defaultFont");
        updateFontSize(config.getFontSize());
    }

    public static Color getBorderColor() {
        return borderColor;
    }

    public static void decreaseFont() {
        int newFontSize = font.getSize() - 1;
        if (newFontSize >= ConfigHandler.MIN_FONT_SIZE) {
            updateFontSize(newFontSize);
        }
    }

    public static void restoreFont() {
        updateFontSize(ConfigHandler.DEFAULT_FONT_SIZE);
    }

    public static void increaseFont() {
        int newFontSize = font.getSize() + 1;
        if (newFontSize <= ConfigHandler.MAX_FONT_SIZE) {
            updateFontSize(newFontSize);
        }
    }

    private static void updateFontSize(float fontSize) {
        font = font.deriveFont(fontSize);
        if (config.setFontSize(font.getSize())) {
            UIManager.put("defaultFont", font);
            FlatLaf.updateUI();
        }
    }

    public static void toggleTheme(Gui gui) {
        UIManager.getDefaults().clear();
        config.toggleTheme();

        initFlatLaf();
        gui.updateUI();
        FlatLaf.updateUI();
    }

    public static Graphics2D createHighQualityGraphics2D(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        return g2d;
    }

    public static BufferedImage getEditedImage(BufferedImage image, ImageDetails imageDetails, boolean jpgImage) {
        int imageType = jpgImage ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

        // Spiegelung & Rotation
        BufferedImage mirrored = getMirroredImage(image, imageDetails.isMirrorX(), imageDetails.isMirrorY(), imageType);
        return getRotatedImage(mirrored, imageDetails.getRotation(), imageType);
    }

    private static BufferedImage getMirroredImage(BufferedImage image, boolean mirrorX, boolean mirrorY, int imageType) {
        if (!mirrorX && !mirrorY) return image;

        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage mirroredImage = new BufferedImage(width, height, imageType);
        Graphics2D g2d = createHighQualityGraphics2D(mirroredImage.getGraphics());

        AffineTransform at = new AffineTransform();
        at.scale(mirrorX ? -1 : 1, mirrorY ? -1 : 1);
        at.translate(mirrorX ? -width : 0, mirrorY ? -height : 0);

        g2d.drawImage(image, at, null);
        g2d.dispose();
        return mirroredImage;
    }

    private static BufferedImage getRotatedImage(BufferedImage image, int rotation, int imageType) {
        if (rotation % 360 == 0) return image;

        int width = image.getWidth();
        int height = image.getHeight();

        double radians = Math.toRadians(rotation);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        int newWidth = (int) Math.floor(width * cos + height * sin);
        int newHeight = (int) Math.floor(height * cos + width * sin);

        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, imageType);
        Graphics2D g2d = createHighQualityGraphics2D(rotatedImage.getGraphics());

        // Zentrieren & rotieren
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - width) / 2.0, (newHeight - height) / 2.0);
        at.rotate(radians, width / 2.0, height / 2.0);
        g2d.drawRenderedImage(image, at);
        g2d.dispose();

        return rotatedImage;
    }

    public static double getImageScaleFactor(Container container, BufferedImage currentImage) {
        int width = container.getWidth();
        int height = container.getHeight();

        int imgWidth = currentImage.getWidth(null);
        int imgHeight = currentImage.getHeight(null);

        double scaleWidth = (double) width / imgWidth;
        double scaleHeight = (double) height / imgHeight;

        // get the minimum scale factor
        return Math.min(scaleWidth, scaleHeight);
    }

    public static boolean valueFitsInRange(Number number, SpinnerNumberModel model) {
        double minValue = ((Number) model.getMinimum()).doubleValue();
        double maxValue = ((Number) model.getMaximum()).doubleValue();
        double stepSize = model.getStepSize().doubleValue();
        double value = number.doubleValue();

        return NumberUtils.valueFitsInRange(value, minValue, maxValue, stepSize);
    }

    public static boolean isEnabled(JComponent component) {
        return component.getName() == null || !component.getName().equals(COMP_DISABLED);
    }

    public static void updateSecretly(JComponent component, Runnable runnable) {
        component.setName(COMP_DISABLED);
        runnable.run();
        component.setName(null);
    }

    public static void setEnabled(Container comp, boolean enabled) {
        if (comp == null) return;

        for (Component child : comp.getComponents()) {
            child.setEnabled(enabled);
            if (child instanceof Container container) {
                setEnabled(container, enabled);
            }
        }
    }
}

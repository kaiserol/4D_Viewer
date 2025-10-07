package de.uzk.utils;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.ui.FlatSpinnerUI;
import com.formdev.flatlaf.ui.FlatTabbedPaneUI;
import com.formdev.flatlaf.ui.FlatTitlePane;
import com.formdev.flatlaf.util.SystemInfo;
import de.uzk.gui.Gui;
import de.uzk.handler.ConfigHandler;
import de.uzk.handler.ImageDetails;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import static de.uzk.Main.config;
import static de.uzk.handler.LanguageHandler.getWord;

public final class GuiUtils {
    public static final String ERROR_MSG = getWord("optionPane.titles.error");
    public static final Color FOCUS_COLOR = Color.decode("#007aff");
    public static final String COMP_DISABLED = "DISABLED";
    public static final String SLIDER_DRAGGED = "DRAGGING";
    private static Color borderColor = null;
    private static Font font = null;

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
            System.setProperty("apple.awt.application.name", getWord("app.title"));
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

        IconUtils.updateSVGIcons();

        font = getDefaultFont();
        setFont(font.deriveFont((float) config.getFontSize()));
    }

    public static Color getBorderColor() {
        return borderColor;
    }

    public static void restoreFont() {
        Font newFont = font.deriveFont((float) ConfigHandler.DEFAULT_FONT_SIZE);
        setFont(newFont);
    }

    public static void incrFont() {
        Font newFont = font.deriveFont((float) (font.getSize() + 1));
        if (newFont.getSize() <= ConfigHandler.MAX_FONT_SIZE) {
            setFont(newFont);
        }
    }

    public static void decrFont() {
        Font newFont = font.deriveFont((float) font.getSize() - 1);
        if (newFont.getSize() >= ConfigHandler.MIN_FONT_SIZE) {
            setFont(newFont);
        }
    }

    private static void setFont(Font newFont) {
        if (newFont != null) {
            font = newFont;
            config.setFontSize(font.getSize());

            UIManager.put("defaultFont", font);
            FlatLaf.updateUI();
        }
    }

    private static Font getDefaultFont() {
        Font font = UIManager.getFont("defaultFont");
        if (font != null) return font;
        return UIManager.getFont("Label.font");
    }

    public static void switchThemes(Gui gui) {
        UIManager.getDefaults().clear();
        config.toggleTheme();

        initFlatLaf();
        gui.updateUI();
        FlatLaf.updateUI();
    }

    public static void updateFontSize(JComponent component, int factor, int style) {
        float newSize = (float) font.getSize() + factor;
        component.setFont(font.deriveFont(newSize).deriveFont(style));
    }

    public static boolean needToRevalidateSize(Container container) {
        if (container == null) return false;

        // banned classes list
        Class<?>[] banned = {CellRendererPane.class, FlatTabbedPaneUI.class, FlatSpinnerUI.class, FlatTitlePane.class};

        for (Component component : container.getComponents()) {
            if (component == null) continue;

            boolean correctContainer = !Arrays.asList(banned).contains(component.getClass()) && !Arrays.asList(banned).contains(component.getClass().getEnclosingClass());
            if (correctContainer && (component.getWidth() < component.getPreferredSize().width || component.getHeight() < component.getPreferredSize().height)) {
                return true;
            }

            if (component instanceof Container castContainer && (needToRevalidateSize(castContainer))) {
                return true;
            }
        }
        return false;
    }

    public static BufferedImage getEditedImage(BufferedImage image, ImageDetails imageDetails, boolean jpgImage) {
        int imageType = jpgImage ? image.getType() : BufferedImage.TYPE_INT_ARGB;
        return getRotatedImage(getMirroredImage(image, imageDetails.isMirrorX(), imageDetails.isMirrorY(), imageType), imageDetails.getRotation(), imageType);
    }

    private static BufferedImage getMirroredImage(BufferedImage image, boolean mirrorX, boolean mirrorY, int imageType) {
        if (!mirrorX && !mirrorY) return image;

        final int width = image.getWidth();
        final int height = image.getHeight();

        BufferedImage mirroredImage = new BufferedImage(width, height, imageType);
        if (mirrorX) {
            if (mirrorY) mirroredImage.getGraphics().drawImage(image, 0, 0, width, height, width, height, 0, 0, null);
            else mirroredImage.getGraphics().drawImage(image, 0, 0, width, height, width, 0, 0, height, null);
        } else mirroredImage.getGraphics().drawImage(image, 0, 0, width, height, 0, height, width, 0, null);

        return mirroredImage;
    }

    private static BufferedImage getRotatedImage(BufferedImage image, int rotation, int imageType) {
        if (rotation % 360 == 0) return image;
        if (rotation < 0) rotation = 360 - (rotation % 360);
        final int w = image.getWidth();
        final int h = image.getHeight();

        double radians = Math.toRadians(rotation);

        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, imageType);
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2.0, (newHeight - h) / 2.0);
        at.rotate(radians, w / 2.0, h / 2.0);

        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        rotatedImage = op.filter(image, rotatedImage);

        return rotatedImage;
    }

    public static Dimension resizeImageSize(Container container, BufferedImage currentImage) {
        int width = container.getWidth();
        int height = container.getHeight();

        int imgWidth = currentImage.getWidth(null);
        int imgHeight = currentImage.getHeight(null);

        double scaleWidth = (double) width / imgWidth;
        double scaleHeight = (double) height / imgHeight;

        // get the minimum scale factor
        double scaleFactor = Math.min(scaleWidth, scaleHeight);
        return new Dimension((int) (imgWidth * scaleFactor), (int) (imgHeight * scaleFactor));
    }

    public static boolean valueFitsInRange(Number number, SpinnerNumberModel model) {
        double minValue = ((Number) model.getMinimum()).doubleValue();
        double maxValue = ((Number) model.getMaximum()).doubleValue();
        double stepSize = model.getStepSize().doubleValue();
        double value = number.doubleValue();

        return NumberUtils.valueFitsInRange(value, minValue, maxValue, stepSize);
    }

    public static boolean isEnabled(JComponent component) {
        return component.getName() == null || !component.getName().equals(GuiUtils.COMP_DISABLED);
    }

    public static void updateSecretly(JComponent component, Runnable runnable) {
        component.setName(GuiUtils.COMP_DISABLED);
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

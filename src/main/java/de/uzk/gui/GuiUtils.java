package de.uzk.gui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import de.uzk.action.ActionType;
import de.uzk.config.Language;
import de.uzk.config.Settings;
import de.uzk.config.Theme;
import de.uzk.utils.NumberUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitResponse;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Locale;

import static de.uzk.Main.*;
import static de.uzk.config.LanguageHandler.getWord;

public final class GuiUtils {
    // Farben
    public static final Color COLOR_BLUE = new Color(0, 122, 255);
    public static final Color COLOR_GREEN = new Color(8, 166, 52);
    public static final Color COLOR_YELLOW = new Color(252, 204, 78);
    public static final Color COLOR_RED = new Color(255, 86, 86);
    public static final Color COLOR_DARK_RED = new Color(148, 0, 0);

    // Eigenschaften
    private static Color textColor;
    private static Color borderColor;
    private static Color backgroundColor;
    private static Font font;

    private GuiUtils() {
    }

    // ========================================
    // Getter
    // ========================================
    private static FlatLaf getDarkMode() {
        return new FlatMacDarkLaf();
    }

    private static FlatLaf getLightMode() {
        return new FlatMacLightLaf();
    }

    public static Color getTextColor() {
        return textColor;
    }

    public static Color getBorderColor() {
        return borderColor;
    }

    public static Color getBackgroundColor() {
        return backgroundColor;
    }

    public static String getFontName() {
        return font.getName();
    }

    public static Desktop getDesktopSecure() {
        if (!Desktop.isDesktopSupported()) return null;
        return Desktop.getDesktop();
    }

    // ========================================
    // Setter
    // ========================================
    public static void setCursor(JComponent component, Cursor cursor) {
        if (component == null) return;
        SwingUtilities.invokeLater(() -> component.setCursor(cursor));
    }

    public static void setImageIcon(JFrame frame) {
        frame.setIconImage(Icons.APP_IMAGE);

        // App-Icon (plattformübergreifend) setzen
        if (Taskbar.isTaskbarSupported()) {
            try {
                Taskbar.getTaskbar().setIconImage(Icons.APP_IMAGE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void setToolTipText(JComponent component, String text) {
        if (component == null) return;
        component.setToolTipText(text);

        Point mouse = component.getMousePosition();
        if (mouse != null) {
            MouseEvent mouseEvent = new MouseEvent(component, MouseEvent.MOUSE_MOVED,
                    System.currentTimeMillis(), 0, mouse.x, mouse.y, 0, false
            );
            // Swing-Tooltip neu initialisieren
            ToolTipManager.sharedInstance().mouseMoved(mouseEvent);
        }
    }

    // ========================================
    // WebLink öffnen
    // ========================================
    public static void openWebLink(URL url) {
        Desktop desktop = getDesktopSecure();
        if (desktop == null) return;

        try {
            desktop.browse(url.toURI());
        } catch (Exception e) {
            logger.error(String.format("Failed opening web link '%s'", url));
        }
    }

    // ========================================
    // Initialsierungen
    // ========================================

    /**
     * Initialsiert plattformspezifische Eigenschaften.
     * Sollte vor Erstellen der GUI aufgerufen werden.
     */
    public static void initSystemProperties() {
        // macOS Eigenschaften
        if (operationSystem.isMacOS()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", getWord("app.name"));
        }
    }

    public static void initMacOS(Gui gui) {
        if (gui == null || !operationSystem.isMacOS()) return;
        Desktop desktop = getDesktopSecure();
        if (desktop == null) return;

        // TODO: Ersetze durch eine Dialog Klasse (About)
        // Über 4D Viewer
        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            desktop.setAboutHandler(e ->
                    JOptionPane.showMessageDialog(null,
                            "4D Viewer v2.1\nErstellt von Oliver Kaiser",
                            "Über 4D Viewer",
                            JOptionPane.INFORMATION_MESSAGE)
            );
        }

        // Einstellungen
        if (desktop.isSupported(Desktop.Action.APP_PREFERENCES)) {
            desktop.setPreferencesHandler(e ->
                    gui.getActionHandler().executeAction(ActionType.SHORTCUT_OPEN_SETTINGS)
            );
        }

        // Cmd+Q abfangen (Quit)
        if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
            desktop.setQuitHandler((QuitEvent e, QuitResponse response) -> {
                response.cancelQuit();
                gui.confirmExitApp();
            });
        }
    }

    // ========================================
    // Aktualisierungen
    // ========================================
    public static void updateFlatLaf() {
        FlatLaf.setup(settings.getTheme().isLight() ? getLightMode() : getDarkMode());

        // Farben Eigenschaften
        textColor = UIManager.getColor("Label.foreground");
        borderColor = UIManager.getColor("Component.borderColor");
        backgroundColor = UIManager.getColor("TextArea.background");

        // Schriftart Eigenschaft
        font = UIManager.getFont("defaultFont");
        FlatLaf.updateUI();

        // Titelleiste auf FlatLaf-Dekoration umstellen
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        // Focus Eigenschaften
        UIManager.put("Component.focusWidth", 0);
        UIManager.put("Component.innerFocusWidth", 0);

        // Rounded Elemente Eigenschaften
        UIManager.put("Button.arc", 5);
        UIManager.put("Component.arc", 5);
        UIManager.put("TextComponent.arc", 5);
        UIManager.put("ProgressBar.arc", 5);

        // TabbedPane Eigenschaften
        UIManager.put("TabbedPane.contentSeparatorHeight", 1);
        UIManager.put("TabbedPane.showTabSeparators", true);
        UIManager.put("TabbedPane.background", backgroundColor);

        // ScrollBar Eigenschaften
        UIManager.put("Component.arrowType", "chevron");
        UIManager.put("ScrollBar.showButtons", true);
        UIManager.put("ScrollBar.trackArc", 999);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.width", 10);

        UIManager.put("ScrollBar.trackInsets", new Insets(0, 0, 0, 0));
        UIManager.put("ScrollBar.thumbInsets", BorderFactory.createEmptyBorder());

        Color trackColor = UIManager.getColor("ScrollBar.background");
        UIManager.put("ScrollBar.track", trackColor);
        UIManager.put("ScrollBar.hoverTrackColor", trackColor);

        Color thumbColor = COLOR_BLUE;
        UIManager.put("ScrollBar.thumb", thumbColor);
        UIManager.put("ScrollBar.hoverThumbColor", thumbColor.darker());
        UIManager.put("ScrollBar.pressedThumbColor", thumbColor.darker());

        // SplitPane Eigenschaften
        UIManager.put("SplitPaneDivider.gripDotCount", 0);
        UIManager.put("SplitPaneDivider.gripDotSize", 3);
        UIManager.put("SplitPaneDivider.gripGap", 3);
        UIManager.put("SplitPane.dividerSize", 10);
        UIManager.put("SplitPane.supportsOneTouchButtons", true);

        // Mnemonics/Icons Eigenschaften
        UIManager.put("Component.hideMnemonics", false);
        UIManager.put("OptionPane.showIcon", true);
        UIManager.put("Dialog.showIcon", true);
        Icons.updateSVGIcons();
    }

    public static void updateLanguage(Gui gui, Language language) {
        // Wenn sich die Sprache nicht ändert, abbrechen
        Language oldLanguage = settings.getLanguage();
        if (!settings.setLanguage(language)) return;

        // UI aktualisieren
        logger.info(String.format("Updating Language from '%s' to '%s'", oldLanguage.getValue(), language.getValue()));
        Locale.setDefault(language.getLocale());
        JComponent.setDefaultLocale(language.getLocale());
        gui.rebuild();
    }

    public static void updateTheme(Gui gui, Theme theme) {
        // Wenn sich das Farbschema nicht ändert, abbrechen
        Theme oldTheme = settings.getTheme();
        if (!settings.setTheme(theme)) return;

        // UI aktualisieren
        logger.info(String.format("Updating Theme from '%s' to '%s'", oldTheme.getValue(), theme.getValue()));
        updateFlatLaf();
        FlatLaf.updateUI();
        gui.updateTheme();
    }

    public static void updateFontSize(Gui gui, int fontSize) {
        if (fontSize < Settings.MIN_FONT_SIZE || fontSize > Settings.MAX_FONT_SIZE) return;

        // Wenn sich die Schriftgröße nicht ändert, abbrechen
        if (!settings.setFontSize(fontSize)) return;

        // UI aktualisieren
        font = font.deriveFont((float) fontSize);
        UIManager.put("defaultFont", font);
        FlatLaf.updateUI();
        gui.handleAction(ActionType.ACTION_UPDATE_FONT);
    }

    // ========================================
    // Bild Bearbeitung & Graphics
    // ========================================
    public static double getImageScaleFactor(BufferedImage image, Container container) {
        int imgWidth = image.getWidth(null);
        int imgHeight = image.getHeight(null);

        double scaleWidth = (double) container.getWidth() / imgWidth;
        double scaleHeight = (double) container.getHeight() / imgHeight;
        return Math.min(scaleWidth, scaleHeight);
    }

    public static Graphics2D createHighQualityGraphics2D(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        return g2d;
    }

    public static BufferedImage getEditedImage(BufferedImage image, boolean transparentBackground) {
        int imageType = transparentBackground ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

        // Spiegelung & Rotation
        BufferedImage mirrored = getMirroredImage(image, workspace.getConfig().isMirrorX(), workspace.getConfig().isMirrorY(), imageType);
        return getRotatedImage(mirrored, workspace.getConfig().getRotation(), imageType);
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
    public static void setEnabled(Container container, boolean enabled) {
        if (container == null) return;

        for (Component component : getComponents(container)) {
            component.setEnabled(enabled);
            if (component instanceof Container newContainer) {
                setEnabled(newContainer, enabled);
            }
        }
    }

    private static Component[] getComponents(Container container) {
        if (container == null) return new Component[0];
        else if (container instanceof JWindow window) return window.getOwnedWindows();
        else if (container instanceof JFrame frame) return frame.getContentPane().getComponents();
        else if (container instanceof JDialog dialog) return dialog.getContentPane().getComponents();
        else if (container instanceof JMenu menu) return menu.getMenuComponents();
        else if (container instanceof JScrollPane scrollPane) return scrollPane.getViewport().getComponents();
        else return container.getComponents();
    }

    public static boolean valueFitsInRange(Number number, SpinnerNumberModel model) {
        double minValue = ((Number) model.getMinimum()).doubleValue();
        double maxValue = ((Number) model.getMaximum()).doubleValue();
        double stepSize = model.getStepSize().doubleValue();
        double value = number.doubleValue();

        return NumberUtils.valueFitsInRange(value, minValue, maxValue, stepSize);
    }

    public static void runWithoutAdjustmentEvents(JScrollBar scrollBar, Runnable action) {
        AdjustmentListener[] listeners = scrollBar.getAdjustmentListeners();
        for (AdjustmentListener l : listeners) scrollBar.removeAdjustmentListener(l);
        try {
            action.run();
        } finally {
            for (AdjustmentListener l : listeners) scrollBar.addAdjustmentListener(l);
        }
    }
}

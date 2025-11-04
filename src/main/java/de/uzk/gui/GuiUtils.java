package de.uzk.gui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.util.ColorFunctions;
import de.uzk.action.ActionType;
import de.uzk.config.Language;
import de.uzk.config.Settings;
import de.uzk.config.Theme;
import de.uzk.markers.Marker;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

    // Inse
    // Farben & Schriftart
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
                logger.error("Failed setting taskbar icon");
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
    // Initialisierungen
    // ========================================

    /**
     * Initialisiert plattformspezifische Eigenschaften.
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
        // Setup Theme
        FlatLaf.setup(settings.getTheme().isLight() ? getLightMode() : getDarkMode());
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        // Farben und Schriftart
        updateBaseColorsAndFont();

        int arc = 999;
        Insets defaultInsets = new Insets(5, 10, 5, 10);
        String arrowType = "chevron"; // Pfeil-Form: caret, chevron, triangle

        // Komponenten-Konfigurationen
        setupMisc(arc, defaultInsets, arrowType);
        setupComboBox(arc, defaultInsets, arrowType);
        setupSpinner(arc, defaultInsets, arrowType);
        setupTabbedPane(arc, arrowType);
        setupScrollBar(arc);
        setupSplitPane();

        // UI aktualisieren
        FlatLaf.updateUI();
        Icons.updateSVGIcons();
    }

    private static void updateBaseColorsAndFont() {
        textColor = UIManager.getColor("Label.foreground");
        borderColor = UIManager.getColor("Component.borderColor");
        backgroundColor = UIManager.getColor("TextArea.background");

        Font defaultFont = UIManager.getFont("defaultFont");
        font = defaultFont.deriveFont((float) settings.getFontSize());
        UIManager.put("defaultFont", font);
    }

    private static void setupMisc(int arc, Insets padding, String arrowType) {
        // Komponente
        UIManager.put("Component.arrowType", arrowType);
        UIManager.put("Component.hideMnemonics", false);
        UIManager.put("Component.arc", arc);

        // Icon
        UIManager.put("Dialog.showIcon", true);
        UIManager.put("OptionPane.showIcon", true);

        // Button
        UIManager.put("Button.arc", arc);
        UIManager.put("Button.margin", padding);

        // Checkbox
        UIManager.put("CheckBox.arc", arc);
        UIManager.put("CheckBox.margin", new Insets(0, 0, 0, 0));

        // Textfeld
        UIManager.put("TextComponent.arc", arc);
        UIManager.put("TextField.arc", arc);
        UIManager.put("TextArea.margin", padding);
        UIManager.put("TextField.margin", padding);
        UIManager.put("TextPane.margin", padding);

        // Sonstige Komponenten
        UIManager.put("List.selectionArc", arc);
        UIManager.put("MenuItem.selectionArc", arc);
        UIManager.put("ProgressBar.arc", arc);
        UIManager.put("TitlePane.buttonArc", arc);
        UIManager.put("ToolBar.hoverButtonGroupArc", arc);
        UIManager.put("Tree.selectionArc", arc);
    }

    private static void setupComboBox(int arc, Insets padding, String arrowType) {
        Color arrowColor = Color.LIGHT_GRAY;
        Color textFieldBg = UIManager.getColor("TextField.background");
        Color defaultButtonBg = UIManager.getColor("Button.default.background");

        UIManager.put("ComboBox.arc", arc);
        UIManager.put("ComboBox.buttonArc", arc);
        UIManager.put("ComboBox.selectionArc", arc);
        UIManager.put("ComboBox.buttonStyle", "button");
        UIManager.put("ComboBox.padding", padding);

        // Pfeilfarben
        UIManager.put("ComboBox.buttonArrowType", arrowType);
        UIManager.put("ComboBox.buttonArrowColor", applyColorAdjustment(arrowColor, 0.0f, false));
        UIManager.put("ComboBox.buttonHoverArrowColor", applyColorAdjustment(arrowColor, 0.1f, true));
        UIManager.put("ComboBox.buttonPressedArrowColor", applyColorAdjustment(arrowColor, 0.3f, true));

        // Hintergrundfarbe
        UIManager.put("ComboBox.background", textFieldBg);
        UIManager.put("ComboBox.popupBackground", textFieldBg);
        UIManager.put("ComboBox.buttonBackground", defaultButtonBg);
        UIManager.put("ComboBox.buttonSeparatorColor", defaultButtonBg);
        UIManager.put("ComboBox.buttonEditableBackground", defaultButtonBg);
    }

    private static void setupSpinner(int arc, Insets padding, String arrowType) {
        Color arrowColor = Color.LIGHT_GRAY;
        Color textFieldBg = UIManager.getColor("TextField.background");
        Color defaultButtonBg = UIManager.getColor("Button.default.background");

        UIManager.put("Spinner.arc", arc);
        UIManager.put("Spinner.buttonArc", arc);
        UIManager.put("Spinner.selectionArc", arc);
        UIManager.put("Spinner.buttonStyle", "roundRect");
        UIManager.put("Spinner.padding", padding);

        // Pfeilfarben
        UIManager.put("Spinner.buttonArrowType", arrowType);
        UIManager.put("Spinner.buttonArrowColor", applyColorAdjustment(arrowColor, 0.0f, false));
        UIManager.put("Spinner.buttonHoverArrowColor", applyColorAdjustment(arrowColor, 0.1f, true));
        UIManager.put("Spinner.buttonPressedArrowColor", applyColorAdjustment(arrowColor, 0.3f, true));

        // Hintergrundfarbe
        UIManager.put("Spinner.background", textFieldBg);
        UIManager.put("Spinner.buttonBackground", defaultButtonBg);
        UIManager.put("Spinner.buttonSeparatorColor", defaultButtonBg);
    }

    private static void setupTabbedPane(int arc, String arrowType) {
        UIManager.put("TabbedPane.buttonArc ", arc);
        UIManager.put("TabbedPane.closeArc", arc);
        UIManager.put("TabbedPane.tabArc", 0);
        UIManager.put("TabbedPane.tabSelectionArc", 0);
        UIManager.put("TabbedPane.tabSelectionHeight", 2);
        UIManager.put("TabbedPane.showTabSeparators", false);
        UIManager.put("TabbedPane.arrowType", arrowType);

        // Linienfarbe
        UIManager.put("TabbedPane.underlineColor", COLOR_BLUE);
        UIManager.put("TabbedPane.inactiveUnderlineColor", settings.getTheme().isLight() ? Color.GRAY : Color.WHITE);

        // Scroll-Eigenschaften
        UIManager.put("TabbedPane.tabsPopupPolicy", "never");
        UIManager.put("TabbedPane.scrollButtonsPolicy", "asNeeded");
    }

    private static void setupScrollBar(int arc) {
        Color trackColor = UIManager.getColor("ScrollBar.background");
        Color thumbColor = COLOR_BLUE;

        UIManager.put("ScrollBar.trackArc", arc);
        UIManager.put("ScrollBar.thumbArc", arc);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.trackInsets", new Insets(0, 0, 0, 0));
        UIManager.put("ScrollBar.thumbInsets", BorderFactory.createEmptyBorder());
        UIManager.put("ScrollBar.showButtons", true);

        // Hinter- und Vordergrundfarbe
        UIManager.put("ScrollBar.track", trackColor);
        UIManager.put("ScrollBar.hoverTrackColor", trackColor);
        UIManager.put("ScrollBar.thumb", thumbColor);
        UIManager.put("ScrollBar.hoverThumbColor", thumbColor.darker());
        UIManager.put("ScrollBar.pressedThumbColor", thumbColor.darker());
    }

    private static void setupSplitPane() {
        Color arrowColor = UIManager.getColor("Label.foreground");

        UIManager.put("SplitPaneDivider.gripDotCount", 0);
        UIManager.put("SplitPaneDivider.gripDotSize", 3);
        UIManager.put("SplitPaneDivider.gripGap", 3);
        UIManager.put("SplitPane.dividerSize", 10);
        UIManager.put("SplitPane.supportsOneTouchButtons", true);

        // Pfeilfarben
        boolean isLight = settings.getTheme().isLight();
        UIManager.put("SplitPaneDivider.oneTouchArrowColor", applyColorAdjustment(arrowColor, 0.3f, isLight));
        UIManager.put("SplitPaneDivider.oneTouchHoverArrowColor", applyColorAdjustment(arrowColor, 0.2f, isLight));
        UIManager.put("SplitPaneDivider.oneTouchPressedArrowColor", applyColorAdjustment(arrowColor, 0.0f, !isLight));
    }

    public static String getAllUIManagerProperties() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== UIManager Properties ===\n");

        // Alle Keys holen und alphabetisch sortieren
        List<Object> keys = new ArrayList<>(UIManager.getDefaults().keySet());
        keys.sort(Comparator.comparing(Object::toString, String.CASE_INSENSITIVE_ORDER));

        for (Object key : keys) {
            Object value = UIManager.get(key).toString().replaceAll("\\n", " ");
            sb.append(String.format("%-40s : %s%n", key, value));
        }

        return sb.toString();
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
        FlatLaf.updateUI();
        updateFlatLaf();
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
        gui.updateTheme();
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

    public static BufferedImage getEditedImage(BufferedImage image, boolean transparentBackground, List<Marker> appliedMarkers) {
        int imageType = transparentBackground ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

        // Spiegelung & Rotation
        BufferedImage marked = getMarkedImage(image, imageType, appliedMarkers);
        BufferedImage mirrored = getMirroredImage(marked, imageType, workspace.getConfig().isMirrorX(), workspace.getConfig().isMirrorY());
        BufferedImage rotated = getRotatedImage(mirrored, imageType, workspace.getConfig().getRotation());

        return getZoomedImage(rotated, workspace.getConfig().getZoom(), imageType);}

    public static BufferedImage makeBackgroundOpaque(BufferedImage image) {
        BufferedImage opaqueBackground = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = createHighQualityGraphics2D(opaqueBackground.getGraphics());
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return opaqueBackground;
    }



    private static BufferedImage getMarkedImage(BufferedImage image, int imageType, List<Marker> appliedMarkers) {
        if (appliedMarkers == null || appliedMarkers.isEmpty()) return image;
        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage markedImage = new BufferedImage(image.getWidth(), image.getHeight(), imageType);
        Graphics2D g2d = createHighQualityGraphics2D(markedImage.getGraphics());
        g2d.drawImage(image, 0, 0, null);
        for (Marker marker : appliedMarkers) {
            marker.draw(g2d, new Rectangle(0, 0, width, height), 1.0);

        }
        g2d.dispose();
        return markedImage;
    }

    private static BufferedImage getMirroredImage(BufferedImage image, int imageType, boolean mirrorX, boolean mirrorY) {
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

    private static BufferedImage getRotatedImage(BufferedImage image, int imageType, int rotation) {
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

    private static BufferedImage getZoomedImage(BufferedImage image, int zoom, int imageType) {
        if(zoom == 100) return image;
        double zoomPercentage = zoom / 100.0;
        int width = image.getWidth();
        int height = image.getHeight();

        int insetX = (width - image.getWidth()) / 2;
        int insetY = (height - image.getHeight()) / 2;

        BufferedImage zoomedImage = new BufferedImage(width, height, imageType);

        Graphics2D g2d = (Graphics2D) zoomedImage.getGraphics();
        g2d.scale(zoomPercentage, zoomPercentage);
        g2d.drawImage(image, -insetX, -insetY, null);
        g2d.dispose();
        return zoomedImage;
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
    private static Color applyColorAdjustment(Color color, float factor, boolean lighten) {
        if (color == null) throw new NullPointerException("Color is null.");
        if (factor < 0) throw new IllegalArgumentException("Factor must be greater than or equal to 0.");
        if (factor > 1) throw new IllegalArgumentException("Factor must be less than or equal to 1.");
        if (factor == 0) return color;

        if (lighten) return ColorFunctions.lighten(color, factor);
        else return ColorFunctions.darken(color, factor);
    }

    public static void setEnabled(Container container, boolean enabled) {
        if (container == null) return;

        for (Component component : getComponents(container)) {
            component.setEnabled(enabled);
            if (component instanceof Container newContainer) {
                setEnabled(newContainer, enabled);
            }
        }
    }

    public static Component[] getComponents(Container container) {
        if (container == null) return new Component[0];
        else if (container instanceof JWindow window) return window.getOwnedWindows();
        else if (container instanceof JFrame frame) return frame.getContentPane().getComponents();
        else if (container instanceof JDialog dialog) return dialog.getContentPane().getComponents();
        else if (container instanceof JMenu menu) return menu.getMenuComponents();
        else if (container instanceof JScrollPane scrollPane) return scrollPane.getViewport().getComponents();
        else return container.getComponents();
    }

    public static void makeComponentsSameSize(JPanel buttonPanel, Class<? extends Component> clazz) {
        int maxWidth = 0;

        // Maximale Breite bestimmen
        for (Component comp : buttonPanel.getComponents()) {
            if (comp.getClass().equals(clazz)) {
                Dimension pref = comp.getPreferredSize();
                maxWidth = Math.max(maxWidth, pref.width);
            }
        }

        // Einheitliche Größe setzen
        for (Component comp : buttonPanel.getComponents()) {
            if (comp.getClass().equals(clazz)) {
                Dimension size = comp.getPreferredSize();
                comp.setPreferredSize(new Dimension(maxWidth, size.height));
            }
        }
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

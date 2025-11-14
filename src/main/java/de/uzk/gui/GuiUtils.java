package de.uzk.gui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.util.ColorFunctions;
import de.uzk.action.ActionType;
import de.uzk.config.Language;
import de.uzk.config.Theme;
import de.uzk.io.Icons;
import de.uzk.markers.Marker;
import de.uzk.utils.ColorUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitResponse;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static de.uzk.Main.*;
import static de.uzk.config.LanguageHandler.getWord;

public final class GuiUtils {
    // Rahmen / Padding
    public static final Border BORDER_EMPTY_NONE = BorderFactory.createEmptyBorder();
    public static final Border BORDER_EMPTY_DEFAULT = BorderFactory.createEmptyBorder(10, 10, 10, 10);
    public static final Border BORDER_EMPTY_MEDIUM = BorderFactory.createEmptyBorder(5, 10, 5, 10);
    public static final Border BORDER_EMPTY_SMALL = BorderFactory.createEmptyBorder(5, 5, 5, 5);
    public static final Border BORDER_EMPTY_TINY = BorderFactory.createEmptyBorder(2, 5, 2, 5);

    // Insets
    public static final Insets INSETS_NONE = new Insets(0, 0, 0, 0);
    public static final Insets INSETS_DEFAULT = new Insets(10, 10, 10, 10);
    public static final Insets INSETS_MEDIUM = new Insets(5, 10, 5, 10);
    public static final Insets INSETS_SMALL = new Insets(5, 5, 5, 5);

    // Bogengröße
    private static final int ARC_DEFAULT = 999;

    // Pfeil-Form: caret, chevron, triangle
    private static final String ARROW_TYPE = "chevron";

    // Farben & Schriftart
    private static Color textColor;
    private static Color borderColor;
    private static Color backgroundColor;
    private static Font font;

    private GuiUtils() {
        // Verhindert Instanziierung dieser Klasse
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

    public static Desktop getDesktopSecurely() {
        if (!Desktop.isDesktopSupported()) return null;
        return Desktop.getDesktop();
    }

    // ========================================
    // Setter
    // ========================================
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

    public static void setCursor(JComponent component, Cursor cursor) {
        if (component == null) return;
        SwingUtilities.invokeLater(() -> component.setCursor(cursor));
    }

    public static void setToolTipText(JComponent component, String text) {
        if (component == null) return;
        component.setToolTipText(text);

        ToolTipManager.sharedInstance().setInitialDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
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
        Desktop desktop = getDesktopSecurely();
        if (desktop == null) return;

        try {
            desktop.browse(url.toURI());
        } catch (Exception e) {
            logger.error(String.format("Failed opening web link '%s'", url));
        }
    }

    // ========================================
    // Einstellungen Aktualisierungen
    // ========================================
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

    public static void initDesktopHandlers(Gui gui) {
        Desktop desktop = getDesktopSecurely();
        if (gui == null || desktop == null) return;

        // Über 4D Viewer
        if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
            desktop.setAboutHandler(e ->
                gui.getActionHandler().executeAction(ActionType.SHORTCUT_SHOW_ABOUT)
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
                gui.closeApp();
            });
        }
    }

    // ========================================
    // Aktualisierungen
    // ========================================
    public static void updateFlatLaf() {
        // Setup Theme
        FlatLaf.setup(settings.getTheme().isLightMode() ? getLightMode() : getDarkMode());
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        // Farben und Schriftart
        updateBaseColorsAndFont();

        // Komponenten-Konfigurationen
        setupMisc();
        setupComboBox();
        setupSpinner();
        setupTabbedPane();
        setupScrollBar();
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

    // ========================================
    // UIManger Aktualisierungen
    // ========================================
    private static void setupMisc() {
        // Komponente
        UIManager.put("Component.arrowType", ARROW_TYPE);
        UIManager.put("Component.hideMnemonics", false);
        UIManager.put("Component.arc", ARC_DEFAULT);

        // Icon
        UIManager.put("Dialog.showIcon", true);
        UIManager.put("OptionPane.showIcon", true);

        // Button
        UIManager.put("Button.arc", ARC_DEFAULT);
        UIManager.put("Button.margin", INSETS_MEDIUM);

        // Checkbox
        UIManager.put("CheckBox.margin", INSETS_NONE);

        // Textfeld
        UIManager.put("TextComponent.arc", ARC_DEFAULT);
        UIManager.put("TextField.arc", ARC_DEFAULT);
        UIManager.put("TextArea.margin", INSETS_MEDIUM);
        UIManager.put("TextField.margin", INSETS_MEDIUM);
        UIManager.put("TextPane.margin", INSETS_MEDIUM);

        // Sonstige Komponenten
        UIManager.put("List.selectionArc", ARC_DEFAULT);
        UIManager.put("MenuItem.selectionArc", ARC_DEFAULT);
        UIManager.put("ProgressBar.arc", ARC_DEFAULT);
        UIManager.put("TitlePane.buttonArc", ARC_DEFAULT);
        UIManager.put("ToolBar.hoverButtonGroupArc", ARC_DEFAULT);
        UIManager.put("Tree.selectionArc", ARC_DEFAULT);
    }

    private static void setupComboBox() {
        Color arrowColor = Color.LIGHT_GRAY;
        Color textFieldBg = UIManager.getColor("TextField.background");
        Color defaultButtonBg = UIManager.getColor("Button.default.background");

        UIManager.put("ComboBox.arc", ARC_DEFAULT);
        UIManager.put("ComboBox.buttonArc", ARC_DEFAULT);
        UIManager.put("ComboBox.selectionArc", ARC_DEFAULT);
        UIManager.put("ComboBox.buttonStyle", "button");
        UIManager.put("ComboBox.padding", INSETS_MEDIUM);

        // Pfeilfarben
        UIManager.put("ComboBox.buttonArrowType", ARROW_TYPE);
        UIManager.put("ComboBox.buttonArrowColor", adjustColor(arrowColor, 0.0f, false));
        UIManager.put("ComboBox.buttonHoverArrowColor", adjustColor(arrowColor, 0.1f, true));
        UIManager.put("ComboBox.buttonPressedArrowColor", adjustColor(arrowColor, 0.3f, true));

        // Hintergrundfarbe
        UIManager.put("ComboBox.background", textFieldBg);
        UIManager.put("ComboBox.popupBackground", textFieldBg);
        UIManager.put("ComboBox.buttonBackground", defaultButtonBg);
        UIManager.put("ComboBox.buttonSeparatorColor", defaultButtonBg);
        UIManager.put("ComboBox.buttonEditableBackground", defaultButtonBg);
    }

    private static void setupSpinner() {
        Color arrowColor = Color.LIGHT_GRAY;
        Color textFieldBg = UIManager.getColor("TextField.background");
        Color defaultButtonBg = UIManager.getColor("Button.default.background");

        UIManager.put("Spinner.arc", ARC_DEFAULT);
        UIManager.put("Spinner.buttonArc", ARC_DEFAULT);
        UIManager.put("Spinner.selectionArc", ARC_DEFAULT);
        UIManager.put("Spinner.buttonStyle", "roundRect");
        UIManager.put("Spinner.padding", INSETS_MEDIUM);

        // Pfeilfarben
        UIManager.put("Spinner.buttonArrowType", ARROW_TYPE);
        UIManager.put("Spinner.buttonArrowColor", adjustColor(arrowColor, 0.0f, false));
        UIManager.put("Spinner.buttonHoverArrowColor", adjustColor(arrowColor, 0.1f, true));
        UIManager.put("Spinner.buttonPressedArrowColor", adjustColor(arrowColor, 0.3f, true));

        // Hintergrundfarbe
        UIManager.put("Spinner.background", textFieldBg);
        UIManager.put("Spinner.buttonBackground", defaultButtonBg);
        UIManager.put("Spinner.buttonSeparatorColor", defaultButtonBg);
    }

    private static void setupTabbedPane() {
        UIManager.put("TabbedPane.buttonArc ", ARC_DEFAULT);
        UIManager.put("TabbedPane.closeArc", ARC_DEFAULT);
        UIManager.put("TabbedPane.tabArc", 0);
        UIManager.put("TabbedPane.tabSelectionArc", 0);
        UIManager.put("TabbedPane.tabSelectionHeight", 2);
        UIManager.put("TabbedPane.showTabSeparators", false);
        UIManager.put("TabbedPane.arrowType", ARROW_TYPE);

        // Linienfarbe
        UIManager.put("TabbedPane.underlineColor", ColorUtils.COLOR_BLUE);
        UIManager.put("TabbedPane.inactiveUnderlineColor", settings.getTheme().isLightMode() ? Color.GRAY : Color.WHITE);

        // Scroll-Eigenschaften
        UIManager.put("TabbedPane.tabsPopupPolicy", "never");
        UIManager.put("TabbedPane.scrollButtonsPolicy", "asNeeded");
    }

    private static void setupScrollBar() {
        Color trackColor = UIManager.getColor("ScrollBar.background");
        Color thumbColor = ColorUtils.COLOR_BLUE;

        UIManager.put("ScrollBar.trackArc", ARC_DEFAULT);
        UIManager.put("ScrollBar.thumbArc", ARC_DEFAULT);
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.trackInsets", INSETS_NONE);
        UIManager.put("ScrollBar.thumbInsets", BORDER_EMPTY_NONE);
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
        boolean isLight = settings.getTheme().isLightMode();
        UIManager.put("SplitPaneDivider.oneTouchArrowColor", adjustColor(arrowColor, 0.3f, isLight));
        UIManager.put("SplitPaneDivider.oneTouchHoverArrowColor", adjustColor(arrowColor, 0.2f, isLight));
        UIManager.put("SplitPaneDivider.oneTouchPressedArrowColor", adjustColor(arrowColor, 0.0f, !isLight));
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
        float offset = 128 * ((workspace.getConfig().getBrightness() - 100) / 100f);
        // TODO: debug auskommentieren wenn du es wieder brauchst
//        System.out.printf("Brightness %d creates offset %f%n", workspace.getConfig().getBrightness(), offset);
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


        g2d.transform(at);

        g2d.drawRenderedImage(image, null);

        for (Marker marker : appliedMarkers) {
            marker.draw(g2d, new Rectangle(0, 0, newWidth, newHeight), 1.0);
        }

        g2d.dispose();

        return transformedImage;
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
    public static Color adjustColor(Color color, float factor, boolean lighten) {
        if (color == null) throw new NullPointerException("Color is null.");
        if (factor < 0) throw new IllegalArgumentException("Factor must be greater than or equal to 0.");
        if (factor > 1) throw new IllegalArgumentException("Factor must be less than or equal to 1.");
        if (factor == 0) return color;

        if (lighten) return ColorFunctions.lighten(color, factor);
        else return ColorFunctions.darken(color, factor);
    }
}

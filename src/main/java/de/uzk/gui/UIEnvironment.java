package de.uzk.gui;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import de.uzk.action.ActionType;
import de.uzk.config.InitialDirectory;
import de.uzk.config.Language;
import de.uzk.config.Theme;
import de.uzk.io.ImageLoader;
import de.uzk.utils.ComponentUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitResponse;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.nio.file.Path;
import java.util.Locale;

import static de.uzk.Main.*;
import static de.uzk.config.LanguageHandler.getWord;

public final class UIEnvironment {
    // Abstände
    public static final int SPACING_TINY = 2;
    public static final int SPACING_SMALL = 5;
    public static final int SPACING_DEFAULT = 10;
    // Insets
    public static final Insets INSETS_NONE = new Insets(0, 0, 0, 0);
    public static final Insets INSETS_SMALL = new Insets(SPACING_SMALL, SPACING_SMALL, SPACING_SMALL, SPACING_SMALL);
    public static final Insets INSETS_MEDIUM = new Insets(SPACING_SMALL, SPACING_DEFAULT, SPACING_SMALL, SPACING_DEFAULT);
    public static final Insets INSETS_DEFAULT = new Insets(SPACING_DEFAULT, SPACING_DEFAULT, SPACING_DEFAULT, SPACING_DEFAULT);
    // Border
    public static final Border BORDER_EMPTY_NONE = BorderFactory.createEmptyBorder(0, 0, 0, 0);
    public static final Border BORDER_EMPTY_TINY = BorderFactory.createEmptyBorder(SPACING_TINY, SPACING_SMALL, SPACING_TINY, SPACING_SMALL);
    public static final Border BORDER_EMPTY_SMALL = BorderFactory.createEmptyBorder(SPACING_SMALL, SPACING_SMALL, SPACING_SMALL, SPACING_SMALL);
    public static final Border BORDER_EMPTY_MEDIUM = BorderFactory.createEmptyBorder(SPACING_SMALL, SPACING_DEFAULT, SPACING_SMALL, SPACING_DEFAULT);
    public static final Border BORDER_EMPTY_DEFAULT = BorderFactory.createEmptyBorder(SPACING_DEFAULT, SPACING_DEFAULT, SPACING_DEFAULT, SPACING_DEFAULT);
    // UIManager Eigenschaften
    public static final int ARC_DEFAULT = 999;
    public static final String ARROW_TYPE = "chevron";
    // Farben & Schriftart
    private static Color textColor;
    private static Color borderColor;
    private static Color backgroundColor;
    private static Font font;

    /**
     * Privater Konstruktor, um eine Instanziierung dieser Klasse zu unterbinden.
     */
    private UIEnvironment() {
        // Verhindert die Instanziierung dieser Klasse
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

    private static FlatLaf getDarkMode() {
        return new FlatMacDarkLaf();
    }

    private static FlatLaf getLightMode() {
        return new FlatMacLightLaf();
    }

    public static LayoutManager getDefaultBorderLayout() {
        return new BorderLayout(SPACING_DEFAULT, SPACING_DEFAULT);
    }

    public static Desktop getDesktopSecurely() {
        if (!Desktop.isDesktopSupported()) return null;
        return Desktop.getDesktop();
    }

    // ========================================
    // Initialisierungen
    // ========================================
    public static void initPlatformProperties() {
        // macOS Eigenschaften
        if (operationSystem.isMacOS()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", getWord("app.name"));
        }
    }

    public static void initGui() {
        Gui gui = new Gui();
        initDesktopIntegration(gui);
    }

    private static void initDesktopIntegration(Gui gui) {
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
                closeApp(gui);
            });
        }
    }

    public static void initImageIcon(Gui gui) {
        Image scaled = ImageLoader.scaleAppIcon(512, 512);
        Image appImage = (scaled != null) ? new ImageIcon(scaled).getImage() : null;

        // App-Image-Icon (plattformübergreifend) setzen
        gui.getContainer().setIconImage(appImage);
        if (Taskbar.isTaskbarSupported()) {
            try {
                Taskbar.getTaskbar().setIconImage(appImage);
            } catch (Exception e) {
                logger.error("Failed to set the taskbar icon.");
            }
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
        textColor = UIManager.getColor("Label.foreground");
        borderColor = UIManager.getColor("Component.borderColor");
        backgroundColor = UIManager.getColor("TextArea.background");

        Font defaultFont = UIManager.getFont("defaultFont");
        font = defaultFont.deriveFont((float) settings.getFontSize());
        UIManager.put("defaultFont", font);

        // Komponenten-Konfigurationen
        UIManagerConfigurator.setupMisc();
        UIManagerConfigurator.setupComboBox();
        UIManagerConfigurator.setupSpinner();
        UIManagerConfigurator.setupTabbedPane();
        UIManagerConfigurator.setupScrollBar();
        UIManagerConfigurator.setupSplitPane();

        // FlatLaf und Icons aktualisieren
        FlatLaf.updateUI();
        ImageLoader.updateIconThemeColors();
    }

    public static void updateLanguage(Gui gui, Language language) {
        // Wenn sich der Wert nicht ändert, abbrechen
        Language oldLanguage = settings.getLanguage();
        if (!settings.setLanguage(language)) return;
        logger.info("Updating Language from '%s' to '%s' ...".formatted(oldLanguage.getValue(), language.getValue()));

        // UI aktualisieren
        Locale.setDefault(language.getLocale());
        JComponent.setDefaultLocale(language.getLocale());
        gui.rebuild();
    }

    public static void updateTheme(Gui gui, Theme theme) {
        // Wenn sich der Wert nicht ändert, abbrechen
        Theme oldTheme = settings.getTheme();
        if (!settings.setTheme(theme)) return;
        logger.info("Updating Theme from '%s' to '%s' ...".formatted(oldTheme.getValue(), theme.getValue()));

        // FlatLaf und Theme aktualisieren
        updateFlatLaf();
        gui.updateTheme();
    }

    public static void updateFontSize(Gui gui, int fontSize) {
        // Wenn sich der Wert nicht ändert, abbrechen
        int oldFontSize = settings.getFontSize();
        if (!settings.setFontSize(fontSize)) return;
        logger.info("Updating Font Size from '%s' to '%s' ...".formatted(oldFontSize, fontSize));
        font = font.deriveFont((float) fontSize);

        // FlatLaf und Font aktualisieren
        updateFlatLaf();
        gui.updateTheme();
        gui.handleAction(ActionType.ACTION_UPDATE_FONT);
    }

    public static void updateConfirmExit(boolean confirmExit) {
        // Wenn sich der Wert nicht ändert, abbrechen
        boolean oldConfirmExit = settings.isConfirmExit();
        if (!settings.setConfirmExit(confirmExit)) return;
        logger.info("Updating Confirm Exit from '%s' to '%s' ...".formatted(oldConfirmExit, confirmExit));
    }

    public static void updateInitialDirectory(InitialDirectory initialDirectory) {
        InitialDirectory oldInitialDirectory = settings.getInitialDirectory();
        if (!settings.setInitialDirectory(initialDirectory)) return;
        logger.info("Updating Initial Directory from '%s' to '%s' ...".formatted(oldInitialDirectory, initialDirectory));
    }

    public static void updateScreenshotDirectory(Path screenshotDirectory) {
        Path oldScreenshotDirectory = settings.getScreenshotDirectory();
        if (!settings.setScreenshotDirectory(screenshotDirectory)) return;
        logger.info("Updating Screenshot Directory from '%s' to '%s'".formatted(oldScreenshotDirectory, screenshotDirectory));
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    public static void closeApp(Gui gui) {
        if (settings.isConfirmExit()) {
            JCheckBox checkBox = ComponentUtils.createCheckBox(getWord("optionPane.closeApp.checkBox"), null);
            Object[] message = new Object[]{getWord("optionPane.closeApp.question"), checkBox};
            int option = JOptionPane.showConfirmDialog(
                gui.getContainer(),
                message,
                getWord("optionPane.title.confirm"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            // Wenn der Benutzer "Nein" klickt → Abbrechen
            if (option != JOptionPane.YES_OPTION) return;

            // Wenn Checkbox aktiv → Einstellung merken
            if (checkBox.isSelected()) UIEnvironment.updateConfirmExit(false);
        }

        // Konfigurationen und Einstellungen abspeichern
        workspace.saveConfigs();
        settings.save();
        history.save();

        // Anwendung beenden
        System.exit(0);
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

    public static void setCursor(JComponent component, Cursor cursor) {
        if (component == null) return;
        SwingUtilities.invokeLater(() -> component.setCursor(cursor));
    }

    public static void openWebLink(URL url) {
        Desktop desktop = getDesktopSecurely();
        if (desktop == null) return;

        try {
            desktop.browse(url.toURI());
        } catch (Exception e) {
            logger.error("Failed to open the web link '%s'.".formatted(url));
        }
    }
}

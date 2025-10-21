package de.uzk.gui.dialogs;

import de.uzk.logger.LogEntry;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

import static de.uzk.Main.imageFileHandler;
import static de.uzk.Main.logger;
import static de.uzk.config.LanguageHandler.getWord;

public class DialogLogViewer {
    // Minimale Fenstergrößen
    private static final int MIN_WIDTH = 400;
    private static final int MIN_HEIGHT = 250;

    // Maximale „Zielgröße“ des Fensters (nicht zwingend)
    private static final int DEFAULT_MAX_WIDTH = 600;
    private static final int DEFAULT_MAX_HEIGHT = 400;

    private final JDialog dialog;
    private JTabbedPane tabs;

    public DialogLogViewer(JFrame frame) {
        this.dialog = new JDialog(frame, getWord("dialog.logViewer.title"), true);
        this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.dialog.setLayout(new BorderLayout());

        // ESC schließt Dialog
        this.dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public void show() {
        this.dialog.getContentPane().removeAll();

        // Panel mit Tabs hinzufügen
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(this.tabs = getTabs(), BorderLayout.CENTER);
        this.dialog.add(panel);

        // Fenstergröße anpassen
        this.dialog.pack();
        this.resizeWindow();

        // Fenster anzeigen
        this.dialog.setLocationRelativeTo(this.dialog.getOwner());
        this.dialog.setVisible(true);
    }

    private void resizeWindow() {
        // Breite der Scrollbar
        int scrollBarWidth = UIManager.getInt("ScrollBar.width");

        // Bildschirmgröße
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screen.width;
        int screenHeight = screen.height;

        // Aktuelle Fenstergröße
        int currentWidth = dialog.getWidth();
        int currentHeight = dialog.getHeight();

        JScrollPane scrollPane = getScrollPane(this.tabs, this.tabs.getSelectedIndex());
        if (scrollPane != null) {
            currentWidth = currentWidth + scrollPane.getWidth() - scrollPane.getViewport().getView().getWidth();
            currentHeight = currentHeight + scrollPane.getHeight() - scrollPane.getViewport().getView().getHeight();
        }

        // Berechnung der neuen Abmessungen
        int newWidth = Math.min(
                Math.max(currentWidth + scrollBarWidth, MIN_WIDTH),
                Math.min(DEFAULT_MAX_WIDTH, screenWidth - 100)
        );
        int newHeight = Math.min(
                Math.max(currentHeight, MIN_HEIGHT),
                Math.min(DEFAULT_MAX_HEIGHT, screenHeight - 100)
        );

        this.dialog.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        this.dialog.setSize(new Dimension(newWidth, newHeight));
    }

    private JTabbedPane getTabs() {
        JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.add(getWord("dialog.logViewer.logs"), getLogsPanel());
        if (imageFileHandler.getMissingImagesCount() > 0) {
            tabs.add(getWord("dialog.logViewer.missingImages"), getMissingImagesPanel());
        }
        return tabs;
    }

    private JComponent getLogsPanel() {
        StringBuilder logContent = new StringBuilder();
        for (LogEntry logEntry : logger.getLogs()) {
            logContent.append(formatLogEntry(logEntry));
        }
        return getEditorPane(StringUtils.wrapHtmlDocument(logContent.toString()));
    }

    private JComponent getMissingImagesPanel() {
        String missingImages = StringUtils.wrapPre(imageFileHandler.getMissingImages());
        return getEditorPane(StringUtils.wrapHtmlDocument(missingImages));
    }

    private String formatLogEntry(LogEntry logEntry) {
        return logEntry.getFormattedText(true);
    }

    private JComponent getEditorPane(String htmlContent) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JEditorPane editorPane = new JEditorPane();
        editorPane.setMargin(new Insets(5, 5, 5, 5));
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        editorPane.setText(htmlContent);

        JScrollPane scrollPane = new JScrollPane(editorPane);
        panel.add(scrollPane);
        return panel;
    }

    private JScrollPane getScrollPane(JTabbedPane tabbedPane, int tabIndex) {
        if (tabIndex < 0 || tabIndex >= tabbedPane.getTabCount()) return null;

        JComponent component = (JComponent) tabbedPane.getComponentAt(tabIndex);
        if (component instanceof JPanel panel) {
            Component[] panelComponents = panel.getComponents();
            if (panelComponents.length == 1 && panelComponents[0] instanceof JScrollPane scrollPane) {
                return scrollPane;
            }
        }
        return null;
    }
}

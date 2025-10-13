package de.uzk.gui;

import de.uzk.logger.LogEntry;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static de.uzk.Main.imageHandler;
import static de.uzk.Main.logger;
import static de.uzk.config.LanguageHandler.getWord;

public class DialogLogViewer {
    private final JDialog dialog;
    private final JTabbedPane tabs;

    public DialogLogViewer(JFrame frame) {
        this.dialog = new JDialog(frame, getWord("dialog.logViewer"), true);
        this.dialog.setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        this.tabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        loadDialogTabs();
        panel.add(tabs);
        this.dialog.add(panel);
    }

    public void show() {
        tabs.removeAll();
        loadDialogTabs();

        resizeWindow();
        this.dialog.setLocationRelativeTo(this.dialog.getOwner());
        this.dialog.setVisible(true);
    }

    private void resizeWindow() {
        this.dialog.pack();

        // Breite der Scrollbar
        int scrollBarWidth = UIManager.getInt("ScrollBar.width");
        if (scrollBarWidth <= 0) scrollBarWidth = 20;

        // Minimale Fenstergrößen
        final int MIN_WIDTH = 300;
        final int MIN_HEIGHT = 200;

        // Maximale „Zielgröße“ des Fensters (nicht zwingend)
        final int DEFAULT_MAX_WIDTH = 800;
        final int DEFAULT_MAX_HEIGHT = 600;

        // Bildschirmgröße
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screen.width;
        int screenHeight = screen.height;

        // Aktuelle Fenstergröße
        int currentWidth = dialog.getWidth();
        int currentHeight = dialog.getHeight();

        // Berechnung der neuen Abmessungen
        int newWidth = Math.min(
                Math.max(currentWidth + scrollBarWidth, MIN_WIDTH),
                Math.min(DEFAULT_MAX_WIDTH, screenWidth - 100)
        );
        int newHeight = Math.min(
                Math.max(currentHeight, MIN_HEIGHT),
                Math.min(DEFAULT_MAX_HEIGHT, screenHeight - 100)
        );

        dialog.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        dialog.setSize(new Dimension(newWidth, newHeight));
    }

    private void loadDialogTabs() {
        tabs.add(getWord("dialog.logViewer.logs"), getLogsPanel());
        if (imageHandler.getMissingImagesCount() > 0) {
            tabs.add(getWord("dialog.logViewer.missingImages"), getMissingImagesPanel());
        }
    }

    private JComponent getLogsPanel() {
        StringBuilder logContent = new StringBuilder();
        for (LogEntry logEntry : logger.getLogs()) {
            logContent.append(formatLogEntry(logEntry));
        }
        return getEditorPane(StringUtils.wrapHtmlDocument(logContent.toString()));
    }

    private JComponent getMissingImagesPanel() {
        String missingImages = StringUtils.wrapP(imageHandler.getAllMissingImages());
        return getEditorPane(StringUtils.wrapHtmlDocument(missingImages));
    }

    private String formatLogEntry(LogEntry logEntry) {
        String header = StringUtils.wrapBold(logEntry.getDateTime()) + " " + logEntry.getSource() + StringUtils.NEXT_LINE;
        String level = StringUtils.wrapBold(StringUtils.wrapColor("[" + logEntry.getLevel() + "]: ", logEntry.getLevel().getColor()));
        String message = logEntry.getMessage();

        return StringUtils.wrapP(header + level + " " + message);
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
}

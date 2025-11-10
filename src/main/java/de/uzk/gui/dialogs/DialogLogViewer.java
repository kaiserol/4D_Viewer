package de.uzk.gui.dialogs;

import de.uzk.gui.GuiUtils;
import de.uzk.gui.SelectableText;
import de.uzk.logger.LogEntry;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import static de.uzk.Main.logger;
import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

public class DialogLogViewer {
    // GUI-Elemente
    private final JDialog dialog;
    private JTabbedPane tabs;

    // Minimale Fenstergrößen
    private static final int MIN_WIDTH = 400;
    private static final int MIN_HEIGHT = 250;

    // Maximale „Zielgröße“ des Fensters (nicht zwingend)
    private static final int DEFAULT_MAX_WIDTH = 600;
    private static final int DEFAULT_MAX_HEIGHT = 400;

    public DialogLogViewer(JFrame frame) {
        this.dialog = new JDialog(frame, getWord("dialog.logViewer"), true);
        this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // ESC schließt Dialog
        this.dialog.getRootPane().registerKeyboardAction(e -> this.dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public void show() {
        this.dialog.getContentPane().removeAll();
        this.dialog.setLayout(new BorderLayout());

        // Tabs hinzufügen
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(GuiUtils.BORDER_PADDING_LARGE);
        panel.add(this.tabs = getTabs(), BorderLayout.CENTER);
        this.dialog.add(panel);

        // Dialog anzeigen
        this.dialog.pack();
        this.resizeWindow();
        this.dialog.setLocationRelativeTo(this.dialog.getOwner());
        this.dialog.setVisible(true);
    }

    private void resizeWindow() {
        // Abmessungen des Bildschirms ermitteln
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screen.width;
        int screenHeight = screen.height;

        // Abmessungen des Dialogs ermitteln
        int dialogWidth = dialog.getWidth();
        int dialogHeight = dialog.getHeight();

        JScrollPane scrollPane = getScrollPane(this.tabs, this.tabs.getSelectedIndex());
        if (scrollPane != null) {
            dialogWidth = dialogWidth + scrollPane.getWidth() - scrollPane.getViewport().getView().getWidth();
            dialogHeight = dialogHeight + scrollPane.getHeight() - scrollPane.getViewport().getView().getHeight();
        }

        // Breite der Scrollbar ermitteln
        int scrollBarWidth = UIManager.getInt("ScrollBar.width");

        // Neue Abmessungen berechnen
        int newWidth = Math.min(
            Math.max(dialogWidth + scrollBarWidth, MIN_WIDTH),
            Math.min(DEFAULT_MAX_WIDTH, screenWidth - 100)
        );
        int newHeight = Math.min(
            Math.max(dialogHeight, MIN_HEIGHT),
            Math.min(DEFAULT_MAX_HEIGHT, screenHeight - 100)
        );

        // Neue Abmessungen setzen
        this.dialog.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        this.dialog.setSize(new Dimension(newWidth, newHeight));
    }

    private JTabbedPane getTabs() {
        JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

        // Tabs hinzufügen
        tabs.add(getWord("dialog.logViewer.logs"), getLogsPanel());
        if (workspace.isOpen()) {
            tabs.add(getWord("dialog.logViewer.imagesReport"), getMissingImagesPanel());
        }
        return tabs;
    }

    private JComponent getLogsPanel() {
        StringBuilder logContent = new StringBuilder();
        for (LogEntry logEntry : logger.getLogs()) {
            logContent.append(logEntry.getFormattedText(true));
        }
        return getScrollableText(StringUtils.wrapHtml(logContent.toString(), "monospaced"));
    }

    private JComponent getMissingImagesPanel() {
        String missingImages = StringUtils.wrapPre(workspace.getMissingImagesReport());
        return getScrollableText(StringUtils.wrapHtml(missingImages, "monospaced"));
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    private JComponent getScrollableText(String htmlContent) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Text in ScrollPane packen
        SelectableText text = new SelectableText(htmlContent);
        text.setMargin(GuiUtils.INSETS_DEFAULT_SMALL);

        JScrollPane scrollPane = new JScrollPane(text);
        panel.add(scrollPane);
        return panel;
    }

    private JScrollPane getScrollPane(JTabbedPane tabbedPane, int tabIndex) {
        if (tabIndex < 0 || tabIndex >= tabbedPane.getTabCount()) return null;

        // ScrollPane ermitteln
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

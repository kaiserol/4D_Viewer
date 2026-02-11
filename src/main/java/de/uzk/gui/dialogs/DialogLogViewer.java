package de.uzk.gui.dialogs;

import de.uzk.gui.SelectableText;
import de.uzk.gui.UIEnvironment;
import de.uzk.logger.LogLevel;
import de.uzk.utils.ComponentUtils;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static de.uzk.Main.logger;
import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

public class DialogLogViewer {
    // Minimale Fenstergrößen
    private static final int MIN_WIDTH = 400;
    private static final int MIN_HEIGHT = 250;
    // Maximale „Zielgröße“ des Fensters (nicht zwingend)
    private static final int DEFAULT_MAX_WIDTH = 600;
    private static final int DEFAULT_MAX_HEIGHT = 400;
    // Dialoge
    private final JDialog dialog;
    private final Map<LogLevel, JCheckBox> filterCheckboxes;
    // Gui Elemente
    private JTabbedPane tabs;

    public DialogLogViewer(Window parentWindow) {
        dialog = ComponentUtils.createDialog(parentWindow, null);

        // Map initialisieren
        filterCheckboxes = new LinkedHashMap<>();
    }

    public void show() {
        dialog.setTitle(getWord("dialog.logViewer"));
        dialog.getContentPane().removeAll();
        dialog.setLayout(new BorderLayout());

        // Inhalte hinzufügen
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(UIEnvironment.BORDER_EMPTY_DEFAULT);
        contentPanel.add(tabs = createTabs(), BorderLayout.CENTER);

        dialog.add(contentPanel);

        // Dialog anzeigen
        dialog.pack();
        resizeWindow();
        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.setVisible(true);
    }

    // ========================================
    // Komponenten-Erzeugung
    // ========================================
    private JTabbedPane createTabs() {
        JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

        // Blockierte Protokollebenen festlegen
        List<LogLevel> blockedLevels = new ArrayList<>(Arrays.stream(LogLevel.values()).filter(l -> l != LogLevel.INFO).toList());

        // Eingestellte Protokollebenen ausblenden
        for (LogLevel level : filterCheckboxes.keySet()) {
            JCheckBox checkBox = filterCheckboxes.get(level);
            if (checkBox.isSelected()) blockedLevels.remove(level);
            else blockedLevels.add(level);
        }

        // Tabs hinzufügen
        tabs.addTab(getWord("dialog.logViewer.tab.log"), createLogsPanel(blockedLevels.toArray(new LogLevel[0])));

        if (workspace.isLoaded()) {
            tabs.addTab(getWord("dialog.logViewer.tab.imagesReport"), createMissingImagesPanel());
        }
        return tabs;
    }

    private JPanel createLogsPanel(LogLevel... blockedLevels) {
        JPanel scrollPaneWrapper = createTextInScrollPane(logger.exportHtml(blockedLevels));

        // Kontrollkästchen für Protokollebenen hinzufügen
        JPanel logLevelsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logLevelsPanel.add(new JLabel(getWord("label.showLogLevels") + ": "));
        addLogLevelCheckboxes(scrollPaneWrapper, logLevelsPanel, blockedLevels);
        scrollPaneWrapper.add(logLevelsPanel, BorderLayout.SOUTH);

        return scrollPaneWrapper;
    }

    private void addLogLevelCheckboxes(JPanel scrollPaneWrapper, JPanel logLevelsPanel, LogLevel... blockedLevels) {
        for (LogLevel level : LogLevel.sortedValues()) {
            boolean blocked = blockedLevels != null && Arrays.stream(blockedLevels).anyMatch(l -> l == level);
            JCheckBox checkBox = new JCheckBox(level.getName(), !blocked);
            checkBox.addActionListener(e -> refreshLogsPanel(scrollPaneWrapper));

            if (level == LogLevel.DEBUG) {
                SwingUtilities.invokeLater(() -> checkBox.setSelected(false));
            }

            logLevelsPanel.add(checkBox);
            filterCheckboxes.put(level, checkBox);
        }
    }

    private JPanel createMissingImagesPanel() {
        return createTextInScrollPane(StringUtils.wrapPre(workspace.getMissingImagesReport()));
    }

    // ==================================================
    // Refresh-Mechanik
    // ==================================================
    private void refreshLogsPanel(JPanel scrollPaneWrapper) {
        JScrollPane scrollPane = ComponentUtils.findFirstComponentRecursively(JScrollPane.class, scrollPaneWrapper);
        SelectableText logsText = ComponentUtils.findFirstComponentRecursively(SelectableText.class, scrollPaneWrapper);
        if (scrollPane == null || logsText == null) return;

        List<LogLevel> blockedLevels = new ArrayList<>();
        for (Map.Entry<LogLevel, JCheckBox> entry : filterCheckboxes.entrySet()) {
            if (!entry.getValue().isSelected()) {
                blockedLevels.add(entry.getKey());
            }
        }

        // Text aktualisieren
        String htmlContent = logger.exportHtml(blockedLevels.toArray(new LogLevel[0]));
        logsText.setText(StringUtils.wrapHtml(htmlContent, "monospaced"));

        // Bildlaufleiste aktualisieren
        SwingUtilities.invokeLater(() -> {
            scrollPane.getVerticalScrollBar().setValue(0);
            scrollPane.getHorizontalScrollBar().setValue(0);
        });
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    private JPanel createTextInScrollPane(String htmlContent) {
        SelectableText text = new SelectableText(StringUtils.wrapHtml(htmlContent, "monospaced"));
        text.setMargin(UIEnvironment.INSETS_SMALL);

        // Text in ScrollPane packen
        JScrollPane scrollPane = new JScrollPane(text);

        // ScrollPane in Panel packen
        JPanel panel = new JPanel(UIEnvironment.getDefaultBorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private void resizeWindow() {
        // Abmessungen des Bildschirms ermitteln
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screen.width;
        int screenHeight = screen.height;

        // Abmessungen des Dialogs ermitteln
        int dialogWidth = dialog.getWidth();
        int dialogHeight = dialog.getHeight();

        // ScrollPane ermitteln
        JComponent selectedTab = (JComponent) tabs.getComponentAt(tabs.getSelectedIndex());
        JScrollPane scrollPane = ComponentUtils.findFirstComponentRecursively(JScrollPane.class, selectedTab);
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
        dialog.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        dialog.setSize(new Dimension(newWidth, newHeight));
    }
}

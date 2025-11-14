package de.uzk.gui.dialogs;

import de.uzk.gui.GuiUtils;
import de.uzk.gui.SelectableText;
import de.uzk.io.Icons;
import de.uzk.utils.ComponentUtils;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static de.uzk.Main.settings;
import static de.uzk.config.LanguageHandler.getWord;

public class DialogAbout {
    // Dialoge
    private final JDialog dialog;

    // Konstanten
    private static final int APP_ICON_SIZE = 150;

    public DialogAbout(JFrame frame) {
        this.dialog = new JDialog(frame, true);
        this.dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // ESC schließt Dialog
        this.dialog.getRootPane().registerKeyboardAction(e -> this.dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public void show() {
        this.dialog.setTitle(getWord("dialog.about").formatted(getWord("app.name")));
        this.dialog.getContentPane().removeAll();
        this.dialog.setLayout(new BorderLayout());

        // Inhalte hinzufügen
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(GuiUtils.BORDER_EMPTY_DEFAULT);
        contentPanel.add(createAboutPanel(), BorderLayout.CENTER);

        this.dialog.add(contentPanel, BorderLayout.CENTER);

        // Dialog anzeigen
        this.dialog.pack();
        this.dialog.setResizable(false);
        this.dialog.setLocationRelativeTo(this.dialog.getOwner());
        this.dialog.setVisible(true);
    }

    // ========================================
    // Komponenten-Erzeugung
    // ========================================
    private JPanel createAboutPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = ComponentUtils.createGridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = GuiUtils.INSETS_DEFAULT;

        // App-Icon
        ImageIcon appIcon = getAppIcon();
        JLabel iconLabel = new JLabel(appIcon);
        panel.add(iconLabel, gbc);

        // Textblock
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        // Titel
        String title = getWord("app.name") + " " + getWord("app.version");
        textPanel.add(new SelectableText(
            StringUtils.wrapHtml(StringUtils.applyFontSize(StringUtils.wrapBold(title), 200))
        ));
        textPanel.add(Box.createVerticalStrut(20));

        // Build, Java Version
        String formattedDate = formatDate(getWord("app.buildDate"));
        String versionText = getWord("dialog.about.buildOn").formatted(formattedDate) + "<br>" +
            getWord("dialog.about.build").formatted(getWord("app.build")) + "<br>" +
            getWord("dialog.about.usedRuntime").formatted(getWord("app.runtime"));
        textPanel.add(new SelectableText(StringUtils.wrapHtml(versionText)));
        textPanel.add(Box.createVerticalStrut(20));

        // Mitwirkende, Git-Link, Copyright
        String copyRightText = getWord("dialog.about.poweredBy").formatted(getWord("app.poweredBy")) + "<br>" +
            getWord("dialog.about.developedBy").formatted(getWord("app.authors")) + "<br>" +
            getWord("dialog.about.sourceCode").formatted(getWord("app.gitLink")) + "<br><br>" +
            getWord("dialog.about.copyright").formatted(getWord("app.copyrightYear") + " " + getWord("app.vendor")) + "<br>" +
            getWord("dialog.about.licensedBy").formatted(getWord("app.license"));
        textPanel.add(new SelectableText(
            StringUtils.wrapHtml(StringUtils.wrapLinks(copyRightText))
        ));

        gbc.gridx = 1;
        panel.add(textPanel, gbc);

        return panel;
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    private static ImageIcon getAppIcon() {
        Image scaled = Icons.APP_IMAGE.getScaledInstance(APP_ICON_SIZE, APP_ICON_SIZE, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private static String formatDate(String dateStr) {
        try {
            Date date = new SimpleDateFormat("dd.MM.yyyy").parse(dateStr);
            String dateFormat = settings.getLanguage().isGerman() ? "dd. MMMM yyyy" : "MMMM dd, yyyy";
            return new SimpleDateFormat(dateFormat).format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }
}

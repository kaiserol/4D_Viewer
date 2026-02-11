package de.uzk.gui.dialogs;

import de.uzk.gui.SelectableText;
import de.uzk.gui.UIEnvironment;
import de.uzk.io.ImageLoader;
import de.uzk.utils.ComponentUtils;
import de.uzk.utils.DateTimeUtils;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.awt.*;

import static de.uzk.config.LanguageHandler.getWord;

public class DialogAbout {
    // Dialoge
    private final JDialog dialog;

    public DialogAbout(Window parentWindow) {
        dialog = ComponentUtils.createDialog(parentWindow, null);
    }

    public void show() {
        dialog.setTitle(getWord("dialog.about").formatted(getWord("app.name")));
        dialog.getContentPane().removeAll();
        dialog.setLayout(new BorderLayout());

        // Inhalte hinzufügen
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(UIEnvironment.BORDER_EMPTY_DEFAULT);
        contentPanel.add(createAboutPanel(), BorderLayout.CENTER);

        dialog.add(contentPanel, BorderLayout.CENTER);

        // Dialog anzeigen
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.setVisible(true);
    }

    // ========================================
    // Komponenten-Erzeugung
    // ========================================
    private JPanel createAboutPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = ComponentUtils.createGridBagConstraints();
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = UIEnvironment.INSETS_DEFAULT;

        Image scaled = ImageLoader.scaleAppIcon(128, 128);
        ImageIcon appIcon = (scaled != null) ? new ImageIcon(scaled) : null;

        // App-Image-Icon setzen
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
        String formattedDate = DateTimeUtils.parseAndReformatDate(getWord("app.buildDate"));
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
}

package de.uzk.gui.areas;

import javax.swing.*;

import static de.uzk.config.LanguageHandler.getWord;

public class AreaRightsOfUseBanner extends JPanel {
    public AreaRightsOfUseBanner() {
        add(new JLabel(getWord("dialog.legal.text.usage")));
    }
}

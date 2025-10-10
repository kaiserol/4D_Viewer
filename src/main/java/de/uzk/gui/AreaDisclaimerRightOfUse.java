package de.uzk.gui;

import javax.swing.*;
import java.awt.*;

import static de.uzk.config.LanguageHandler.getWord;

public class AreaDisclaimerRightOfUse extends AreaContainerInteractive<JPanel> {
    public AreaDisclaimerRightOfUse(Gui gui) {
        super(new JPanel(), gui);
        init();
    }

    private void init() {
        this.container.setLayout(new BorderLayout());

        JLabel useRightLabel = new JLabel(getWord("dialog.disclaimer.rightOfUse"));
        useRightLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.container.add(useRightLabel);
    }
}

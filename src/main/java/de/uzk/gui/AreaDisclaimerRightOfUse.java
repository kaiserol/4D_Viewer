package de.uzk.gui;

import de.uzk.utils.StringUtils;

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

        String rightOfUse = getWord("dialog.disclaimer.rightOfUse");
        String htmlContent = StringUtils.wrapHtml(StringUtils.applyDivAlignment(rightOfUse, "center"));
        SelectableText text = new SelectableText(htmlContent);
        this.container.add(text);
    }
}

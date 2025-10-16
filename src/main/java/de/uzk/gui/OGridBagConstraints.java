package de.uzk.gui;

import java.awt.*;

// TODO: Klasse löschen?
public class OGridBagConstraints extends GridBagConstraints {
    public OGridBagConstraints() {
        this(new Insets(0, 0, 0, 0), CENTER, NONE);
    }

    public OGridBagConstraints(Insets insets, int anchor, int fill) {
        super(0, 0, 1, 1, 0, 0, anchor, fill, insets, 0, 0);
    }

    public void setPos(int gridx, int gridy) {
        this.gridx = gridx;
        this.gridy = gridy;
    }

    public void setInsets(int top, int left, int bottom, int right) {
        if (insets == null) {
            insets = new Insets(top, left, bottom, right);
        } else {
            insets.set(top, left, bottom, right);
        }
    }

    public void setPosAndInsets(int gridx, int gridy, int topPadding, int leftPadding, int bottomPadding, int rightPadding) {
        setPos(gridx, gridy);
        setInsets(topPadding, leftPadding, bottomPadding, rightPadding);
    }

    public void setSizeAndWeight(int gridwidth, int gridheight, double weightx, double weighty) {
        setHorizontal(gridwidth, weightx);
        setVertical(gridheight, weighty);
    }

    public void setHorizontal(int gridwidth, double weightx) {
        this.gridwidth = gridwidth;
        this.weightx = weightx;
    }

    public void setVertical(int gridheight, double weighty) {
        this.gridheight = gridheight;
        this.weighty = weighty;
    }
}

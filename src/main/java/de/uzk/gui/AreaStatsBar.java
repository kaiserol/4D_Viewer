package de.uzk.gui;

import de.uzk.image.Axis;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import java.awt.*;

import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

public class AreaStatsBar extends AreaContainerInteractive<JPanel> {
    private JLabel labelTime;
    private JLabel labelLevel;
    private JLabel labelTimeLevel;

    public AreaStatsBar(Gui gui) {
        super(new JPanel(), gui);
        init();
    }

    private void init() {
        this.container.setLayout(new BorderLayout());
        this.container.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Zeit-Label
        this.labelTime = new JLabel("", SwingConstants.CENTER);
        this.container.add(this.labelTime, BorderLayout.CENTER);

        // Zeit-Level-Panel
        JPanel panelTimeLevel = new JPanel(new BorderLayout(10, 0));
        panelTimeLevel.setOpaque(false);

        // Zeit-Level-Label
        this.labelTimeLevel = new JLabel("", SwingConstants.LEFT);
        this.labelTimeLevel.setOpaque(true);
        panelTimeLevel.add(this.labelTimeLevel, BorderLayout.WEST);

        // Ebenen-Label
        this.labelLevel = new JLabel("", SwingConstants.RIGHT);
        panelTimeLevel.add(this.labelLevel, BorderLayout.EAST);
        this.container.add(panelTimeLevel, BorderLayout.SOUTH);
    }

    @Override
    public void toggleOn() {
        updateTime();
        updateLevel();
        updateTimeLevel();
    }

    @Override
    public void toggleOff() {
        updateTime();
        updateLevel();
        updateTimeLevel();
    }

    @Override
    public void update(Axis axis) {
        switch (axis) {
            case TIME -> updateTime();
            case LEVEL -> updateLevel();
        }
        updateTimeLevel();
    }

    @Override
    public void updateTheme() {
        Color backgroundColor = GuiUtils.getBackgroundColor();
        this.container.setBackground(backgroundColor);
        this.labelTimeLevel.setBackground(backgroundColor);
    }

    private void updateTime() {
        int time =  (int) (workspace.getTime() * workspace.getConfig().getTimeUnit());
        int seconds = time % 60;
        int minute = time / 60 % 60;
        int hour = time / 60 / 60;

        String timeString = String.format("%02d:%02d:%02d", hour, minute, seconds);
        this.labelTime.setText(StringUtils.wrapHtml(StringUtils.wrapBold(StringUtils.applyFontSize(
                timeString, 175))));
    }

    private void updateLevel() {
        double level =  (int) (workspace.getLevel() * workspace.getConfig().getLevelUnit());
        String levelString = String.format("%.01f μm", level);
        this.labelLevel.setText(StringUtils.wrapHtml(StringUtils.applyFontSize(
                levelString, 75)));
    }

    private void updateTimeLevel() {
        int time = workspace.getTime();
        int level =  workspace.getLevel();
        String timeString = String.format("%s: %d", getWord("items.nav.axis.time"), time);
        String levelString = String.format("%s: %d", getWord("items.nav.axis.level"), level);
        this.labelTimeLevel.setText(StringUtils.wrapHtml(StringUtils.wrapItalic(StringUtils.applyFontSize(
                timeString + " / " + levelString, 75))));
    }
}

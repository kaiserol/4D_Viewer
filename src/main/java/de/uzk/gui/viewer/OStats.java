package de.uzk.gui.viewer;

import de.uzk.action.ActionType;
import de.uzk.gui.AreaContainerInteractive;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.image.ImageFile;
import de.uzk.image.Axis;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;

import static de.uzk.Main.imageFileHandler;
import static de.uzk.config.LanguageHandler.getWord;

// TODO: Überarbeite Klasse
public class OStats extends AreaContainerInteractive<JPanel> {
    private JLabel timeStateLabel;
    private JLabel levelStateLabel;
    private JLabel timeCountLabel;
    private JLabel levelCountLabel;
    private JLabel currentImageTextLabel;

    public OStats(Gui gui) {
        super(new JPanel(), gui);
        init();
    }

    private void init() {
        this.container.setLayout(new GridBagLayout());

        // GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 5);

        // timeLabel
        JLabel timeLabel = new JLabel(getWord("viewer.labels.image") + ":");
        this.container.add(timeLabel, gbc);

        // gbc
        gbc.gridy++;

        // timeLabel
        JLabel levelLabel = new JLabel(getWord("viewer.labels.level") + ":");
        this.container.add(levelLabel, gbc);

        // gbc
        gbc.gridx++;

        // levelValueLabel
        levelCountLabel = new JLabel();
        this.container.add(levelCountLabel, gbc);

        // gbc
        gbc.gridy = 0;

        // timeLabel
        timeCountLabel = new JLabel();
        this.container.add(timeCountLabel, gbc);

        // gbc
        gbc.gridx++;
        gbc.gridheight = 2;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;

        // timePanel
        JPanel timePanel = new JPanel(new BorderLayout());

        // timeState
        this.timeStateLabel = new JLabel();
        this.timeStateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timePanel.add(this.timeStateLabel, BorderLayout.CENTER);

        // currentFileLabel
        this.currentImageTextLabel = new JLabel();
        this.currentImageTextLabel.setBorder(new EmptyBorder(0, 5, 0, 5));
        this.currentImageTextLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timePanel.add(this.currentImageTextLabel, BorderLayout.SOUTH);

        this.container.add(timePanel, gbc);

        // gbc
        gbc.gridx++;
        gbc.gridy++;
        gbc.gridheight = 1;
        gbc.weighty = 0;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.SOUTHEAST;
        gbc.insets = new Insets(0, 0, 0, 0);

        // levelState
        this.levelStateLabel = new JLabel();
        this.levelStateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        this.container.add(this.levelStateLabel, gbc);
    }

    @Override
    public void handleAction(ActionType actionType) {
        if (actionType == ActionType.ACTION_UPDATE_TIME_UNIT) {
            updateStats(Axis.TIME);
        } else if (actionType == ActionType.ACTION_UPDATE_LEVEL_UNIT) {
            updateStats(Axis.LEVEL);
        }
    }

    @Override
    public void toggleOn() {
        updateCountLabel(Axis.TIME, imageFileHandler.getTime(), imageFileHandler.getMaxTime());
        updateStateLabel(Axis.TIME, 0, 0);

        updateCountLabel(Axis.LEVEL, imageFileHandler.getLevel(), imageFileHandler.getMaxLevel());
        updateStateLabel(Axis.LEVEL, 0, 0);
        updateCurrentImageText();
    }

    @Override
    public void toggleOff() {
        updateCountLabel(Axis.TIME, 0, 0);
        updateStateLabel(Axis.TIME, 0, 0);

        updateCountLabel(Axis.LEVEL, 0, 0);
        updateStateLabel(Axis.LEVEL, 0, 0);
        updateCurrentImageText();
    }

    @Override
    public void update(Axis axis) {
        updateCurrentImageText();
        updateStats(axis);
    }

    @Override
    public void updateTheme() {
        this.container.setBorder(new CompoundBorder(new MatteBorder(0, 0, 2, 0, GuiUtils.getBorderColor()),
                new EmptyBorder(5, 5, 5, 5)));
    }

    private void updateStats(Axis axis) {
        if (axis == Axis.TIME) {
            updateCountLabel(Axis.TIME, imageFileHandler.getTime(), imageFileHandler.getMaxTime());
            updateStateLabel(Axis.TIME, imageFileHandler.getTime(), imageFileHandler.getShiftTimeUnit());
        } else {
            updateCountLabel(Axis.LEVEL, imageFileHandler.getLevel(), imageFileHandler.getMaxLevel());
            updateStateLabel(Axis.LEVEL, imageFileHandler.getLevel(), imageFileHandler.getShiftLevelUnit());
        }
    }

    private void updateCountLabel(Axis axis, int value, int maxValue) {
        if (axis == Axis.TIME) timeCountLabel.setText(value + " / " + maxValue);
        else levelCountLabel.setText(value + " / " + maxValue);
    }

    private void updateStateLabel(Axis axis, int value, Number multiplier) {
        if (axis == Axis.TIME) {
            timeStateLabel.setText(StringUtils.wrapHtml(
                    "<h1 style='margin: 0; padding: 0;'>" + StringUtils.formatTime(value, multiplier.doubleValue()) + "</h1>"));
        } else {
            levelStateLabel.setText(StringUtils.wrapHtml(StringUtils.wrapBold(
                    StringUtils.formatLevel(value, multiplier.doubleValue()))));
        }
    }

    private void updateCurrentImageText() {
        ImageFile imageFile = imageFileHandler.getImageFile();
        if (imageFile == null) this.currentImageTextLabel.setText(null);
        else this.currentImageTextLabel.setText(imageFile.getName());
    }
}

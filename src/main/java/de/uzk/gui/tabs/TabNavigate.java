package de.uzk.gui.tabs;

import de.uzk.action.ActionType;
import de.uzk.config.Config;
import de.uzk.edit.LevelUnitEdit;
import de.uzk.edit.TimeUnitEdit;
import de.uzk.gui.Gui;
import de.uzk.gui.observer.ObserverContainer;
import de.uzk.image.Axis;
import de.uzk.utils.ComponentUtils;

import javax.swing.*;
import java.awt.*;

import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

// TODO: Überarbeite Klasse
public class TabNavigate extends ObserverContainer<JPanel> {
    // Gui Elemente
    private JSpinner timeUnitSpinner;
    private JSpinner levelUnitSpinner;

    public TabNavigate(Gui gui) {
        super(new JPanel(), gui);
        init();
    }

    private void init() {
        this.container.setLayout(new BorderLayout()); //Um die Inputs ganz oben platzieren zu können

        JPanel editorPanel = new JPanel(new GridBagLayout());

        // GridBagConstraints
        GridBagConstraints gbc = ComponentUtils.createGridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 5, 15);

        // imageLabel
        JLabel imageLabel = new JLabel(getWord("menu.nav.time.layer") + ":");
        editorPanel.add(imageLabel, gbc);

        // gbc
        gbc.gridy++;

        // timeSpinner
        SpinnerNumberModel timeSpinnerModel = new SpinnerNumberModel(30, Config.MIN_TIME_UNIT, Config.MAX_TIME_UNIT, 0.1);
        this.timeUnitSpinner = getUnitSpinner(timeSpinnerModel, Axis.TIME);
        editorPanel.add(timeUnitSpinner, gbc);

        // gbc
        gbc.gridx++;
        gbc.weightx = 1;

        // imageUnitLabel
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel imageUnitLabel = new JLabel(getWord("menu.nav.time.unit"));
        editorPanel.add(imageUnitLabel, gbc);

        // gbc
        gbc.gridy++;
        gbc.gridx--;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        // levelLabel
        JLabel levelLabel = new JLabel(getWord("menu.nav.level.layer") + ":");
        editorPanel.add(levelLabel, gbc);

        // gbc
        gbc.gridy++;
        gbc.gridheight = 200;
        // timeSpinner
        SpinnerNumberModel levelSpinnerModel = new SpinnerNumberModel(1, Config.MIN_LEVEL_UNIT, Config.MAX_LEVEL_UNIT, 0.1);
        this.levelUnitSpinner = getUnitSpinner(levelSpinnerModel, Axis.LEVEL);
        editorPanel.add(levelUnitSpinner, gbc);

        // gbc
        gbc.gridx++;
        gbc.weightx = 1;

        // timeUnitLabel
        gbc.anchor = GridBagConstraints.CENTER;
        JLabel levelUnitLabel = new JLabel(getWord("menu.nav.level.unit"));
        editorPanel.add(levelUnitLabel, gbc);

        this.container.add(editorPanel, BorderLayout.NORTH);
    }


    public JSpinner getUnitSpinner(SpinnerNumberModel spinnerModel, Axis axis) {
        JSpinner spinner = new JSpinner(spinnerModel);

        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0.0");
        spinner.setEditor(editor);

        spinner.addChangeListener(e -> {
            if (spinner.isEnabled()) {
                updateUnitValue(spinner, axis);
            }
        });
        return spinner;
    }

    private void updateUnitValue(JSpinner spinner, Axis axis) {
        Number value = (Number) spinner.getValue();
        boolean success = switch (axis) {
            case TIME -> workspace.getEditManager().performEdit(new TimeUnitEdit(value.doubleValue()));
            case LEVEL -> workspace.getEditManager().performEdit(new LevelUnitEdit(value.doubleValue()));
        };
        if(success) {
            gui.handleAction(ActionType.ACTION_UPDATE_UNIT);
        }
    }

    @Override
    public void toggleOn() {
        ComponentUtils.setEnabled(this.container, true);
        this.timeUnitSpinner.setValue(workspace.getConfig().getTimeUnit());
        this.levelUnitSpinner.setValue(workspace.getConfig().getLevelUnit());
    }

    @Override
    public void toggleOff() {
        ComponentUtils.setEnabled(this.container, false);
        this.timeUnitSpinner.setValue(workspace.getConfig().getTimeUnit());
        this.levelUnitSpinner.setValue(workspace.getConfig().getLevelUnit());
    }

    @Override
    public void handleAction(ActionType actionType) {
        if(actionType == ActionType.ACTION_UPDATE_UNIT) {
            timeUnitSpinner.setValue(workspace.getConfig().getTimeUnit());
            levelUnitSpinner.setValue(workspace.getConfig().getLevelUnit());
        }
    }

}

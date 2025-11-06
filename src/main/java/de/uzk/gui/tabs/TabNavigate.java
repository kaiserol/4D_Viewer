package de.uzk.gui.tabs;

import de.uzk.config.Config;
import de.uzk.gui.areas.AreaContainerInteractive;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.gui.OGridBagConstraints;
import de.uzk.image.Axis;

import javax.swing.*;
import java.awt.*;

import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

// TODO: Überarbeite Klasse
public class TabNavigate extends AreaContainerInteractive<JPanel> {
    // GUI-Elemente
    private JSlider timeSlider;
    private JSlider levelSlider;
    private JSpinner timeUnitSpinner;
    private JSpinner levelUnitSpinner;

    public TabNavigate(Gui gui) {
        super(new JPanel(), gui);
        init();
    }

    private void init() {
        this.container.setLayout(new GridBagLayout());

        // GridBagConstraints
        OGridBagConstraints gbc = new OGridBagConstraints(new Insets(0, 0, 5, 15), GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);

        // imageLabel
        JLabel imageLabel = new JLabel(getWord("items.nav.axis.time.layer") + ":");
        this.container.add(imageLabel, gbc);

        // gbc
        gbc.setPosAndInsets(0, 1, 0, 0, 0, 15);

        // timeSpinner
        SpinnerNumberModel timeSpinnerModel = new SpinnerNumberModel(30, 1, Config.MAX_TIME_UNIT, 0.1);
        this.timeUnitSpinner = getUnitSpinner(timeSpinnerModel, Axis.TIME);
        this.container.add(timeUnitSpinner, gbc);

        // gbc
        gbc.setPosAndInsets(1, 1, 0, 0, 0, 0);
        gbc.weightx = 1;

        // imageUnitLabel
        JLabel imageUnitLabel = new JLabel(getWord("items.nav.axis.time.unit"));
        this.container.add(imageUnitLabel, gbc);

        // gbc
        gbc.setPosAndInsets(1, 2, 10, 0, 0, 0);

        // timeSlider
        this.timeSlider = getSlider(Axis.TIME);
        // TODO: Diese Slider tun zur Zeit nichts
        // this.container.add(this.timeSlider, gbc);

        // gbc
        gbc.setPosAndInsets(0, 2, 15, 0, 5, 15);
        gbc.weightx = 0;

        // levelLabel
        JLabel levelLabel = new JLabel(getWord("items.nav.axis.level.layer") + ":");
        this.container.add(levelLabel, gbc);

        // gbc
        gbc.setPosAndInsets(0, 3, 0, 0, 0, 15);

        // timeSpinner
        SpinnerNumberModel levelSpinnerModel = new SpinnerNumberModel(1, 0.1, Config.MAX_LEVEL_UNIT, 0.1);
        this.levelUnitSpinner = getUnitSpinner(levelSpinnerModel, Axis.LEVEL);
        this.container.add(levelUnitSpinner, gbc);

        // gbc
        gbc.setPosAndInsets(1, 3, 0, 0, 0, 0);
        gbc.weightx = 1;

        // timeUnitLabel
        JLabel levelUnitLabel = new JLabel(getWord("items.nav.axis.level.unit"));
        this.container.add(levelUnitLabel, gbc);

        // gbc
        gbc.setPosAndInsets(1, 5, 10, 0, 0, 0);
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // levelSlider
        this.levelSlider = getSlider(Axis.LEVEL);
        // TODO: Diese Slider tun zur Zeit nichts
        // this.container.add(this.levelSlider, gbc);
    }

    public JSpinner getUnitSpinner(SpinnerNumberModel spinnerModel, Axis axis) {
        JSpinner spinner = new JSpinner(spinnerModel);

        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0.0");
        spinner.setEditor(editor);

        Number number = switch (axis) {
            case TIME -> workspace.getConfig().getTimeUnit();
            case LEVEL -> workspace.getConfig().getLevelUnit();
        };
        if (GuiUtils.valueFitsInRange(number, spinnerModel)) spinner.setValue(number);
        else updateUnitValue(spinner, axis);


        spinner.addChangeListener(e -> {
            if (spinner.isEnabled()) {
                updateUnitValue(spinner, axis);
            }
        });
        return spinner;
    }

    private void updateUnitValue(JSpinner spinner, Axis axis) {
        Number value = (Number) spinner.getValue();
        switch (axis) {
            case TIME -> workspace.getConfig().setTimeUnit(value.doubleValue());
            case LEVEL -> workspace.getConfig().setLevelUnit(value.doubleValue());
        }
    }

    private JSlider getSlider(Axis axis) {
        JSlider slider = new JSlider(SwingConstants.HORIZONTAL);

        slider.addChangeListener(e -> {
            if (slider.isEnabled()) {
                int value = slider.getValue();
                boolean isAdjusting = slider.getValueIsAdjusting();
                update(slider, axis, value, isAdjusting);
            }
        });
        slider.setPaintLabels(true);
        slider.setSnapToTicks(true);
        return slider;
    }

    private void update(JSlider slider, Axis axis, int newValue, boolean isAdjusting) {


        int oldValue = axis == Axis.TIME ? workspace.getTime() : workspace.getLevel();
        if (oldValue == newValue) return;

        if (!isAdjusting) {
            // sets the slider to the oldValue
            updateSliderValueSecretly(slider, oldValue);
            return;
        }

//        if (axis == Axis.TIME) imageFileHandler.setTime(newValue);
//        else imageFileHandler.setLevel(newValue);
        gui.update(axis);
    }

    @Override
    public void toggleOn() {
        GuiUtils.setEnabled(this.container, true);
        updateSliderValuesSecretly(timeSlider, workspace.getTime(), workspace.getMaxTime());
        updateSliderValuesSecretly(levelSlider, workspace.getLevel(), workspace.getMaxLevel());
        this.timeUnitSpinner.setValue(workspace.getConfig().getTimeUnit());
        this.levelUnitSpinner.setValue(workspace.getConfig().getLevelUnit());
    }

    @Override
    public void toggleOff() {
        GuiUtils.setEnabled(this.container, false);
        updateSliderValuesSecretly(timeSlider, 0, 0);
        updateSliderValuesSecretly(levelSlider, 0, 0);
        this.timeUnitSpinner.setValue(workspace.getConfig().getTimeUnit());
        this.levelUnitSpinner.setValue(workspace.getConfig().getLevelUnit());
    }

    @Override
    public void update(Axis axis) {
        if (axis == Axis.TIME) {
            // sets only a new value to timeSlider if the slider is not moving
            if (!timeSlider.getValueIsAdjusting()) updateSliderValueSecretly(timeSlider, workspace.getTime());
        } else {
            // sets only a new value to levelSlider if the slider is not moving
            if (!levelSlider.getValueIsAdjusting()) updateSliderValueSecretly(levelSlider, workspace.getLevel());
        }
    }

    private void updateSliderValueSecretly(JSlider slider, int value) {
//        GuiUtils.updateSecretly(slider, () -> slider.setValue(value));
    }

    private void updateSliderValuesSecretly(JSlider slider, int value, int max) {
//        GuiUtils.updateSecretly(slider, () -> {
//            slider.setMaximum(max);
//            slider.setMinimum(0);
//            slider.setValue(value);
//            slider.setMinorTickSpacing(1);
//            slider.setMajorTickSpacing(max);
//        });
    }
}

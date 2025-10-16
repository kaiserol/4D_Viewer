package de.uzk.gui.tabs;

import de.uzk.action.ActionHandler;
import de.uzk.action.ActionType;
import de.uzk.gui.*;
import de.uzk.image.Axis;
import de.uzk.gui.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Dictionary;
import java.util.Hashtable;

import static de.uzk.Main.imageFileHandler;
import static de.uzk.config.LanguageHandler.getWord;
import static de.uzk.gui.GuiUtils.COLOR_BLUE;

// TODO: Überarbeite Klasse
public class TabNavigate extends AreaContainerInteractive<JPanel> {
    private final ActionHandler actionHandler;
    private JSlider timeSlider;
    private JSlider levelSlider;

    public TabNavigate(Gui gui, ActionHandler actionHandler) {
        super(new JPanel(), gui);
        this.actionHandler = actionHandler;
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
        SpinnerNumberModel timeSpinnerModel = new SpinnerNumberModel(30, 1, 600, 0.1);
        JSpinner timeUnitSpinner = getUnitSpinner(timeSpinnerModel, Axis.TIME);
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
        this.container.add(this.timeSlider, gbc);

        // gbc
        gbc.setPosAndInsets(0, 3, 15, 0, 5, 15);
        gbc.weightx = 0;

        // levelLabel
        JLabel levelLabel = new JLabel(getWord("items.nav.axis.level.layer") + ":");
        this.container.add(levelLabel, gbc);

        // gbc
        gbc.setPosAndInsets(0, 4, 0, 0, 0, 15);

        // timeSpinner
        SpinnerNumberModel levelSpinnerModel = new SpinnerNumberModel(1, 0.1, 1000, 0.1);
        JSpinner levelUnitSpinner = getUnitSpinner(levelSpinnerModel, Axis.LEVEL);
        this.container.add(levelUnitSpinner, gbc);

        // gbc
        gbc.setPosAndInsets(1, 4, 0, 0, 0, 0);
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
        this.container.add(this.levelSlider, gbc);
    }

    public JSpinner getUnitSpinner(SpinnerNumberModel spinnerModel, Axis axis) {
        JSpinner spinner = new JSpinner(spinnerModel);

        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0.00");
        spinner.setEditor(editor);

        boolean isTime = axis == Axis.TIME;
        Number number = isTime ? imageFileHandler.getShiftTimeUnit() : imageFileHandler.getShiftLevelUnit();
        if (GuiUtils.valueFitsInRange(number, spinnerModel)) spinner.setValue(number);
        else updateUnitValue(spinner, isTime);

        spinner.addChangeListener(e -> {
            if (GuiUtils.isEnabled(spinner)) {
                updateUnitValue(spinner, isTime);
                if (isTime) gui.handleAction(ActionType.ACTION_UPDATE_TIME_UNIT);
                else gui.handleAction(ActionType.ACTION_UPDATE_LEVEL_UNIT);
            }
        });
        return spinner;
    }

    private void updateUnitValue(JSpinner spinner, boolean isTime) {
        Number value = (Number) spinner.getValue();
        if (isTime) imageFileHandler.setShiftTimeUnit(value.doubleValue());
        else imageFileHandler.setShiftLevelUnit(value.doubleValue());
    }

    private JSlider getSlider(Axis axis) {
        JSlider slider = new JSlider(SwingConstants.HORIZONTAL);

        boolean isShift = axis == Axis.TIME;
        slider.addChangeListener(e -> {
            if (GuiUtils.isEnabled(slider)) {
                int value = slider.getValue();
                boolean isAdjusting = slider.getValueIsAdjusting();
                update(slider, axis, value, isAdjusting);
            }
        });
        slider.addMouseWheelListener(e -> actionHandler.mouseWheelMoved(isShift, e.getWheelRotation()));
        slider.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) actionHandler.mouseWheelMoved(isShift, 1);
                else if (e.getKeyCode() == KeyEvent.VK_LEFT) actionHandler.mouseWheelMoved(isShift, -1);
            }
        });
        slider.setPaintLabels(true);
        slider.setSnapToTicks(true);
        return slider;
    }

    private void update(JSlider slider, Axis axis, int newValue, boolean isAdjusting) {
        if (imageFileHandler.isEmpty()) return;

        int oldValue = axis == Axis.TIME ? imageFileHandler.getTime() : imageFileHandler.getLevel();
        if (oldValue == newValue) return;

        if (!isAdjusting) {
            // sets the slider to the oldValue
            updateSliderValueSecretly(slider, oldValue);
            return;
        }

        if (axis == Axis.TIME) imageFileHandler.setTime(newValue);
        else imageFileHandler.setLevel(newValue);
        gui.update(axis);
    }

    @Override
    public void handleAction(ActionType actionType) {
        if (actionType == ActionType.ACTION_UPDATE_PIN_TIME) {
            updateSliderLabels(Axis.TIME);
        }
    }

    @Override
    public void toggleOn() {
        GuiUtils.setEnabled(this.container, true);
        updateSliderValuesSecretly(timeSlider, imageFileHandler.getTime(), imageFileHandler.getMaxTime());
        updateSliderValuesSecretly(levelSlider, imageFileHandler.getLevel(), imageFileHandler.getMaxLevel());
        updateSliderLabels(Axis.TIME);
        updateSliderLabels(Axis.LEVEL);
    }

    @Override
    public void toggleOff() {
        GuiUtils.setEnabled(this.container, false);
        updateSliderValuesSecretly(timeSlider, 0, 0);
        updateSliderValuesSecretly(levelSlider, 0, 0);
        updateSliderLabels(Axis.TIME);
        updateSliderLabels(Axis.LEVEL);
    }

    @Override
    public void update(Axis axis) {
        if (axis == Axis.TIME) {
            // sets only a new value to timeSlider if the slider is not moving
            if (!timeSlider.getValueIsAdjusting()) updateSliderValueSecretly(timeSlider, imageFileHandler.getTime());
            updateSliderLabels(Axis.LEVEL);
        } else {
            // sets only a new value to levelSlider if the slider is not moving
            if (!levelSlider.getValueIsAdjusting()) updateSliderValueSecretly(levelSlider, imageFileHandler.getLevel());
            updateSliderLabels(Axis.TIME);
        }
    }

    private void updateSliderValueSecretly(JSlider slider, int value) {
        GuiUtils.updateSecretly(slider, () -> slider.setValue(value));
    }

    private void updateSliderValuesSecretly(JSlider slider, int value, int max) {
        GuiUtils.updateSecretly(slider, () -> {
            slider.setMaximum(max);
            slider.setMinimum(0);
            slider.setValue(value);
        });
    }

    private void updateSliderLabels(Axis axis) {
        JSlider slider = (axis == Axis.LEVEL) ? levelSlider : timeSlider;
        Dictionary<Integer, JLabel> dictionary = new Hashtable<>();

        // update slider labels
        if (!imageFileHandler.isEmpty() && axis != null) updateDictionary(axis, dictionary);
        else dictionary.put(0, new JLabel("0"));

        slider.setLabelTable(dictionary);
    }

    private void updateDictionary(Axis axis, Dictionary<Integer, JLabel> dictionary) {
        int searchValue = (axis == Axis.LEVEL) ? imageFileHandler.getTime() : imageFileHandler.getLevel();
        int max = (axis == Axis.LEVEL) ? imageFileHandler.getMaxLevel() : imageFileHandler.getMaxTime();

        // Create a dictionary of labels and positions to mark specific values
//        List<Integer> missingNumbers = (axis == Axis.TIME) ? imageHandler.getMissingTimes(searchValue) : imageHandler.getMissingLevels(searchValue);

//        for (int missingNum : missingNumbers) {
//            JLabel missingNumLabel = new JLabel(String.valueOf(missingNum));
//            missingNumLabel.setForeground(Color.RED);
//            missingNumLabel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
//            dictionary.put(missingNum, missingNumLabel);
//        }

        // sets labels for the beginning and ending
        final int pinTime = imageFileHandler.getPinTime();
        if (axis == Axis.TIME && pinTime != -1) {
            JLabel missingNumLabel = new JLabel(String.valueOf(pinTime));
            missingNumLabel.setForeground(COLOR_BLUE);
            missingNumLabel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
            dictionary.put(pinTime, missingNumLabel);
        }
        if (dictionary.get(0) == null) dictionary.put(0, new JLabel("0"));
        if (dictionary.get(max) == null) dictionary.put(max, new JLabel(String.valueOf(max)));
    }
}

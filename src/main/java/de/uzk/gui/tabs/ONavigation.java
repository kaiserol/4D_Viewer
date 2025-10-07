package de.uzk.gui.tabs;

import de.uzk.actions.ActionType;
import de.uzk.actions.ActionTypeListener;
import de.uzk.gui.*;
import de.uzk.actions.ActionHandler;
import de.uzk.image.ImageLayer;
import de.uzk.gui.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import static de.uzk.Main.imageHandler;
import static de.uzk.gui.GuiUtils.FOCUS_COLOR;
import static de.uzk.config.LanguageHandler.getWord;

public class ONavigation extends OTabContent implements ActionTypeListener {
    private final ActionHandler actionHandler;
    private JSlider timeSlider;
    private JSlider levelSlider;

    public ONavigation(Gui gui, ActionHandler actionHandler) {
        super(new JPanel(), gui);
        this.actionHandler = actionHandler;
        gui.addActionTypeListener(this);
        init();
    }

    private void init() {
        this.container.setLayout(new GridBagLayout());

        // GridBagConstraints
        OGridBagConstraints gbc = new OGridBagConstraints(
                new Insets(0, 0, 5, 15),
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);

        // imageLabel
        JLabel imageLabel = new JLabel(getWord("items.nav.image.layer") + ":");
        this.container.add(imageLabel, gbc);

        // gbc
        gbc.setPosAndInsets(0, 1, 0, 0, 0, 15);

        // timeSpinner
        OSpinnerNumberModel timeSpinnerModel = new OSpinnerNumberModel(30, 1, 600, 0.01, true);
        JSpinner timeUnitSpinner = getUnitSpinner(timeSpinnerModel, ImageLayer.TIME);
        this.container.add(timeUnitSpinner, gbc);

        // gbc
        gbc.setPosAndInsets(1, 1, 0, 0, 0, 0);
        gbc.weightx = 1;

        // imageUnitLabel
        JLabel imageUnitLabel = new JLabel(getWord("items.nav.image.unit"));
        this.container.add(imageUnitLabel, gbc);

        // gbc
        gbc.setPosAndInsets(1, 2, 10, 0, 0, 0);

        // timeSlider
        this.timeSlider = getSlider(ImageLayer.TIME);
        this.container.add(this.timeSlider, gbc);

        // gbc
        gbc.setPosAndInsets(0, 3, 15, 0, 5, 15);
        gbc.weightx = 0;

        // levelLabel
        JLabel levelLabel = new JLabel(getWord("items.nav.level.layer") + ":");
        this.container.add(levelLabel, gbc);

        // gbc
        gbc.setPosAndInsets(0, 4, 0, 0, 0, 15);

        // timeSpinner
        OSpinnerNumberModel levelSpinnerModel = new OSpinnerNumberModel(1, 0.1, 100, 0.01, true);
        JSpinner levelUnitSpinner = getUnitSpinner(levelSpinnerModel, ImageLayer.LEVEL);
        this.container.add(levelUnitSpinner, gbc);

        // gbc
        gbc.setPosAndInsets(1, 4, 0, 0, 0, 0);
        gbc.weightx = 1;

        // timeUnitLabel
        JLabel levelUnitLabel = new JLabel(getWord("items.nav.level.unit"));
        this.container.add(levelUnitLabel, gbc);

        // gbc
        gbc.setPosAndInsets(1, 5, 10, 0, 0, 0);
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        // levelSlider
        this.levelSlider = getSlider(ImageLayer.LEVEL);
        this.container.add(this.levelSlider, gbc);
    }

    public JSpinner getUnitSpinner(OSpinnerNumberModel spinnerModel, ImageLayer layer) {
        JSpinner spinner = new JSpinner(spinnerModel);

        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0.00");
        spinner.setEditor(editor);

        boolean isTime = layer == ImageLayer.TIME;
        Number number = isTime ? imageHandler.getTimeUnit() : imageHandler.getLevelUnit();
        if (GuiUtils.valueFitsInRange(number, spinnerModel)) spinner.setValue(number);
        else updateUnitValue(spinner, isTime);

        spinner.addChangeListener(e -> {
            if (GuiUtils.isEnabled(spinner)) {
                updateUnitValue(spinner, isTime);
                if (isTime) gui.handleAction(ActionType.UPDATE_TIME_UNIT);
                else gui.handleAction(ActionType.UPDATE_LEVEL_UNIT);
            }
        });
        return spinner;
    }

    private void updateUnitValue(JSpinner spinner, boolean isTime) {
        Number value = (Number) spinner.getValue();
        if (isTime) imageHandler.setTimeUnit(value.doubleValue());
        else imageHandler.setLevelUnit(value.doubleValue());
    }

    private JSlider getSlider(ImageLayer layer) {
        JSlider slider = new JSlider(SwingConstants.HORIZONTAL);

        boolean isShift = layer == ImageLayer.TIME;
        slider.addChangeListener(e -> {
            if (GuiUtils.isEnabled(slider)) {
                int value = slider.getValue();
                boolean isAdjusting = slider.getValueIsAdjusting();
                update(slider, layer, value, isAdjusting);
            }
        });
        slider.addMouseWheelListener(e -> actionHandler.mouseWheelMoved(isShift, e.getWheelRotation(), false));
        slider.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) actionHandler.mouseWheelMoved(isShift, 1, true);
                else if (e.getKeyCode() == KeyEvent.VK_LEFT) actionHandler.mouseWheelMoved(isShift, -1, true);
            }
        });
        slider.setPaintLabels(true);
        slider.setSnapToTicks(true);
        return slider;
    }

    private void update(JSlider slider, ImageLayer layer, int newValue, boolean isAdjusting) {
        if (imageHandler.isEmpty()) return;

        int oldValue = layer == ImageLayer.TIME ? imageHandler.getTime() : imageHandler.getLevel();
        if (oldValue == newValue) return;

        if (!isAdjusting) {
            // sets the slider to the oldValue
            updateSliderValueSecretly(slider, oldValue);
            return;
        }

        if (layer == ImageLayer.TIME) imageHandler.setTime(newValue, false);
        else imageHandler.setLevel(newValue, false);
        gui.update(layer);
    }

    @Override
    public void toggleOn() {
        GuiUtils.setEnabled(this.container, true);

        updateSliderValuesSecretly(timeSlider, imageHandler.getTime(), imageHandler.getMaxTime());
        updateSliderValuesSecretly(levelSlider, imageHandler.getLevel(), imageHandler.getMaxLevel());
        updateSliderLabels(ImageLayer.TIME);
        updateSliderLabels(ImageLayer.LEVEL);
    }

    @Override
    public void toggleOff() {
        GuiUtils.setEnabled(this.container, false);
        updateSliderValuesSecretly(timeSlider, 0, 0);
        updateSliderValuesSecretly(levelSlider, 0, 0);
        updateSliderLabels(ImageLayer.TIME);
        updateSliderLabels(ImageLayer.LEVEL);
    }

    @Override
    public void update(ImageLayer layer) {
        if (layer == ImageLayer.TIME) {
            // sets only a new value to timeSlider if the slider is not moving
            if (!timeSlider.getValueIsAdjusting())
                updateSliderValueSecretly(timeSlider, imageHandler.getTime());
            updateSliderLabels(ImageLayer.LEVEL);
        } else {
            // sets only a new value to levelSlider if the slider is not moving
            if (!levelSlider.getValueIsAdjusting())
                updateSliderValueSecretly(levelSlider, imageHandler.getLevel());
            updateSliderLabels(ImageLayer.TIME);
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

    private void updateSliderLabels(ImageLayer layer) {
        JSlider slider = (layer == ImageLayer.LEVEL) ? levelSlider : timeSlider;
        Dictionary<Integer, JLabel> dictionary = new Hashtable<>();



        // update slider labels
        if (!imageHandler.isEmpty() && layer != null) updateDictionary(layer, dictionary);
        else dictionary.put(0, new JLabel("0"));

        slider.setLabelTable(dictionary);
    }

    private void updateDictionary(ImageLayer layer, Dictionary<Integer, JLabel> dictionary) {
        int searchValue = (layer == ImageLayer.LEVEL) ? imageHandler.getTime() : imageHandler.getLevel();
        int max = (layer == ImageLayer.LEVEL) ? imageHandler.getMaxLevel() : imageHandler.getMaxTime();

        // Create a dictionary of labels and positions to mark specific values
        List<Integer> missingNumbers = (layer == ImageLayer.TIME) ? imageHandler.getMissingTimes(searchValue) : imageHandler.getMissingLevels(searchValue);

        for (int missingNum : missingNumbers) {
            JLabel missingNumLabel = new JLabel(String.valueOf(missingNum));
            missingNumLabel.setForeground(Color.RED);
            missingNumLabel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
            GuiUtils.updateFontSize(missingNumLabel, -3, Font.BOLD);
            dictionary.put(missingNum, missingNumLabel);
        }

        // sets labels for the beginning and ending
        final int pinTime = imageHandler.getPinTime();
        if (layer == ImageLayer.TIME && pinTime != -1) {
            JLabel missingNumLabel = new JLabel(String.valueOf(pinTime));
            missingNumLabel.setForeground(FOCUS_COLOR);
            missingNumLabel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
            GuiUtils.updateFontSize(missingNumLabel, 0, Font.BOLD);
            dictionary.put(pinTime, missingNumLabel);
        }
        if (dictionary.get(0) == null) dictionary.put(0, new JLabel("0"));
        if (dictionary.get(max) == null) dictionary.put(max, new JLabel(String.valueOf(max)));
    }

    @Override
    public void handleAction(ActionType actionType) {
        if (actionType == ActionType.UPDATE_PIN_TIME) {
            updateSliderLabels(ImageLayer.TIME);
        }
    }
}

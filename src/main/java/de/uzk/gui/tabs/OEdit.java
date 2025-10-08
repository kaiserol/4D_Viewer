package de.uzk.gui.tabs;

import de.uzk.actions.ActionType;
import de.uzk.actions.ActionTypeListener;
import de.uzk.gui.*;
import de.uzk.actions.ActionHandler;
import de.uzk.gui.GuiUtils;
import de.uzk.utils.NumberUtils;

import javax.swing.*;
import java.awt.*;

import static de.uzk.Main.config;
import static de.uzk.Main.imageHandler;
import static de.uzk.actions.ActionUtils.ACTION_SCREENSHOT;
import static de.uzk.config.LanguageHandler.getWord;

public class OEdit extends OTabContent implements ActionTypeListener, WindowFocusListener {
    private final ActionHandler actionHandler;
    private JSpinner degreeSpinner;
    private JLabel screenshots;

    public OEdit(Gui gui, ActionHandler actionHandler) {
        super(new JPanel(), gui);
        this.actionHandler = actionHandler;
        gui.addActionTypeListener(this);
        gui.addWindowFocusListener(this);
        init();
    }

    private void init() {
        this.container.setLayout(new GridBagLayout());

        // GridBagConstraints
        OGridBagConstraints gbc = new OGridBagConstraints(
                new Insets(0, 0, 5, 15),
                GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
        gbc.setHorizontal(2, 0);

        // mirrorXBox
        JCheckBox mirrorXBox = new JCheckBox(getWord("items.edit.mirrorHor"));
        initCheckBox(mirrorXBox, true);
        this.container.add(mirrorXBox, gbc);

        // gbc
        gbc.setPos(0, 1);

        // mirrorYBox
        JCheckBox mirrorYBox = new JCheckBox(getWord("items.edit.mirrorVert"));
        initCheckBox(mirrorYBox, false);
        this.container.add(mirrorYBox, gbc);

        // gbc
        gbc.setPosAndInsets(0, 2, 0, 0, 10, 15);

        // Create a SpinnerModel for numeric values
        OSpinnerNumberModel degreeSpinnerModel = new OSpinnerNumberModel(0, 0, 359, 1, true);
        this.degreeSpinner = getDegreeSpinner(degreeSpinnerModel);
        this.container.add(this.degreeSpinner, gbc);

        // gbc
        gbc.setPosAndInsets(2, 2, 0, 0, 10, 0);
        gbc.setHorizontal(1,1);

        // degreeLabel
        JLabel degreeLabel = new JLabel(getWord("items.edit.rotateImage"));
        this.container.add(degreeLabel, gbc);

        // gbc
        gbc.setPosAndInsets(0, 3, 0, 0, 10, 10);
        gbc.setSizeAndWeight(1, 1, 0, 1);
        gbc.anchor = GridBagConstraints.SOUTHWEST;

        // screenshotNumberLabel
        JLabel screenshotNumberLabel = new JLabel(getWord("items.edit.screenshotNumber") + ":");
        this.container.add(screenshotNumberLabel, gbc);

        // gbc
        gbc.setPosAndInsets(1, 3, 0, 0, 10, 15);

        // screenshotLabel
        this.screenshots = new JLabel();
        this.container.add(this.screenshots, gbc);
        updateScreenshotCounter();

        // gbc
        gbc.setPosAndInsets(0, 4, 0,0,0,0);
        gbc.setSizeAndWeight(3, 1, 1, 0);

        // screenshotButton
        JButton screenshotButton = new JButton(getWord("items.edit.screenshot"));
        screenshotButton.addActionListener(a -> actionHandler.executeEdit(ACTION_SCREENSHOT));
        this.container.add(screenshotButton, gbc);
    }

    private void initCheckBox(JCheckBox checkBox, boolean isMirrorXBox) {
        boolean startValue = isMirrorXBox ? imageHandler.getImageDetails().isMirrorX() : imageHandler.getImageDetails().isMirrorY();
        checkBox.setSelected(startValue);
        checkBox.addItemListener(e -> {
            if (GuiUtils.isEnabled(checkBox)) {
                if (isMirrorXBox) imageHandler.getImageDetails().setMirrorX(checkBox.isSelected());
                else imageHandler.getImageDetails().setMirrorY(checkBox.isSelected());
                gui.handleAction(ActionType.EDIT_IMAGE);
            }
        });
    }

    public JSpinner getDegreeSpinner(OSpinnerNumberModel spinnerModel) {
        JSpinner spinner = new JSpinner(spinnerModel);

        Number rotation = imageHandler.getImageDetails().getRotation();
        if (GuiUtils.valueFitsInRange(rotation, spinnerModel)) spinner.setValue(rotation);
        else setRotationInImageHandler(spinner);

        spinner.addChangeListener(e -> {
            if (GuiUtils.isEnabled(spinner)) {
                setRotationInImageHandler(spinner);
                gui.handleAction(ActionType.EDIT_IMAGE);
            }
        });
        return spinner;
    }

    private void setRotationInImageHandler(JSpinner spinner) {
        Number value = (Number) spinner.getValue();
        imageHandler.getImageDetails().setRotation(value.intValue());
    }

    @Override
    public void toggleOn() {
        GuiUtils.setEnabled(this.container, true);
    }

    @Override
    public void toggleOff() {
        GuiUtils.setEnabled(this.container, false);
    }

    @Override
    public void handleAction(ActionType actionType) {
        if (actionType == ActionType.TURN_IMAGE_90_LEFT) {
            degreeSpinner.setValue(NumberUtils.turn90Left(imageHandler.getImageDetails().getRotation()));
        } else if (actionType == ActionType.TURN_IMAGE_90_RIGHT) {
            degreeSpinner.setValue(NumberUtils.turn90Right(imageHandler.getImageDetails().getRotation()));
        } else if (actionType == ActionType.UPDATE_SCREENSHOT_COUNTER) {
            updateScreenshotCounter();
        }
    }

    private void updateScreenshotCounter() {
        this.screenshots.setText(String.valueOf(config.getScreenshots()));
    }

    @Override
    public void gainedWindowFocus() {
        updateScreenshotCounter();
    }
}
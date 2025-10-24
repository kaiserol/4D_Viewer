package de.uzk.gui.tabs;

import de.uzk.action.ActionHandler;
import de.uzk.action.ActionType;
import de.uzk.config.Config;
import de.uzk.utils.ScreenshotHelper;
import de.uzk.gui.*;
import de.uzk.utils.NumberUtils;

import javax.swing.*;
import java.awt.*;

import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

// TODO: Überarbeite Klasse
public class TabEdit extends AreaContainerInteractive<JPanel> {
    private final ActionHandler actionHandler;
    private JSpinner degreeSpinner;
    private JLabel screenshots;

    public TabEdit(Gui gui, ActionHandler actionHandler) {
        super(new JPanel(), gui);
        this.actionHandler = actionHandler;
        init();
    }

    private void init() {
        this.container.setLayout(new GridBagLayout());

        // GridBagConstraints
        OGridBagConstraints gbc = new OGridBagConstraints(new Insets(0, 0, 5, 15), GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
        gbc.setHorizontal(2, 0);

        // mirrorXBox
        JCheckBox mirrorXBox = new JCheckBox(getWord("items.edit.mirrorHor"));
        mirrorXBox.setFocusable(false);
        initCheckBox(mirrorXBox, true);
        this.container.add(mirrorXBox, gbc);

        // gbc
        gbc.setPos(0, 1);

        // mirrorYBox
        JCheckBox mirrorYBox = new JCheckBox(getWord("items.edit.mirrorVert"));
        mirrorYBox.setFocusable(false);
        initCheckBox(mirrorYBox, false);
        this.container.add(mirrorYBox, gbc);

        // gbc
        gbc.setPosAndInsets(0, 2, 0, 0, 10, 15);

        // Create a SpinnerModel for numeric values
        CyclingSpinnerNumberModel degreeSpinnerModel = new CyclingSpinnerNumberModel(0, 0, Config.MAX_ROTATION, 1);
        this.degreeSpinner = getDegreeSpinner(degreeSpinnerModel);
        this.container.add(this.degreeSpinner, gbc);

        // gbc
        gbc.setPosAndInsets(2, 2, 0, 0, 10, 0);
        gbc.setHorizontal(1, 1);

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

        // gbc
        gbc.setPosAndInsets(0, 4, 0, 0, 0, 0);
        gbc.setSizeAndWeight(3, 1, 1, 0);

        // screenshotButton
        JButton screenshotButton = new JButton(getWord("items.edit.takeScreenshot"));
        screenshotButton.addActionListener(e -> actionHandler.executeAction(ActionType.SHORTCUT_TAKE_SCREENSHOT));
        this.container.add(screenshotButton, gbc);
    }

    private void initCheckBox(JCheckBox checkBox, boolean isMirrorXBox) {
        boolean startValue =  (isMirrorXBox ? workspace.getConfig().isMirrorX() : workspace.getConfig().isMirrorY());
        checkBox.setSelected(startValue);
        checkBox.addItemListener(e -> {
//            if (GuiUtils.isEnabled(checkBox)) {
//                if (isMirrorXBox) imageFileHandler.setImageMirrorX(checkBox.isSelected());
//                else imageFileHandler.setImageMirrorY(checkBox.isSelected());
//                gui.handleAction(ActionType.ACTION_EDIT_IMAGE);
//            }
        });
    }

    public JSpinner getDegreeSpinner(SpinnerNumberModel spinnerModel) {
        JSpinner spinner = new JSpinner(spinnerModel);

        Number rotation = workspace.getConfig().getRotation();
        if (GuiUtils.valueFitsInRange(rotation, spinnerModel)) spinner.setValue(rotation);
        else setRotationInImageHandler(spinner);

        spinner.addChangeListener(e -> {
//            if (GuiUtils.isEnabled(spinner)) {
//                setRotationInImageHandler(spinner);
//                gui.handleAction(ActionType.ACTION_EDIT_IMAGE);
//            }
        });
        return spinner;
    }

    private void setRotationInImageHandler(JSpinner spinner) {
        Number value = (Number) spinner.getValue();
        workspace.getConfig().setRotation(value.intValue());
    }

    @Override
    public void handleAction(ActionType actionType) {
        if (actionType == ActionType.SHORTCUT_TURN_IMAGE_90_LEFT) {
            degreeSpinner.setValue(NumberUtils.turn90Left(workspace.getConfig().getRotation()));
        } else if (actionType == ActionType.SHORTCUT_TURN_IMAGE_90_RIGHT) {
            degreeSpinner.setValue(NumberUtils.turn90Right(workspace.getConfig().getRotation()));
        } else if (actionType == ActionType.ACTION_UPDATE_SCREENSHOT_COUNTER) {
            updateScreenshotCounter();
        }
    }

    @Override
    public void toggleOn() {
        GuiUtils.setEnabled(this.container, true);
        updateScreenshotCounter();
    }

    @Override
    public void toggleOff() {
        GuiUtils.setEnabled(this.container, false);
        updateScreenshotCounter();
    }

    @Override
    public void appGainedFocus() {
        updateScreenshotCounter();
    }

    private void updateScreenshotCounter() {
        this.screenshots.setText(String.valueOf(ScreenshotHelper.getScreenshotCount()));
    }
}

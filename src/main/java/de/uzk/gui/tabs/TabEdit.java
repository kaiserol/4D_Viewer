package de.uzk.gui.tabs;

import de.uzk.action.ActionType;
import de.uzk.config.Config;
import de.uzk.gui.*;
import de.uzk.utils.NumberUtils;
import de.uzk.utils.ScreenshotHelper;

import javax.swing.*;
import java.awt.*;

import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

// TODO: Überarbeite Klasse
public class TabEdit extends AreaContainerInteractive<JPanel> {
    // GUI-Elemente
    private JSpinner degreeSpinner;
    private JSpinner zoomSpinner;
    private JLabel screenshots;
    private JCheckBox mirrorXBox;
    private JCheckBox mirrorYBox;


    public TabEdit(Gui gui) {
        super(new JPanel(), gui);
        init();
    }

    private void init() {
        this.container.setLayout(new GridBagLayout());

        // GridBagConstraints
        OGridBagConstraints gbc = new OGridBagConstraints(new Insets(0, 0, 5, 15), GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL);
        gbc.setHorizontal(2, 0);

        // mirrorXBox
        this.mirrorXBox = new JCheckBox(getWord("items.edit.mirrorHor"));
        mirrorXBox.setFocusable(false);
        initCheckBox(mirrorXBox, true);
        this.container.add(mirrorXBox, gbc);

        // gbc
        gbc.setPos(0, 1);

        // mirrorYBox
        this.mirrorYBox = new JCheckBox(getWord("items.edit.mirrorVert"));
        mirrorYBox.setFocusable(false);
        initCheckBox(mirrorYBox, false);
        this.container.add(mirrorYBox, gbc);

        // gbc
        gbc.setPosAndInsets(0, 2, 0, 0, 0, 15);

        // Create a SpinnerModel for numeric values
        CyclingSpinnerNumberModel degreeSpinnerModel = new CyclingSpinnerNumberModel(0, 0, Config.MAX_ROTATION, 1);
        this.degreeSpinner = getDegreeSpinner(degreeSpinnerModel);
        this.container.add(this.degreeSpinner, gbc);

        // gbc
        gbc.setPosAndInsets(2, 2, 0, 0, 0, 0);

        // degreeLabel
        JLabel degreeLabel = new JLabel(getWord("items.edit.rotateImage"));
        this.container.add(degreeLabel, gbc);

        gbc.anchor = GridBagConstraints.FIRST_LINE_END;
        gbc.setPosAndInsets(0, 3, 0, 0, 0, 15);
        SpinnerNumberModel zoomSpinnerModel = new SpinnerNumberModel(workspace.getConfig().getZoom(), 50, 500, 10);
        this.zoomSpinner = new JSpinner(zoomSpinnerModel);
        this.zoomSpinner.addChangeListener(e -> {
            if(zoomSpinner.isEnabled()) {
                int newValue = zoomSpinnerModel.getNumber().intValue();
                if (newValue != workspace.getConfig().getZoom()) {
                    workspace.getConfig().setZoom(newValue);
                    gui.handleAction(ActionType.ACTION_EDIT_IMAGE);
                }
            }
        });
        this.container.add(zoomSpinner, gbc);

        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.setPosAndInsets(2, 3, 10, 0, 0, 0);
        JLabel zoomLabel = new JLabel("Zoom percentage: "); //TODO Übersetzungen hinzufügen
        this.container.add(zoomLabel, gbc);


        gbc.setHorizontal(1, 1);
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
        screenshotButton.addActionListener(e -> gui.getActionHandler().executeAction(ActionType.SHORTCUT_TAKE_SCREENSHOT));
        this.container.add(screenshotButton, gbc);
    }

    private void initCheckBox(JCheckBox checkBox, boolean isMirrorXBox) {
        boolean startValue =  (isMirrorXBox ? workspace.getConfig().isMirrorX() : workspace.getConfig().isMirrorY());
        checkBox.setSelected(startValue);
        checkBox.addItemListener(e -> {
                if(this.container.isEnabled()) {
                    if (isMirrorXBox) workspace.getConfig().setMirrorX(checkBox.isSelected());
                    else workspace.getConfig().setMirrorY(checkBox.isSelected());
                    gui.handleAction(ActionType.ACTION_EDIT_IMAGE);
                }
        });
    }

    public JSpinner getDegreeSpinner(SpinnerNumberModel spinnerModel) {
        JSpinner spinner = new JSpinner(spinnerModel);

        Number rotation = workspace.getConfig().getRotation();
        if (GuiUtils.valueFitsInRange(rotation, spinnerModel)) spinner.setValue(rotation);
        else setRotationInImageHandler(spinner);

        spinner.addChangeListener(e -> {
            if(this.container.isEnabled()) {
                setRotationInImageHandler(spinner);
                gui.handleAction(ActionType.ACTION_EDIT_IMAGE);
            }
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
        updateValues();
        updateScreenshotCounter();
    }

    private void updateValues() {
        this.degreeSpinner.setValue(workspace.getConfig().getRotation());
        this.mirrorXBox.setSelected(workspace.getConfig().isMirrorX());
        this.mirrorYBox.setSelected(workspace.getConfig().isMirrorY());
        this.zoomSpinner.setValue(workspace.getConfig().getZoom());
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

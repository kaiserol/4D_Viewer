package de.uzk.gui.tabs;

import de.uzk.action.ActionType;
import de.uzk.config.Config;
import de.uzk.gui.ComponentUtils;
import de.uzk.gui.Gui;
import de.uzk.gui.OGridBagConstraints;
import de.uzk.gui.areas.AreaContainerInteractive;
import de.uzk.utils.NumberUtils;
import de.uzk.utils.SnapshotHelper;

import javax.swing.*;
import java.awt.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

public class TabEdit extends AreaContainerInteractive<JPanel> {
    // GUI-Elemente
    private JCheckBox mirrorXBox, mirrorYBox;
    private JSlider contrastSlider, brightnessSlider, zoomSlider;
    private JSpinner degreeSpinner;
    private JLabel snapshots;

    public TabEdit(Gui gui) {
        super(new JPanel(), gui);
        init();
    }

    private void init() {
        this.container.setLayout(new GridBagLayout());

        // Layout Manager
        OGridBagConstraints gbc = new OGridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        // Kontrollkästchen (Horizontales und Vertikales spiegeln)
        this.mirrorXBox = ComponentUtils.createCheckBox(getWord("items.edit.mirrorX"), newValue ->
            setConfigValue(newValue, workspace.getConfig()::isMirrorX, workspace.getConfig()::setMirrorX));
        this.mirrorYBox = ComponentUtils.createCheckBox(getWord("items.edit.mirrorY"), newValue ->
            setConfigValue(newValue, workspace.getConfig()::isMirrorY, workspace.getConfig()::setMirrorY));
        addRow(this.mirrorXBox, gbc, 0);
        addRow(this.mirrorYBox, gbc, 5);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;

        // Schieberegler (Helligkeit, Kontrast und Zoom)
        this.brightnessSlider = ComponentUtils.createSlider(Config.MIN_BRIGHTNESS, Config.MAX_BRIGHTNESS, newValue ->
            setConfigValue(newValue, workspace.getConfig()::getBrightness, workspace.getConfig()::setBrightness));
        this.contrastSlider = ComponentUtils.createSlider(Config.MIN_CONTRAST, Config.MAX_CONTRAST, newValue ->
            setConfigValue(newValue, workspace.getConfig()::getContrast, workspace.getConfig()::setContrast));
        this.zoomSlider = ComponentUtils.createSlider(Config.MIN_ZOOM, Config.MAX_ZOOM, newValue ->
            setConfigValue(newValue, workspace.getConfig()::getZoom, workspace.getConfig()::setZoom));

        addLabeledRow(gbc, getWord("items.edit.brightness"), this.brightnessSlider, 15);
        addLabeledRow(gbc, getWord("items.edit.contrast"), this.contrastSlider, 10);
        addLabeledRow(gbc, getWord("items.edit.zoom"), this.zoomSlider, 10);

        // Drehfeld (Rotation)
        degreeSpinner = ComponentUtils.createSpinner(Config.MIN_ROTATION, Config.MAX_ROTATION, newValue ->
            setConfigValue(newValue, workspace.getConfig()::getRotation, workspace.getConfig()::setRotation));
        addLabeledRow(gbc, getWord("items.edit.rotation"), degreeSpinner, 10);

        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.weighty = 1;
        gbc.gridwidth = 2;

        // Momentaufnahmen
        JPanel snapshotsPanel = new JPanel(new BorderLayout(10, 0));
        snapshotsPanel.add(new JLabel(getWord("items.edit.snapshots") + ":"), BorderLayout.WEST);
        snapshotsPanel.add(snapshots = new JLabel(), BorderLayout.CENTER);
        addRow(snapshotsPanel, gbc, 15);

        gbc.weighty = 0;

        // Schaltfläche (Momentaufnahme machen)
        JButton snapshotsButton = new JButton(getWord("items.edit.takeSnapshot"));
        snapshotsButton.addActionListener(e -> gui.getActionHandler().executeAction(ActionType.SHORTCUT_TAKE_SNAPSHOT));
        addRow(snapshotsButton, gbc, 5);
    }

    // ========================================
    // Observer Methoden
    // ========================================
    @Override
    public void handleAction(ActionType actionType) {
        switch (actionType) {
            case SHORTCUT_TURN_IMAGE_90_LEFT ->
                ComponentUtils.setValueSecurely(this.degreeSpinner, NumberUtils.turn90Left(workspace.getConfig().getRotation()));
            case SHORTCUT_TURN_IMAGE_90_RIGHT ->
                ComponentUtils.setValueSecurely(this.degreeSpinner, NumberUtils.turn90Right(workspace.getConfig().getRotation()));
            case ACTION_UPDATE_SNAPSHOT_COUNTER -> updateSnapshotCounter();
        }
    }

    @Override
    public void toggleOn() {
        ComponentUtils.setEnabled(this.container, true);

        ComponentUtils.setValueSecurely(this.mirrorXBox, workspace.getConfig().isMirrorX());
        ComponentUtils.setValueSecurely(this.mirrorYBox, workspace.getConfig().isMirrorY());
        ComponentUtils.setValueSecurely(this.brightnessSlider, workspace.getConfig().getBrightness());
        ComponentUtils.setValueSecurely(this.contrastSlider, workspace.getConfig().getContrast());
        ComponentUtils.setValueSecurely(this.zoomSlider, workspace.getConfig().getZoom());
        ComponentUtils.setValueSecurely(this.degreeSpinner, workspace.getConfig().getRotation());
        updateSnapshotCounter();
    }

    @Override
    public void toggleOff() {
        ComponentUtils.setEnabled(this.container, false);

        ComponentUtils.setValueSecurely(this.mirrorXBox, false);
        ComponentUtils.setValueSecurely(this.mirrorYBox, false);
        ComponentUtils.setValueSecurely(this.brightnessSlider, Config.MIN_BRIGHTNESS);
        ComponentUtils.setValueSecurely(this.contrastSlider, Config.MIN_CONTRAST);
        ComponentUtils.setValueSecurely(this.zoomSlider, Config.MIN_ZOOM);
        ComponentUtils.setValueSecurely(this.degreeSpinner, Config.MIN_ROTATION);
        updateSnapshotCounter();
    }

    @Override
    public void appGainedFocus() {
        updateSnapshotCounter();
    }

    // ========================================
    // Aktualisierungen
    // ========================================
    private <T> void setConfigValue(T newValue, Supplier<T> getter, Predicate<T> setter) {
        // Wenn sich der Wert nicht ändert, abbrechen
        T settingsValue = getter.get();
        if (settingsValue.equals(newValue)) return;

        boolean hasValueChanged = setter.test(newValue);
        if (hasValueChanged) gui.handleAction(ActionType.ACTION_EDIT_IMAGE);
    }

    private void updateSnapshotCounter() {
        this.snapshots.setText(String.valueOf(SnapshotHelper.getSnapshotsCount()));
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    private void addLabeledRow(GridBagConstraints gbc, String labelText, JComponent component, int topInset) {
        gbc.insets.top = topInset;
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        gbc.insets.right = 15;
        this.container.add(new JLabel(labelText + ":"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.insets.right = 0;
        this.container.add(component, gbc);
    }

    private void addRow(JComponent component, GridBagConstraints gbc, int topInset) {
        gbc.insets.top = topInset;
        gbc.gridx = 0;
        gbc.gridy++;
        this.container.add(component, gbc);
    }
}

package de.uzk.gui.tabs;

import de.uzk.action.ActionType;
import de.uzk.config.Config;
import de.uzk.edit.Edit;
import de.uzk.edit.image.*;
import de.uzk.gui.Gui;
import de.uzk.gui.UIEnvironment;
import de.uzk.gui.observer.ObserverContainer;
import de.uzk.io.PathManager;
import de.uzk.io.SnapshotHelper;
import de.uzk.utils.ComponentUtils;
import de.uzk.utils.NumberUtils;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.function.Function;
import java.util.function.Supplier;

import static de.uzk.Main.logger;
import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

public class TabEdit extends ObserverContainer<JPanel> {
    // Gui Elemente
    private JCheckBox mirrorXBox, mirrorYBox;
    private JSlider contrastSlider, brightnessSlider, zoomSlider;
    private JSpinner degreeSpinner;
    private JLabel snapshots;

    public TabEdit(Gui gui) {
        super(new JPanel(), gui);
        init();
    }

    private void init() {
        container.setLayout(new GridBagLayout());

        // Layout Manager
        GridBagConstraints gbc = ComponentUtils.createGridBagConstraints();
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;

        // Kontrollkästchen (Horizontales und Vertikales spiegeln) hinzufügen
        mirrorXBox = ComponentUtils.createCheckBox(getWord("menu.edit.mirrorX"), newValue ->
            setConfigValue(newValue, workspace.getConfig()::isMirrorX, MirrorEdit::mirrorXEdit));
        mirrorYBox = ComponentUtils.createCheckBox(getWord("menu.edit.mirrorY"), newValue ->
            setConfigValue(newValue, workspace.getConfig()::isMirrorY, MirrorEdit::mirrorYEdit));
        ComponentUtils.addRow(container, gbc, mirrorXBox, 0);
        ComponentUtils.addRow(container, gbc, mirrorYBox, 5);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;

        // Schieberegler (Helligkeit, Kontrast und Zoom) hinzufügen
        brightnessSlider = ComponentUtils.createSlider(Config.MIN_BRIGHTNESS, Config.MAX_BRIGHTNESS, newValue ->
            setConfigValue(newValue, workspace.getConfig()::getBrightness, BrightnessEdit::new));
        contrastSlider = ComponentUtils.createSlider(Config.MIN_CONTRAST, Config.MAX_CONTRAST, newValue ->
            setConfigValue(newValue, workspace.getConfig()::getContrast, ContrastEdit::new));
        zoomSlider = ComponentUtils.createSlider(Config.MIN_ZOOM, Config.MAX_ZOOM, newValue ->
            setConfigValue(newValue, workspace.getConfig()::getZoom, ZoomEdit::new));

        ComponentUtils.addLabeledRow(container, gbc, getWord("menu.edit.brightness"), brightnessSlider, 15);
        ComponentUtils.addLabeledRow(container, gbc, getWord("menu.edit.contrast"), contrastSlider, 10);
        ComponentUtils.addLabeledRow(container, gbc, getWord("menu.edit.zoom"), zoomSlider, 10);

        // Drehfeld (Rotation) hinzufügen
        degreeSpinner = ComponentUtils.createSpinner(Config.MIN_ROTATION, Config.MAX_ROTATION, true, newValue ->
            setConfigValue(newValue, workspace.getConfig()::getRotation, RotateImageEdit::new));
        ComponentUtils.addLabeledRow(container, gbc, getWord("menu.edit.rotation"), degreeSpinner, 10);

        gbc.gridwidth = 2;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.SOUTHWEST;

        // Label (Momentaufnahmen) hinzufügen
        JPanel snapshotsPanel = new JPanel(UIEnvironment.getDefaultBorderLayout());
        snapshotsPanel.add(new JLabel(getWord("menu.edit.snapshots") + ":"), BorderLayout.WEST);
        snapshotsPanel.add(snapshots = new JLabel(), BorderLayout.CENTER);
        ComponentUtils.addRow(container, gbc, snapshotsPanel, 15);

        gbc.weighty = 0;

        // Schaltfläche (Momentaufnahme machen) hinzufügen
        JButton snapshotsButton = new JButton(getWord("menu.edit.takeSnapshot"));
        snapshotsButton.addActionListener(e -> gui.getActionHandler().executeAction(ActionType.SHORTCUT_TAKE_SNAPSHOT));
        ComponentUtils.addRow(container, gbc, snapshotsButton, 5);

        JButton openSnapshotsButton = new JButton(getWord("menu.edit.openSnapshotFolder"));
        openSnapshotsButton.addActionListener(e -> {
            Desktop desktop = UIEnvironment.getDesktopSecurely();
            if (desktop == null) return;
            try {
                desktop.open(PathManager.getProjectSnapshotsDirectory().toFile());
            } catch (IOException ex) {
                logger.exception(ex, "Could not open snapshot folder '%s'".formatted(PathManager.getProjectSnapshotsDirectory()));
                JOptionPane.showMessageDialog(null, "Couldn't open folder.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        ComponentUtils.addRow(container, gbc, openSnapshotsButton, 5);
    }

    // ========================================
    // Observer Methoden
    // ========================================
    @Override
    public void handleAction(ActionType actionType) {
        switch (actionType) {
            case SHORTCUT_TURN_IMAGE_90_LEFT -> {
                int rotation = NumberUtils.snapToLeft90(workspace.getConfig().getRotation());
                ComponentUtils.setValueSecurely(degreeSpinner, rotation);

                workspace.getConfig().setRotation(rotation);
                gui.handleAction(ActionType.ACTION_EDIT_IMAGE);
            }
            case SHORTCUT_TURN_IMAGE_90_RIGHT -> {
                int rotation = NumberUtils.snapToRight90(workspace.getConfig().getRotation());
                ComponentUtils.setValueSecurely(degreeSpinner, rotation);

                workspace.getConfig().setRotation(rotation);
                gui.handleAction(ActionType.ACTION_EDIT_IMAGE);
            }
            case ACTION_UPDATE_SNAPSHOT_COUNTER -> updateSnapshotCounter();
            case ACTION_EDIT_IMAGE -> setCorrectValues();

        }
    }

    @Override
    public void toggleOn() {
        ComponentUtils.setEnabled(container, true);

        setCorrectValues();
        updateSnapshotCounter();
    }

    private void setCorrectValues() {
        ComponentUtils.setValueSecurely(mirrorXBox, workspace.getConfig().isMirrorX());
        ComponentUtils.setValueSecurely(mirrorYBox, workspace.getConfig().isMirrorY());
        ComponentUtils.setValueSecurely(brightnessSlider, workspace.getConfig().getBrightness());
        ComponentUtils.setValueSecurely(contrastSlider, workspace.getConfig().getContrast());
        ComponentUtils.setValueSecurely(zoomSlider, workspace.getConfig().getZoom());
        ComponentUtils.setValueSecurely(degreeSpinner, workspace.getConfig().getRotation());

    }

    @Override
    public void toggleOff() {
        ComponentUtils.setEnabled(container, false);

        ComponentUtils.setValueSecurely(mirrorXBox, false);
        ComponentUtils.setValueSecurely(mirrorYBox, false);
        ComponentUtils.setValueSecurely(brightnessSlider, Config.MIN_BRIGHTNESS);
        ComponentUtils.setValueSecurely(contrastSlider, Config.MIN_CONTRAST);
        ComponentUtils.setValueSecurely(zoomSlider, Config.MIN_ZOOM);
        ComponentUtils.setValueSecurely(degreeSpinner, Config.MIN_ROTATION);
        updateSnapshotCounter();
    }

    @Override
    public void appGainedFocus() {
        updateSnapshotCounter();
    }

    // ========================================
    // Aktualisierungen
    // ========================================

    private <T> void setConfigValue(T newValue, Supplier<T> oldGetter, Function<T, Edit> editConstructor) {
        T settingsValue = oldGetter.get();
        if (settingsValue.equals(newValue)) return;

        Edit edit = editConstructor.apply(newValue);
        boolean hasValueChanged = workspace.getEditManager().performEdit(edit);
        if (hasValueChanged) gui.handleAction(ActionType.ACTION_EDIT_IMAGE);
    }

    private void updateSnapshotCounter() {
        snapshots.setText(String.valueOf(SnapshotHelper.getSnapshotsCount()));
    }
}

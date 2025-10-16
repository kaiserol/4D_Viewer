package de.uzk.gui;

import de.uzk.action.ActionHandler;
import de.uzk.action.ActionType;
import de.uzk.action.HandleActionListener;
import de.uzk.gui.dialogs.DialogImageLoad;
import de.uzk.gui.menubar.AppMenuBar;
import de.uzk.gui.viewer.OViewer;
import de.uzk.image.Axis;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static de.uzk.Main.*;
import static de.uzk.config.LanguageHandler.getWord;

public class Gui extends AreaContainerInteractive<JFrame> {
    private final List<HandleActionListener> handleActionListeners;
    private final List<ToggleListener> toggleListeners;
    private final List<UpdateImageListener> updateImageListeners;
    private final List<UpdateThemeListener> updateThemeListeners;
    private final List<AppFocusListener> appFocusListeners;
    private boolean windowInitialized;

    public Gui() {
        super(new JFrame(), null);
        this.handleActionListeners = new ArrayList<>();
        this.toggleListeners = new ArrayList<>();
        this.updateImageListeners = new ArrayList<>();
        this.updateThemeListeners = new ArrayList<>();
        this.appFocusListeners = new ArrayList<>();
        build();
    }

    public void rebuild() {
        logger.info("Rebuilding UI ...");
        this.container.getContentPane().removeAll();

        // Verhindert, dass alte UI-Objekte weiter existieren (alte Ereignis-Listener müssen bereinigt werden)
        this.handleActionListeners.clear();
        this.toggleListeners.clear();
        this.updateImageListeners.clear();
        this.updateThemeListeners.clear();
        this.appFocusListeners.clear();

        // Inhalt initialisieren
        initContent();
        updateTheme();

        // Prüfe, ob Bilder geladen sind
        if (imageFileHandler.hasImageFilesDirectory()) toggleOn();
        else toggleOff();

        // Fenster packen
        this.container.pack();
    }

    private void build() {
        logger.info("Building UI ...");
        GuiUtils.initFlatLaf();

        this.container.setIconImage(Icons.APP_IMAGE);
        this.container.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.container.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExitApp();
            }
        });
        this.container.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                if (windowInitialized) Gui.this.appGainedFocus();
                windowInitialized = true;
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                if (windowInitialized) Gui.this.appLostFocus();
            }
        });

        // Inhalt initialisieren
        initContent();
        updateTheme();

        // Lade Image-Files
        String tempImageFilesDirectory = configHandler.getTempImageFilesDirectory();
        if (tempImageFilesDirectory != null) loadImageFiles(tempImageFilesDirectory, true);
        else toggleOff();

        // Fenster sichtbar machen
        this.container.pack();
        this.container.setLocationRelativeTo(null);
        this.container.setVisible(true);
    }

    private void initContent() {
        this.container.setTitle(getWord("app.name"));
        this.container.setLayout(new BorderLayout());
        ActionHandler actionHandler = new ActionHandler(this);

        // menuBar
        AppMenuBar menuBar = new AppMenuBar(this, actionHandler);
        this.container.setJMenuBar(menuBar.getContainer());

        // mainPanel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // directory selection
        AreaDirectorySelection directorySelection = new AreaDirectorySelection(this);
        mainPanel.add(directorySelection.getContainer(), BorderLayout.NORTH);

        // splitPane (areaTabs, areaViewer)
        AreaTabs areaTabs = new AreaTabs(this, actionHandler);
        OViewer areaViewer = new OViewer(this, actionHandler);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        splitPane.add(areaTabs.getContainer());
        splitPane.add(areaViewer.getContainer());
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // disclaimer
        AreaDisclaimerRightOfUse disclaimer = new AreaDisclaimerRightOfUse(this);
        mainPanel.add(disclaimer.getContainer(), BorderLayout.SOUTH);
        this.container.add(mainPanel);
    }

    public void loadImageFiles(String directoryPath, boolean isGuiBeingBuilt) {
        File file = new File(directoryPath);
        if (!file.exists()) {
            if (isGuiBeingBuilt) return;
            String message = getWord("optionPane.directory.theDirectory") + ": '" + directoryPath + "' " +
                    getWord("optionPane.directory.doesNotExisting") + ".";
            JOptionPane.showMessageDialog(
                    this.container,
                    message,
                    getWord("optionPane.title.error"),
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Nur laden, wenn Verzeichnis nicht bereits geladen ist
        File directory = file.isDirectory() ? file : file.getParentFile();
        File imageFilesDirectory = imageFileHandler.getImageFilesDirectory();
        if (directory.equals(imageFilesDirectory)) return;

        // Prüfe, ob das Verzeichnis passende Bilder-Dateien hat
        boolean opened = new DialogImageLoad().openImageFilesDirectory(this.container, directoryPath);
        if (opened) {
            toggleOn();
        } else {
            if (isGuiBeingBuilt) return;
            String message = getWord("optionPane.directory.theDirectory") + ": '" + directoryPath + "' " +
                    getWord("optionPane.directory.hasNo") + " " + imageFileHandler.getImageFileNameExtension().getDescription() + ".";
            JOptionPane.showMessageDialog(
                    this.container,
                    message,
                    getWord("optionPane.title.error"),
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void addHandleActionListener(HandleActionListener handleActionListener) {
        this.handleActionListeners.add(handleActionListener);
    }

    public void addToggleListener(ToggleListener toggleListener) {
        this.toggleListeners.add(toggleListener);
    }

    public void addUpdateImageListener(UpdateImageListener updateImageListener) {
        this.updateImageListeners.add(updateImageListener);
    }

    public void addUpdateThemeListener(UpdateThemeListener updateThemeListener) {
        this.updateThemeListeners.add(updateThemeListener);
    }

    public void addAppFocusListener(AppFocusListener appFocusListener) {
        this.appFocusListeners.add(appFocusListener);
    }

    @Override
    public void handleAction(ActionType actionType) {
        if (actionType == ActionType.SHORTCUT_TOGGLE_PIN_TIME) {
            imageFileHandler.togglePinTime();
            actionType = ActionType.ACTION_UPDATE_PIN_TIME;
        }

        for (HandleActionListener listener : handleActionListeners) {
            listener.handleAction(actionType);
        }
    }

    @Override
    public void toggleOn() {
        for (ToggleListener listener : toggleListeners) {
            listener.toggleOn();
        }
        updateUI();
    }

    @Override
    public void toggleOff() {
        imageFileHandler.removeImageFilesDirectory();
        imageFileHandler.clear();

        for (ToggleListener listener : toggleListeners) {
            listener.toggleOff();
        }
        updateUI();
    }

    @Override
    public void update(Axis axis) {
        if (axis == null) return;
        for (UpdateImageListener listener : updateImageListeners) {
            listener.update(axis);
        }
        updateUI();
    }

    @Override
    public void updateTheme() {
        for (UpdateThemeListener listener : updateThemeListeners) {
            listener.updateTheme();
        }
    }

    @Override
    public void appGainedFocus() {
        for (AppFocusListener listener : appFocusListeners) {
            listener.appGainedFocus();
        }
    }

    public void updateUI() {
        this.container.revalidate();
        this.container.repaint();
    }

    public void confirmExitApp() {
        if (configHandler.isConfirmExit()) {
            JCheckBox checkBox = new JCheckBox(getWord("optionPane.closeApp.dont_ask_again"));
            Object[] message = new Object[]{getWord("optionPane.closeApp.question"), checkBox};

            int option = JOptionPane.showConfirmDialog(
                    this.container,
                    message,
                    getWord("optionPane.title.confirm"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            // Wenn der Benutzer "Nein" klickt → Abbrechen
            if (option != JOptionPane.YES_OPTION) return;

            // Wenn Checkbox aktiv → Einstellung merken
            if (checkBox.isSelected()) configHandler.setConfirmExit(false);
        }

        // Config abspeichern
        configHandler.saveConfig();

        // Anwendung beenden
        exit();
    }

    public static void exitApp(Window window, Runnable exitAction) {
        if (configHandler.isConfirmExit()) {
            int option = JOptionPane.showConfirmDialog(
                    window,
                    getWord("optionPane.insurance.question"),
                    getWord("optionPane.insurance.title"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            // Wenn der Benutzer "Nein" klickt → Abbrechen
            if (option != JOptionPane.YES_OPTION) return;
        }

        // Anwendung beenden
        if (exitAction != null) exitAction.run();
        exit();
    }

    private static void exit() {
        System.exit(0);
    }
}
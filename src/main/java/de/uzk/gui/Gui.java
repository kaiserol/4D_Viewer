package de.uzk.gui;

import de.uzk.action.ActionHandler;
import de.uzk.action.ActionType;
import de.uzk.action.HandleActionListener;
import de.uzk.gui.dialogs.DialogImageLoad;
import de.uzk.gui.menubar.AppMenuBar;
import de.uzk.image.Axis;
import de.uzk.image.ImageFileNameExtension;
import de.uzk.image.LoadingResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static de.uzk.Main.*;
import static de.uzk.config.LanguageHandler.getWord;

public class Gui extends AreaContainerInteractive<JFrame> {
    // Mindestgröße des Fensters
    private static final int MIN_WIDTH = 400;
    private static final int MIN_HEIGHT = 100;

    // Observer Pattern - Listeners
    private final List<HandleActionListener> handleActionListeners;
    private final List<ToggleListener> toggleListeners;
    private final List<UpdateImageListener> updateImageListeners;
    private final List<UpdateThemeListener> updateThemeListeners;
    private final List<AppFocusListener> appFocusListeners;
    private boolean windowInitialized;

    public Gui(Path imageFilesDirectory) {
        super(new JFrame(), null);
        this.handleActionListeners = new ArrayList<>();
        this.toggleListeners = new ArrayList<>();
        this.updateImageListeners = new ArrayList<>();
        this.updateThemeListeners = new ArrayList<>();
        this.appFocusListeners = new ArrayList<>();
        build(imageFilesDirectory);
    }

    private void build(Path imageFilesDirectory) {
        logger.info("Building UI ...");
        GuiUtils.initFlatLaf();

        this.container.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
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
        if (!loadImageFiles(imageFilesDirectory, settings.getFileNameExt(), true)) {
            toggleOff();
        }

        // Fenster sichtbar machen
        this.container.pack();
        this.container.setLocationRelativeTo(null);
        this.container.setVisible(true);
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
        if (workspace.isOpen()) toggleOn();
        else toggleOff();

        // Fenster packen
        this.container.pack();
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

        // directorySelection
        AreaDirectorySelection directorySelection = new AreaDirectorySelection(this);
        mainPanel.add(directorySelection.getContainer(), BorderLayout.NORTH);

        // splitPane (tabs, imageViewer)
        AreaTabs tabs = new AreaTabs(this, actionHandler);
        AreaImageViewer imageViewer = new AreaImageViewer(this, actionHandler);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        splitPane.add(tabs.getContainer());
        splitPane.add(imageViewer.getContainer());
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // disclaimer
        AreaDisclaimerRightOfUse disclaimer = new AreaDisclaimerRightOfUse(this);
        mainPanel.add(disclaimer.getContainer(), BorderLayout.SOUTH);
        this.container.add(mainPanel);
    }

    public boolean loadImageFiles(Path directoryPath, ImageFileNameExtension extension, boolean isGuiBeingBuilt) {
        workspace.saveConfig();
        if (directoryPath == null) return false;

        // Prüfe, ob das Verzeichnis passende Bilder hat
        LoadingResult result = new DialogImageLoad(this.container).loadImages(directoryPath, extension);
        switch (result) {
            case LOADED -> {
                toggleOn();
                this.handleAction(ActionType.ACTION_ADD_MARKER);
                return true;
            }
            case ALREADY_LOADED -> {
                if (isGuiBeingBuilt) return false;
                String message = getWord("optionPane.directory.the") + " " + extension + " " + getWord("file.directory") + ": '" + directoryPath + "' " + getWord("optionPane.directory.alreadyLoaded") + ".";
                JOptionPane.showMessageDialog(this.container, message, getWord("optionPane.title.error"), JOptionPane.ERROR_MESSAGE);
            }
            case DIRECTORY_NOT_EXISTING -> {
                if (isGuiBeingBuilt) return false;
                String message = getWord("optionPane.directory.the") + " " + getWord("file.directory") + ": '" + directoryPath + "' " + getWord("optionPane.directory.doesNotExisting") + ".";
                JOptionPane.showMessageDialog(this.container, message, getWord("optionPane.title.error"), JOptionPane.ERROR_MESSAGE);
            }
            case DIRECTORY_HAS_NO_IMAGES -> {
                if (isGuiBeingBuilt) return false;
                String message = getWord("optionPane.directory.the") + " " + getWord("file.directory") + ": '" + directoryPath + "' " + getWord("optionPane.directory.hasNo") + " " + extension.getDescription() + ".";
                JOptionPane.showMessageDialog(this.container, message, getWord("optionPane.title.error"), JOptionPane.ERROR_MESSAGE);
            }
        }
        return false;
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
            workspace.togglePinTime();
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
        workspace.clear(true);

        for (ToggleListener listener : toggleListeners) {
            listener.toggleOff();
        }
        updateUI();
    }

    @Override
    public void update(Axis axis) {
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
        // Überprüfe, ob Bilder noch vorhanden sind
        workspace.checkMissingFiles();

        for (AppFocusListener listener : appFocusListeners) {
            listener.appGainedFocus();
        }
    }

    public void updateUI() {
        this.container.revalidate();
        this.container.repaint();
    }

    public void confirmExitApp() {
        if (settings.isConfirmExit()) {
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
            if (checkBox.isSelected()) settings.setConfirmExit(false);
        }

        // Settings, Config abspeichern & Anwendung beenden
        workspace.saveConfig();
        settings.save();
        System.exit(0);
    }
}
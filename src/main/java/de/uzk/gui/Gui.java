package de.uzk.gui;

import de.uzk.action.ActionHandler;
import de.uzk.action.ActionType;
import de.uzk.action.HandleActionListener;
import de.uzk.gui.areas.AreaContainerInteractive;
import de.uzk.gui.areas.AreaImageDirectoryPath;
import de.uzk.gui.areas.AreaImageViewer;
import de.uzk.gui.areas.AreaTabs;
import de.uzk.gui.dialogs.DialogImagesLoad;
import de.uzk.gui.menubar.AppMenuBar;
import de.uzk.image.Axis;
import de.uzk.image.ImageFileType;
import de.uzk.image.LoadingResult;
import de.uzk.image.MissingImagesReport;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static de.uzk.Main.*;
import static de.uzk.config.LanguageHandler.getWord;

public class Gui extends AreaContainerInteractive<JFrame> {
    // GUI-Elemente
    private final ActionHandler actionHandler;
    private final DialogImagesLoad dialogImagesLoad;

    // Observer Listener
    private final List<HandleActionListener> handleActionListeners;
    private final List<ToggleListener> toggleListeners;
    private final List<UpdateImageListener> updateImageListeners;
    private final List<UpdateThemeListener> updateThemeListeners;
    private final List<AppFocusListener> appFocusListeners;

    // Mindestgröße des Fensters
    private static final int MIN_WIDTH = 400;
    private static final int MIN_HEIGHT = 100;

    private boolean hasUnsavedChanges;

    public Gui() {
        super(new JFrame(), null);

        // Observer Listener initialisieren
        this.handleActionListeners = new ArrayList<>();
        this.toggleListeners = new ArrayList<>();
        this.updateImageListeners = new ArrayList<>();
        this.updateThemeListeners = new ArrayList<>();
        this.appFocusListeners = new ArrayList<>();

        // ActionHandler erstellen
        this.actionHandler = new ActionHandler(this);

        // Dialog für das Laden von Bildern erstellen
        this.dialogImagesLoad = new DialogImagesLoad(this.container);

        // Gui erstellen
        build();

        this.registerConfigSaved();
    }

    public void registerUnsavedChange() {
        if (!this.hasUnsavedChanges) {
            this.container.setTitle(this.container.getTitle() + "*");
        }

        this.hasUnsavedChanges = true;
    }

    public void registerConfigSaved() {
        if (this.hasUnsavedChanges) {
            this.container.setTitle(this.container.getTitle().replace("*", ""));
        }
        this.hasUnsavedChanges = false;
    }

    private void build() {
        logger.info("Building UI ...");

        // Fenster initialisieren
        initFrame();

        // Menüleiste & Inhalt hinzufügen
        addMenuBar();
        addContent();
        updateTheme();

        // Bilder laden

        // TODO: Warum rausgenommen (für mich)
//        if (!openImagesDirectory(history.getLastIfExists(), workspace.getConfig().getImageFileType(), true))
        if (!openImagesDirectory(history.getLastIfExists(), null, true)) {
            toggleOff();
        }

        // Fenster anzeigen
        this.container.pack();
        this.container.setLocationRelativeTo(null);
        this.container.setVisible(true);
    }

    public void rebuild() {
        logger.info("Rebuilding UI ...");

        // Observer Listener zurücksetzen (alte Ereignis-Listener müssen bereinigt werden)
        this.handleActionListeners.clear();
        this.toggleListeners.clear();
        this.updateImageListeners.clear();
        this.updateThemeListeners.clear();
        this.appFocusListeners.clear();

        // Inhalt entfernen
        this.container.getContentPane().removeAll();

        // Menüleiste & Inhalt hinzufügen
        addMenuBar();
        addContent();
        updateTheme();

        // Prüfe, ob Bilder geladen sind
        if (workspace.isOpen()) toggleOn();
        else toggleOff();

        // Fenster packen
        this.container.pack();
    }

    private void initFrame() {
        GuiUtils.updateFlatLaf();
        GuiUtils.setImageIcon(this.container);

        this.container.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
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
                appGainedFocus();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                appLostFocus();
            }
        });
    }

    private void addMenuBar() {
        AppMenuBar menuBar = new AppMenuBar(this);
        this.container.setJMenuBar(menuBar.getContainer());
    }

    private void addContent() {
        this.container.setTitle(getWord("app.name"));
        this.container.setLayout(new BorderLayout());

        // Panel erstellen
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Bilder Verzeichnis Pfad hinzufügen
        AreaImageDirectoryPath imageDirectoryPath = new AreaImageDirectoryPath(this);
        mainPanel.add(imageDirectoryPath.getContainer(), BorderLayout.NORTH);

        // SplitPane: Tabs & Bilder-Betrachter erstellen
        AreaTabs tabs = new AreaTabs(this);
        AreaImageViewer imageViewer = new AreaImageViewer(this);

        // SplitPane hinzufügen
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        splitPane.add(tabs.getContainer());
        splitPane.add(imageViewer.getContainer());
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Panel zum Container hinzufügen
        this.container.add(mainPanel);
    }

    // ========================================
    // Getter und Setter
    // ========================================
    public ActionHandler getActionHandler() {
        return actionHandler;
    }

    // ========================================
    // Observer Registrierung
    // ========================================
    public void registerHandleActionListener(HandleActionListener handleActionListener) {
        this.handleActionListeners.add(handleActionListener);
    }

    public void registerToggleListener(ToggleListener toggleListener) {
        this.toggleListeners.add(toggleListener);
    }

    public void registerUpdateImageListener(UpdateImageListener updateImageListener) {
        this.updateImageListeners.add(updateImageListener);
    }

    public void registerUpdateThemeListener(UpdateThemeListener updateThemeListener) {
        this.updateThemeListeners.add(updateThemeListener);
    }

    public void registerAppFocusListener(AppFocusListener appFocusListener) {
        this.appFocusListeners.add(appFocusListener);
    }

    // ========================================
    // Observer Methoden
    // ========================================
    @Override
    public void handleAction(ActionType actionType) {
        if (actionType == null) return;
        switch (actionType) {
            case SHORTCUT_PIN_TIME -> workspace.togglePinTime();
            case ACTION_EDIT_IMAGE, ACTION_ADD_MARKER, ACTION_REMOVE_MARKER -> this.registerUnsavedChange();
        }

        // Observer ausführen
        for (HandleActionListener observer : handleActionListeners) observer.handleAction(actionType);
    }

    @Override
    public void toggleOn() {
        // Observer ausführen
        for (ToggleListener observer : toggleListeners) observer.toggleOn();
        updateUI();
    }

    @Override
    public void toggleOff() {
        // Workspace leeren
        workspace.clear(true);

        // Observer ausführen
        for (ToggleListener observer : toggleListeners) observer.toggleOff();
        updateUI();
    }

    @Override
    public void update(Axis axis) {
        if (axis == null) return;

        // Observer ausführen
        for (UpdateImageListener observer : updateImageListeners) observer.update(axis);
        updateUI();
    }

    @Override
    public void updateTheme() {
        // Observer ausführen
        for (UpdateThemeListener observer : updateThemeListeners) observer.updateTheme();
        updateUI();
    }

    @Override
    public void appGainedFocus() {
        // Prüfe, ob Bilder noch vorhanden sind
        new MissingImagesReport(workspace).log();

        // Observer ausführen
        for (AppFocusListener observer : appFocusListeners) observer.appGainedFocus();
    }

    @Override
    public void appLostFocus() {
        // Observer ausführen
        for (AppFocusListener observer : appFocusListeners) observer.appLostFocus();
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    public void updateUI() {
        this.container.revalidate();
        this.container.repaint();
    }

    public boolean openImagesDirectory(Path imagesDirectory, ImageFileType imageFileType, boolean isGuiBeingBuilt) {
        // Prüfe, ob das Verzeichnis passende Bilder hat
        LoadingResult result = this.dialogImagesLoad.show(imagesDirectory, imageFileType);
        switch (result) {
            case LOADING_SUCCESSFUL -> {
                toggleOn();
                handleAction(ActionType.ACTION_ADD_MARKER);
                return true;
            }
            case DIRECTORY_ALREADY_LOADED -> {
                if (isGuiBeingBuilt) return false;
                String message = getWord("optionPane.directory.msgAlreadyLoaded")
                    .formatted(imageFileType.getType(), imagesDirectory);
                JOptionPane.showMessageDialog(
                    this.container,
                    message,
                    getWord("optionPane.title.error"),
                    JOptionPane.ERROR_MESSAGE
                );
            }
            case DIRECTORY_DOES_NOT_EXIST -> {
                if (isGuiBeingBuilt) return false;
                String message = getWord("optionPane.directory.msgDoesNotExist")
                    .formatted(imagesDirectory);
                JOptionPane.showMessageDialog(
                    this.container,
                    message,
                    getWord("optionPane.title.error"),
                    JOptionPane.ERROR_MESSAGE
                );
            }
            case DIRECTORY_HAS_NO_IMAGES -> {
                if (isGuiBeingBuilt) return false;
                String message = getWord("optionPane.directory.msgHasNoImages")
                    .formatted(imagesDirectory, imageFileType.getType());
                JOptionPane.showMessageDialog(
                    this.container,
                    message,
                    getWord("optionPane.title.error"),
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
        return false;
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

        // Dateien abspeichern
        settings.save();
        history.save();
        workspace.save();

        // Anwendung beenden
        System.exit(0);
    }
}
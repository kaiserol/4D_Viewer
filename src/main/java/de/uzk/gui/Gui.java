package de.uzk.gui;

import de.uzk.action.ActionHandler;
import de.uzk.action.ActionType;
import de.uzk.gui.areas.AreaContainerInteractive;
import de.uzk.gui.areas.AreaImageViewer;
import de.uzk.gui.areas.AreaImagesDirectoryPath;
import de.uzk.gui.areas.AreaTabs;
import de.uzk.gui.menubar.AppMenuBar;
import de.uzk.gui.observer.*;
import de.uzk.image.Axis;
import de.uzk.image.ProjectHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import static de.uzk.Main.*;
import static de.uzk.config.LanguageHandler.getWord;

public class Gui extends AreaContainerInteractive<JFrame> {
    // Gui Elemente
    private final ActionHandler actionHandler;

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

        // Gui erstellen
        build();

        this.registerConfigSaved();
    }

    public ActionHandler getActionHandler() {
        return actionHandler;
    }

    // ========================================
    // Komponenten-Erzeugung
    // ========================================
    private void build() {
        logger.info("Building UI ...");

        // Fenster initialisieren
        UIEnvironment.updateFlatLaf();
        UIEnvironment.initImageIcon(this);

        this.container.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        this.container.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.container.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                UIEnvironment.closeApp(Gui.this);
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

        // Menüleiste & Inhalt hinzufügen
        createMenuBar();
        createContent();

        // Bilder laden
        // TODO: Warum rausgenommen (für mich)
//        if (!openImagesDirectory(history.getLastIfExists(), workspace.getConfig().getImageFileType(), true))
        if (!ProjectHelper.loadImagesDirectory(this, history.getLastIfExists(), null, true)) {
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
        createMenuBar();
        createContent();

        // Prüfe, ob Bilder geladen sind
        if (workspace.isLoaded()) toggleOn();
        else toggleOff();

        // Fenster packen
        this.container.pack();
    }

    private void createMenuBar() {
        AppMenuBar menuBar = new AppMenuBar(this);
        this.container.setJMenuBar(menuBar.getContainer());
    }

    private void createContent() {
        this.container.setTitle(getWord("app.name"));
        this.container.setLayout(new BorderLayout());

        // Panel erstellen
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(UIEnvironment.BORDER_EMPTY_DEFAULT);

        // Bilder Verzeichnis Pfad hinzufügen
        AreaImagesDirectoryPath imagesDirectoryPath = new AreaImagesDirectoryPath(this);
        mainPanel.add(imagesDirectoryPath.getContainer(), BorderLayout.NORTH);

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

        // Theme aktualisieren
        updateTheme();
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
        workspace.reset();

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
        workspace.logMissingImages();

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
}
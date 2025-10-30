package de.uzk.gui;

import de.uzk.action.ActionHandler;
import de.uzk.action.ActionType;
import de.uzk.action.HandleActionListener;
import de.uzk.gui.dialogs.DialogImageLoad;
import de.uzk.gui.menubar.AppMenuBar;
import de.uzk.image.Axis;
import de.uzk.image.ImageFileType;
import de.uzk.image.LoadingResult;

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
    // Mindestgröße des Fensters
    private static final int MIN_WIDTH = 400;
    private static final int MIN_HEIGHT = 100;

    // Observer Pattern
    private final List<HandleActionListener> handleActionListeners;
    private final List<ToggleListener> toggleListeners;
    private final List<UpdateImageListener> updateImageListeners;
    private final List<UpdateThemeListener> updateThemeListeners;
    private final List<AppFocusListener> appFocusListeners;
    private boolean windowInitialized;

    // Dialog für das Laden von Bildern
    private final DialogImageLoad dialogImageLoad;

    public Gui() {
        super(new JFrame(), null);
        this.handleActionListeners = new ArrayList<>();
        this.toggleListeners = new ArrayList<>();
        this.updateImageListeners = new ArrayList<>();
        this.updateThemeListeners = new ArrayList<>();
        this.appFocusListeners = new ArrayList<>();
        this.windowInitialized = false;
        this.dialogImageLoad = new DialogImageLoad(this.container);

        // Gui erstellen
        build();
    }

    private void build() {
        logger.info("Building UI ...");
        GuiUtils.updateFlatLaf();

        this.container.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        this.container.setIconImage(Icons.APP_IMAGE);
        this.container.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.container.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExitApp();
            }
        });
        // TODO: Werden diese WindowFocusListener auch richtig aufgerufen? (soll nur aufgerufen werden, wenn das Fenster, aber auch alle Dialoge Fokus erhalten / verlieren)
        this.container.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                if (windowInitialized) appGainedFocus();
                windowInitialized = true;
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                if (windowInitialized) appLostFocus();
            }
        });

        // Inhalt initialisieren
        initContent();
        updateTheme();

        // Image-Files laden
        // TODO: Jedes Verzeichnis kann seine eigene Config-File haben (dort enthalten ist auch die ImageFileType -> beachten und darauf aufbauen config suchen, wenn nicht vorhanden, dann aktuelle config nehmen), es kann sein dass Verzeichnis (aus der Historie) entweder ungültig ist oder keine Config hat, weil Config gelöscht (Config und markers würde ich in einem ordner = gleichbenannt mit Verzeichnis speichern und daraus dann die config laden...)
        // TODO: snapshots (soll so heißen anstatt screenshots) screenshot == bildschrim, snap = momentaufnahme, jedes verzeichnis soll einen snapshots ordner haben
        if (!loadImageFiles(history.getLast(), workspace.getConfig().getImageFileType(), true)) {
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
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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

        // disclaimerRightOfUse
        AreaDisclaimerRightOfUse disclaimerRightOfUse = new AreaDisclaimerRightOfUse(this);
        mainPanel.add(disclaimerRightOfUse.getContainer(), BorderLayout.SOUTH);
        this.container.add(mainPanel);
    }

    public boolean loadImageFiles(Path directory, ImageFileType imageFileType, boolean isGuiBeingBuilt) {
        // Prüfe, ob das Verzeichnis passende Bilder hat
        LoadingResult result = this.dialogImageLoad.show(directory, imageFileType);
        switch (result) {
            case LOADED -> {
                toggleOn();
                handleAction(ActionType.ACTION_ADD_MARKER);
                return true;
            }
            case ALREADY_LOADED -> {
                if (isGuiBeingBuilt) return false;
                String message = getWord("optionPane.directory.the") + " " + imageFileType + " " +
                        getWord("file.directory") + ": '" + directory + "' " +
                        getWord("optionPane.directory.alreadyLoaded") + ".";
                JOptionPane.showMessageDialog(
                        this.container,
                        message,
                        getWord("optionPane.title.error"),
                        JOptionPane.ERROR_MESSAGE
                );
            }
            case DIRECTORY_NOT_EXISTING -> {
                if (isGuiBeingBuilt) return false;
                String message = getWord("optionPane.directory.the") + " " + getWord("file.directory") + ": '" + directory + "' " +
                        getWord("optionPane.directory.doesNotExisting") + ".";
                JOptionPane.showMessageDialog(
                        this.container,
                        message,
                        getWord("optionPane.title.error"),
                        JOptionPane.ERROR_MESSAGE
                );
            }
            case DIRECTORY_HAS_NO_IMAGES -> {
                if (isGuiBeingBuilt) return false;
                String message = getWord("optionPane.directory.the") + " " + getWord("file.directory") + ": '" + directory + "' " +
                        getWord("optionPane.directory.hasNo") + " " + imageFileType.getDescription() + ".";
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

    // ======================================
    // Observer Registrierung
    // ======================================
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

    // ======================================
    // Observer Ausführen
    // ======================================
    @Override
    public void handleAction(ActionType actionType) {
        // Prüfe, ob der Zeitpunkt angepinnt wurde
        if (actionType == ActionType.SHORTCUT_TOGGLE_PIN_TIME) workspace.togglePinTime();

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
        workspace.checkMissingFiles();

        // Observer ausführen
        for (AppFocusListener observer : appFocusListeners) observer.appGainedFocus();
    }

    @Override
    public void appLostFocus() {
        // Observer ausführen
        for (AppFocusListener observer : appFocusListeners) observer.appLostFocus();
    }

    // ======================================
    // Sonstige Methoden
    // ======================================
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

        // Dateien abspeichern
        settings.save();
        history.save();
        workspace.save();

        // Anwendung beenden
        System.exit(0);
    }
}
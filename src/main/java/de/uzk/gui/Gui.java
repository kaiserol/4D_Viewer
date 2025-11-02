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
    // GUI-Elemente
    private final ActionHandler actionHandler;
    private final DialogImageLoad dialogImageLoad;

    // Observer Listener
    private final List<HandleActionListener> handleActionListeners;
    private final List<ToggleListener> toggleListeners;
    private final List<UpdateImageListener> updateImageListeners;
    private final List<UpdateThemeListener> updateThemeListeners;
    private final List<AppFocusListener> appFocusListeners;

    // Mindestgröße des Fensters
    private static final int MIN_WIDTH = 400;
    private static final int MIN_HEIGHT = 100;

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
        this.dialogImageLoad = new DialogImageLoad(this.container);

        // Gui erstellen
        build();
    }

    private void build() {
        logger.info("Building UI ...");

        // Fenster initialisieren
        initFrame();

        // Menüleiste & Inhalt hinzufügen
        addMenuBar();
        addContent();
        updateTheme();

        // Image-Files laden
        if (!loadImageFiles(history.getLast(), workspace.getConfig().getImageFileType(), true)) {
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

        // mainPanel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // directorySelection
        AreaDirectorySelection directorySelection = new AreaDirectorySelection(this);
        mainPanel.add(directorySelection.getContainer(), BorderLayout.NORTH);

        // splitPane (tabs, imageViewer)
        AreaTabs tabs = new AreaTabs(this);
        AreaImageViewer imageViewer = new AreaImageViewer(this);

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

    // ========================================
    // Hilfsmethoden
    // ========================================
    public void updateUI() {
        this.container.revalidate();
        this.container.repaint();
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
                        getWord("file.directory") + " '" + directory + "' " +
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
                String message = getWord("optionPane.directory.the") + " " + getWord("file.directory") + " '" + directory + "' " +
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
                String message = getWord("optionPane.directory.the") + " " + getWord("file.directory") + " '" + directory + "' " +
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
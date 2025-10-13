package de.uzk.gui;

import de.uzk.action.ActionHandler;
import de.uzk.action.ActionType;
import de.uzk.action.HandleActionListener;
import de.uzk.gui.menubar.AppMenuBar;
import de.uzk.gui.viewer.OViewer;
import de.uzk.image.ImageLayer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
        logger.info("Rebuilding UI...");
        this.container.getContentPane().removeAll();

        // Verhindert, dass alte UI-Objekte weiter existieren (alte Ereignis-Listener müssen bereinigt werden)
        this.handleActionListeners.clear();
        this.toggleListeners.clear();
        this.updateImageListeners.clear();
        this.updateThemeListeners.clear();
        this.appFocusListeners.clear();
        loadUI();
    }

    private void build() {
        logger.info("Building UI...");
        GuiUtils.initFlatLaf();

        this.container.setIconImage(Icons.APP_IMAGE);
        this.container.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.container.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeApp(getFrame(), config::saveConfig);
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

        loadUI();
        this.container.setLocationRelativeTo(null);
        this.container.setVisible(true);
    }

    private void loadUI() {
        initContent();
        handleAction(ActionType.ACTION_LOAD_IMAGES);
        updateTheme();
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

    public JFrame getFrame() {
        return this.container;
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
            imageHandler.togglePinTime();
            actionType = ActionType.ACTION_UPDATE_PIN_TIME;
        }

        for (HandleActionListener listener : handleActionListeners) {
            listener.handleAction(actionType);
        }
    }

    @Override
    public void toggleOn() {
        imageHandler.toFirst(ImageLayer.TIME);
        imageHandler.toFirst(ImageLayer.LEVEL);
        imageHandler.setDefaultPinTime();
        handleAction(ActionType.ACTION_UPDATE_PIN_TIME);

        for (ToggleListener listener : toggleListeners) {
            listener.toggleOn();
        }
        updateUI();
    }

    @Override
    public void toggleOff() {
        imageHandler.clear();
        imageHandler.setDefaultPinTime();
        handleAction(ActionType.ACTION_UPDATE_PIN_TIME);

        for (ToggleListener listener : toggleListeners) {
            listener.toggleOff();
        }
        updateUI();
    }

    @Override
    public void update(ImageLayer layer) {
        if (layer == null) return;
        for (UpdateImageListener listener : updateImageListeners) {
            listener.update(layer);
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
}
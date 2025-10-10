package de.uzk.gui;

import de.uzk.actions.ActionHandler;
import de.uzk.actions.ActionType;
import de.uzk.actions.ActionTypeListener;
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
    private final List<ActionTypeListener> actionTypeListeners;
    private final List<ToggleListener> toggleListeners;
    private final List<UpdateImageListener> updateImageListeners;
    private final List<UpdateUIListener> updateUIListeners;
    private final List<AppFocusListener> appFocusListeners;
    private boolean windowInitialized;

    public Gui() {
        super(new JFrame(getWord("app.name")), null);
        this.actionTypeListeners = new ArrayList<>();
        this.toggleListeners = new ArrayList<>();
        this.updateImageListeners = new ArrayList<>();
        this.updateUIListeners = new ArrayList<>();
        this.appFocusListeners = new ArrayList<>();
        build();
    }

    public void rebuild() {
        logger.info("Rebuilding UI...");
        this.container.getContentPane().removeAll();

        // Prevents that old UI objects are continuing living (old event listeners have to be cleaned)
        this.actionTypeListeners.clear();
        this.toggleListeners.clear();
        this.updateImageListeners.clear();
        this.updateUIListeners.clear();
        this.appFocusListeners.clear();
        loadUI();
    }

    private void build() {
        logger.info("Building UI...");
        GuiUtils.initFlatLaf();

        SwingUtilities.invokeLater(() -> {
            this.container.setIconImage(Icons.APP_IMAGE);
            this.container.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    closeApp(getFrame(), config::saveConfig);
                }
            });
            this.container.setLayout(new BorderLayout());
            this.container.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
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
        });
    }

    private void loadUI() {
        initContent();
        handleAction(ActionType.LOAD_IMAGES);
        updateUI();
        this.container.pack();
    }

    private void initContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        ActionHandler actionHandler = new ActionHandler(this);

        // directory selection
        AreaDirectorySelection directorySelection = new AreaDirectorySelection(this);
        mainPanel.add(directorySelection.getContainer(), BorderLayout.NORTH);

        // splitPane (tabs, viewer)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOneTouchExpandable(true);

        AreaTabs areaTabs = new AreaTabs(this, actionHandler);
        OViewer viewer = new OViewer(this, actionHandler);
        splitPane.add(areaTabs.getContainer());
        splitPane.add(viewer.getContainer());
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // disclaimer
        AreaDisclaimerRightOfUse disclaimer = new AreaDisclaimerRightOfUse(this);
        mainPanel.add(disclaimer.getContainer(), BorderLayout.SOUTH);
        this.container.add(mainPanel);

        // menuBar
        AppMenuBar menuBar = new AppMenuBar(this, actionHandler);
        this.container.setJMenuBar(menuBar.getContainer());
    }

    public JFrame getFrame() {
        return this.container;
    }

    public void addActionTypeListener(ActionTypeListener listener) {
        this.actionTypeListeners.add(listener);
    }

    public void addToggleListener(ToggleListener listener) {
        this.toggleListeners.add(listener);
    }

    public void addUpdateImageListener(UpdateImageListener listener) {
        // Weil sonst während eines update() calls ein weiterer listener hinzugefügt werden könnte
        SwingUtilities.invokeLater(() -> this.updateImageListeners.add(listener));
    }

    public void addUpdateUIListener(UpdateUIListener listener) {
        this.updateUIListeners.add(listener);
    }

    public void addAppFocusListener(AppFocusListener listener) {
        this.appFocusListeners.add(listener);
    }

    @Override
    public void handleAction(ActionType actionType) {
        if (actionType == ActionType.TOGGLE_PIN_TIME) {
            imageHandler.togglePinTime();
            actionType = ActionType.UPDATE_PIN_TIME;
        }

        for (ActionTypeListener listener : actionTypeListeners) {
            listener.handleAction(actionType);
        }
    }

    @Override
    public void toggleOn() {
        imageHandler.toFirst();
        imageHandler.setDefaultPinTime();
        handleAction(ActionType.UPDATE_PIN_TIME);
        handleAction(ActionType.UPDATE_IMAGE);

        for (ToggleListener listener : toggleListeners) {
            listener.toggleOn();
        }
        this.container.revalidate();
    }

    @Override
    public void toggleOff() {
        imageHandler.clear();
        imageHandler.setDefaultPinTime();
        handleAction(ActionType.UPDATE_PIN_TIME);
        handleAction(ActionType.UPDATE_IMAGE);

        for (ToggleListener listener : toggleListeners) {
            listener.toggleOff();
        }
        this.container.revalidate();
    }

    @Override
    public void update(ImageLayer layer) {
        if (layer == null) return;
        for (UpdateImageListener listener : updateImageListeners) {
            listener.update(layer);
        }
        this.container.revalidate();
    }

    @Override
    public void updateUI() {
        for (UpdateUIListener listener : updateUIListeners) {
            listener.updateUI();
        }
    }

    @Override
    public void appGainedFocus() {
        for (AppFocusListener listener : appFocusListeners) {
            listener.appGainedFocus();
        }
    }
}
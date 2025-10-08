package de.uzk.gui;

import de.uzk.actions.ActionType;
import de.uzk.actions.ActionTypeListener;
import de.uzk.gui.others.OImprint;
import de.uzk.gui.others.OMenuBar;
import de.uzk.gui.others.ODirectory;
import de.uzk.gui.tabs.Tabs;
import de.uzk.gui.viewer.OViewer;
import de.uzk.actions.ActionHandler;
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

public class Gui extends InteractiveContainer<JFrame> implements WindowFocusListener, ActionTypeListener {
    private final List<ToggleListener> toggleListeners;
    private final List<UpdateImageListener> updateImageListeners;
    private final List<UpdateUIListener> updateUIListeners;
    private final List<ActionTypeListener> actionTypeListeners;
    private final List<WindowFocusListener> windowFocusListeners;
    private boolean windowInitialized;

    public Gui() {
        super(new JFrame(getWord("app.title")), null);
        this.toggleListeners = new ArrayList<>();
        this.updateImageListeners = new ArrayList<>();
        this.updateUIListeners = new ArrayList<>();
        this.actionTypeListeners = new ArrayList<>();
        this.windowFocusListeners = new ArrayList<>();

        // create frame
        create();
    }

    public void rebuild() {
        this.container.getContentPane().removeAll();
        addContent();
    }

    private void create() {
        logger.info("Creating UI...");
        GuiUtils.initFlatLaf();

        SwingUtilities.invokeLater(() -> {
            this.container.setIconImage(IconUtils.APP_IMAGE);
            this.container.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    closeApplication(getFrame(), container, config::saveConfig);
                }
            });
            this.container.setLayout(new BorderLayout());
            this.container.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            this.container.addWindowFocusListener(new WindowAdapter() {
                @Override
                public void windowGainedFocus(WindowEvent e) {
                    if (windowInitialized) gainedWindowFocus();
                    windowInitialized = true;
                }
            });

            // add content
            addContent();

            // set visible
            this.container.setLocationRelativeTo(null);
            this.container.setVisible(true);
        });
    }

    private void addContent() {
        // init
        initContent();
        // load images -> if there is no path specified, the gui will be toggled off
        handleAction(ActionType.LOAD_IMAGES);
        // updateUI
        updateUI();
        // pack
        this.container.pack();
    }

    private void initContent() {
        // mainPanel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // actionHandler, imageDisplay
        ActionHandler actionHandler = new ActionHandler(this);

        // path
        ODirectory path = new ODirectory(this);
        mainPanel.add(path.getContainer(), BorderLayout.NORTH);

        // splitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOneTouchExpandable(true);

        // tabs
        Tabs tabs = new Tabs(this, actionHandler);
        splitPane.add(tabs.getContainer());

        // viewer
        OViewer viewer = new OViewer(this, actionHandler);
        splitPane.add(viewer.getContainer());

        // splitPane
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // imprint
        OImprint imprint = new OImprint(this);
        mainPanel.add(imprint.getContainer(), BorderLayout.SOUTH);
        this.container.add(mainPanel);

        // menuBar
        OMenuBar menuBar = new OMenuBar(this, actionHandler);
        this.container.setJMenuBar(menuBar.getContainer());
    }

    public JFrame getFrame() {
        return this.container;
    }

    public void addToggleListener(ToggleListener listener) {
        this.toggleListeners.add(listener);
    }

    public void addUpdateImageListener(UpdateImageListener listener) {
        this.updateImageListeners.add(listener);
    }

    public void addUpdateUIListener(UpdateUIListener listener) {
        this.updateUIListeners.add(listener);
    }

    public void addActionTypeListener(ActionTypeListener listener) {
        this.actionTypeListeners.add(listener);
    }

    public void addWindowFocusListener(WindowFocusListener listener) {
        this.windowFocusListeners.add(listener);
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
    public void gainedWindowFocus() {
        for (WindowFocusListener listener : windowFocusListeners) {
            listener.gainedWindowFocus();
        }
    }
}
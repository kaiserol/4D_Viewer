package de.uzk.gui.viewer;

import de.uzk.gui.ActionType;
import de.uzk.gui.ActionTypeListener;
import de.uzk.gui.Gui;
import de.uzk.gui.InteractiveContainer;
import de.uzk.handler.ActionHandler;
import de.uzk.handler.ImageLayer;
import de.uzk.utils.GuiUtils;
import de.uzk.utils.IconUtils;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static de.uzk.Main.imageHandler;
import static de.uzk.utils.GuiUtils.SLIDER_DRAGGED;
import static de.uzk.handler.LanguageHandler.getWord;

public class OViewer extends InteractiveContainer<JPanel> implements ActionTypeListener {
    private final ActionHandler actionHandler;
    private JScrollBar timeBar;
    private JScrollBar levelBar;
    private JLabel pinTimeLabel;
    private JButton clearImagesButton;

    public OViewer(Gui gui, ActionHandler actionHandler) {
        super(new JPanel(), gui);
        this.actionHandler = actionHandler;
        gui.addActionTypeListener(this);
        init();
    }

    private void init() {
        this.container.setLayout(new BorderLayout());
        this.container.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                focusOnImageView(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
                focusOnImageView(false);
            }
        });
        this.container.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!container.isFocusOwner()) {
                    container.requestFocusInWindow();
                }
            }
        });
        this.container.addMouseWheelListener(this.actionHandler);
        this.container.addKeyListener(this.actionHandler);
        this.container.setFocusable(true);

        // statsPanel
        this.container.add(new OStats(gui).getContainer(), BorderLayout.NORTH);

        // imageViewPanel
        JPanel imagePanel = new JPanel(new BorderLayout());

        // timeBar
        timeBar = new JScrollBar(Adjustable.HORIZONTAL);
        initScrollBar(timeBar, ImageLayer.TIME, 10);
        imagePanel.add(timeBar, BorderLayout.NORTH);

        // levelBar
        levelBar = new JScrollBar(Adjustable.VERTICAL);
        initScrollBar(levelBar, ImageLayer.LEVEL, 5);
        imagePanel.add(levelBar, BorderLayout.EAST);

        // imageDisplay
        JPanel imageDisplayBorderPanel = new JPanel(new BorderLayout());
        imageDisplayBorderPanel.setBorder(new EmptyBorder(0, 15, 0, 0));
        imageDisplayBorderPanel.add(new OImager(gui).getContainer(), BorderLayout.CENTER);
        imagePanel.add(imageDisplayBorderPanel, BorderLayout.CENTER);

        // checkPointLabel
        JToolBar southToolBar = new JToolBar();
        southToolBar.setFloatable(false);
        southToolBar.setLayout(new BorderLayout());

        // deleteButton
        this.clearImagesButton = new JButton(IconUtils.DELETE_ICON);
        this.clearImagesButton.addActionListener(e -> clearImages());
        this.clearImagesButton.setToolTipText(getWord("tooltips.clearImages"));
        southToolBar.add(this.clearImagesButton, BorderLayout.WEST);

        this.pinTimeLabel = new JLabel();
        this.pinTimeLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.pinTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        southToolBar.add(this.pinTimeLabel);

        // warningButton
        // TODO: muss entfernt werden und in die MenuBar verschoben werden
        JButton logsButton = new JButton("[Info]");
        logsButton.setToolTipText(getWord("tooltips.showInformation"));
        logsButton.addActionListener(e -> new OInfo(gui));
        southToolBar.add(logsButton, BorderLayout.EAST);
        imagePanel.add(southToolBar, BorderLayout.SOUTH);
        this.container.add(imagePanel, BorderLayout.CENTER);
    }

    private void focusOnImageView(boolean focus) {
        Color color = focus ? GuiUtils.FOCUS_COLOR : GuiUtils.getBorderColor();
        this.container.setBorder(new MatteBorder(2, 2, 2, 2, color));
    }

    private void clearImages() {
        int choice = JOptionPane.showConfirmDialog(gui.getFrame(),
                getWord("optionPane.clearAllImagesMsg"), getWord("optionPane.titles.confirm"),
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) gui.toggleOff();
        else this.container.requestFocusInWindow();
    }

    private void initScrollBar(JScrollBar scrollBar, ImageLayer layer, int blockIncrement) {
        if (layer == null) return;
        scrollBar.addAdjustmentListener(e -> {
            if (GuiUtils.isEnabled(scrollBar)) {
                int value = scrollBar.getValue();
                boolean isDragging = scrollBar.getName() != null && scrollBar.getName().equals(SLIDER_DRAGGED);
                boolean isAdjusting = scrollBar.getValueIsAdjusting();

                // can be invoked by dragging the scrollbar or pressing the buttons
                update(scrollBar, layer, value, isDragging, isAdjusting);
            }
        });
        scrollBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                scrollBar.setName(SLIDER_DRAGGED);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                scrollBar.setName(null);
            }
        });
        scrollBar.setBlockIncrement(blockIncrement);
    }

    private void update(JScrollBar scrollBar, ImageLayer layer, int newValue, boolean isDragged, boolean isAdjusting) {
        if (imageHandler.isEmpty()) return;

        int oldValue = layer == ImageLayer.TIME ? imageHandler.getTime() : imageHandler.getLevel();
        if (oldValue == newValue) return;

        boolean mouseReleased = isDragged && !isAdjusting;
        if (mouseReleased) updateScrollBar(scrollBar, oldValue);
        else {
            if (layer == ImageLayer.TIME) imageHandler.setTime(newValue, !isDragged);
            else imageHandler.setLevel(newValue, !isDragged);
            gui.update(layer);
        }
    }

    @Override
    public void toggleOn() {
        updateScrollBars(imageHandler.getTime(), imageHandler.getMaxTime(), imageHandler.getLevel(), imageHandler.getMaxLevel());
        enableViewer(true);
    }

    @Override
    public void toggleOff() {
        updateScrollBars(0, 0, 0, 0);
        enableViewer(false);
    }

    @Override
    public void updateUI() {
        focusOnImageView(this.container.isFocusOwner());
    }

    private void updateScrollBars(int time, int maxTime, int level, int maxLevel) {
        GuiUtils.updateSecretly(timeBar, () -> timeBar.setValues(time, 0, 0, maxTime));
        GuiUtils.updateSecretly(levelBar, () -> levelBar.setValues(level, 0, 0, maxLevel));
    }

    private void updateScrollBar(JScrollBar scrollBar, int value) {
        GuiUtils.updateSecretly(scrollBar, () -> scrollBar.setValue(value));
    }

    private void enableViewer(boolean enabled) {
        this.timeBar.setEnabled(enabled);
        this.levelBar.setEnabled(enabled);
        this.clearImagesButton.setEnabled(enabled);
    }

    @Override
    public void update(ImageLayer layer) {
        // updates the scrollBar if not moving anymore
        if (layer == ImageLayer.TIME) {
            if (!timeBar.getValueIsAdjusting()) updateScrollBar(timeBar, imageHandler.getTime());
            gui.handleAction(ActionType.UPDATE_PIN_TIME);
        } else {
            if (!levelBar.getValueIsAdjusting()) updateScrollBar(levelBar, imageHandler.getLevel());
        }
    }

    @Override
    public void handleAction(ActionType actionType) {
        if (actionType == ActionType.UPDATE_PIN_TIME) {
            updatePinTime();
        }
    }

    private void updatePinTime() {
        if (imageHandler.hasPinTime()) {
            double multiplier = imageHandler.getTimeUnit();
            int duration = imageHandler.getPinTime() - imageHandler.getTime();

            String factorString;
            if (duration == 0) factorString = "";
            else factorString = duration > 0 ? "+" : "-";

            this.pinTimeLabel.setText(getWord("items.edit.pinTime") + ": " +
                    StringUtils.formatTime(imageHandler.getPinTime(), multiplier) +
                    " (" + factorString + StringUtils.formatTime(Math.abs(duration), multiplier) + ")");
        } else {
            this.pinTimeLabel.setText(null);
        }
    }
}

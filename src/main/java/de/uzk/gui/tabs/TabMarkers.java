package de.uzk.gui.tabs;

import de.uzk.action.ActionType;
import de.uzk.gui.Gui;
import de.uzk.gui.marker.MarkerEditor;
import de.uzk.gui.marker.MarkerInfo;
import de.uzk.gui.observer.ObserverContainer;
import de.uzk.image.Axis;
import de.uzk.markers.Marker;
import de.uzk.utils.ComponentUtils;

import javax.swing.*;
import java.awt.*;

import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

public class TabMarkers extends ObserverContainer<JPanel> {
    public TabMarkers(Gui gui) {
        super(new JPanel(), gui);
        this.rerender();
    }

    private void rerender() {
        java.util.List<Marker> currentMarkers =  workspace.getMarkers().getAllMarkers();

        this.container.removeAll();
        this.container.setLayout(new BorderLayout());

        JButton add = new JButton(getWord("menu.markers.addMarker"));
        add.addActionListener(e -> {
            MarkerEditor initial = new MarkerEditor(workspace.getCurrentImageFile());
            int option = JOptionPane.showConfirmDialog(
                    gui.getContainer(),
                    initial,
                    getWord("dialog.markers.newMarker"),
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (option == JOptionPane.OK_OPTION) {
                workspace.getMarkers().addMarker(initial.getMarker());
                gui.handleAction(ActionType.ACTION_ADD_MARKER);
                gui.updateUI();
            }
        });

        this.container.add(add, BorderLayout.SOUTH);

        if (!currentMarkers.isEmpty()) {
            Box panel = new Box(BoxLayout.Y_AXIS);
            for (Marker currentMarker : currentMarkers) {
                JPanel next = new MarkerInfo(currentMarker, this.gui);

                panel.add(next);
                panel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
            this.container.add(new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        } else {
            JLabel noneLabel = new JLabel(getWord("menu.markers.noMarkersSet"));
            noneLabel.setHorizontalAlignment(SwingConstants.CENTER);
            this.container.add(noneLabel, BorderLayout.CENTER);
        }
    }

    @Override
    public void handleAction(ActionType actionType) {
        if (actionType == ActionType.ACTION_ADD_MARKER || actionType == ActionType.ACTION_REMOVE_MARKER) {
            this.rerender();
        }
    }

    @Override
    public void toggleOn() {
        ComponentUtils.setEnabled(this.container, true);
    }

    @Override
    public void toggleOff() {
        ComponentUtils.setEnabled(this.container, false);
    }

    @Override
    public void update(Axis axis) {
        if (axis == Axis.TIME) {
            this.rerender();
        }
    }
}

package de.uzk.gui.tabs;

import de.uzk.action.ActionType;
import de.uzk.gui.AreaContainerInteractive;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.gui.marker.MarkerEditor;
import de.uzk.gui.marker.MarkerMappingInfo;
import de.uzk.image.Axis;
import de.uzk.markers.MarkerMapping;

import javax.swing.*;
import java.awt.*;

import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

public class TabMarkers extends AreaContainerInteractive<JPanel> {
    public TabMarkers(Gui gui) {
        super(new JPanel(), gui);
        this.rerender();
    }

    private void rerender() {
        java.util.List<MarkerMapping> currentMarkers =  workspace.getMarkers().getAllMarkers();

        this.container.removeAll();
        this.container.setLayout(new BorderLayout());

        JButton add = new JButton(getWord("items.markers.addMarker"));
        add.addActionListener(e -> {
            MarkerEditor initial = new MarkerEditor(workspace.getImageFile());
            int option = JOptionPane.showConfirmDialog(
                    null,
                    initial,
                    getWord("dialog.markers.newMarker"),
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (option == JOptionPane.OK_OPTION) {
                workspace.getMarkers().addMarker(initial.getMarker(), workspace.getTime());
                gui.handleAction(ActionType.ACTION_ADD_MARKER);
                gui.updateUI();
            }
        });

        this.container.add(add, BorderLayout.SOUTH);

        if (!currentMarkers.isEmpty()) {
            Box panel = new Box(BoxLayout.Y_AXIS);
            for (MarkerMapping currentMarker : currentMarkers) {
                JPanel next = new MarkerMappingInfo(currentMarker, this.gui);
                next.setBorder(BorderFactory.createEmptyBorder());
                panel.add(next);
            }


            this.container.add(panel, BorderLayout.CENTER);
        } else {
            JLabel noneLabel = new JLabel(getWord("items.markers.noMarkersSet"));
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
        GuiUtils.setEnabled(this.container, true);
    }

    @Override
    public void toggleOff() {
        GuiUtils.setEnabled(this.container, false);
    }

    @Override
    public void update(Axis axis) {
        if (axis == Axis.TIME) {
            this.rerender();
        }
    }
}

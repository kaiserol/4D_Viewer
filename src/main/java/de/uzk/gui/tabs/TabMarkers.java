package de.uzk.gui.tabs;

import de.uzk.action.ActionType;
import de.uzk.gui.AreaContainerInteractive;
import de.uzk.gui.Gui;
import de.uzk.gui.marker.MarkerEditor;
import de.uzk.gui.marker.MarkerMappingInfo;
import de.uzk.image.ImageLayer;
import de.uzk.markers.MarkerMapping;

import javax.swing.*;
import java.awt.*;

import static de.uzk.Main.imageHandler;
import static de.uzk.Main.markerHandler;
import static de.uzk.config.LanguageHandler.getWord;

public class TabMarkers extends AreaContainerInteractive<JPanel> {
    public TabMarkers(Gui gui) {
        super(new JPanel(), gui);
        this.rerender();
    }

    private void rerender() {

        java.util.List<MarkerMapping> currentMarkers = markerHandler.getMarkers();

        this.container.removeAll();
        this.container.setLayout(new BorderLayout());



        if (!currentMarkers.isEmpty()) {
            Box panel = new Box(BoxLayout.Y_AXIS);
            for (MarkerMapping currentMarker : currentMarkers) {
                JPanel next = new MarkerMappingInfo(currentMarker, this.gui).getContainer();
                next.setBorder(BorderFactory.createEmptyBorder());
                panel.add(next);
            }


            this.container.add(panel, BorderLayout.CENTER);
        } else {
            JLabel noneLabel = new JLabel(getWord("items.markers.noMarkersSet"));
            noneLabel.setHorizontalAlignment(SwingConstants.CENTER);
            this.container.add( noneLabel, BorderLayout.CENTER);
        }

        JButton add = new JButton(getWord("items.markers.addMarker"));
        add.addActionListener(e -> {
            MarkerEditor initial = new MarkerEditor(imageHandler.getCurrentImage());
            int option = JOptionPane.showConfirmDialog(
                    null,
                    initial,
                    getWord("dialog.markers.newMarker"),
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (option == JOptionPane.OK_OPTION) {
                markerHandler.addMarker(initial.getMarker(), imageHandler.getTime());
                gui.handleAction(ActionType.ACTION_ADD_MARKER);
                gui.updateUI();
            }

        });

        this.container.add(add, BorderLayout.SOUTH);

    }






    @Override
    public void handleAction(ActionType actionType) {
        if(actionType == ActionType.ACTION_ADD_MARKER || actionType == ActionType.ACTION_REMOVE_MARKER) {
            this.rerender();
        }
    }

    @Override
    public void update(ImageLayer layer) {
        if(layer == ImageLayer.TIME) {
            this.rerender();
        }
    }
}

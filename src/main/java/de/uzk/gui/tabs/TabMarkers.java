package de.uzk.gui.tabs;

import de.uzk.actions.ActionType;
import de.uzk.actions.ActionTypeListener;
import de.uzk.gui.Gui;
import de.uzk.gui.UpdateImageListener;
import de.uzk.gui.marker.MarkerEditor;
import de.uzk.gui.marker.MarkerMappingInfo;
import de.uzk.image.ImageLayer;
import de.uzk.markers.MarkerMapping;

import javax.swing.*;
import java.awt.*;

import static de.uzk.Main.imageHandler;
import static de.uzk.Main.markerHandler;

public class TabMarkers extends CustomTab implements ActionTypeListener, UpdateImageListener {



    public TabMarkers(Gui gui) {
        super(new JPanel(), gui);
        gui.addUpdateImageListener(this);
        gui.addActionTypeListener(this);
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
            JLabel noneLabel = new JLabel("No markers set.");
            noneLabel.setHorizontalAlignment(SwingConstants.CENTER);
            this.container.add( noneLabel, BorderLayout.CENTER);
        }

        JButton add = new JButton("Add Marker to current image");
        add.addActionListener(e -> {
            MarkerEditor initial = new MarkerEditor(imageHandler.getCurrentImage());
            int option = JOptionPane.showConfirmDialog(null, initial, "New Marker", JOptionPane.OK_CANCEL_OPTION);
            if(option == JOptionPane.OK_OPTION) {
                markerHandler.addMarker(initial.getMarker(), imageHandler.getTime());
                gui.handleAction(ActionType.ADD_MARKER);
                gui.updateUI();
            }

        });

        this.container.add(add, BorderLayout.SOUTH);

    }






    @Override
    public void handleAction(ActionType actionType) {
        if(actionType == ActionType.ADD_MARKER  || actionType == ActionType.REMOVE_MARKER) {
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

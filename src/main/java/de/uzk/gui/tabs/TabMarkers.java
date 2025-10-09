package de.uzk.gui.tabs;

import de.uzk.actions.ActionType;
import de.uzk.actions.ActionTypeListener;
import de.uzk.gui.Gui;
import de.uzk.gui.OGridBagConstraints;
import de.uzk.gui.UpdateImageListener;
import de.uzk.gui.marker.MarkerEditor;
import de.uzk.image.ImageLayer;
import de.uzk.markers.Marker;

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

        Marker currentMarker = markerHandler.getMarker(imageHandler.getTime());

        this.container.removeAll();


        if (currentMarker != null) {
            this.container.setLayout(new BorderLayout());
            this.container.add(new JLabel("Edit marker"), BorderLayout.NORTH);
            JButton remove = new JButton("Remove marker");
            this.container.add(remove, BorderLayout.SOUTH);
        } else {
            this.container.setLayout(new GridBagLayout());
            OGridBagConstraints gbc = new OGridBagConstraints();

            gbc.setSizeAndWeight(1, 1, 10.0, 0.0);
            gbc.setPosAndInsets(0, 0, 0, 0, 0, 0);
            gbc.anchor = OGridBagConstraints.FIRST_LINE_START;
            JButton add = new JButton("Add Marker to current image");
            add.addActionListener(e -> {
                MarkerEditor initial = new MarkerEditor(imageHandler.getCurrentImage());
                int option = JOptionPane.showConfirmDialog(null, initial, "New Marker", JOptionPane.OK_CANCEL_OPTION);
                if(option == JOptionPane.OK_OPTION) {
                    markerHandler.addMarker(initial.getMarker());
                    gui.handleAction(ActionType.ADD_MARKER);
                    gui.updateUI();
                }

            });

            this.container.add(add, gbc);

            gbc.setPos(0, 10);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.setSizeAndWeight(0, 10, 0, 10);
            this.container.add(new JLabel("No markers on the current image"), gbc);


        }

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

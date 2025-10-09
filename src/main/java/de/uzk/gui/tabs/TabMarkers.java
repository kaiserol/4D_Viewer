package de.uzk.gui.tabs;

import de.uzk.actions.ActionType;
import de.uzk.actions.ActionTypeListener;
import de.uzk.gui.Gui;
import de.uzk.gui.OGridBagConstraints;
import de.uzk.gui.UpdateUIListener;
import de.uzk.gui.others.MarkerAppearanceSelector;
import de.uzk.markers.Marker;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

import static de.uzk.Main.imageHandler;
import static de.uzk.Main.markerHandler;

public class TabMarkers extends CustomTab implements  UpdateUIListener {

    private Marker currentMarker;

    public TabMarkers(Gui gui) {
        super(new JPanel(), gui);
        gui.addUpdateUIListener(this);
        this.rerender();
    }

    private void rerender() {


        this.container.removeAll();

        if (currentMarker != null) {
            this.container.setLayout(new BorderLayout());
            this.container.add(new JLabel("Edit marker"), BorderLayout.NORTH);

            JPanel valuesPanel = this.createValuesPanel(currentMarker);

            this.container.add(valuesPanel, BorderLayout.CENTER);

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
                MarkerAppearanceSelector initial = new MarkerAppearanceSelector();
                int option = JOptionPane.showConfirmDialog(null, initial, "New Marker", JOptionPane.OK_CANCEL_OPTION);
                if(option != JOptionPane.CANCEL_OPTION) {
                    markerHandler.addMarker(new Marker(10, 10, 100, 100, initial.getShape(), initial.getColor()));
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

    private JSpinner createNumberField(int initialValue, Consumer<Integer> setter) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(initialValue, 0, Integer.MAX_VALUE, 1));

        spinner.addChangeListener(e -> {
            setter.accept((Integer) spinner.getModel().getValue());
            gui.updateUI();
        });
        return spinner;
    }

    private JPanel createValuesPanel(Marker marker) {
        JPanel valuesPanel = new JPanel(new GridBagLayout());

        OGridBagConstraints gbc = new OGridBagConstraints();
        gbc.ipady = 10;

        JLabel[] labels = {new JLabel("X: "), new JLabel("Y: "), new JLabel("Width: "), new JLabel("Height: "),};

        JSpinner[] spinners = {createNumberField(marker.getX(), marker::setX), createNumberField(marker.getY(), marker::setY), createNumberField(marker.getWidth(), marker::setWidth), createNumberField(marker.getHeight(), marker::setHeight)};

        gbc.fill = OGridBagConstraints.HORIZONTAL;
        gbc.setSizeAndWeight(1, 1, 0.3, 0);

        int y = 0;
        for (; y < labels.length; y++) {
            gbc.gridy = y;

            gbc.gridx = 0;
            gbc.weightx = 0.3;
            valuesPanel.add(labels[y], gbc);

            gbc.gridx = 1;
            gbc.weightx = 0.7;
            valuesPanel.add(spinners[y], gbc);
        }

        return valuesPanel;
    }




    @Override
    public void updateUI() {

        Marker current = markerHandler.getMarker(imageHandler.getTime());
        if(current != this.currentMarker) {
            this.currentMarker = current;
            this.rerender();
        }
    }
}

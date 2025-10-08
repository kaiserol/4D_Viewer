package de.uzk.gui.tabs;

import de.uzk.actions.ActionType;
import de.uzk.actions.ActionTypeListener;
import de.uzk.gui.*;
import de.uzk.markers.Marker;
import de.uzk.markers.RectMarker;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

import static de.uzk.Main.imageHandler;
import static de.uzk.Main.markerHandler;

public class MarkersTab extends TabContent implements ActionTypeListener {

    public MarkersTab( Gui gui) {
        super(new JPanel(), gui);
        gui.addActionTypeListener(this);
        this.render();
    }

    private void render() {
        this.container.removeAll();




        Marker currentMarker = markerHandler.getMarker(imageHandler.getTime());
        if(currentMarker != null) {
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
            gbc.setPosAndInsets(0,0,0,0,0,0);
            gbc.anchor = OGridBagConstraints.FIRST_LINE_START;
            JButton add = new JButton("Add Marker to current image");
            add.addActionListener(e -> {
                markerHandler.addMarker(new RectMarker(10, 10, 100, 100));
                gui.handleAction(ActionType.ADD_MARKER);
                gui.updateUI();
            });

            this.container.add(add, gbc);

            gbc.setPos(0,10);
            gbc.fill = GridBagConstraints.BOTH;
            gbc.setSizeAndWeight(0, 10, 0, 10);
            this.container.add(new JLabel("No markers on the current image"), gbc);
        }

    }

    private JSpinner createNumberField(int initialValue, Consumer<Integer> setter) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(initialValue,0, Integer.MAX_VALUE, 1));

        spinner.addChangeListener(e -> {
            setter.accept((Integer)spinner.getModel().getValue());
            gui.updateUI();
        });
        return spinner;
    }

    private JPanel createValuesPanel(Marker marker) {
        JPanel valuesPanel = new JPanel(new GridBagLayout());

        OGridBagConstraints gbc = new OGridBagConstraints();
        gbc.ipady = 10;

        JLabel[] labels = {
                new JLabel("X: "),
                new JLabel("Y: "),
                new JLabel("Width: "),
                new JLabel("Height: "),
        };

        JSpinner[] spinners = {
                createNumberField(marker.getX(), marker::setX),
                createNumberField(marker.getY(),marker::setY),
                createNumberField(marker.getWidth(), marker::setWidth),
                createNumberField(marker.getHeight(), marker::setHeight)
        };

        gbc.fill = OGridBagConstraints.HORIZONTAL;
        gbc.setSizeAndWeight(1,1,0.3,0);

        int y = 0;
        for(;y < labels.length; y++) {
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
    public void handleAction(ActionType actionType) {
        if (actionType == ActionType.ADD_MARKER || actionType == ActionType.REMOVE_MARKER) {
            this.render();
        }
    }
}

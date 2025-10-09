package de.uzk.gui.marker;

import de.uzk.gui.*;
import de.uzk.image.ImageFile;
import de.uzk.markers.Marker;
import de.uzk.markers.MarkerShape;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class MarkerEditor extends Container {
    private final Marker marker;
    private final MarkerPreview preview;
    private final java.util.List<Runnable> onUpdate = new ArrayList<>();

    public MarkerEditor(ImageFile onto) {
        this.marker = new Marker(0, 0, 100, 100, MarkerShape.RECTANGLE, Color.RED);

        this.preview = new MarkerPreview(Icons.loadImage(onto.getFile()), marker, this);
        init();
    }

    public Marker getMarker() {
        return this.marker;
    }

    private void init() {

        this.setLayout(new GridBagLayout());
        OGridBagConstraints gbc = new OGridBagConstraints();
        gbc.fill = GridBagConstraints.NONE;

        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.setPos(1, 0);
        gbc.weightx = 0.1;
        this.add(new JLabel("Shape: "), gbc);

        gbc.setPos(2, 0);
        gbc.weightx = 0.2;
        JComboBox<MarkerShape> selectShape = new JComboBox<>(MarkerShape.values());
        selectShape.setSelectedItem(this.marker.getShape());
        selectShape.addActionListener(l -> {
            this.marker.setShape((MarkerShape) selectShape.getSelectedItem());
            this.preview.repaint();
        });
        this.add(selectShape, gbc);


        gbc.setPos(1, 1);
        gbc.weightx = 0.1;
        this.add(new JLabel("Color: "), gbc);

        gbc.setPos(2, 1);
        gbc.weightx = 0.2;
        gbc.ipady = 10;
        gbc.ipadx = 30;
        JButton color = new JButton("");
        color.setBackground(this.marker.getColor());
        color.addActionListener(a -> {
            Color newColor = JColorChooser.showDialog(null, "Color", this.marker.getColor());
            if (newColor != null) {
                color.setBackground(newColor);
                this.marker.setColor(newColor);
                this.preview.repaint();
            }
        });
        this.add(color, gbc);

        gbc.setPos(1, 2);
        gbc.setSizeAndWeight(2, 4, 0.3, 0);
        JPanel valuesPanel = this.createValuesPanel();
        this.add(valuesPanel, gbc);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.setPos(0, 0);

        gbc.setSizeAndWeight(1, 3, 0.7, 1.0);
        this.add(this.preview, gbc);

    }

    void changed() {
        this.onUpdate.forEach(Runnable::run);
    }

    private JPanel createValuesPanel() {
        JPanel valuesPanel = new JPanel(new GridBagLayout());

        OGridBagConstraints gbc = new OGridBagConstraints();
        gbc.ipady = 10;

        JLabel[] labels = {new JLabel("X: "), new JLabel("Y: "), new JLabel("Width: "), new JLabel("Height: "),};

        JSpinner[] spinners = {
                createSpinner(this.marker::getX, this.marker::setX),
                createSpinner(this.marker::getY, this.marker::setY),
                createSpinner(this.marker::getWidth, this.marker::setWidth),
                createSpinner(this.marker::getHeight, this.marker::setHeight)
        };

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

    private JSpinner createSpinner(IntSupplier get, IntConsumer set) {
        SpinnerNumberModel model = new SpinnerNumberModel(get.getAsInt(), 0, Integer.MAX_VALUE, 1);
        JSpinner spinner = new JSpinner(model);

        spinner.addChangeListener(e -> {
            set.accept((Integer) spinner.getModel().getValue());
            this.preview.repaint();
        });

        this.onUpdate.add(() -> model.setValue(get.getAsInt()));

        return spinner;
    }
}
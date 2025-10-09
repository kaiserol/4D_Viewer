package de.uzk.gui.others;

import de.uzk.gui.Gui;
import de.uzk.gui.InteractiveContainer;
import de.uzk.gui.OGridBagConstraints;
import de.uzk.markers.MarkerShape;

import javax.swing.*;
import java.awt.*;

public class MarkerAppearanceSelector extends Container {
    private Color color = Color.RED;
    private MarkerShape shape = MarkerShape.RECTANGLE;

    public MarkerAppearanceSelector() {
        init();
    }

    public Color getColor() {
        return this.color;
    }

    public MarkerShape getShape() {
        return this.shape;
    }

    private void init() {

        this.setLayout(new GridBagLayout());
        OGridBagConstraints gbc = new OGridBagConstraints();

        gbc.setPos(0, 0);
        gbc.weightx = 0.4;
        this.add(new JLabel("Shape: "), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        JComboBox<MarkerShape> selectShape = new JComboBox<>(MarkerShape.values());
        selectShape.setSelectedItem(this.shape);
        selectShape.addActionListener(l -> this.shape = (MarkerShape) selectShape.getSelectedItem());
        this.add(selectShape, gbc);

        gbc.setPos(0, 1);
        gbc.weightx = 0.4;
        this.add(new JLabel("Color: "), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.6;
        gbc.ipady = 10;
        gbc.ipadx = 30;
        JButton color = new JButton("");
        color.setBackground(this.color);
        color.addActionListener(a -> {
            Color newColor = JColorChooser.showDialog(null, "Color", this.color);
            if (newColor != null) {
                color.setBackground(newColor);
                this.color = newColor;
            }
        });
        this.add(color, gbc);

    }
}
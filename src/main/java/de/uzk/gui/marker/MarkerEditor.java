package de.uzk.gui.marker;

import de.uzk.gui.Icons;
import de.uzk.gui.OGridBagConstraints;
import de.uzk.image.ImageFile;
import de.uzk.markers.Marker;
import de.uzk.markers.MarkerShape;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import static de.uzk.config.LanguageHandler.getWord;

public class MarkerEditor extends Container {
    private final Marker marker;
    private final MarkerPreview preview;
    private final java.util.List<Runnable> onUpdate = new ArrayList<>();
    private final JColorChooser colorChooser;

    public MarkerEditor(ImageFile onto) {
        this(onto, new Marker());
    }

    public MarkerEditor(ImageFile onto, Marker marker) {
        this.marker = marker;
        this.preview = new MarkerPreview(Icons.loadImage(onto.getPath(), true), marker, this);
        this.colorChooser = new JColorChooser();
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
        this.add(new JLabel(getWord("dialog.markers.shape")), gbc);

        gbc.setPos(2, 0);
        gbc.weightx = 0.2;
        JComboBox<MarkerShape> selectShape = new JComboBox<>(MarkerShape.sortedValues());
        selectShape.setSelectedItem(this.marker.getShape());
        selectShape.addActionListener(l -> {
            this.marker.setShape((MarkerShape) selectShape.getSelectedItem());
            this.preview.repaint();
        });
        this.add(selectShape, gbc);


        gbc.setPos(1, 1);
        gbc.weightx = 0.1;
        this.add(new JLabel(getWord("dialog.markers.color")), gbc);

        gbc.setPos(2, 1);
        gbc.weightx = 0.2;
        gbc.ipady = 10;
        gbc.ipadx = 30;
        JButton color = new JButton("");
        color.setBackground(this.marker.getColor());
        color.addActionListener(a -> openColorChooserDialog(color));
        this.add(color, gbc);

        gbc.setPos(1, 2);
        gbc.setSizeAndWeight(1, 1, 0.1, 0);
        gbc.weightx = 0.1;
        this.add(new JLabel(getWord("dialog.markers.label")), gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.2;
        JTextField nameInput = new JTextField(this.marker.getLabel());
        nameInput.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateName();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateName();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateName();
            }

            private void updateName() {
                marker.setLabel(nameInput.getText());
                preview.repaint();
            }
        });
        this.add(nameInput, gbc);

        gbc.setPos(1, 3);
        gbc.setSizeAndWeight(2, 4, 0.3, 0);
        JPanel valuesPanel = this.createValuesPanel();
        this.add(valuesPanel, gbc);


        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.setPos(0, 0);

        gbc.setSizeAndWeight(1, 4, 0.7, 1.0);
        this.add(this.preview, gbc);

    }

    private void openColorChooserDialog(JButton color) {
        this.colorChooser.setColor(color.getBackground());
        ActionListener okListener = e -> {
            Color selected = this.colorChooser.getColor();
            color.setBackground(selected);
            this.marker.setColor(selected);
            this.preview.repaint();
        };

        // Dialog anzeigen
        JDialog dialog = JColorChooser.createDialog(this, getWord("dialog.markers.color"),
                true, this.colorChooser, okListener, null
        );
        dialog.setVisible(true);
    }

    void changed() {
        this.onUpdate.forEach(Runnable::run);
    }

    private JPanel createValuesPanel() {
        JPanel valuesPanel = new JPanel(new GridBagLayout());
        OGridBagConstraints gbc = new OGridBagConstraints();
        gbc.ipady = 10;

        JLabel[] labels = {new JLabel("X: "), new JLabel("Y: "), new JLabel(getWord("dialog.markers.width")), new JLabel(getWord("dialog.markers.height")),};
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
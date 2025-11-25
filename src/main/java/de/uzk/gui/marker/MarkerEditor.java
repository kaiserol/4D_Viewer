package de.uzk.gui.marker;

import de.uzk.gui.UIEnvironment;
import de.uzk.gui.dialogs.DialogColorChooser;
import de.uzk.image.ImageFile;
import de.uzk.markers.Marker;
import de.uzk.utils.DateTimeUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.Consumer;

import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

public class MarkerEditor extends Container {
    private final Marker marker;
    private final java.util.List<Runnable> onUpdate = new ArrayList<>();
    private final DialogColorChooser dialogColorChooser;

    public MarkerEditor(ImageFile onto) {
        this(onto, new Marker());
    }

    public MarkerEditor(ImageFile onto, Marker marker) {
        this.marker = marker;
         this.dialogColorChooser = new DialogColorChooser(null);
        init();
    }

    public Marker getMarker() {
        return this.marker;
    }

    private void init() {
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = UIEnvironment.INSETS_DEFAULT;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        this.add(new JLabel(getWord("dialog.markers.label")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        this.add(getLabelInput(), gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        this.add(new JLabel(getWord("dialog.markers.color")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        this.add(getColorButton(), gbc);

        TimeInput fromInput = new TimeInput(marker.getFrom(), 0, marker.getTo());
        TimeInput toInput = new TimeInput(marker.getTo(), marker.getFrom(), workspace.getMaxTime());

        fromInput.onChange(value -> {
            this.marker.setFrom(value);
            toInput.setMinimum(value);
        });
        toInput.onChange(value -> {
            this.marker.setTo(value);
            fromInput.setMaximum(value);
        });

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        this.add(new JLabel(getWord("menu.markers.visibleFromImage")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        this.add(fromInput, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        this.add(new JLabel(getWord("menu.markers.visibleToImage")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        this.add(toInput, gbc);

    }

    private static class TimeInput extends JPanel {

        private final SpinnerNumberModel model;
        private transient Consumer<Integer> changed;

        public TimeInput(int value , int minTime, int maxTime) {
            model = new SpinnerNumberModel(value, minTime, maxTime, 1);
            init();
        }

        private void init() {
            setLayout(new GridLayout(1, 2));

            JSpinner numberInput = new JSpinner(this.model);
            JLabel label = new JLabel("(" + DateTimeUtils.formatFrameTimeStamp(getValue()) + ")", SwingConstants.RIGHT);
            numberInput.addChangeListener(e -> {
                label.setText("(" + DateTimeUtils.formatFrameTimeStamp(getValue()) + ")");
                changed.accept(getValue());
            });

            add(numberInput);
            add(label);
        }

        public void onChange(Consumer<Integer> consumer) {
            changed = consumer;
        }

        public int getValue() {
            return model.getNumber().intValue();
        }

        public void setValue(int value) {
            if(value >= (Integer)model.getMinimum() && value <= (Integer)model.getMaximum()) {
                model.setValue(value);
                if(changed != null) {
                    changed.accept(value);
                }
            }
        }

        public void setMinimum(int min) {
            model.setMinimum(min);
            if(getValue() < min) {
                setValue(min);
            }
        }

        public void setMaximum(int max) {
            model.setMaximum(max);
            if(getValue() > max) {
                setValue(max);
            }
        }
    }

    private JTextField getLabelInput() {
        JTextField nameInput = new JTextField(marker.getLabel());
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

            }
        });
        return nameInput;
    }

    private JButton getColorButton() {
        JButton button = new JButton();
        button.setBackground(this.marker.getColor());
        button.addActionListener(a -> selectColor(button));
        return button;
    }

    private void selectColor(JButton color) {
        Color selected = this.dialogColorChooser.chooseColor(this.marker.getColor());
        color.setBackground(selected);
        this.marker.setColor(selected);
    }

    void changed() {
        this.onUpdate.forEach(Runnable::run);
    }

}
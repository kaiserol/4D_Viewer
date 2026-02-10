package de.uzk.gui.marker;

import de.uzk.gui.UIEnvironment;
import de.uzk.gui.dialogs.DialogColorChooser;
import de.uzk.markers.Marker;
import de.uzk.markers.interactions.MarkerInteractionHandler;
import de.uzk.utils.DateTimeUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.function.Consumer;

import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

/**
 * Dialog zum Bearbeiten von Markereigenschaften.
 * Diese Klasse ist nur für "selten" veränderte Eigenschaften wie Farbe, Name etc. zuständig
 * Markerposition, -größe und -rotation finden in {@link MarkerInteractionHandler} statt.
 *
 */
public class MarkerEditor extends Container {
    private final DialogColorChooser dialogColorChooser;
    protected Marker marker;
    protected GridBagConstraints gbc;


    public MarkerEditor(Marker marker) {
        this.marker = marker;
        this.dialogColorChooser = new DialogColorChooser(null);
        init();
    }

    public Marker getMarker() {
        return this.marker;
    }

    protected void init() {
        this.setLayout(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = UIEnvironment.INSETS_DEFAULT;

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        add(new JLabel(getWord("dialog.markers.kind")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(getKindInput(), gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        this.add(new JLabel(getWord("dialog.markers.color")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        this.add(getColorButton(), gbc);


        TimeInput fromInput = new TimeInput(marker.getFrom(), 0, marker.getTo());
        TimeInput toInput = new TimeInput(marker.getTo(), marker.getFrom(), workspace.getMaxTime());

        // Diese beiden Handler stellen sicher, dass from <= to
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

    private JComboBox<MarkerKind> getKindInput() {
        JComboBox<MarkerKind> list = new JComboBox<>(MarkerKind.values());

        list.setSelectedItem(MarkerKind.fromMarker(marker));
        list.addItemListener(e -> {
            MarkerKind kind = (MarkerKind) e.getItem();

            marker = kind.switchKind(marker);
        });
        return list;
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

    private static class TimeInput extends JPanel {

        private final SpinnerNumberModel model;
        private transient Consumer<Integer> changed;

        public TimeInput(int value, int minTime, int maxTime) {
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
            if (value >= (Integer) model.getMinimum() && value <= (Integer) model.getMaximum()) {
                model.setValue(value);
                if (changed != null) {
                    changed.accept(value);
                }
            }
        }

        public void setMinimum(int min) {
            model.setMinimum(min);
            if (getValue() < min) {
                setValue(min);
            }
        }

        public void setMaximum(int max) {
            model.setMaximum(max);
            if (getValue() > max) {
                setValue(max);
            }
        }
    }
}
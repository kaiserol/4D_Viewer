package de.uzk.gui.marker;

import de.uzk.gui.UIEnvironment;
import de.uzk.gui.dialogs.DialogColorChooser;
import de.uzk.markers.Marker;
import de.uzk.markers.interactions.MarkerInteractionHandler;
import de.uzk.utils.DateTimeUtils;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Function;

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
    private final Marker initial;
    protected GridBagConstraints gbc;


    public MarkerEditor(Marker marker) {
        this.marker = initial = marker;
        dialogColorChooser = new DialogColorChooser(null);
        init();
    }

    public Marker getMarker() {
        return marker;
    }

    protected void init() {
        setLayout(new GridBagLayout());
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
        add(new JLabel(getWord("dialog.markers.color")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(getColorButton(), gbc);


        RangeInput timeStart = new RangeInput(marker.getTimeStart(), 0, marker.getTimeEnd(), DateTimeUtils::formatFrameTimeStamp);
        RangeInput timeEnd = new RangeInput(marker.getTimeEnd(), marker.getTimeStart(), workspace.getMaxTime(), DateTimeUtils::formatFrameTimeStamp);

        // Diese beiden Handler stellen sicher, dass from <= to
        timeStart.onChange(value -> {
            marker.setTimeStart(value);
            timeEnd.setMinimum(value);
        });

        timeEnd.onChange(value -> {
            marker.setTimeEnd(value);
            timeStart.setMaximum(value);
        });

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        add(new JLabel(getWord("menu.markers.timeStart")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(timeStart, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        add(new JLabel(getWord("menu.markers.timeEnd")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(timeEnd, gbc);

        RangeInput levelStart = new RangeInput(marker.getLevelStart(), 0, marker.getLevelEnd(), this::levelFormat);
        RangeInput levelEnd = new RangeInput(marker.getLevelEnd(), marker.getLevelStart(), workspace.getMaxLevel(), this::levelFormat);

        // Diese beiden Handler stellen sicher, dass from <= to
        levelStart.onChange(value -> {
            marker.setLevelStart(value);
            levelEnd.setMinimum(value);
        });

        levelEnd.onChange(value -> {
            marker.setLevelEnd(value);
            levelStart.setMaximum(value);
        });

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        add(new JLabel(getWord("menu.markers.levelStart")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(levelStart, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 1;
        add(new JLabel(getWord("menu.markers.levelEnd")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        add(levelEnd, gbc);


    }

    private JComboBox<MarkerKind> getKindInput() {
        JComboBox<MarkerKind> list = new JComboBox<>(MarkerKind.values());

        list.setSelectedItem(MarkerKind.fromMarker(marker));
        list.addItemListener(e -> {
            MarkerKind kind = (MarkerKind) e.getItem();

            if(kind != MarkerKind.fromMarker(initial)) {
                marker = kind.switchKind(marker);
            } else {
                marker = initial;
            }
        });
        return list;
    }

    private JButton getColorButton() {
        JButton button = new JButton();
        button.setBackground(marker.getColor());
        button.addActionListener(a -> selectColor(button));
        return button;
    }

    private void selectColor(JButton color) {
        Color selected = dialogColorChooser.chooseColor(marker.getColor());
        color.setBackground(selected);
        marker.setColor(selected);
    }

    private String levelFormat(int level) {
        return "%.2f nm".formatted(level * workspace.getConfig().getLevelUnit());
    }

    private static class  RangeInput extends JPanel {

        private final SpinnerNumberModel model;
        private transient Consumer<Integer> changed;
        private final Function<Integer, String> labelFormatter;

        public RangeInput(int value, int minTime, int maxTime, Function<Integer, String> labelFormatter) {
            model = new SpinnerNumberModel(value, minTime, maxTime, 1);
            this.labelFormatter = labelFormatter;
            init();
        }

        private void init() {
            setLayout(new GridLayout(1, 2));

            JSpinner numberInput = new JSpinner(model);
            JLabel label = new JLabel("(" + labelFormatter.apply(getValue()) + ")", SwingConstants.RIGHT);
            numberInput.addChangeListener(e -> {
                label.setText("(" + labelFormatter.apply(getValue()) + ")");
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
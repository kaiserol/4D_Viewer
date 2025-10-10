package de.uzk.gui.marker;

import de.uzk.actions.ActionType;
import de.uzk.gui.Gui;
import de.uzk.gui.InteractiveContainer;
import de.uzk.gui.OGridBagConstraints;
import de.uzk.image.ImageLayer;
import de.uzk.markers.Marker;
import de.uzk.markers.MarkerMapping;

import javax.swing.*;
import java.awt.*;

import static de.uzk.Main.imageHandler;
import static de.uzk.config.LanguageHandler.getWord;

public class MarkerMappingInfo extends InteractiveContainer<JPanel> {

    private final MarkerMapping mapping;

    public MarkerMappingInfo(MarkerMapping mapping, Gui gui) {
        super(new JPanel(), gui);
        this.mapping = mapping;
        init();
    }

    private void init() {
        this.container.setPreferredSize(new Dimension(150, 100));
        this.container.setMinimumSize(new Dimension(150, 100));
        this.container.setMaximumSize(new Dimension(500, 100));
        this.container.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        this.container.setLayout(new GridBagLayout());
        OGridBagConstraints c = new OGridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.gridwidth = 2;
        c.weightx = 0.7;
        c.setInsets(10, 10, 0, 0);

        JLabel nameLabel=new JLabel(this.mapping.getMarker().getLabel());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        this.container.add(nameLabel, c);

        SpinnerNumberModel fromModel = new SpinnerNumberModel(this.mapping.getFrom(), 0, imageHandler.getMaxTime(), 1);
        SpinnerNumberModel toModel = new SpinnerNumberModel(this.mapping.getTo(), this.mapping.getFrom(), imageHandler.getMaxTime(), 1);

        fromModel.addChangeListener(e -> {
            int newValue = fromModel.getNumber().intValue();
            this.mapping.setFrom(newValue);
            toModel.setMinimum(newValue);
            gui.update(ImageLayer.TIME);
            if(toModel.getNumber().intValue() < newValue) {
                toModel.setValue(newValue);
            }
        });

        toModel.addChangeListener(e -> {
            int newValue = toModel.getNumber().intValue();
            this.mapping.setTo(newValue);
        });


        c.gridy = 1;
        c.gridwidth = 1;
        this.container.add(new JLabel(getWord("items.markers.visibleFromImage")), c);

        c.gridx = 1;
        JSpinner minimum = new JSpinner(fromModel);
        this.container.add(minimum, c);

        c.setPos(0, 2);
        this.container.add(new JLabel(getWord("items.markers.visibleToImage")), c);

        c.gridx = 1;
        JSpinner maximum = new JSpinner(toModel);
        this.container.add(maximum, c);


        c.setPos(2, 0);
        c.setInsets(0,0,0,0);
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        c.setSizeAndWeight(1,3,0.1,1);
        this.container.add(getEditButton(), c);

    }

    private GenericMarkerPreview getEditButton() {
        GenericMarkerPreview edit = new GenericMarkerPreview(this.mapping.getMarker().getShape(), this.mapping.getMarker().getColor());
        edit.setOnClick(() -> {

            MarkerEditor initial = new MarkerEditor(imageHandler.getCurrentImage(), new Marker(this.mapping.getMarker()));
            int option = JOptionPane.showConfirmDialog(null, initial, getWord("dialog.markers.editMarker"), JOptionPane.OK_CANCEL_OPTION);
            if(option == JOptionPane.OK_OPTION) {
                this.mapping.setMarker(initial.getMarker());
                gui.handleAction(ActionType.ADD_MARKER);

                gui.updateUI();
            }
        });
        return edit;
    }


}

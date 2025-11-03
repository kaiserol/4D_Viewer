package de.uzk.gui.marker;

import de.uzk.action.ActionType;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.gui.OGridBagConstraints;
import de.uzk.image.Axis;
import de.uzk.markers.Marker;
import de.uzk.markers.MarkerMapping;

import javax.swing.*;
import java.awt.*;

import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

/* Nur JPanel erweitern, da diese Komponente dynamisch während UI Updates erstellt wird.
AreaContainerInteractive würde während der initialisierung eventhandler registrieren, was eine Exception
 auslöst. */
public class MarkerMappingInfo extends JPanel {
    private final Gui gui;
    private final MarkerMapping mapping;

    public MarkerMappingInfo(MarkerMapping mapping, Gui gui) {
        this.gui = gui;
        this.mapping = mapping;
        init();
    }

    private void init() {
        this.setPreferredSize(new Dimension(150, 120));
        this.setMinimumSize(new Dimension(150, 120));
        this.setMaximumSize(new Dimension(500, 120));
        this.setBorder(BorderFactory.createLineBorder(GuiUtils.getBorderColor()));
        this.setLayout(new GridBagLayout());

        OGridBagConstraints c = new OGridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.gridwidth = 2;
        c.weightx = 0.7;
        c.setInsets(5, 10, 10, 10);

        JLabel nameLabel= new JLabel(this.mapping.getMarker().getLabel());
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        this.add(nameLabel, c);

        SpinnerNumberModel fromModel = new SpinnerNumberModel(this.mapping.getFrom(), 0, workspace.getMaxTime(), 1);
        SpinnerNumberModel toModel = new SpinnerNumberModel(this.mapping.getTo(), this.mapping.getFrom(), workspace.getMaxTime(), 1);

        fromModel.addChangeListener(e -> {
            int newValue = fromModel.getNumber().intValue();
            this.mapping.setFrom(newValue);
            toModel.setMinimum(newValue);
            if(toModel.getNumber().intValue() < newValue) {
                toModel.setValue(newValue);
            }
                gui.update(Axis.TIME);

        });

        toModel.addChangeListener(e -> {
            int newValue = toModel.getNumber().intValue();
            this.mapping.setTo(newValue);
        });


        c.gridy = 1;
        c.gridwidth = 1;
        this.add(new JLabel(getWord("items.markers.visibleFromImage") + ":"), c);

        c.gridx = 1;
        JSpinner minimum = new JSpinner(fromModel);
        this.add(minimum, c);

        c.setPos(0, 2);
        this.add(new JLabel(getWord("items.markers.visibleToImage") + ":"), c);

        c.gridx = 1;
        JSpinner maximum = new JSpinner(toModel);
        this.add(maximum, c);


        c.setPos(2, 0);
        c.setInsets(0,10,10,0);
        c.anchor = GridBagConstraints.FIRST_LINE_END;
        c.setSizeAndWeight(1,3,0.1,1);
        this.add(getEditButton(), c);

    }

    private GenericMarkerPreview getEditButton() {
        GenericMarkerPreview edit = new GenericMarkerPreview(this.mapping.getMarker().getShape(), this.mapping.getMarker().getColor());
        edit.setOnClick(() -> {

            MarkerEditor initial = new MarkerEditor(workspace.getImageFile(), new Marker(this.mapping.getMarker()));
            int option = JOptionPane.showConfirmDialog(
                    null,
                    initial,
                    getWord("dialog.markers.editMarker"),
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (option == JOptionPane.OK_OPTION) {
                this.mapping.setMarker(initial.getMarker());
                gui.handleAction(ActionType.ACTION_ADD_MARKER);

                gui.updateUI();
            }
        });
        return edit;
    }


}

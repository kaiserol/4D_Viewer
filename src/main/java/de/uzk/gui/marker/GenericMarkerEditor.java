package de.uzk.gui.marker;

import de.uzk.markers.GenericMarker;
import de.uzk.markers.GenericMarkerShape;

import javax.swing.*;
import java.awt.*;

import static de.uzk.config.LanguageHandler.getWord;

public class GenericMarkerEditor extends MarkerEditor {

    public GenericMarkerEditor(int startTime) {
        this(new GenericMarker(startTime));
    }

    public GenericMarkerEditor(GenericMarker marker) {
        super(marker);
    }

    @Override
    protected void init() {
        super.init();
        gbc.gridx = 0;
        gbc.gridy ++;
        gbc.gridwidth = 1;
        this.add(new JLabel(getWord("dialog.markers.shape")), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        this.add(getShapeInput(), gbc);

    }

    private JComboBox<GenericMarkerShape> getShapeInput() {
        JComboBox<GenericMarkerShape> list = new JComboBox<>(GenericMarkerShape.values());
        GenericMarker marker = (GenericMarker) getMarker();
        list.addItemListener(e -> marker.setShape((GenericMarkerShape)list.getSelectedItem()));
        list.setSelectedItem(marker.getShape());
        return list;
    }
}

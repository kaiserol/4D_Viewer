package de.uzk.gui.tabs;

import de.uzk.action.ActionType;
import de.uzk.gui.Gui;
import de.uzk.gui.marker.GenericMarkerEditor;
import de.uzk.gui.marker.MarkerEditor;
import de.uzk.gui.marker.MarkerInfo;
import de.uzk.gui.observer.ObserverContainer;
import de.uzk.image.Axis;
import de.uzk.markers.AbstractMarker;
import de.uzk.markers.ArrowMarker;
import de.uzk.utils.ComponentUtils;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

public class TabMarkers extends ObserverContainer<JPanel> {
    public TabMarkers(Gui gui) {
        super(new JPanel(), gui);
        rebuildContainer();
    }

    private void rebuildContainer() {
        java.util.List<AbstractMarker> currentMarkers =  workspace.getMarkers().getAllMarkers();

        container.removeAll();
        container.setLayout(new BorderLayout());

        JButton addGeneric = createAddButton(getWord("menu.markers.addMarker"), () -> new GenericMarkerEditor(workspace.getTime()));
        JButton addArrow = createAddButton("Add Arrow", () -> new MarkerEditor(new ArrowMarker(workspace.getTime())));

        Box addButtons = new Box(BoxLayout.Y_AXIS);

        addButtons.add(addGeneric);

        addButtons.add(addArrow);

        container.add(addButtons, BorderLayout.SOUTH);

        if (!currentMarkers.isEmpty()) {
            Box panel = new Box(BoxLayout.Y_AXIS);
            for (AbstractMarker currentMarker : currentMarkers) {
                JPanel next = new MarkerInfo(currentMarker, this.gui);

                panel.add(next);
                panel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
            container.add(new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        } else {
            JLabel noneLabel = new JLabel(getWord("menu.markers.noMarkersSet"));
            noneLabel.setHorizontalAlignment(SwingConstants.CENTER);
            container.add(noneLabel, BorderLayout.CENTER);
        }
        container.revalidate();
    }

    private JButton createAddButton(String label, Supplier<MarkerEditor> editorSupplier) {
        JButton addButton = new JButton(label);
        addButton.setMaximumSize(new Dimension(Integer.MAX_VALUE,addButton.getPreferredSize().height));
        addButton.addActionListener(e -> {
            MarkerEditor editor = editorSupplier.get();
            int option = JOptionPane.showConfirmDialog(
                gui.getContainer(),
                editor,
                getWord("dialog.markers.newMarker"),
                JOptionPane.OK_CANCEL_OPTION
            );

            if (option == JOptionPane.OK_OPTION) {
                workspace.getMarkers().addMarker(editor.getMarker());
                gui.handleAction(ActionType.ACTION_ADD_MARKER);
                gui.updateUI();
            }
        });
        return addButton;
    }

    @Override
    public void handleAction(ActionType actionType) {
        if (actionType == ActionType.ACTION_ADD_MARKER || actionType == ActionType.ACTION_REMOVE_MARKER) {
            rebuildContainer();
        }
    }

    @Override
    public void toggleOn() {
        ComponentUtils.setEnabled(this.container, true);
    }

    @Override
    public void toggleOff() {
        ComponentUtils.setEnabled(this.container, false);
    }

    @Override
    public void update(Axis axis) {
        if (axis == Axis.TIME) {
            rebuildContainer();
        }
    }
}

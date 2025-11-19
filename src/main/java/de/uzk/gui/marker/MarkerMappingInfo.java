package de.uzk.gui.marker;

import de.uzk.action.ActionType;
import de.uzk.gui.Gui;
import de.uzk.gui.OGridBagConstraints;
import de.uzk.gui.UIEnvironment;
import de.uzk.image.Axis;
import de.uzk.io.ImageLoader;
import de.uzk.markers.Marker;
import de.uzk.markers.MarkerMapping;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

    @Override
    public Dimension getMaximumSize() {
        int height = this.getMinimumSize().height;
        int width = super.getMaximumSize().width;
        return new Dimension(width, height);
    }

    private void init() {
        this.setBorder(new CompoundBorder(
            BorderFactory.createLineBorder(UIEnvironment.getBorderColor()), // Tatsächliche bBorder
            BorderFactory.createEmptyBorder(5, 5, 5, 5) // Padding
        ));
        this.setLayout(new GridLayout(1, 3, 5, 0));

        GridBagConstraints c = new GridBagConstraints();

        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.VERTICAL;

        c.weightx = 2;
        c.gridheight = 1;
        c.gridwidth = 2;

        this.add(getJumpLink());

        c.weightx = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.NORTHEAST;


        c.gridx += 1;
        this.add(getEditButton());

        c.gridx += 1;
        this.add(getDeleteButton());

    }

    private JLabel getJumpLink() {
        JLabel link = new JLabel(mapping.getMarker().getLabel());
        link.setAlignmentY(Component.CENTER_ALIGNMENT);

        link.setToolTipText("Jump to " + mapping.getMarker().getLabel());
        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                link.setFont(link.getFont().deriveFont(Font.BOLD));
                link.setForeground(Color.BLUE);

            }

            @Override
            public void mouseClicked(MouseEvent e) {
                workspace.setTime(mapping.getFrom());
                gui.update(Axis.TIME);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                link.setFont(link.getFont().deriveFont(Font.PLAIN));
                link.setForeground(UIEnvironment.getTextColor());
            }
        });
        return link;
    }

    private JButton getEditButton() {
        JButton edit = new JButton(ImageLoader.ICON_EDIT);
        edit.setForeground(Color.BLUE);
        edit.addActionListener(a -> {

            MarkerEditor initial = new MarkerEditor(workspace.getCurrentImageFile(), new Marker(this.mapping.getMarker()));
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

    private JButton getDeleteButton() {
        JButton deleteButton = new JButton(ImageLoader.ICON_DELETE);
        deleteButton.setForeground(Color.RED);
        deleteButton.addActionListener(a -> {
            workspace.getMarkers().remove(mapping);
            gui.handleAction(ActionType.ACTION_REMOVE_MARKER);
        });
        return deleteButton;
    }


}

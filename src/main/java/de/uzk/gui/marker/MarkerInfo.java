package de.uzk.gui.marker;

import de.uzk.action.ActionType;
import de.uzk.gui.Gui;
import de.uzk.gui.UIEnvironment;
import de.uzk.image.Axis;
import de.uzk.io.ImageLoader;
import de.uzk.markers.Marker;

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
public class MarkerInfo extends JPanel {
    private final Gui gui;
    private final Marker marker;

    public MarkerInfo(Marker marker, Gui gui) {
        this.gui = gui;
        this.marker = marker;
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

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.VERTICAL;

        gbc.weightx = 2;
        gbc.gridheight = 1;
        gbc.gridwidth = 2;

        this.add(getJumpLink(), gbc);

        gbc.weightx = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHEAST;


        gbc.gridx += 1;
        this.add(getEditButton(), gbc);

        gbc.gridx += 1;
        this.add(getDeleteButton(), gbc);

    }

    private JLabel getJumpLink() {
        JLabel link = new JLabel(marker.getLabel());
        link.setAlignmentY(Component.CENTER_ALIGNMENT);

        link.setToolTipText("Jump to " + marker.getLabel());
        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                link.setFont(link.getFont().deriveFont(Font.BOLD));
                link.setForeground(Color.BLUE);

            }

            @Override
            public void mouseClicked(MouseEvent e) {
                workspace.setTime(marker.getFrom());
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
        edit.setToolTipText(getWord("menu.markers.tooltipEditMarker"));
        edit.addActionListener(a -> {

            Marker copy = new Marker(marker);
            MarkerEditor initial = new MarkerEditor(marker);
            int option = JOptionPane.showConfirmDialog(
                gui.getContainer(),
                initial,
                getWord("dialog.markers.editMarker"),
                JOptionPane.OK_CANCEL_OPTION
            );

            if (option == JOptionPane.OK_OPTION) {

                gui.handleAction(ActionType.ACTION_EDIT_MARKER);

                gui.updateUI();
            } else {
                // Resetten
                marker.cloneFrom(copy);
            }
        });
        return edit;
    }

    private JButton getDeleteButton() {
        JButton deleteButton = new JButton(ImageLoader.ICON_DELETE);
        deleteButton.setForeground(Color.RED);
        deleteButton.setToolTipText(getWord("menu.markers.tooltipRemoveMarker"));
        deleteButton.addActionListener(a -> {
            workspace.getMarkers().remove(marker);
            gui.handleAction(ActionType.ACTION_REMOVE_MARKER);
        });
        return deleteButton;
    }


}

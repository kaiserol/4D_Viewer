package de.uzk.gui.marker;

import de.uzk.action.ActionType;
import de.uzk.edit.markers.MarkerEdit;
import de.uzk.edit.markers.RemoveMarkerEdit;
import de.uzk.edit.markers.RenameMarkerEdit;
import de.uzk.gui.Gui;
import de.uzk.gui.UIEnvironment;
import de.uzk.image.Axis;
import de.uzk.io.ImageLoader;
import de.uzk.markers.Marker;
import de.uzk.utils.ColorUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;


public class MarkerInfo extends JPanel
/* Nur JPanel erweitern, da diese Komponente dynamisch während UI Updates erstellt wird.
   `extends AreaContainerInteractive` würde während der initialisierung Eventhandler registrieren,
    was einen Stack-Overflow auslöst */ {
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
        this.setBorder(new CompoundBorder(BorderFactory.createLineBorder(UIEnvironment.getBorderColor()), // Tatsächliche bBorder
            BorderFactory.createEmptyBorder(5, 5, 5, 5) // Padding
        ));
        this.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.weightx = 10;
        gbc.gridheight = 1;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(0,0,0,10);

        this.add(getLabel(), gbc);

        gbc.weightx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0,0,0,0);

        gbc.gridx += 4;
        gbc.fill = GridBagConstraints.REMAINDER;
        add(getJumpButton(), gbc);

        gbc.gridx += 1;
        this.add(getEditButton(), gbc);

        gbc.gridx += 1;
        this.add(getDeleteButton(), gbc);

    }

    private JTextField getLabel() {
        JTextField field = new JTextField(8);
        field.setText(marker.getLabel());

        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    requestFocus();
                }
            }
        });
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
            }
            @Override
            public void focusLost(FocusEvent e) {
                workspace.getEditManager().performEdit(new RenameMarkerEdit(marker, field.getText()));
                gui.handleAction(ActionType.ACTION_EDIT_MARKER);
            }
        });
        return field;
    }

    private JButton getJumpButton() {
        JButton jump = new JButton(ImageLoader.ICON_STEP_FORWARD);
        jump.setBackground(ColorUtils.COLOR_BLUE);

        jump.setToolTipText(getWord("menu.markers.tooltipJumpToMarker").formatted(marker.getLabel()));
        jump.addActionListener(a -> {
            workspace.setTime(marker.getFrom());
            gui.update(Axis.TIME);
        });
        return jump;
    }

    private JButton getEditButton() {
        JButton edit = new JButton(ImageLoader.ICON_EDIT);
        edit.setToolTipText(getWord("menu.markers.tooltipEditMarker"));
        edit.addActionListener(a -> {
            Marker ma = marker.copy();
            MarkerEditor editor = new MarkerEditor(ma);
            int option = JOptionPane.showOptionDialog(gui.getContainer(), editor, getWord("dialog.markers.editMarker"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
            if (option == JOptionPane.OK_OPTION) {
                workspace.getEditManager().performEdit(new MarkerEdit(marker, editor.getMarker()));
                gui.handleAction(ActionType.ACTION_EDIT_MARKER);
                gui.updateUI();
            }
        });
        return edit;
    }

    private JButton getDeleteButton() {
        JButton deleteButton = new JButton(ImageLoader.ICON_DELETE);
        deleteButton.setBackground(ColorUtils.COLOR_RED);
        deleteButton.setToolTipText(getWord("menu.markers.tooltipRemoveMarker"));
        deleteButton.addActionListener(a -> {
            workspace.getEditManager().performEdit(new RemoveMarkerEdit(marker));
            gui.handleAction(ActionType.ACTION_REMOVE_MARKER);
        });
        return deleteButton;
    }


}

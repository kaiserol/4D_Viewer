package de.uzk.gui.marker;


import de.uzk.action.ActionType;
import de.uzk.edit.markers.MarkerEdit;
import de.uzk.edit.markers.RenameMarkerEdit;
import de.uzk.gui.Gui;
import de.uzk.io.ImageLoader;
import de.uzk.markers.Marker;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

import static de.uzk.Main.workspace;

public class MarkerContextMenu extends JPopupMenu{
    private static final Color[] COLOR_OPTIONS = {
        Color.RED,
        Color.BLUE,
        Color.GREEN,
        Color.YELLOW,
        Color.CYAN,
        Color.MAGENTA,
        Color.BLACK,
        Color.LIGHT_GRAY,
        Color.WHITE
    };

    private final Marker initial;
    private Marker marker;
    private final Gui gui;

    public MarkerContextMenu(Marker marker, Gui gui) {
        this.marker = marker.copy();
        initial = marker;
        this.gui = gui;
        initialize();
    }

    private void initialize() {
        MarkerEditor markerEditor = new MarkerEditor(marker);

        JPanel titlePanel = new JPanel();
        titlePanel.add(getLabelInput());
        titlePanel.add(getHideButton());
        add(titlePanel);
        addSeparator();
        add(getColorPanel());
    }

    private JTextField getLabelInput() {
        JTextField field = new JTextField(8);
        field.setText(marker.getLabel());

        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setText();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setText();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setText();
            }

            private void setText() {
                if(field.getText().trim().isEmpty()) return;
                marker.setLabel(field.getText());
                registerChanges();
            }
        });

        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    requestFocus();
                    setVisible(false);
                }
            }
        });

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                 workspace.getEditManager().performEdit(new RenameMarkerEdit(marker, field.getText()));
                 gui.handleAction(ActionType.ACTION_EDIT_MARKER);
            }
        });
        return field;
    }

    private JPanel getColorPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.WEST;
        for(Color color: COLOR_OPTIONS) {

            JButton button = getButton(color);
            panel.add(button, gbc);
            if(gbc.gridx == 2) {
                gbc.gridx = 0;
                gbc.gridy++;
            } else {
                gbc.gridx++;
            }
        }

        return panel;
    }

    private @NotNull JButton getButton(Color color) {
        JButton button = new JButton(" ".repeat(5));

        button.setBackground(color);
        button.addActionListener(e -> {
            marker.setColor(color);
            registerChanges();
        });
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        return button;
    }

    private JButton getHideButton() {
        JButton hideButton = new JButton(ImageLoader.ICON_EYE_CLOSED);
        hideButton.setBackground(Color.GRAY);
        hideButton.addActionListener(e -> {
            marker.setHidden(true);
            registerChanges();
            setVisible(false);
        });
        return hideButton;
    }

    private void registerChanges() {

        workspace.getEditManager().performEdit(new MarkerEdit(initial, marker));
        gui.handleAction(ActionType.ACTION_EDIT_MARKER);
    }

}

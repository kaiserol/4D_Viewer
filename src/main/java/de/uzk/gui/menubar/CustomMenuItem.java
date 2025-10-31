package de.uzk.gui.menubar;

import de.uzk.action.ActionHandler;
import de.uzk.action.ActionType;
import de.uzk.action.Shortcut;
import de.uzk.gui.GuiUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public final class CustomMenuItem extends CustomMenuBarNode {
    public CustomMenuItem(String text, Icon icon, ActionHandler actionHandler, ActionType actionType, boolean showShortcutsAsTooltip) {
        this(text);
        this.setIcon(icon);
        this.setAction(actionHandler, actionType, showShortcutsAsTooltip);
    }

    public CustomMenuItem(String text, Icon icon, ActionHandler actionHandler, ActionType actionType) {
        this(text, icon, actionHandler, actionType, false);
    }

    public CustomMenuItem(String text, ActionHandler actionHandler, ActionType actionType, boolean showShortcutsAsTooltip) {
        this(text, null, actionHandler, actionType, showShortcutsAsTooltip);
    }

    public CustomMenuItem(String text, ActionHandler actionHandler, ActionType actionType) {
        this(text, actionHandler, actionType, false);
    }

    public CustomMenuItem(String text) {
        super(new JMenuItem(), text);
    }

    private void setIcon(Icon icon) {
        if (icon != null && this.getComponent() instanceof JMenuItem menuItem) {
            menuItem.setIcon(icon);
        }
    }

    private void setAction(ActionHandler actionHandler, ActionType actionType, boolean showShortcutsAsTooltip) {
        if (actionHandler == null || actionType == null) return;
        if (!(getComponent() instanceof JMenuItem menuItem)) return;

        // 1. Standard-Aktion beim Anklicken
        menuItem.addActionListener(a -> actionHandler.executeAction(actionType));

        // 2. Alle definierten Shortcuts abrufen
        List<Shortcut> shortcuts = actionType.getShortcuts();
        if (shortcuts == null || shortcuts.isEmpty()) return;

        // 3. Definition des Haupt Shortcuts
        Shortcut primaryShortcut = shortcuts.get(0);

        @SuppressWarnings("MagicConstant")
        KeyStroke primaryStroke = KeyStroke.getKeyStroke(primaryShortcut.getExtendedKeyCode(), primaryShortcut.getModifiersEx());

        if (!showShortcutsAsTooltip) {
            // 4. Ersten Shortcut als sichtbaren Accelerator im Menü setzen
            menuItem.setAccelerator(primaryStroke);

            // 5. Weitere Shortcuts ebenfalls binden (z. B. alternative Tasten)
            if (shortcuts.size() > 1) {
                InputMap inputMap = menuItem.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
                ActionMap actionMap = menuItem.getActionMap();

                for (int i = 1; i < shortcuts.size(); i++) {
                    Shortcut shortcut = shortcuts.get(i);
                    @SuppressWarnings("MagicConstant")
                    KeyStroke keyStroke = KeyStroke.getKeyStroke(shortcut.getExtendedKeyCode(), shortcut.getModifiersEx());

                    inputMap.put(keyStroke, "action-" + shortcut);
                    actionMap.put("action-" + shortcut, new AbstractAction() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            actionHandler.executeAction(actionType);
                        }
                    });
                }
            }
        } else {
            // 6. Optional: Tooltip mit Shortcut-Anzeige
            GuiUtils.setToolTipText(menuItem, primaryShortcut.toString());
        }
    }
}
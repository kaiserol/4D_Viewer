package de.uzk.gui.menubar;

import de.uzk.actions.ActionHandler;

import javax.swing.*;
import java.awt.event.*;
import java.util.Objects;

public final class CustomMenuItem extends CustomMenuNode {
    private Icon icon;
    private boolean hasListener;

    // TODO: Warum verwendet man accelerators... und accelerator? Warum nicht das eine löschen
    // Warum gibt es hasListener & toggleable, und überprüfe ob es richtig verwendet wird, benenne vielleicht besser um
    // setAction()
    public CustomMenuItem(String text, Icon icon, ActionListener action, KeyEvent... accelerators) {
        this(text);
        this.setIcon(icon);
        this.setAction(action, accelerators);
    }

    public CustomMenuItem(String text, Icon icon, ActionHandler action, KeyEvent accelerator) {
        this(text);
        this.setIcon(icon);
        this.setAction(action, accelerator);
    }

    public CustomMenuItem(String text, ActionListener action, KeyEvent... accelerators) {
        this(text, null, action, accelerators);
    }

    public CustomMenuItem(String text, boolean toggleable) {
        super(new JMenuItem(), text, toggleable);
    }

    public CustomMenuItem(String text) {
        this(text, false);
    }

    private void setIcon(Icon icon) {
        this.icon = icon;
        if (icon != null && this.component instanceof JMenuItem menuItem) {
            menuItem.setIcon(icon);
        }
    }

    public void setAction(ActionHandler action, KeyEvent accelerator) {
        // only one listener registration allowed
        if (this.hasListener) {
            return;
        }

        if (action != null && this.component instanceof JMenuItem menuItem) {
            this.hasListener = true;
            menuItem.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (menuItem.contains(e.getPoint())) {
                        // TODO: muss hier nicht keyReleased stehen? Wenn ja, ändere den Code so ab, dass es funktioniert
                        action.keyPressed(accelerator);
                    }
                }
            });

            addAccelerators(null, menuItem, accelerator);
        }
    }

    public void setAction(ActionListener action, KeyEvent... accelerators) {
        // only one listener registration allowed
        if (this.hasListener) {
            return;
        }

        if (action != null && this.component instanceof JMenuItem menuItem) {
            this.hasListener = true;
            menuItem.addActionListener(action);
            addAccelerators(action, menuItem, accelerators);
        }
    }

    private void addAccelerators(ActionListener action, JMenuItem menuItem, KeyEvent... accelerators) {
        if (accelerators != null && accelerators.length > 0) {
            menuItem.setAccelerator(KeyStroke.getKeyStrokeForEvent(accelerators[0]));
            if (action == null) return;

            InputMap inputMap = menuItem.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = menuItem.getActionMap();

            for (int i = 1; i < accelerators.length; i++) {
                KeyEvent keyEvent = accelerators[i];
                KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(keyEvent);
                inputMap.put(keyStroke, keyStroke);
                actionMap.put(keyStroke, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        action.actionPerformed(e);
                    }
                });
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CustomMenuItem menuItem = (CustomMenuItem) o;
        return Objects.equals(this.icon, menuItem.icon) &&
                Objects.equals(this.hasListener, menuItem.hasListener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.icon, this.hasListener);
    }
}
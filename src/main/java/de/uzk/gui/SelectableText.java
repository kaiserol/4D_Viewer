package de.uzk.gui;

import de.uzk.action.Shortcut;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * {@code SelectableText} ist eine spezialisierte {@link JEditorPane}-Komponente,
 * die HTML-Text anzeigt und gleichzeitig selektierbar macht.
 * <p>
 * Hauptmerkmale:
 * <ul>
 *   <li>Zeigt den Textcursor (I-Beam), solange sich die Maus über dem Text befindet.</li>
 *   <li>Wenn sich die Maus über einem Hyperlink befindet und die Command- (bzw. Ctrl-)Taste gedrückt ist,
 *       erscheint der Hand-Cursor (Pointer).</li>
 *   <li>Beim Klicken auf einen Link wird der Standardbrowser über {@link GuiUtils#openWebLink(java.net.URL)} geöffnet.</li>
 *   <li>Zeigt einen Tooltip („Open in browser…“), wenn sich die Maus über einem Link befindet und Command gedrückt ist.</li>
 * </ul>
 */
public class SelectableText extends JEditorPane implements HyperlinkListener {
    /**
     * true, wenn die Command-/Strg-Taste aktuell gedrückt ist
     */
    private volatile boolean commandPressed = false;

    /**
     * true, wenn sich der Mauszeiger aktuell über einem Hyperlink befindet
     */
    private boolean overLink = false;

    /**
     * Erstellt eine neue {@code SelectableText}-Instanz mit gegebenem HTML-Inhalt.
     *
     * @param htmlContent HTML-Text, der in der Komponente angezeigt werden soll.
     */
    public SelectableText(String htmlContent) {
        setEditable(false);
        setOpaque(false);
        setContentType("text/html");
        setText(htmlContent);

        // Standardcursor: Textcursor
        setTextCursor();

        // Unsichtbares Caret (kein blinkender Balken)
        setCaret(new NoBlinkCaret());

        // Maus-Events
        addMouseListener(new MouseCaretListener());
        addMouseMotionListener(new MouseMotionCaretListener());

        // Hyperlink-Events
        addHyperlinkListener(this);

        // Globale Tastaturüberwachung aktivieren
        setupGlobalKeyListener();
    }

    // ==========================================================
    // Innere Klassen
    // ==========================================================

    /**
     * Caret-Implementierung, die nicht blinkt und unsichtbar ist.
     */
    private static class NoBlinkCaret extends DefaultCaret {
        public NoBlinkCaret() {
            setBlinkRate(0);
        }

        @Override
        public void paint(Graphics g) {
            // kein sichtbares Caret
        }
    }

    /**
     * Listener, der den Cursor setzt, wenn die Maus das Feld betritt oder verlässt.
     */
    private class MouseCaretListener extends MouseAdapter {
        @Override
        public void mouseEntered(MouseEvent e) {
            setTextCursor();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /**
     * Listener, der Mausbewegungen überwacht und den Cursor dynamisch anpasst.
     */
    private class MouseMotionCaretListener extends MouseMotionAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            updateCursor();
        }
    }

    // ==========================================================
    // HyperlinkListener
    // ==========================================================

    /**
     * Reagiert auf Hyperlink-Ereignisse (ENTERED, EXITED, ACTIVATED).
     */
    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
            overLink = true;
            updateCursor();
        } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
            overLink = false;
            updateCursor();
        } else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            if (commandPressed) GuiUtils.openWebLink(e.getURL());
        }
    }

    // ==========================================================
    // Globale Tastaturüberwachung
    // ==========================================================

    /**
     * Registriert einen globalen KeyEventDispatcher, der Command/Ctrl-Status systemweit überwacht.
     */
    private void setupGlobalKeyListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            boolean oldValue = commandPressed;
            int mask = Shortcut.CTRL_DOWN;

            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if ((e.getModifiersEx() & mask) != 0) {
                    commandPressed = true;
                }
            } else if (e.getID() == KeyEvent.KEY_RELEASED) {
                commandPressed = (e.getModifiersEx() & mask) != 0;
            }

            // Falls sich der Status geändert hat → Cursor aktualisieren
            if (commandPressed != oldValue) {
                SwingUtilities.invokeLater(this::updateCursor);
            }
            return false;
        });
    }

    // ==========================================================
    // Hilfsfunktionen
    // ==========================================================

    /**
     * Setzt den Textcursor (I-Beam).
     */
    private void setTextCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        setToolTipText(null);
    }

    /**
     * Setzt den Handcursor (Pointer).
     */
    private void setHandCursor() {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText("Open in browser…");
    }

    /**
     * Aktualisiert Cursor und Tooltip basierend auf aktuellem Zustand.
     */
    private void updateCursor() {
        if (overLink && commandPressed) {
            setHandCursor();
        } else {
            setTextCursor();
        }
    }
}

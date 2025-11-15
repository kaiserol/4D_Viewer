package de.uzk.gui;

import de.uzk.action.Shortcut;
import de.uzk.utils.ColorUtils;
import de.uzk.utils.ComponentUtils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Objects;

import static de.uzk.config.LanguageHandler.getWord;
import static javax.swing.event.HyperlinkEvent.EventType.*;

/**
 * {@code SelectableText} ist eine spezialisierte {@link JEditorPane}-Komponente,
 * die HTML-Text anzeigt und gleichzeitig selektierbar macht.
 * <p>
 * Hauptmerkmale:
 * <ul>
 *   <li>Zeigt den Standard-Cursor, solange sich die Maus über dem Text befindet.</li>
 *   <li>Wenn sich die Maus über einem Hyperlink befindet und die Command- (bzw. Ctrl-)Taste gedrückt ist,
 *       erscheint der Hand-Cursor (Pointer).</li>
 *   <li>Beim Klicken auf einen Link wird der Standardbrowser über {@link UIEnvironment#openWebLink(java.net.URL)} geöffnet.</li>
 *   <li>Zeigt einen Tooltip („Open in browser“), wenn sich die Maus über einem Link befindet.</li>
 * </ul>
 */
public class SelectableText extends JEditorPane implements HyperlinkListener {

    /**
     * True, wenn die Command-/Strg-Taste aktuell gedrückt ist
     */
    private volatile boolean commandPressed = false;

    /**
     * True, wenn sich der Mauszeiger aktuell über einem Hyperlink befindet
     */
    private boolean overLink = false;

    /**
     * Referenz auf das zuletzt betretene Link-Element (für Styling)
     */
    private Element currentLinkElement = null;

    /**
     * Merkt sich den aktuellen Cursor-/Tooltip-Zustand, um unnötige Updates zu vermeiden
     */
    private CursorMode currentCursorMode = CursorMode.TEXT;

    /**
     * Farbe, die einen aktiven Hyperlink darstellt
     */
    private static final Color COLOR_ACTIVE_LINK = ColorUtils.COLOR_BLUE;

    /**
     * Mögliche Cursor-/Tooltip-Zustände
     */
    private enum CursorMode {
        TEXT,
        LINK_HOVER,
        LINK_CTRL_HOVER
    }

    /**
     * Erstellt eine neue {@code SelectableText}-Instanz mit gegebenem HTML-Inhalt.
     *
     * @param htmlContent HTML-Text, der angezeigt werden soll.
     */
    public SelectableText(String htmlContent) {
        setContentType("text/html");
        setText(htmlContent);
        setEditable(false);
        setOpaque(false);
        setMargin(UIEnvironment.INSETS_NONE);

        // Unsichtbares Caret (kein blinkender Balken)
        setCaret(ComponentUtils.getNoBlinkCaret());
        setDefaultCursor();

        // Maus- & Hyperlink-Events
        addMouseMotionListener(new MouseMovementListener());
        addHyperlinkListener(this);

        // Globale Tastaturüberwachung aktivieren
        setupGlobalKeyListener();

        // Standard-Link-Styling setzen
        SwingUtilities.invokeLater(() -> {
            if (getDocument() instanceof HTMLDocument htmlDoc) {
                StyleSheet styleSheet = htmlDoc.getStyleSheet();
                String hexDefaultColor = ColorUtils.colorToHex(UIEnvironment.getTextColor());
                styleSheet.addRule(String.format("a { color: %s; }", hexDefaultColor));
                styleSheet.addRule(String.format("a:visited { color: %s; }", hexDefaultColor));
                styleSheet.addRule(String.format("a:hover { color: %s; }", hexDefaultColor));
            }
        });
    }

    // ========================================
    // Innere Klassen
    // ========================================
    /**
     * Listener, der Cursor bei Mausbewegungen aktualisiert
     */
    private class MouseMovementListener extends MouseMotionAdapter {
        @Override
        public void mouseMoved(MouseEvent e) {
            updateCursor();
        }
    }

    // ========================================
    // HyperlinkListener
    // ========================================

    /**
     * Reagiert auf Hyperlink-Ereignisse (ENTERED, EXITED, ACTIVATED).
     */
    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (Objects.equals(e.getEventType(), ENTERED)) {
            overLink = true;
            currentLinkElement = e.getSourceElement();
            updateCursor();
        } else if (Objects.equals(e.getEventType(), EXITED)) {
            resetLinkElement();
        } else if (Objects.equals(e.getEventType(), ACTIVATED)) {
            if (commandPressed) {
                UIEnvironment.openWebLink(e.getURL());
                updateCursor();
                commandPressed = false;
            }
        }
    }

    // ========================================
    // Tastaturüberwachung
    // ========================================

    /**
     * Überwacht systemweit, ob Ctrl/Command gedrückt ist
     */
    private void setupGlobalKeyListener() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            boolean oldValue = commandPressed;
            if (e.getID() == KeyEvent.KEY_PRESSED || e.getID() == KeyEvent.KEY_RELEASED) {
                commandPressed = (e.getModifiersEx() & Shortcut.CTRL_DOWN) != 0;
            }

            // Nur bei Änderung aktualisieren
            if (commandPressed != oldValue) {
                SwingUtilities.invokeLater(this::updateCursor);
            }
            return false;
        });
    }

    // ========================================
    // Cursor- und Tooltip-Logik
    // ========================================

    /**
     * Aktualisiert Cursor, Tooltip und ggf. Link-Farbe nur, wenn sich der Zustand tatsächlich ändert.
     */
    private void updateCursor() {
        CursorMode newMode;
        if (overLink && commandPressed) {
            newMode = CursorMode.LINK_CTRL_HOVER;
        } else if (overLink) {
            newMode = CursorMode.LINK_HOVER;
        } else {
            newMode = CursorMode.TEXT;
        }

        // Nur wenn sich der Zustand ändert
        if (newMode != currentCursorMode) {
            currentCursorMode = newMode;

            switch (newMode) {
                case LINK_CTRL_HOVER -> {
                    applyLinkHoverStyle(true);
                    UIEnvironment.setCursor(this, ComponentUtils.HAND_CURSOR);
                    UIEnvironment.setToolTipText(this, getWord("tooltip.openInBrowser"));
                }
                case LINK_HOVER -> {
                    applyLinkHoverStyle(false);
                    UIEnvironment.setCursor(this, ComponentUtils.HAND_CURSOR);

                    String tooltipText = String.format("%s (%s %s)",
                        getWord("tooltip.openInBrowser"),
                        Shortcut.getModifiersList(Shortcut.CTRL_DOWN).get(0),
                        getWord("tooltip.click")
                    );
                    UIEnvironment.setToolTipText(this, tooltipText);
                }
                default -> setDefaultCursor();
            }
        }
    }

    // ========================================
    // HTML Styling
    // ========================================

    /**
     * Färbt den Hyperlink nur dann ein, wenn Command/Ctrl gedrückt ist.
     *
     * @param active True, wenn Link aktiv ist, sonst false
     */
    private void applyLinkHoverStyle(boolean active) {
        if (currentLinkElement == null) return;
        if (!(getDocument() instanceof HTMLDocument htmlDoc)) return;

        int startIndex = currentLinkElement.getStartOffset();
        int endIndex = currentLinkElement.getEndOffset();
        int length = Math.max(0, endIndex - startIndex);

        SimpleAttributeSet set = new SimpleAttributeSet();
        Color color = active ? COLOR_ACTIVE_LINK : UIEnvironment.getTextColor();
        StyleConstants.setForeground(set, color);

        // Farbe ändern
        SwingUtilities.invokeLater(() -> htmlDoc.setCharacterAttributes(startIndex, length, set, false));
    }

    // ========================================
    // Hilfsmethoden
    // ========================================

    /**
     * Setzt Standard-Cursor und entfernt Tooltip
     */
    private void setDefaultCursor() {
        applyLinkHoverStyle(false);
        UIEnvironment.setCursor(this, ComponentUtils.DEFAULT_CURSOR);
        UIEnvironment.setToolTipText(this, null);
    }

    /**
     * Entfernt Hyperlink-Styling.
     */
    private void resetLinkElement() {
        overLink = false;
        updateCursor();
        currentLinkElement = null;
    }
}
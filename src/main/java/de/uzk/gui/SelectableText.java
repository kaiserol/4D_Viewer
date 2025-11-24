package de.uzk.gui;

import de.uzk.action.Shortcut;
import de.uzk.utils.ColorUtils;
import de.uzk.utils.ComponentUtils;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URI;
import java.net.URL;
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
     * {@code true}, wenn die Command-/Strg-Taste aktuell gedrückt ist
     */
    private volatile boolean commandPressed = false;

    /**
     * {@code true}, wenn sich der Mauszeiger aktuell über einem Hyperlink befindet
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
        addMouseListener(new PrivateMouseExitListener());
        addMouseMotionListener(new PrivateMouseMotionListener());
        addHyperlinkListener(this);

        // Globale Tastaturüberwachung aktivieren
        setupGlobalKeyListener();

        // Standard-Link-Styling setzen
        SwingUtilities.invokeLater(() -> {
            HTMLDocument htmlDoc = getHTMLDocument();
            if (htmlDoc == null) return;

            StyleSheet styleSheet = htmlDoc.getStyleSheet();
            String hexDefaultColor = ColorUtils.colorToHex(UIEnvironment.getTextColor());
            styleSheet.addRule("a { color: %s; }".formatted(hexDefaultColor));
            styleSheet.addRule("a:visited { color: %s; }".formatted(hexDefaultColor));
            styleSheet.addRule("a:hover { color: %s; }".formatted(hexDefaultColor));
        });
    }

    // ========================================
    // Innere Klassen
    // ========================================

    /**
     * Listener, der Cursor bei Mauseintritt und -austritt aktualisiert
     */
    private class PrivateMouseExitListener extends MouseAdapter {
        @Override
        public void mouseEntered(MouseEvent e) {
            checkEnteredLinkAt(e.getPoint());
            updateCursor();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            resetLinkElement();
        }
    }

    /**
     * Listener, der Cursor bei Mausbewegungen aktualisiert
     */
    private class PrivateMouseMotionListener extends MouseMotionAdapter {
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
            this.overLink = true;
            this.currentLinkElement = e.getSourceElement();
            updateCursor();
        } else if (Objects.equals(e.getEventType(), EXITED)) {
            resetLinkElement();
        } else if (Objects.equals(e.getEventType(), ACTIVATED)) {
            if (this.commandPressed) {
                UIEnvironment.openWebLink(e.getURL());
                updateCursor();
                this.commandPressed = false;
            }
        }
    }

    /**
     * Prüft, ob unter der angegebenen Mausposition ein Hyperlink vorhanden ist.
     * Wenn ja, wird ein ENTERED-HyperlinkEvent erzeugt und an {@link #hyperlinkUpdate(HyperlinkEvent)} weitergeleitet.
     *
     * @param mousePos Die Mausposition relativ zur Komponente
     */
    private void checkEnteredLinkAt(Point mousePos) {
        HTMLDocument htmlDoc = getHTMLDocument();
        if (htmlDoc == null) return;

        // Holt das Character-Leaf-Element an dieser Position
        Element leaf = getLeafElementAt(htmlDoc, mousePos);
        if (leaf == null) return;

        // Findet ein <a>-Element oder HREF-Attribut unter dem Leaf
        Element linkElement = findLinkElement(leaf);
        if (linkElement == null) return;

        // Löst ein HyperlinkEvent vom Typ ENTERED für das angegebene Link-Element aus
        fireHyperLinkEntered(linkElement, htmlDoc);
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

                    String tooltipText = "%s (%s %s)".formatted(
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

    // ========================================
    // Hilfsmethoden
    // ========================================

    /**
     * Gibt das aktuelle Dokument als {@link HTMLDocument} zurück, falls das Dokument
     * ein HTML-Dokument ist. Andernfalls wird {@code null} zurückgegeben.
     *
     * @return Das aktuelle {@code HTMLDocument} oder {@code null}, wenn kein HTML-Dokument vorhanden ist
     */
    private HTMLDocument getHTMLDocument() {
        return (getDocument() instanceof HTMLDocument doc) ? doc : null;
    }

    /**
     * Liefert das {@link Element} an die durch die Mausposition angegebene Stelle,
     * die ein Zeichen (Leaf-Element) repräsentiert.
     *
     * @param htmlDoc  Das HTML-Dokument, in dem die Suche durchgeführt wird
     * @param mousePos Die Mausposition relativ zur Komponente
     * @return Das Zeichen-Element an der gegebenen Position oder {@code null},
     * wenn die Position ungültig ist oder kein Element gefunden wurde
     */
    private Element getLeafElementAt(HTMLDocument htmlDoc, Point mousePos) {
        int pos = viewToModel2D(mousePos);
        return (pos >= 0) ? htmlDoc.getCharacterElement(pos) : null;
    }

    /**
     * Durchläuft die Eltern-Hierarchie eines gegebenen Leaf-Elements, um das nächstgelegene
     * {@code <a>}-Tag oder ein Element mit einem HREF-Attribut zu finden.
     *
     * @param leaf Das Ausgangselement (Leaf) im HTML-Dokument
     * @return Das nächstgelegene Link-Element ({@code <a>} oder Element mit HREF),
     * oder {@code null}, wenn kein solches Element gefunden wird
     */
    private Element findLinkElement(Element leaf) {
        Element e = leaf;

        while (e != null) {
            AttributeSet attrs = e.getAttributes();

            // Prüfe, ob ein <a>-Tag vorliegt, welches ein HREF-Attribut besitzt
            Object tagA = attrs.getAttribute(HTML.Tag.A);
            if (tagA instanceof AttributeSet aAttrs && aAttrs.getAttribute(HTML.Attribute.HREF) != null) {
                return e;
            }

            // Prüfe, ob HREF-Attribut vorliegt
            if (attrs.getAttribute(HTML.Attribute.HREF) != null) {
                return e;
            }

            e = e.getParentElement();
        }
        return null;
    }


    /**
     * Löst ein {@link HyperlinkEvent} vom Typ {@code ENTERED} für das angegebene Link-Element aus.
     * <p>
     * Wenn das Link-Element ein HREF-Attribut enthält, wird dieses als URL verwendet.
     * Andernfalls wird der Text des Elements als Beschreibung versucht in eine URL umzuwandeln.
     * Ungültige oder relative URLs führen dazu, dass kein Event ausgelöst wird.
     *
     * @param linkElement Das Element, das als Hyperlink behandelt werden soll
     * @param htmlDoc     Das HTML-Dokument, aus dem der Text des Elements ggf. extrahiert wird
     */
    private void fireHyperLinkEntered(Element linkElement, HTMLDocument htmlDoc) {
        // Attribute des gefundenen Links
        AttributeSet attrs = linkElement.getAttributes();
        Object hrefValue = attrs.getAttribute(HTML.Attribute.HREF);

        // URL und Beschreibung bestimmen
        URL url;
        String desc;

        if (hrefValue != null) {
            desc = hrefValue.toString();
            try {
                url = new URI(desc).toURL();
            } catch (Exception e) {
                // Ungültige oder relative URL
                return;
            }
        } else {
            // Wenn kein HREF-Attribut existiert, wird der Text als Beschreibung gesetzt
            try {
                int start = linkElement.getStartOffset();
                int end = linkElement.getEndOffset();
                desc = htmlDoc.getText(start, Math.max(0, end - start));
                url = new URI(desc).toURL();
            } catch (Exception e) {
                return;
            }
        }

        // HyperlinkEvent erzeugen und ENTERED-Event auslösen
        HyperlinkEvent entered = new HyperlinkEvent(this, ENTERED, url, desc, linkElement);
        hyperlinkUpdate(entered);
    }

    /**
     * Färbt den Hyperlink nur dann ein, wenn Command/Ctrl gedrückt ist.
     *
     * @param active {@code true}, wenn Link aktiv ist, sonst false
     */
    private void applyLinkHoverStyle(boolean active) {
        if (currentLinkElement == null) return;

        HTMLDocument htmlDoc = getHTMLDocument();
        if (htmlDoc == null) return;

        int startIndex = currentLinkElement.getStartOffset();
        int endIndex = currentLinkElement.getEndOffset();
        int length = Math.max(0, endIndex - startIndex);

        SimpleAttributeSet set = new SimpleAttributeSet();
        Color color = active ? COLOR_ACTIVE_LINK : UIEnvironment.getTextColor();
        StyleConstants.setForeground(set, color);

        // Farbe ändern
        SwingUtilities.invokeLater(() -> htmlDoc.setCharacterAttributes(startIndex, length, set, false));
    }
}
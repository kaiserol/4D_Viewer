package de.uzk.gui;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class SelectableText extends JEditorPane implements HyperlinkListener {
    public SelectableText(String htmlContent) {
        setEditable(false);
        setOpaque(false);
        setContentType("text/html");
        setText(htmlContent);

        // Benutzerdefinierten Textcursor setzen
        setCaret(getDefaultCaret());

        // Hyperlink-Klicks abfangen
        addHyperlinkListener(this);
    }

    private static Caret getDefaultCaret() {
        Caret caret = new DefaultCaret() {
            @Override
            public void paint(Graphics g) {
            }
        };
        caret.setBlinkRate(0);
        return caret;
    }

    @Override
    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            GuiUtils.openWebLink(e.getURL());
        }
    }
}

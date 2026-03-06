package de.uzk.image;

import de.uzk.config.Config;
import de.uzk.edit.image.MoveEdit;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static de.uzk.Main.workspace;

public class ImageDragger extends MouseAdapter implements KeyListener {
    private boolean shift;
    private MoveEdit moveEdit;
    private Point last;
    private final ImageEditor imageEditor;

    public ImageDragger(ImageEditor imageEditor) {
        this.imageEditor = imageEditor;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (shift) {
            if(moveEdit == null) {
                moveEdit = new MoveEdit();
                last = e.getPoint();
            }
            int dx =  last.x - e.getX();
            int dy =  last.y - e.getY();
            moveEdit.update(dx, dy);
            Config config = workspace.getConfig();
            config.setInsets(config.getInsetX() + dx, config.getInsetY() + dy);
            imageEditor.updateImage(true);
            last = e.getPoint();
        }
    }


    @Override
    public void mouseReleased(MouseEvent e) {
        if (shift) {
            workspace.getEditManager().registerEdit(moveEdit);
            moveEdit = null;
            last = null;
        }
    }


    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            shift = true;
            e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            SwingUtilities.invokeLater(e.getComponent()::repaint);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            shift = false;
            e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {

    }
}

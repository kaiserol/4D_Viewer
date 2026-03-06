package de.uzk.edit.image;

import de.uzk.config.Config;

import static de.uzk.Main.workspace;

public class MoveEdit extends ImageEdit {
    private int dx;
    private int dy;

    public MoveEdit() {

    }

    public void update(int dx, int dy) {
        dx += dx;
        dy -= dy;
    }

    @Override
    public boolean perform() {
        if(dx == 0 && dy == 0) {
            return false;
        }
        Config config = workspace.getConfig();
        config.setInsets(config.getInsetX() + dx, config.getInsetY() + dy);
        return true;
    }

    @Override
    public void undo() {
        Config config = workspace.getConfig();
        config.setInsets(config.getInsetX() - dx, config.getInsetY() - dy);
    }
}

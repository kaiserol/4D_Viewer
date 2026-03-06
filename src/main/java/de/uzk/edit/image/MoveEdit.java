package de.uzk.edit.image;

import de.uzk.config.Config;

import static de.uzk.Main.workspace;

public class MoveEdit extends ImageEdit {
    private int dx;
    private int dy;

    public MoveEdit() {}

    public MoveEdit(int dx, int dy) {
        update(dx, dy);
    }

    public void update(int dx, int dy) {
        this.dx += dx;
        this.dy += dy;
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

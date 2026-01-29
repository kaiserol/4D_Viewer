package de.uzk.edit;

import static de.uzk.Main.workspace;

public abstract class ImageEdit {
    protected final int time;
    protected final int level;

    protected ImageEdit(int time, int level) {
        if(time < 0 || time > workspace.getMaxTime()) {
            throw new IllegalArgumentException("Time must be between 0 and " + workspace.getMaxTime());
        } else if (level < 0 || level > workspace.getMaxLevel()) {
            throw new IllegalArgumentException("Level must be between 0 and " + workspace.getMaxLevel());
        }
        this.time = time;
        this.level = level;
    }
}

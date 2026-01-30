package de.uzk.edit.image;

import static de.uzk.Main.workspace;

public class RotateImageEdit extends ImageEdit {
    private final int degrees;

    public RotateImageEdit( int newRotation) {
        this.degrees = workspace.getConfig().getRotation() -  newRotation;
    }

    @Override
    public void undo() {
         workspace.getConfig().setRotation(workspace.getConfig().getRotation() + degrees);
    }

    @Override
    public boolean perform() {
        return workspace.getConfig().setRotation(workspace.getConfig().getRotation() - degrees);
    }
}

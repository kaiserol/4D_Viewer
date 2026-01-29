package de.uzk.edit;

import java.awt.*;

import static de.uzk.Main.workspace;

public class RotateImageEdit extends ImageEdit {
    private final int degrees;

    public RotateImageEdit( int degrees) {

        this.degrees = degrees;
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

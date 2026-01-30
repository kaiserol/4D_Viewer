package de.uzk.edit.image;

import static de.uzk.Main.workspace;

public class BrightnessEdit extends ImageEdit {
    private final int brightnessDifference;

    public BrightnessEdit(int newContrast) {
        brightnessDifference = workspace.getConfig().getBrightness() - newContrast;
    }

    @Override
    public boolean perform() {
        return workspace.getConfig().setBrightness(workspace.getConfig().getBrightness() - brightnessDifference);
    }

    @Override
    public void undo() {
        workspace.getConfig().setBrightness(workspace.getConfig().getBrightness() + brightnessDifference);
    }
}

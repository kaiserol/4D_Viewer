package de.uzk.edit.image;

import static de.uzk.Main.workspace;

public class ContrastEdit extends ImageEdit {
    private final int contrastDifference;

    public ContrastEdit(int newContrast) {
        contrastDifference = workspace.getConfig().getContrast() - newContrast;
    }

    @Override
    public boolean perform() {
        return workspace.getConfig().setContrast(workspace.getConfig().getContrast() -  contrastDifference);
    }

    @Override
    public void undo() {
        workspace.getConfig().setContrast(workspace.getConfig().getContrast() + contrastDifference);
    }
}

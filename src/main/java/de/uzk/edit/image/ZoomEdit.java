package de.uzk.edit.image;


import static de.uzk.Main.workspace;

public class ZoomEdit extends ImageEdit {
    private final int percentageDifference;

    public ZoomEdit(int percentage) {
        percentageDifference = workspace.getConfig().getZoom() - percentage;
    }

    @Override
    public boolean perform() {
        return workspace.getConfig().setZoom(workspace.getConfig().getZoom() - percentageDifference);
    }

    @Override
    public void undo() {
        workspace.getConfig().setZoom(workspace.getConfig().getZoom() + percentageDifference);
    }
}

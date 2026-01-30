package de.uzk.edit;

import de.uzk.action.ActionType;

import static de.uzk.Main.workspace;

public class LevelUnitEdit extends Edit {
    private final double levelUnitDifference;

    public LevelUnitEdit(double newLevelUnit) {
        levelUnitDifference = workspace.getConfig().getLevelUnit() - newLevelUnit;
    }

    @Override
    public boolean perform() {
        return workspace.getConfig().setLevelUnit(workspace.getConfig().getLevelUnit() - levelUnitDifference);
    }

    @Override
    public void undo() {
        workspace.getConfig().setLevelUnit(workspace.getConfig().getLevelUnit() + levelUnitDifference);
    }

    @Override
    public ActionType getActionType() {
        return ActionType.ACTION_UPDATE_UNIT;
    }
}

package de.uzk.edit;

import de.uzk.action.ActionType;

import static de.uzk.Main.workspace;

public class TimeUnitEdit extends Edit {
    private final double timeUnitDifference;

    public TimeUnitEdit(double newTimeUnit) {
        this.timeUnitDifference = workspace.getConfig().getTimeUnit() - newTimeUnit;

    }


    @Override
    public boolean perform() {
        return workspace.getConfig().setTimeUnit(workspace.getConfig().getTimeUnit() - timeUnitDifference);
    }

    @Override
    public void undo() {
        workspace.getConfig().setTimeUnit(workspace.getConfig().getTimeUnit() + timeUnitDifference);

    }

    @Override
    public ActionType getActionType() {
        return ActionType.ACTION_UPDATE_UNIT;
    }
}

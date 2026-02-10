package de.uzk.edit;

import de.uzk.action.ActionType;

import java.util.LinkedList;

public class EditManager {
    private final LinkedList<Edit> editsMade;
    private final LinkedList<Edit> editsUndone;

    public EditManager() {
        editsMade = new LinkedList<>();
        editsUndone = new LinkedList<>();
    }

    public boolean performEdit(MaybeRedundantEdit edit) {
        if (edit.isRedundant()) return false;
        return performEdit((Edit) edit);
    }

    public void registerEdit(MaybeRedundantEdit edit) {
        if (edit.isRedundant()) return;
        registerEdit((Edit) edit);
    }

    public boolean performEdit(Edit edit) {
        if (edit.perform()) {
            registerEdit(edit);
            return true;
        }
        return false;
    }

    public void registerEdit(Edit edit) {
        editsMade.push(edit);
        editsUndone.clear();
    }

    public ActionType undoLastEdit() {
        if (editsMade.isEmpty()) return null;
        Edit last = editsMade.pop();
        last.undo();
        editsUndone.push(last);
        return last.getActionType();
    }

    public ActionType redoLastEdit() {
        if (editsUndone.isEmpty()) return null;
        Edit last = editsUndone.pop();
        last.perform();
        editsMade.push(last);
        return last.getActionType();
    }

}

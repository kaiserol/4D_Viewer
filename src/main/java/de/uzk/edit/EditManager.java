package de.uzk.edit;

import de.uzk.action.ActionType;

import java.util.LinkedList;

public class EditManager {
    private final LinkedList<Edit> editsMade;
    private final LinkedList<Edit> editsUndone;
    public EditManager( ) {
        editsMade = new LinkedList<>();
        editsUndone = new LinkedList<>();
    }

    public boolean performEdit(Edit edit) {
        boolean valid = edit.perform();
        if(valid) {
            editsMade.push(edit);
            editsUndone.clear();
        }
        return valid;
    }

    public ActionType undoLastEdit() {
        if(editsMade.isEmpty()) return null;
        Edit last = editsMade.pop();
        last.undo();
        editsUndone.push(last);
        return last.getActionType();
    }

    public ActionType redoLastEdit() {
        if(editsUndone.isEmpty()) return null;
        Edit last = editsUndone.pop();
        last.perform();
        editsMade.push(last);
        return last.getActionType();
    }

}

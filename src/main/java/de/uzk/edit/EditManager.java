package de.uzk.edit;

import java.util.LinkedList;

public class EditManager {
    private LinkedList<Edit> editsMade;
    private LinkedList<Edit> editsUndone;

    public EditManager() {
        editsMade = new LinkedList<>();
        editsUndone = new LinkedList<>();
    }

    public void performEdit(Edit edit) {
        edit.redo();
        editsMade.push(edit);
        //TODO: was passiert wenn der user jetzt "redo" wählt?
    }

    public boolean undoLastEdit() {
        Edit last = editsMade.pop();
        if(last != null) {
            last.undo();
            editsUndone.push(last);
            return true;
        }
        return false;
    }

    public boolean redoLastEdit() {
        Edit last = editsUndone.pop();
        if(last != null) {
            last.redo();
            editsMade.push(last);
            return true;
        }
        return false;
    }

    public Edit viewLastEdit() {
        return editsMade.peek();
    }
}

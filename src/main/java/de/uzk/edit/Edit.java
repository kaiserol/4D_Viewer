package de.uzk.edit;

public abstract class Edit {

    protected Edit() {}

    public abstract void undo();

    public abstract void redo();
}

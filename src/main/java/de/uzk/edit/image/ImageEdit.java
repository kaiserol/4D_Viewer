package de.uzk.edit.image;

import de.uzk.action.ActionType;
import de.uzk.edit.Edit;

/**
 * Basisklasse für Edits, die sämtliche Bilder bearbeiten und somit ActionType.ACTION_EDIT_IMAGE auslösen
 * */
public abstract class ImageEdit extends Edit {
    @Override public ActionType getActionType() { return ActionType.ACTION_EDIT_IMAGE; }
}

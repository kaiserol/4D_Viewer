package de.uzk.edit;

import de.uzk.action.ActionType;

public abstract class ImageEdit extends Edit {
    @Override public ActionType getType() { return ActionType.ACTION_EDIT_IMAGE; }
}

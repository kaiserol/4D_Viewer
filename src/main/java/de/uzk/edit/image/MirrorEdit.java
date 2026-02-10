package de.uzk.edit.image;

import java.util.function.Function;

import static de.uzk.Main.workspace;

/**
 * MirrorX/MirrorY Eigenschaften
 *
 */
public class MirrorEdit extends ImageEdit {
    private final boolean isMirror;
    private final Function<Boolean, Boolean> setter;

    private MirrorEdit(boolean isMirror, Function<Boolean, Boolean> setter) {
        this.isMirror = isMirror;
        this.setter = setter;
    }

    public static MirrorEdit mirrorXEdit(boolean mirror) {
        return new MirrorEdit(mirror, workspace.getConfig()::setMirrorX);
    }

    public static MirrorEdit mirrorYEdit(boolean mirror) {
        return new MirrorEdit(mirror, workspace.getConfig()::setMirrorY);
    }

    @Override
    public boolean perform() {
        return setter.apply(isMirror);
    }

    @Override
    public void undo() {
        setter.apply(!isMirror);
    }
}

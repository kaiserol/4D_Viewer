package de.uzk.config;

import com.fasterxml.jackson.annotation.JsonValue;

import static de.uzk.config.LanguageHandler.getWord;

public enum InitialDirectory {
    ROOT,
    HOME,
    LAST_OPENED,
    CWD;

    @Override
    public String toString() {
        return switch(this) {
            case ROOT -> getWord("dialog.settings.initialDirectory.root");
            case HOME -> getWord("dialog.settings.initialDirectory.home");
            case LAST_OPENED -> getWord("dialog.settings.initialDirectory.lastOpened");
            case CWD -> getWord("dialog.settings.initialDirectory.cwd");
        };
    }

    @JsonValue
    public String getId() { return super.toString().toLowerCase(); }
}

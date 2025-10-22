package de.uzk;

import de.uzk.config.Config;

import java.nio.file.Path;

public class Workspace {
    private Path location;
    private Config config;

    public Workspace(Path location) {
        this.location = location;

    }
}

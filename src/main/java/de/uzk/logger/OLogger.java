package de.uzk.logger;

import de.uzk.utils.SystemConstants;

import java.util.ArrayList;
import java.util.List;

import static de.uzk.logger.OLevel.*;

public class OLogger {
    private final String name;
    private final List<OLogInfo> logs;
    private boolean open;

    public OLogger(String name) {
        this.name = name;
        this.logs = new ArrayList<>();
        this.open = true;
    }

    public String getName() {
        return name;
    }

    public List<OLogInfo> getLogs() {
        return logs;
    }

    public void warning(String message) {
        log(WARNING, message);
    }

    public void info(String message) {
        log(INFO, message);
    }

    public void error(String message) {
        log(ERROR, message);
    }

    public void logException(Exception e) {
        StringBuilder msg = new StringBuilder();
        msg.append(e.getClass().getPackageName()).append(".").append(e.getClass().getSimpleName()).append(" ");
        msg.append(e.getMessage()).append(SystemConstants.NEXT_LINE);

        StackTraceElement[] stackTrace = e.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            msg.append("\tat ").append(stackTrace[i]);
            if (i < stackTrace.length - 1) msg.append(SystemConstants.NEXT_LINE);
        }
        error(msg.toString());
    }

    private void log(OLevel level, String message) {
        if (this.open) {
            OLogInfo logInfo = new OLogInfo(level, message + SystemConstants.NEXT_LINE);
            if (level != null) {
                logs.add(logInfo);
                System.out.print(logInfo);
            }
        }
    }

    public void close() {
        this.open = false;
    }

    public void open() {
        this.open = true;
    }
}

package de.uzk.logger;

import de.uzk.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static de.uzk.logger.LogLevel.*;

public class LogEntryHandler {
    private final String name;
    private final List<LogEntry> logEntry;

    public LogEntryHandler(String name) {
        this.name = name;
        this.logEntry = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<LogEntry> getLogs() {
        return logEntry;
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
        msg.append(e.getMessage()).append(StringUtils.NEXT_LINE);

        StackTraceElement[] stackTrace = e.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            msg.append("\tat ").append(stackTrace[i]);
            if (i < stackTrace.length - 1) msg.append(StringUtils.NEXT_LINE);
        }
        log(EXCEPTION, msg.toString());
    }

    private void log(LogLevel level, String message) {
        String msgText = message == null ? "" : message;
        LogEntry logEntry = new LogEntry(level, msgText + StringUtils.NEXT_LINE);

        if (level != null) {
            this.logEntry.add(logEntry);
            System.out.print(logEntry.getFormattedText(false));
        }
    }
}

package de.uzk.logger;

import de.uzk.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static de.uzk.logger.LogLevel.*;

public class LogDataHandler {
    private final String name;
    private final List<LogData> logData;
    private boolean open;

    public LogDataHandler(String name) {
        this.name = name;
        this.logData = new ArrayList<>();
        this.open = true;
    }

    public String getName() {
        return name;
    }

    public List<LogData> getLogs() {
        return logData;
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
        error(msg.toString());
    }

    private void log(LogLevel level, String message) {
        if (this.open) {
            LogData logData = new LogData(level, message + StringUtils.NEXT_LINE);
            if (level != null) {
                this.logData.add(logData);
                System.out.print(logData);
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

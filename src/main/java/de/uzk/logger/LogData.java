package de.uzk.logger;

import de.uzk.config.SystemConstants;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogData {
    private static final SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");
    private final LogLevel level;
    private final String message;
    private final String source;
    private final String dateTime;

    public LogData(LogLevel level, String message) {
        this.level = level;
        this.message = message;
        this.source = initSource();
        this.dateTime = initDateTime();
    }

    private String initSource() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length >= 6) {
            // stackTrace[0] (getStackTrace)
            // stackTrace[1] (initSource)
            // stackTrace[2] (constructor)
            // stackTrace[3] (log)
            // stackTrace[4] (log-method caller)
            // stackTrace[5] log-method caller-caller

            StackTraceElement caller = stackTrace[5];
            String className = caller.getClassName();
            String methodName = caller.getMethodName();
            return className + " " + methodName;
        }
        return null;
    }

    private String initDateTime() {
        return format.format(new Date());
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getSource() {
        return source;
    }

    public String getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return this.getDateTime() + " " + this.getSource() + SystemConstants.NEXT_LINE +
                "[" + this.getLevel() + "]: " + this.getMessage();
    }
}
package de.uzk.logger;

import de.uzk.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import static de.uzk.logger.LogLevel.EXCEPTION;

public class LogEntry {
    // Formate
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");

    // Protokoll Attribute
    private final String dateTime;
    private final String source;
    private final LogLevel level;
    private final String message;

    public LogEntry(LogLevel level, String message) {
        this.dateTime = initDateTime();
        this.source = initSource();
        this.level = level;
        this.message = message;
    }

    private String initDateTime() {
        return DATE_TIME_FORMAT.format(new Date());
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

    public String getDateTime() {
        return this.dateTime;
    }

    public String getSource() {
        return this.source;
    }

    public LogLevel getLevel() {
        return this.level;
    }

    public String getMessage() {
        if (this.message == null) return "";
        return this.message;
    }

    public String getFormattedText(boolean includeHTML) {
        String headerText = this.getDateTime() + " " + this.getSource() + StringUtils.NEXT_LINE;
        String levelText = "[" + this.getLevel() + "]: ";
        String[] content = this.getMessage().split(StringUtils.NEXT_LINE);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < content.length; i++) {
            if (i > 0 && level != EXCEPTION) sb.append(" ".repeat(levelText.length()));
            sb.append(content[i]).append(StringUtils.NEXT_LINE);
        }

        if (includeHTML) {
            String headerTextHTML = StringUtils.wrapBold(headerText);
            String levelTextHTML = StringUtils.applyColor(StringUtils.wrapBold(levelText), this.getLevel().getColor());
            return StringUtils.wrapPre(headerTextHTML + levelTextHTML + sb);
        }
        return headerText + levelText + sb;
    }

    @Override
    public String toString() {
        return this.message;
    }
}
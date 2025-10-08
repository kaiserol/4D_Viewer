package de.uzk.utils;

import de.uzk.logger.LogData;

import java.awt.*;
import java.io.File;

public class StringUtils {
    public static final String FILE_SEP = File.separator;
    public static final String NEXT_LINE = System.lineSeparator();

    private StringUtils() {
    }

    public static String formatLevel(int level, double multiplier) {
        return String.format("%.01f μm", level * multiplier);
    }

    public static String formatTime(int time, double multiplier) {
        time = (int) (time * multiplier);

        int seconds = time % 60;
        int minute = time / 60 % 60;
        int hour = time / 60 / 60;

        return String.format("%02d:%02d:%02d", hour, minute, seconds);
    }

    public static String formatArray(Object[] arr, String arrSep, char leftBorder, char rightBorder) {
        if (arr == null) return String.valueOf(leftBorder) + rightBorder;

        int iMax = arr.length - 1;
        if (iMax == -1) return String.valueOf(leftBorder) + rightBorder;

        StringBuilder arrBuilder = new StringBuilder();
        arrBuilder.append(leftBorder);

        for (int i = 0; i <= iMax; i++) {
            arrBuilder.append(arr[i]);
            if (i == iMax) break;
            arrBuilder.append(arrSep);
        }
        return arrBuilder.append(rightBorder).toString();
    }

    public static String javaToHTML(String javaString) {
        return "<pre>" + javaString.replace(StringUtils.NEXT_LINE, "<br>").
                replace("\t", "    ") + "</pre>";
    }

    public static String toHTML(LogData logData) {
        String color;
        switch (logData.getLevel()) {
            case DEBUG -> color = "blue";
            case ERROR -> color = "red";
            case INFO -> color = "green";
            case WARNING -> color = "orange";
            default -> color = "black";
        }
        return "<b>" + logData.getDateTime() + "</b> " + logData.getSource() + StringUtils.NEXT_LINE +
                "<font color=" + color + ">[" + logData.getLevel() + "]:</font> " + logData.getMessage();
    }

    public static String colorToHex(Color color) {
        if (color == null) return "#000000";

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int rgb = (r << 16) | (g << 8) | b;
        return String.format("#%06X", rgb);
    }

    public static String[] splitTextInWords(String text) {
        return text == null ? new String[0] : text.split("\\s+");
    }
}
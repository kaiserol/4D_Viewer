package de.uzk.utils;

import de.uzk.config.SystemConstants;
import de.uzk.logger.LogData;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StringUtils {
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
        return "<pre>" + javaString.replace(SystemConstants.NEXT_LINE, "<br>").
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
        return "<b>" + logData.getDateTime() + "</b> " + logData.getSource() + SystemConstants.NEXT_LINE +
                "<font color=" + color + ">[" + logData.getLevel() + "]:</font> " + logData.getMessage();
    }

    public static String[] splitTextInWords(String text) {
        return text == null ? new String[0] : text.split("\\s+");
    }

    public static void updateUsedWords(String text, Map<String, Integer> usedWords) {
        if (text == null) return;
        String lower = text.toLowerCase();
        String[] words = splitTextInWords(lower);

        for (String word : words) {
            Integer integer = usedWords.put(word, 1);
            if (integer != null) usedWords.put(word, 1 + integer);
        }
    }

    public static List<String> getAllowedWords(String text, Map<String, Integer> usedWords) {
        final List<String> allowedWords = new ArrayList<>();
        if ((text != null && !text.isEmpty())) {
            final String lower = text.toLowerCase();
            allowedWords.addAll(List.of(splitTextInWords(lower)));

            usedWords.keySet().forEach(w -> {
                int count = usedWords.get(w);
                if (count > 1) allowedWords.remove(w);
            });
        }
        return allowedWords;
    }

    public static String colorToHex(Color color) {
        if (color == null) return "#000000";

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int rgb = (r << 16) | (g << 8) | b;
        return String.format("#%06X", rgb);
    }

    public static boolean isAsciiLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }
}
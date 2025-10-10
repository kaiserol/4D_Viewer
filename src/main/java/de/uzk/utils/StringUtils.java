package de.uzk.utils;

import org.intellij.lang.annotations.MagicConstant;

import java.awt.*;
import java.io.File;

public final class StringUtils {
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

    public static String[] splitTextInWords(String text) {
        return text == null ? new String[0] : text.split("\\s+");
    }

    // ---------- HTML FORMAT ----------

    public static String wrapColor(String text, Color color) {
        return "<font color=\"" + colorToHex(color) + "\">" + text + "</font>";
    }

    public static String wrapBold(String text) {
        return "<b>" + text + "</b>";
    }

    public static String wrapItalic(String text) {
        return "<i>" + text + "</i>";
    }

    public static String wrapCenter(String text) {
        return "<center>" + text + "</center>";
    }

    public static String wrapP(String text) {
        return "<p>" + text
                .replace(NEXT_LINE, "<br>")
                .replace("\t", "    ")
                + "</p>";
    }

    public static String wrapHtmlDocument(String htmlContent) {
        String fontFamilyText = "font-family: %s;".formatted("monospaced");
        return wrapHtml("<head><style>body { %s } p {margin: 5px 0}</style></head><body>".formatted(fontFamilyText) +
                htmlContent + "</body>");
    }

    public static String wrapHtml(String text) {
        return "<html>" + text + "</html>";
    }

    // ---------- FONTSTYLE HANDLING ----------

    public static String applyFontStyle(String text, @MagicConstant(flags = {Font.PLAIN, Font.BOLD, Font.ITALIC}) int fontStyle) {
        return switch (fontStyle) {
            case Font.BOLD -> wrapBold(text);
            case Font.ITALIC -> wrapItalic(text);
            case Font.BOLD | Font.ITALIC -> wrapBold(wrapItalic(text));
            default -> text;
        };
    }

    // ---------- COLOR HELPER ----------

    public static String colorToHex(Color color) {
        if (color == null) return "#000000";

        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int rgb = (r << 16) | (g << 8) | b;
        return String.format("#%06X", rgb);
    }
}
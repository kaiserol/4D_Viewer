package de.uzk.image;

import de.uzk.utils.ColorUtils;
import de.uzk.utils.StringUtils;

import java.util.*;

import static de.uzk.Main.logger;
import static de.uzk.Main.workspace;

public class MissingImagesReport {
    private final Map<Integer, List<Integer>> missingByTime;

    public MissingImagesReport() {
        this.missingByTime = new HashMap<>();
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    public static String createImageFileRow(ImageFile imageFile) {
        String fileName = imageFile.getFileName();
        int time = imageFile.getTime();
        int level = imageFile.getLevel();

        String text = "- Image: '%s' (Time=%s, Level=%s)".formatted(fileName, time, level);
        return text + StringUtils.NEXT_LINE;
    }

    public static String createImageFileRow(int time, int level) {
        ImageFile imageFile = workspace.getImageFile(time, level);
        return createImageFileRow(imageFile);
    }

    public static String createHeaderText(int count, String type) {
        String base = count + " " + (count == 1 ? "Image is" : "Images are");
        return base + " " + type + ":";
    }

    public static String createReport(String headerText, StringBuilder reportBuilder) {
        String lineBreaksPattern = "(%s)+$".formatted(StringUtils.NEXT_LINE);
        String formattedReport = reportBuilder.toString().replaceAll(lineBreaksPattern, "");
        return headerText + StringUtils.NEXT_LINE + formattedReport;
    }

    public int getMissingImagesCount() {
        return this.missingByTime.values().stream().mapToInt(List::size).sum();
    }

    public void clear() {
        this.missingByTime.clear();
    }

    public void logReport(boolean onLoading) {
        if (!workspace.isLoaded()) return;

        // Neuen Report aufbauen
        StringBuilder reportBuilder = new StringBuilder();
        Map<Integer, List<Integer>> newMissingByTime = new HashMap<>();
        List<ImageFile> newMissingList = new ArrayList<>();

        int missingCount = 0;

        // Durchlaufe Matrix und finde fehlende Bilder
        for (int time = 0; time <= workspace.getMaxTime(); time++) {
            List<Integer> levelList = new ArrayList<>();
            newMissingByTime.put(time, levelList);

            for (int level = 0; level <= workspace.getMaxLevel(); level++) {
                ImageFile imageFile = workspace.getImageFile(time, level);
                if (!imageFile.exists()) {
                    reportBuilder.append(createImageFileRow(imageFile));
                    levelList.add(level);
                    newMissingList.add(imageFile);
                    missingCount++;
                }
            }
        }

        // Alte Liste aus Map ableiten
        List<ImageFile> oldMissingList = getAllMissingImagesFromMap(this.missingByTime);

        // Prüfen, ob sich etwas geändert hat
        if (hasDifferences(oldMissingList, newMissingList)) {
            if (onLoading) {
                if (missingCount == 0) return;

                String headerText = createHeaderText(missingCount, "missing");
                String reportOutput = createReport(headerText, reportBuilder);
                logger.warn(reportOutput);
            } else logListDifferences(oldMissingList, newMissingList);
        }

        // Map aktualisieren
        this.missingByTime.clear();
        this.missingByTime.putAll(newMissingByTime);
    }

    // ========================================
    // Vergleichslogik
    // ========================================
    private List<ImageFile> getAllMissingImagesFromMap(Map<Integer, List<Integer>> map) {
        List<ImageFile> list = new ArrayList<>();
        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            int time = entry.getKey();
            for (int level : entry.getValue()) {
                list.add(workspace.getImageFile(time, level));
            }
        }
        return list;
    }

    private boolean hasDifferences(List<ImageFile> oldList, List<ImageFile> newList) {
        return !Objects.equals(oldList, newList);
    }

    private void logListDifferences(List<ImageFile> oldList, List<ImageFile> newList) {
        List<ImageFile> newlyMissingImages = new ArrayList<>(newList);
        List<ImageFile> restoredImages = new ArrayList<>(oldList);
        newlyMissingImages.removeAll(oldList);
        restoredImages.removeAll(newList);

        // Report ausgeben
        if (!restoredImages.isEmpty()) {
            StringBuilder reportBuilder = new StringBuilder();
            restoredImages.forEach(imageFile -> reportBuilder.append(createImageFileRow(imageFile)));

            // Report ausgeben
            String headerText = createHeaderText(restoredImages.size(), "restored");
            String reportOutput = createReport(headerText, reportBuilder);
            logger.info(reportOutput);
        }

        // Report ausgeben
        if (!newlyMissingImages.isEmpty()) {
            StringBuilder reportBuilder = new StringBuilder();
            newlyMissingImages.forEach(imageFile -> reportBuilder.append(createImageFileRow(imageFile)));

            // Report ausgeben
            String headerText = createHeaderText(newlyMissingImages.size(), "newly missing");
            String reportOutput = createReport(headerText, reportBuilder);
            logger.warn(reportOutput);
        }
    }

    // ========================================
    // Reporting
    // ========================================
    public String getHtmlReport() {
        // Report erstellen
        StringBuilder reportBuilder = new StringBuilder();

        // Durchlaufe Matrix und finde fehlende Bilder
        for (int time = 0; time < workspace.getMaxTime(); time++) {
            List<Integer> missingLevels = missingByTime.get(time);
            if (missingLevels == null || missingLevels.isEmpty()) continue;

            reportBuilder.append(StringUtils.wrapBold("--- Time: " + time + " ---")).append(StringUtils.NEXT_LINE);
            reportBuilder.append("Missing Levels: ").append(missingLevels).append(StringUtils.NEXT_LINE);
            reportBuilder.append("Expected Images:").append(StringUtils.NEXT_LINE);
            for (int level : missingLevels) reportBuilder.append(createImageFileRow(time, level));
            reportBuilder.append(StringUtils.NEXT_LINE);
        }

        int missingCount = getMissingImagesCount();
        if (missingCount == 0) {
            if (!workspace.isLoaded()) return "";

            int maxTime = workspace.getMaxTime();
            int maxLevel = workspace.getMaxLevel();
            String loadedImages = "%d Images (%dx%d)".formatted((maxTime + 1) * (maxLevel + 1), maxTime + 1, maxLevel + 1);
            reportBuilder.append(loadedImages).append(StringUtils.NEXT_LINE).append(StringUtils.NEXT_LINE);

            String formattedSection = StringUtils.applyColor(StringUtils.wrapBold("Further Information:"), ColorUtils.COLOR_BLUE);
            reportBuilder.append(formattedSection).append(StringUtils.NEXT_LINE);
            reportBuilder.append("Max Time: ").append(maxTime).append(StringUtils.NEXT_LINE);
            reportBuilder.append("Max Level: ").append(maxLevel).append(StringUtils.NEXT_LINE);

            String headerText = "Loaded Images:" + StringUtils.NEXT_LINE;
            String formattedHeaderText = StringUtils.applyColor(StringUtils.wrapBold(headerText), ColorUtils.COLOR_BLUE);
            return formattedHeaderText + reportBuilder;
        } else {
            String headerText = createHeaderText(missingCount, "missing") + StringUtils.NEXT_LINE;
            String formattedHeaderText = StringUtils.applyColor(StringUtils.wrapBold(headerText), ColorUtils.COLOR_RED);
            return createReport(formattedHeaderText, reportBuilder);
        }
    }
}
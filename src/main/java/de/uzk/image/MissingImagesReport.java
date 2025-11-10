package de.uzk.image;

import de.uzk.gui.GuiUtils;
import de.uzk.utils.StringUtils;

import java.util.*;

import static de.uzk.Main.logger;
import static de.uzk.Main.workspace;

public class MissingImagesReport {
    private final Map<Integer, List<Integer>> missingByTime;

    public MissingImagesReport() {
        this.missingByTime = new HashMap<>();
    }

    public int getMissingImagesCount() {
        return this.missingByTime.values().stream().mapToInt(List::size).sum();
    }

    public void clear() {
        this.missingByTime.clear();
    }

    public void logReport(boolean onLoading) {
        if (!workspace.isOpen()) return;

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
            if (onLoading) logReport(missingCount, "missing", reportBuilder);
            else logListDifferences(oldMissingList, newMissingList);
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
            String headerText = createReportHeader(restoredImages.size(), "restored");
            logger.info(headerText + reportBuilder);
        }

        // Report ausgeben
        if (!newlyMissingImages.isEmpty()) {
            StringBuilder reportBuilder = new StringBuilder();
            newlyMissingImages.forEach(imageFile -> reportBuilder.append(createImageFileRow(imageFile)));

            // Report ausgeben
            String headerText = createReportHeader(newlyMissingImages.size(), "newly missing");
            logger.warning(headerText + reportBuilder);
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
            if (!workspace.isOpen()) return "";

            int maxTime = workspace.getMaxTime();
            int maxLevel = workspace.getMaxLevel();
            String loadedImages = String.format("%d (%dx%d)", (maxTime + 1) * (maxLevel + 1), maxTime + 1, maxLevel + 1);
            reportBuilder.append(StringUtils.wrapBold("Max Time: ")).append(maxTime).append(StringUtils.NEXT_LINE);
            reportBuilder.append(StringUtils.wrapBold("Max Level: ")).append(maxLevel).append(StringUtils.NEXT_LINE);
            reportBuilder.append(StringUtils.wrapBold("In Total: ")).append(loadedImages).append(StringUtils.NEXT_LINE);

            String headerText = "Loaded Images:" + StringUtils.NEXT_LINE;
            String formattedText = StringUtils.applyColor(StringUtils.wrapBold(headerText), GuiUtils.COLOR_BLUE);
            return formattedText + StringUtils.NEXT_LINE + reportBuilder;
        } else {
            String headerText = createReportHeader(missingCount, "missing");
            String formattedText = StringUtils.applyColor(StringUtils.wrapBold(headerText), GuiUtils.COLOR_RED);
            return formattedText + StringUtils.NEXT_LINE + reportBuilder;
        }
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    public static String createImageFileRow(ImageFile imageFile) {
        String fileName = imageFile.getFileName();
        int time = imageFile.getTime();
        int level = imageFile.getLevel();

        String text = String.format("- Image: '%s' (time=%s, level=%s)", fileName, time, level);
        return text + StringUtils.NEXT_LINE;
    }

    public static String createImageFileRow(int time, int level) {
        ImageFile imageFile = workspace.getImageFile(time, level);
        return createImageFileRow(imageFile);
    }

    public static String createReportHeader(int count, String type) {
        String base = count + " " + (count == 1 ? "image is" : "images are");
        return base + " " + type + ":" + StringUtils.NEXT_LINE;
    }

    public static void logReport(int count, String type, StringBuilder report) {
        if (count > 0) {
            logger.warning(createReportHeader(count, type) + report);
        }
    }
}
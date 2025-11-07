package de.uzk.image;

import de.uzk.config.Config;
import de.uzk.utils.StringUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.uzk.Main.logger;
import static de.uzk.gui.GuiUtils.COLOR_RED;

public class MissingImagesReport {
    private final Map<Integer, List<ImageFile>> missingByTime;
    private final Workspace workspace;
    private final Config config;
    private final ImageFile referenceImageFile;

    public MissingImagesReport(Workspace workspace) {
        this.missingByTime = new HashMap<>();
        this.workspace = workspace;
        this.config = workspace.getConfig();
        this.referenceImageFile = findExistingImageFile();
        fillReport();
    }

    public int getMissingImagesCount() {
        return this.missingByTime.values().stream().mapToInt(List::size).sum();
    }

    public String getFormattedReport() {

        StringBuilder report = new StringBuilder();
        int missing = 0;

        // Durchlaufe die ganze Matrix
        for (List<ImageFile> missingList : missingByTime.values()) {
            if (missingList.isEmpty()) continue;
            int time = missingList.get(0).getTime();
            report.append(StringUtils.wrapBold("--- Time: " + time + " ---")).append(StringUtils.NEXT_LINE);
            report.append("Missing Levels: ").append(missingList.size()).append(StringUtils.NEXT_LINE);
            report.append("Expected Images:").append(StringUtils.NEXT_LINE);
            for (ImageFile file : missingList) {
                report.append(formatImageFileRow(file)).append(" ");
            }
            report.append(StringUtils.NEXT_LINE);


        }

        if (this.missingByTime.isEmpty()) {
            report.append("No missing images.").append(StringUtils.NEXT_LINE);
            return report.toString();
        } else {
            String headerText = createReportHeader(missing);
            String formattedText = StringUtils.applyColor(StringUtils.wrapBold(headerText), COLOR_RED);
            return formattedText + StringUtils.NEXT_LINE + report;
        }
    }

    public void log() {
        StringBuilder report = new StringBuilder();
        int count = 0;
        for (List<ImageFile> missingList : missingByTime.values()) {
            if (missingList.isEmpty()) continue;
            for (ImageFile missing : missingList) report.append(formatImageFileRow(missing));
            report.append(StringUtils.NEXT_LINE);
            count += missingList.size();
        }
        if (count > 0) {
            logger.warning(createReportHeader(count) + report);
        }
    }

    private void fillReport() {
        for (int t = 0; t < workspace.getMaxTime(); t++) {
            List<ImageFile> missing = new ArrayList<>();
            for (int l = 0; l < workspace.getMaxLevel(); l++) {
                ImageFile imageFile = workspace.getImageFile(t, l);
                if (imageFile == null) {
                    ImageFile dummy = new ImageFile(getImageFilePath(t, l), t, l);
                    missing.add(dummy);
                    workspace.setImageFile(t, l, dummy);
                } else if (!imageFile.exists()) {
                    missing.add(imageFile);
                }
            }
            missingByTime.put(t, missing);
        }
    }

    private String createReportHeader(int count) {
        String base = count + " " + (count == 1 ? "image is" : "images are");
        return base + " missing:" + StringUtils.NEXT_LINE;
    }

    private ImageFile findExistingImageFile() {
        for (int time = 0; time <= this.workspace.getMaxTime(); time++) {
            for (int level = 0; level <= this.workspace.getMaxLevel(); level++) {
                ImageFile imageFile = this.workspace.getImageFile(time, level);
                if (imageFile != null && imageFile.exists()) return imageFile;
            }
        }
        return null;
    }

    private Path getImageFilePath(int time, int level) {
        String fileName = referenceImageFile.getFileName();
        Path parentDirectory = referenceImageFile.getFilePath().getParent();

        int timeStrLength = workspace.getTimeStr(fileName).length();
        int levelStrLength = workspace.getLevelStr(fileName).length();
        String extension = workspace.getExtension(fileName);

        // Dynamische Bestandteile erzeugen
        String timeStr = (workspace.getConfig().getTimeSep() + "%0" + timeStrLength + "d").formatted(time);
        String levelStr = (workspace.getConfig().getLevelSep() + "%0" + levelStrLength + "d").formatted(level);
        return parentDirectory.resolve(timeStr + levelStr + "." + extension);
    }

    private String formatImageFileRow(ImageFile imageFile) {
        String fileName = imageFile.getFileName();
        int time = imageFile.getTime();
        int level = imageFile.getLevel();

        String text = String.format("- Filename: '%s' (time=%s, level=%s)", fileName, time, level);
        return text + StringUtils.NEXT_LINE;
    }
}
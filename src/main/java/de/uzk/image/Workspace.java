package de.uzk.image;

import de.uzk.config.Config;
import de.uzk.markers.Markers;
import de.uzk.utils.NumberUtils;
import de.uzk.utils.StringUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.StreamSupport;

import static de.uzk.Main.history;
import static de.uzk.Main.logger;
import static de.uzk.gui.GuiUtils.COLOR_RED;

public class Workspace {
    // Eigenschaften
    private Path imagesDirectory;
    private Config config;
    private Markers markers;
    private ImageFile[][] matrix;
    private ImageFile imageFile;
    private int maxTime;
    private int maxLevel;
    private int missingImagesCount;
    private final List<Integer> pinTimes;

    public Workspace() {
        this.pinTimes = new ArrayList<>();
        clear(true);
    }

    // ========================================
    // Getter und Setter
    // ========================================
    public Path getImagesDirectory() {
        return this.imagesDirectory;
    }

    public Config getConfig() {
        return this.config;
    }

    public Markers getMarkers() {
        return this.markers;
    }

    private void load(ImageFileType imageFileType) {
        this.config = Config.load();
        this.config.setImageFileType(imageFileType);
        this.markers = Markers.load();
    }

    public void save() {
        if (isOpen()) {
            this.config.save();
            this.markers.save();
        }
    }

    public boolean isOpen() {
        return this.matrix != null;
    }

    public ImageFile getImageFile() {
        return this.imageFile;
    }

    private ImageFile getImageFile(int time, int level) {
        return this.matrix[time][level];
    }

    private void setImageFile(int time, int level, ImageFile imageFile) {
        this.matrix[time][level] = imageFile;
    }

    public int getMaxTime() {
        return this.maxTime;
    }

    public int getTime() {
        return isOpen() ? this.imageFile.getTime() : 0;
    }

    public void setTime(int time) {
        if (isOpen() && checkTime(time)) {
            ImageFile imageFile = getImageFile(time, this.imageFile.getLevel());
            if (imageFile != null) this.imageFile = imageFile;
        }
    }

    private boolean checkTime(int time) {
        if (time >= 0 && time <= this.maxTime) return true;
        logger.error("Invalid Time: " + time);
        return false;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public int getLevel() {
        return isOpen() ? this.imageFile.getLevel() : 0;
    }

    public void setLevel(int level) {
        if (isOpen() && checkLevel(level)) {
            ImageFile imageFile = getImageFile(this.imageFile.getTime(), level);
            if (imageFile != null) this.imageFile = imageFile;
        }
    }

    private boolean checkLevel(int level) {
        if (level >= 0 && level <= this.maxLevel) return true;
        logger.error("Invalid Level: " + level);
        return false;
    }

    public int getMissingImages() {
        return this.missingImagesCount;
    }

    public List<Integer> getPinTimes() {
        return new ArrayList<>(this.pinTimes);
    }

    public boolean isPinned(int time) {
        if (!isOpen() && !checkTime(time)) return false;
        return this.pinTimes.contains(time);
    }

    public void togglePinTime() {
        if (!isOpen()) return;

        int time = this.imageFile.getTime();
        if (isPinned(time)) this.pinTimes.remove(time);
        else this.pinTimes.add(time);
    }

    // ========================================
    // Zurücksetzen
    // ========================================
    public void clear(boolean removeImageFilesDirectory) {
        if (removeImageFilesDirectory) {
            this.imagesDirectory = null;
            this.config = Config.getDefault();
            this.markers = new Markers();
        }

        this.matrix = null;
        this.imageFile = null;
        this.maxLevel = 0;
        this.maxTime = 0;
        this.missingImagesCount = 0;
        this.pinTimes.clear();
    }

    // ========================================
    // Navigieren Methoden
    // ========================================
    public void toFirst(Axis axis) {
        if (!isOpen()) return;
        switch (axis) {
            case TIME -> this.imageFile = getImageFile(0, this.imageFile.getLevel());
            case LEVEL -> this.imageFile = getImageFile(this.imageFile.getTime(), 0);
        }
    }

    public void toLast(Axis axis) {
        if (!isOpen()) return;
        switch (axis) {
            case TIME -> this.imageFile = getImageFile(this.maxTime, this.imageFile.getLevel());
            case LEVEL -> this.imageFile = getImageFile(this.imageFile.getTime(), this.maxLevel);
        }
    }

    public void prev(Axis axis) {
        if (!isOpen()) return;
        switch (axis) {
            case TIME -> {
                int prevTime = Math.max(0, this.imageFile.getTime() - 1);
                this.imageFile = getImageFile(prevTime, this.imageFile.getLevel());
            }
            case LEVEL -> {
                int prevLevel = Math.max(0, this.imageFile.getLevel() - 1);
                this.imageFile = getImageFile(this.imageFile.getTime(), prevLevel);
            }
        }
    }

    public void next(Axis axis) {
        if (!isOpen()) return;
        switch (axis) {
            case TIME -> {
                int nextTime = Math.min(this.maxTime, this.imageFile.getTime() + 1);
                this.imageFile = getImageFile(nextTime, this.imageFile.getLevel());
            }
            case LEVEL -> {
                int nextLevel = Math.min(this.maxLevel, this.imageFile.getLevel() + 1);
                this.imageFile = getImageFile(this.imageFile.getTime(), nextLevel);
            }
        }
    }

    // ========================================
    // Lade Bilder aus dem Verzeichnis
    // ========================================
    public LoadingResult openImagesDirectory(Path imagesDirectory, ImageFileType imageFileType, LoadingImageListener progress) {
        if (imagesDirectory != null && Files.isDirectory(imagesDirectory)) {
            // Prüfe, ob das Verzeichnis bereits in der UI geladen ist
            boolean sameDirectory = Objects.equals(this.imagesDirectory, imagesDirectory);
            boolean sameFileType = this.config.getImageFileType() == imageFileType;
            if (sameDirectory && sameFileType) return LoadingResult.ALREADY_LOADED;

            // Verzeichnis, Config & Markers speichern
            Path oldImagesDirectory = this.imagesDirectory;
            Config oldConfig = this.config;
            Markers oldMarkers = this.markers;
            this.save();

            // Verzeichnis, Config & Markers laden
            this.imagesDirectory = imagesDirectory;
            this.load(imageFileType);

            // Lade das Verzeichnis, wenn es Bilder hat
            LoadingResult badResult;
            try {
                if (this.loadImages(progress)) {
                    history.add(imagesDirectory);
                    return LoadingResult.LOADED;
                }
                badResult = LoadingResult.DIRECTORY_HAS_NO_IMAGES;
            } catch (InterruptedException e) {
                badResult = LoadingResult.INTERRUPTED;
            }

            // Variablen zurücksetzen
            this.imagesDirectory = oldImagesDirectory;
            this.config = oldConfig;
            this.markers = oldMarkers;
            return badResult;
        }
        return LoadingResult.DIRECTORY_NOT_EXISTING;
    }

    private boolean loadImages(LoadingImageListener progress) throws InterruptedException {
        progress.onLoadingStart();

        // Pfade laden
        List<Path> paths;
        try (DirectoryStream<Path> directory = Files.newDirectoryStream(this.imagesDirectory)) {
            paths = StreamSupport.stream(directory.spliterator(), true).toList();
        } catch (IOException e) {
            progress.onLoadingComplete(0);
            return false;
        }

        progress.onScanningStart(paths.size(), 0, 0);
        Set<ImageFile> imageFiles = new TreeSet<>(ImageFile::compareTo);
        int tempMaxTime = 0;
        int tempMaxLevel = 0;

        // Dateinamen Muster erstellen
        String fileNamePattern = getImageFileNamePattern();

        // Durchlaufe alle Pfade
        for (int number = 1; number <= paths.size(); number++) {
            if (Thread.currentThread().isInterrupted()) throw new InterruptedException();

            Path path = paths.get(number - 1);
            String fileName = path.getFileName().toString();

            // Prüft, ob der Pfad eine reguläre Datei ist und der Name dem Muster entspricht
            if (Files.isRegularFile(path) && fileName.matches(fileNamePattern)) {
                int time = NumberUtils.parseInteger(getTimeStr(fileName));
                int level = NumberUtils.parseInteger(getLevelStr(fileName));

                // Grenzwert bestimmen
                if (0 > time || time >= 10_000) {
                    logger.warning("The image '%s' has an invalid time.".formatted(fileName));
                    continue;
                }
                if (0 > level || level >= 10_000) {
                    logger.warning("The image '%s' has an invalid level.".formatted(fileName));
                    continue;
                }

                // Maximalwerte bestimmen (Matrixgröße)
                tempMaxTime = Math.max(tempMaxTime, time);
                tempMaxLevel = Math.max(tempMaxLevel, level);
                imageFiles.add(new ImageFile(path, time, level));
            }

            // Fortschritt aktualisieren
            progress.onScanningUpdate(paths.size(), number, path, imageFiles.size());
        }

        progress.onScanningComplete(paths.size(), paths.size(), imageFiles.size());

        // Wenn keine Bilder gefunden wurden → Abbruch
        if (imageFiles.isEmpty()) {
            progress.onLoadingComplete(0);
            return false;
        }

        // Matrix vorbereiten
        clear(false);
        this.maxTime = tempMaxTime;
        this.maxLevel = tempMaxLevel;

        // Erstelle die Matrix und setze imageFile auf (time=0, level=0)
        int imagesCount = createMatrix(imageFiles);
        this.imageFile = getImageFile(0, 0);

        progress.onLoadingComplete(imagesCount);
        return true;
    }

    // ========================================
    // Erstelle Matrix mit ImageFiles
    // ========================================
    private int createMatrix(Set<ImageFile> imageFiles) {
        int tSize = this.maxTime + 1;
        int lSize = this.maxLevel + 1;
        this.matrix = new ImageFile[tSize][lSize];

        StringBuilder report = new StringBuilder();
        int count = 0;
        int duplicated = 0;

        // Bilddateien in Matrix einfügen
        for (ImageFile imageFile : imageFiles) {
            int time = imageFile.getTime();
            int level = imageFile.getLevel();

            // Prüfe, ob das ImageFile auf der aktuellen Position bereits belegt ist
            if (getImageFile(time, level) == null) {
                setImageFile(time, level, imageFile);
                count++;
            } else {
                appendImageFileToReport(imageFile, report);
                duplicated++;
            }
        }

        logReport(duplicated, "duplicated", report);

        int expectedImagesCount = tSize * lSize;
        if (count < expectedImagesCount) {
            checkMissingImageFiles();
        }
        return count;
    }

    // ========================================
    // Report fehlender ImageFiles
    // ========================================
    public void checkMissingImageFiles() {
        if (!isOpen()) return;

        // Finde eine existierende ImageFile als Referenz
        ImageFile imageFileReference = findExistingImageFile();
        if (imageFileReference == null) return;

        StringBuilder report = new StringBuilder();
        int missing = 0;

        // Durchlaufe die ganze Matrix
        for (int time = 0; time <= this.maxTime; time++) {
            for (int level = 0; level <= this.maxLevel; level++) {
                ImageFile imageFile = getImageFile(time, level);
                if (imageFile == null || !imageFile.exists()) {
                    if (imageFile == null) {
                        imageFile = new ImageFile(getImageFilePath(time, level, imageFileReference), time, level);
                        setImageFile(time, level, imageFile);
                    }
                    appendImageFileToReport(imageFile, report);
                    missing++;
                }
            }
        }

        if (missing > this.missingImagesCount) {
            logReport(missing, "missing", report);
        } else if (missing < this.missingImagesCount) {
            // Es wurden einige Bilder wieder hergestellt
            int imageFiles = (this.maxTime + 1) * (this.maxLevel + 1) - missing;
            logger.info("Some missing images were restored." + StringUtils.NEXT_LINE + "Loaded images: " + imageFiles);
        }
        this.missingImagesCount = missing;
    }

    // ========================================
    // Report fehlender ImageFiles als String
    // ========================================
    public String getMissingImagesCount() {
        if (!isOpen()) return "";

        StringBuilder report = new StringBuilder();
        int missing = 0;

        // Durchlaufe die ganze Matrix
        for (int time = 0; time <= this.maxTime; time++) {
            List<Integer> missingLevels = new ArrayList<>();

            for (int level = 0; level <= this.maxLevel; level++) {
                ImageFile imageFile = getImageFile(time, level);
                if (!imageFile.exists()) {
                    missingLevels.add(level);
                    missing++;
                }
            }

            if (!missingLevels.isEmpty()) {
                report.append(StringUtils.wrapBold("--- Time: " + time + " ---")).append(StringUtils.NEXT_LINE);
                report.append("Missing Levels: ").append(missingLevels).append(StringUtils.NEXT_LINE);
                report.append("Expected Images:").append(StringUtils.NEXT_LINE);
                for (int level : missingLevels) appendImageFileToReport(getImageFile(time, level), report);
                report.append(StringUtils.NEXT_LINE);
            }
        }

        if (missing == 0) {
            report.append("No missing images.").append(StringUtils.NEXT_LINE);
            return report.toString();
        } else {
            String headerText = formatReportHeader(missing, "missing");
            String formattedText = StringUtils.applyColor(StringUtils.wrapBold(headerText), COLOR_RED);
            return formattedText + StringUtils.NEXT_LINE + report;
        }
    }

    // ========================================
    // Report Methoden
    // ========================================
    private void appendImageFileToReport(ImageFile imageFile, StringBuilder reportBuilder) {
        reportBuilder.append(formatImageFileRow(imageFile));
    }

    private String formatImageFileRow(ImageFile imageFile) {
        String fileName = imageFile.getFileName();
        int time = imageFile.getTime();
        int level = imageFile.getLevel();

        String text = String.format("- Filename: '%s' (time=%s, level=%s)", fileName, time, level);
        return text + StringUtils.NEXT_LINE;
    }

    private String formatReportHeader(int count, String type) {
        String base = count + " " + (count == 1 ? "image is" : "images are");
        return base + " " + type + ":" + StringUtils.NEXT_LINE;
    }

    private void logReport(int count, String type, StringBuilder report) {
        if (count > 0) {
            logger.warning(formatReportHeader(count, type) + report);
        }
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    public String getImageFileNamePattern() {
        return "(?i)" +                               // Case-insensitive Matching
            this.config.getTimeSep() + "\\d+" +       // Zeitkomponente (mind. 1 Ziffer)
            this.config.getLevelSep() + "\\d+" +      // Levelkomponente (mind. 1 Ziffer)
            "\\." + StringUtils.formatArray(this.config.getImageFileType().getExtensions(), "|", '(', ')') + "$";
    }

    private Path getImageFilePath(int time, int level, ImageFile imageFileReference) {
        String fileName = imageFileReference.getFileName();
        Path parentDirectory = imageFileReference.getFilePath().getParent();

        int timeStrLength = getTimeStr(fileName).length();
        int levelStrLength = getLevelStr(fileName).length();
        String extension = getExtension(fileName);

        // Dynamische Bestandteile erzeugen
        String timeStr = (this.config.getTimeSep() + "%0" + timeStrLength + "d").formatted(time);
        String levelStr = (this.config.getLevelSep() + "%0" + levelStrLength + "d").formatted(level);
        return parentDirectory.resolve(timeStr + levelStr + "." + extension);
    }

    private ImageFile findExistingImageFile() {
        for (int time = 0; time <= this.maxTime; time++) {
            for (int level = 0; level <= this.maxLevel; level++) {
                ImageFile imageFile = getImageFile(time, level);
                if (imageFile != null && imageFile.exists()) return imageFile;
            }
        }
        return null;
    }

    private String getTimeStr(String fileName) {
        int startIndex = fileName.indexOf(this.config.getTimeSep()) + this.config.getTimeSep().length();
        int endIndex = fileName.lastIndexOf(this.config.getLevelSep());
        return fileName.substring(startIndex, endIndex);
    }

    private String getLevelStr(String fileName) {
        int startIndex = fileName.indexOf(this.config.getLevelSep()) + this.config.getLevelSep().length();
        int dotIndex = fileName.lastIndexOf('.');
        return fileName.substring(startIndex, dotIndex);
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return fileName.substring(dotIndex + 1);
    }
}
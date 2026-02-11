package de.uzk.image;

import de.uzk.config.Config;
import de.uzk.edit.EditManager;
import de.uzk.markers.Markers;
import de.uzk.utils.NumberUtils;
import de.uzk.utils.StringUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.StreamSupport;

import static de.uzk.Main.*;

// Der Workspace entspricht einem Projekt
public class Workspace {
    private final MissingImagesReport missingImagesReport;
    private final List<Integer> pinTimes;
    private final EditManager editManager = new EditManager();
    // Konfigurationen und Markierungen
    private Path imagesDirectory;
    private Config config;
    private Markers markers;
    // ImageFiles
    private ImageFile[][] matrix;
    private ImageFile currentImageFile;
    // Time, Level
    private int time;
    private int level;
    private int maxTime;
    private int maxLevel;

    public Workspace() {
        missingImagesReport = new MissingImagesReport();
        pinTimes = new ArrayList<>();
        reset();
    }

    public Path getImagesDirectory() {
        return imagesDirectory;
    }

    public Config getConfig() {
        return config;
    }

    public EditManager getEditManager() {
        return editManager;
    }

    public Markers getMarkers() {
        return markers;
    }

    private void loadConfigs(ImageFileType imageFileType) {
        config = Config.load();
        markers = Markers.load();
        if (imageFileType != null) {
            config.setImageFileType(imageFileType);
        }
    }

    public void saveConfigs() {
        if (isLoaded()) {
            config.save();
            markers.save();
        }
    }

    public boolean isLoaded() {
        return matrix != null;
    }

    ImageFile getImageFile(int time, int level) {
        return matrix[time][level];
    }

    void setImageFile(int time, int level, ImageFile imageFile) {
        matrix[time][level] = imageFile;
    }

    public ImageFile getCurrentImageFile() {
        return currentImageFile;
    }

    private void setCurrentImageFile(int time, int level) {
        currentImageFile = getImageFile(time, level);
        this.time = currentImageFile.getTime();
        this.level = currentImageFile.getLevel();
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        if (!isLoaded() || isTimeInvalid(time)) return;
        setCurrentImageFile(time, currentImageFile.getLevel());
    }

    private boolean isTimeInvalid(int time) {
        if (NumberUtils.valueInRange(time, 0, maxTime)) return false;
        logger.error("Invalid Time: " + time);
        return true;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (!isLoaded() || isLevelInvalid(level)) return;
        setCurrentImageFile(currentImageFile.getTime(), level);
    }

    private boolean isLevelInvalid(int level) {
        if (NumberUtils.valueInRange(level, 0, maxLevel)) return false;
        logger.error("Invalid Level: " + level);
        return true;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public List<Integer> getPinTimes() {
        return new ArrayList<>(pinTimes);
    }

    public boolean isPinned(Integer time) {
        if (!isLoaded() && isTimeInvalid(time)) return false;
        return pinTimes.contains(time);
    }

    public void togglePinTime() {
        if (!isLoaded()) return;

        Integer time = currentImageFile.getTime();
        if (isPinned(time)) pinTimes.remove(time);
        else pinTimes.add(time);
    }

    // ========================================
    // Navigieren Methoden
    // ========================================
    public void toFirst(Axis axis) {
        if (!isLoaded()) return;
        switch (axis) {
            case TIME -> setCurrentImageFile(0, currentImageFile.getLevel());
            case LEVEL -> setCurrentImageFile(currentImageFile.getTime(), 0);
        }
    }

    public void toLast(Axis axis) {
        if (!isLoaded()) return;
        switch (axis) {
            case TIME -> setCurrentImageFile(maxTime, currentImageFile.getLevel());
            case LEVEL -> setCurrentImageFile(currentImageFile.getTime(), maxLevel);
        }
    }

    public void prev(Axis axis) {
        if (!isLoaded()) return;
        switch (axis) {
            case TIME -> {
                int prevTime = Math.max(0, currentImageFile.getTime() - 1);
                setCurrentImageFile(prevTime, currentImageFile.getLevel());
            }
            case LEVEL -> {
                int prevLevel = Math.max(0, currentImageFile.getLevel() - 1);
                setCurrentImageFile(currentImageFile.getTime(), prevLevel);
            }
        }
    }

    public void next(Axis axis) {
        if (!isLoaded()) return;
        switch (axis) {
            case TIME -> {
                int nextTime = Math.min(maxTime, currentImageFile.getTime() + 1);
                setCurrentImageFile(nextTime, currentImageFile.getLevel());
            }
            case LEVEL -> {
                int nextLevel = Math.min(maxLevel, currentImageFile.getLevel() + 1);
                setCurrentImageFile(currentImageFile.getTime(), nextLevel);
            }
        }
    }

    // ========================================
    // Zurücksetzen
    // ========================================
    public void reset() {
        imagesDirectory = null;
        config = Config.getDefault();
        markers = new Markers();
        clearTemp();
    }

    private void clearTemp() {
        // ImageFiles
        matrix = null;
        currentImageFile = null;

        // Zeit, Level
        time = 0;
        level = 0;
        maxLevel = 0;
        maxTime = 0;

        // Listen leeren
        missingImagesReport.clear();
        pinTimes.clear();
    }

    // ========================================
    // Lade Bilder aus dem Verzeichnis
    // ========================================
    public LoadingResult loadImagesDirectory(Path imagesDirectory, ImageFileType imageFileType, LoadingImageListener progress) {
        if (imagesDirectory != null && Files.isDirectory(imagesDirectory)) {
            // Prüfe, ob das Verzeichnis bereits in der UI geladen ist
            boolean sameDirectory = Objects.equals(this.imagesDirectory, imagesDirectory);
            boolean sameFileType = config.getImageFileType() == imageFileType;
            if (sameDirectory && sameFileType) return LoadingResult.DIRECTORY_ALREADY_LOADED;

            // Verzeichnis, Config & Markers speichern
            Path oldImagesDirectory = this.imagesDirectory;
            Config oldConfig = config;
            Markers oldMarkers = markers;
            saveConfigs();

            // Verzeichnis, Config & Markers laden
            this.imagesDirectory = imagesDirectory;
            loadConfigs(imageFileType);

            // Lade das Verzeichnis, wenn es Bilder hat
            LoadingResult badResult;
            try {
                if (loadImages(progress)) {
                    history.add(imagesDirectory);
                    return LoadingResult.LOADING_SUCCESSFUL;
                }
                badResult = LoadingResult.DIRECTORY_HAS_NO_IMAGES;
            } catch (InterruptedException e) {
                badResult = LoadingResult.LOADING_INTERRUPTED;
            }

            // Variablen zurücksetzen
            this.imagesDirectory = oldImagesDirectory;
            config = oldConfig;
            markers = oldMarkers;
            return badResult;
        }
        return LoadingResult.DIRECTORY_DOES_NOT_EXIST;
    }

    private boolean loadImages(LoadingImageListener progress) throws InterruptedException {
        progress.onLoadingStart();

        // Pfade laden
        List<Path> paths;
        try (DirectoryStream<Path> directory = Files.newDirectoryStream(imagesDirectory, Files::isRegularFile)) {
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
        final String fileNamePattern = getImageFileNamePattern();
        final int MAX_TIME = 9_999;
        final int MAX_LEVEL = 999;

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
                boolean validTime = NumberUtils.valueInRange(time, 0, MAX_TIME);
                boolean validLevel = NumberUtils.valueInRange(level, 0, MAX_LEVEL);
                if (!validTime || !validLevel) {
                    List<String> invalidParts = new ArrayList<>();
                    if (!validTime)
                        invalidParts.add("Invalid Time=%d => Valid Range=[%d, %d]".formatted(time, 0, MAX_TIME));
                    if (!validLevel)
                        invalidParts.add("Invalid Level=%d => Valid Range=[%d, %d]".formatted(level, 0, MAX_LEVEL));

                    logger.warn("Could not load the image-file '%s'. (Cause: %s)".formatted(fileName, String.join(" | ", invalidParts)));
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
        clearTemp();
        maxTime = tempMaxTime;
        maxLevel = tempMaxLevel;

        // Erstelle die Matrix und setze imageFile auf (time=0, level=0)
        int imagesCount = createMatrix(imageFiles);
        setCurrentImageFile(0, 0);

        progress.onLoadingComplete(imagesCount);
        return true;
    }

    // ========================================
    // Erstelle Matrix
    // ========================================
    private int createMatrix(Set<ImageFile> imageFiles) {
        int tSize = maxTime + 1;
        int lSize = maxLevel + 1;
        matrix = new ImageFile[tSize][lSize];

        // Neuen Report aufbauen
        StringBuilder reportBuilder = new StringBuilder();
        int duplicatedCount = 0;
        int imagesCount = 0;

        // ImageFiles in Matrix einfügen
        for (ImageFile imageFile : imageFiles) {
            int time = imageFile.getTime();
            int level = imageFile.getLevel();

            // Prüfe, ob das ImageFile auf der aktuellen Position bereits belegt ist
            if (getImageFile(time, level) == null) {
                setImageFile(time, level, imageFile);
                imagesCount++;
            } else {
                reportBuilder.append(MissingImagesReport.createImageFileRow(imageFile));
                duplicatedCount++;
            }
        }

        // Report ausgeben
        if (duplicatedCount > 0) {
            String headerText = MissingImagesReport.createHeaderText(duplicatedCount, "duplicated");
            String reportOutput = MissingImagesReport.createReport(headerText, reportBuilder);
            logger.warn(reportOutput);
        }

        int expectedImagesCount = tSize * lSize;
        if (imagesCount < expectedImagesCount) {
            ImageFile referenceImageFile = imageFiles.stream().findFirst().orElse(null);
            addDummyImageFiles(referenceImageFile);
            missingImagesReport.logReport(true);
        }
        return imagesCount;
    }

    private void addDummyImageFiles(ImageFile referenceImageFile) {
        if (referenceImageFile == null) return;

        // Durchlaufe die ganze Matrix
        for (int time = 0; time <= workspace.maxTime; time++) {
            for (int level = 0; level <= workspace.maxLevel; level++) {
                ImageFile imageFile = getImageFile(time, level);

                // Füge Dummy ImageFile hinzu, falls die Matrix an dieser Position null zurückgibt
                if (imageFile == null) {
                    Path dummyImagePath = workspace.getDummyImageFilePath(time, level, referenceImageFile);
                    ImageFile dummyImageFile = new ImageFile(dummyImagePath, time, level);
                    workspace.setImageFile(time, level, dummyImageFile);
                }
            }
        }
    }

    // ========================================
    // Reporting
    // ========================================

    public void logMissingImages() {
        missingImagesReport.logReport(false);
    }

    public String getMissingImagesReport() {
        return missingImagesReport.getHtmlReport();
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    public String getImageFileNamePattern() {
        return "(?i)" +                               // Case-insensitive Matching
            config.getTimeSep() + "\\d+" +       // Zeitkomponente (mind. 1 Ziffer)
            config.getLevelSep() + "\\d+" +      // Levelkomponente (mind. 1 Ziffer)
            "\\." + StringUtils.formatArray(config.getImageFileType().getExtensions(), "|", '(', ')') + "$";
    }

    private Path getDummyImageFilePath(int time, int level, ImageFile referenceImageFile) {
        String fileName = referenceImageFile.getFileName();
        Path parentDirectory = referenceImageFile.getFilePath().getParent();

        int timeStrLength = getTimeStr(fileName).length();
        int levelStrLength = getLevelStr(fileName).length();
        String extension = getExtension(fileName);

        // Dynamische Bestandteile erzeugen
        String timeStr = (config.getTimeSep() + "%0" + timeStrLength + "d").formatted(time);
        String levelStr = (config.getLevelSep() + "%0" + levelStrLength + "d").formatted(level);
        return parentDirectory.resolve(timeStr + levelStr + "." + extension);
    }

    private String getTimeStr(String fileName) {
        int startIndex = fileName.indexOf(config.getTimeSep()) + config.getTimeSep().length();
        int endIndex = fileName.lastIndexOf(config.getLevelSep());
        return fileName.substring(startIndex, endIndex);
    }

    private String getLevelStr(String fileName) {
        int startIndex = fileName.indexOf(config.getLevelSep()) + config.getLevelSep().length();
        int dotIndex = fileName.lastIndexOf('.');
        return fileName.substring(startIndex, dotIndex);
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return fileName.substring(dotIndex + 1);
    }
}
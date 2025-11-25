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

import static de.uzk.Main.*;

// Der Workspace entspricht einem Projekt
public class Workspace {
    // Konfigurationen und Markierungen
    private Path imagesDirectory;
    private Config config;
    private Markers markers;

    // ImageFiles
    private ImageFile[][] matrix;
    private ImageFile currentImageFile;
    private final MissingImagesReport missingImagesReport;

    // Time, Level
    private int time;
    private int level;
    private int maxTime;
    private int maxLevel;
    private final List<Integer> pinTimes;

    public Workspace() {
        this.missingImagesReport = new MissingImagesReport();
        this.pinTimes = new ArrayList<>();
        reset();
    }

    public Path getImagesDirectory() {
        return this.imagesDirectory;
    }

    public Config getConfig() {
        return this.config;
    }

    public Markers getMarkers() {
        return this.markers;
    }

    private void loadConfigs(ImageFileType imageFileType) {
        this.config = Config.load();
        this.markers = Markers.load();
        if (imageFileType != null) {
            this.config.setImageFileType(imageFileType);
        }
    }

    public void saveConfigs() {
        if (isLoaded()) {
            this.config.save();
            this.markers.save();
        }
    }

    public boolean isLoaded() {
        return this.matrix != null;
    }

    ImageFile getImageFile(int time, int level) {
        return this.matrix[time][level];
    }

    void setImageFile(int time, int level, ImageFile imageFile) {
        this.matrix[time][level] = imageFile;
    }

    public ImageFile getCurrentImageFile() {
        return this.currentImageFile;
    }

    private void setCurrentImageFile(int time, int level) {
        this.currentImageFile = getImageFile(time, level);
        this.time = currentImageFile.getTime();
        this.level = currentImageFile.getLevel();
    }

    public int getTime() {
        return this.time;
    }

    public void setTime(int time) {
        if (!isLoaded() || isTimeInvalid(time)) return;
        setCurrentImageFile(time, this.currentImageFile.getLevel());
    }

    private boolean isTimeInvalid(int time) {
        if (NumberUtils.valueInRange(time, 0, this.maxTime)) return false;
        logger.error("Invalid Time: " + time);
        return true;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        if (!isLoaded() || isLevelInvalid(level)) return;
        setCurrentImageFile(this.currentImageFile.getTime(), level);
    }

    private boolean isLevelInvalid(int level) {
        if (NumberUtils.valueInRange(level, 0, this.maxLevel)) return false;
        logger.error("Invalid Level: " + level);
        return true;
    }

    public int getMaxTime() {
        return this.maxTime;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public List<Integer> getPinTimes() {
        return new ArrayList<>(this.pinTimes);
    }

    public boolean isPinned(Integer time) {
        if (!isLoaded() && isTimeInvalid(time)) return false;
        return this.pinTimes.contains(time);
    }

    public void togglePinTime() {
        if (!isLoaded()) return;

        Integer time = this.currentImageFile.getTime();
        if (isPinned(time)) this.pinTimes.remove(time);
        else this.pinTimes.add(time);
    }

    // ========================================
    // Navigieren Methoden
    // ========================================
    public void toFirst(Axis axis) {
        if (!isLoaded()) return;
        switch (axis) {
            case TIME -> setCurrentImageFile(0, this.currentImageFile.getLevel());
            case LEVEL -> setCurrentImageFile(this.currentImageFile.getTime(), 0);
        }
    }

    public void toLast(Axis axis) {
        if (!isLoaded()) return;
        switch (axis) {
            case TIME -> setCurrentImageFile(this.maxTime, this.currentImageFile.getLevel());
            case LEVEL -> setCurrentImageFile(this.currentImageFile.getTime(), this.maxLevel);
        }
    }

    public void prev(Axis axis) {
        if (!isLoaded()) return;
        switch (axis) {
            case TIME -> {
                int prevTime = Math.max(0, this.currentImageFile.getTime() - 1);
                setCurrentImageFile(prevTime, this.currentImageFile.getLevel());
            }
            case LEVEL -> {
                int prevLevel = Math.max(0, this.currentImageFile.getLevel() - 1);
                setCurrentImageFile(this.currentImageFile.getTime(), prevLevel);
            }
        }
    }

    public void next(Axis axis) {
        if (!isLoaded()) return;
        switch (axis) {
            case TIME -> {
                int nextTime = Math.min(this.maxTime, this.currentImageFile.getTime() + 1);
                setCurrentImageFile(nextTime, this.currentImageFile.getLevel());
            }
            case LEVEL -> {
                int nextLevel = Math.min(this.maxLevel, this.currentImageFile.getLevel() + 1);
                setCurrentImageFile(this.currentImageFile.getTime(), nextLevel);
            }
        }
    }

    // ========================================
    // Zurücksetzen
    // ========================================
    public void reset() {
        this.imagesDirectory = null;
        this.config = Config.getDefault();
        this.markers = new Markers();
        clearTemp();
    }

    private void clearTemp() {
        // ImageFiles
        this.matrix = null;
        this.currentImageFile = null;

        // Zeit, Level
        this.time = 0;
        this.level = 0;
        this.maxLevel = 0;
        this.maxTime = 0;

        // Listen leeren
        this.missingImagesReport.clear();
        this.pinTimes.clear();
    }

    // ========================================
    // Lade Bilder aus dem Verzeichnis
    // ========================================
    public LoadingResult loadImagesDirectory(Path imagesDirectory, ImageFileType imageFileType, LoadingImageListener progress) {
        if (imagesDirectory != null && Files.isDirectory(imagesDirectory)) {
            // Prüfe, ob das Verzeichnis bereits in der UI geladen ist
            boolean sameDirectory = Objects.equals(this.imagesDirectory, imagesDirectory);
            boolean sameFileType = this.config.getImageFileType() == imageFileType;
            if (sameDirectory && sameFileType) return LoadingResult.DIRECTORY_ALREADY_LOADED;

            // Verzeichnis, Config & Markers speichern
            Path oldImagesDirectory = this.imagesDirectory;
            Config oldConfig = this.config;
            Markers oldMarkers = this.markers;
            this.saveConfigs();

            // Verzeichnis, Config & Markers laden
            this.imagesDirectory = imagesDirectory;
            this.loadConfigs(imageFileType);

            // Lade das Verzeichnis, wenn es Bilder hat
            LoadingResult badResult;
            try {
                if (this.loadImages(progress)) {
                    history.add(imagesDirectory);
                    return LoadingResult.LOADING_SUCCESSFUL;
                }
                badResult = LoadingResult.DIRECTORY_HAS_NO_IMAGES;
            } catch (InterruptedException e) {
                badResult = LoadingResult.LOADING_INTERRUPTED;
            }

            // Variablen zurücksetzen
            this.imagesDirectory = oldImagesDirectory;
            this.config = oldConfig;
            this.markers = oldMarkers;
            return badResult;
        }
        return LoadingResult.DIRECTORY_DOES_NOT_EXIST;
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
        this.maxTime = tempMaxTime;
        this.maxLevel = tempMaxLevel;

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
        int tSize = this.maxTime + 1;
        int lSize = this.maxLevel + 1;
        this.matrix = new ImageFile[tSize][lSize];

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
            this.missingImagesReport.logReport(true);
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
        this.missingImagesReport.logReport(false);
    }

    public String getMissingImagesReport() {
        return this.missingImagesReport.getHtmlReport();
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

    private Path getDummyImageFilePath(int time, int level, ImageFile referenceImageFile) {
        String fileName = referenceImageFile.getFileName();
        Path parentDirectory = referenceImageFile.getFilePath().getParent();

        int timeStrLength = getTimeStr(fileName).length();
        int levelStrLength = getLevelStr(fileName).length();
        String extension = getExtension(fileName);

        // Dynamische Bestandteile erzeugen
        String timeStr = (this.config.getTimeSep() + "%0" + timeStrLength + "d").formatted(time);
        String levelStr = (this.config.getLevelSep() + "%0" + levelStrLength + "d").formatted(level);
        return parentDirectory.resolve(timeStr + levelStr + "." + extension);
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
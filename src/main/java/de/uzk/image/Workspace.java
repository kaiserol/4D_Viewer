package de.uzk.image;

import de.uzk.config.Config;
import de.uzk.markers.Markers;
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
    private Path imageFilesDirectory;
    private Config config;
    private Markers markers;

    private ImageFile[][] matrix;
    private ImageFile imageFile;
    private int maxTime;
    private int maxLevel;
    private int missingImagesCount;

    // Pin-Zeiten
    private final List<Integer> pinTimes;

    public Workspace() {
        this.pinTimes = new ArrayList<>();
        clear(true);
    }

    public Path getImageFilesDirectory() {
        return this.imageFilesDirectory;
    }

    public LoadingResult openDirectory(Path directory, ImageFileType imageFileType, LoadingImageListener progress) {
        if (directory != null && Files.isDirectory(directory)) {
            // Prüfe, ob das Verzeichnis bereits in der UI geladen ist
            boolean sameDirectory = Objects.equals(this.imageFilesDirectory, directory);
            boolean sameType = this.config.getImageFileType() == imageFileType;
            if (sameDirectory && sameType) return LoadingResult.ALREADY_LOADED;

            // Verzeichnis, Config & Markers speichern
            Path oldImageFilesDirectory = this.imageFilesDirectory;
            Config oldConfig = this.config;
            Markers oldMarkers = this.markers;
            this.save();

            // Verzeichnis, Config & Markers laden
            this.imageFilesDirectory = directory;
            this.load(imageFileType);

            // Lade das Verzeichnis, wenn es Image-Files hat
            LoadingResult badResult;
            try {
                if (this.loadImageFiles(progress)) {
                    history.add(directory);
                    return LoadingResult.LOADED;
                }
                badResult = LoadingResult.DIRECTORY_HAS_NO_IMAGES;
            } catch (InterruptedException e) {
                badResult = LoadingResult.INTERRUPTED;
            }

            // Variablen zurücksetzen
            this.imageFilesDirectory = oldImageFilesDirectory;
            this.config = oldConfig;
            this.markers = oldMarkers;
            return badResult;
        }
        return LoadingResult.DIRECTORY_NOT_EXISTING;
    }

    public Markers getMarkers() {
        return this.markers;
    }

    public Config getConfig() {
        return this.config;
    }

    private void load(ImageFileType imageFileType) {
        Path directoryName = this.imageFilesDirectory.getFileName();
        this.config = Config.load(directoryName);
        this.config.setImageFileType(imageFileType);
        this.markers = Markers.load(directoryName);
    }

    public void save() {
        if (isOpen()) {
            Path directoryName = this.imageFilesDirectory.getFileName();
            this.config.save(directoryName);
            this.markers.save(directoryName);
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
            ImageFile loadedImageFile = getImageFile(time, this.imageFile.getLevel());
            if (loadedImageFile != null) this.imageFile = loadedImageFile;
        }
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public int getLevel() {
        return isOpen() ? this.imageFile.getLevel() : 0;
    }

    public void setLevel(int level) {
        if (isOpen() && checkLevel(level)) {
            ImageFile loadedImageFile = getImageFile(this.imageFile.getTime(), level);
            if (loadedImageFile != null) this.imageFile = loadedImageFile;
        }
    }

    private boolean checkTime(int time) {
        if (time >= 0 && time <= this.maxTime) return true;
        logger.error("Invalid Time: " + time);
        return false;
    }

    private boolean checkLevel(int level) {
        if (level >= 0 && level <= this.maxLevel) return true;
        logger.error("Invalid Level: " + level);
        return false;
    }

    public int getMissingImagesCount() {
        return this.missingImagesCount;
    }

    public List<Integer> getPinTimes() {
        return new ArrayList<>(this.pinTimes);
    }

    public boolean isPinned(int time) {
        return this.pinTimes.contains(time);
    }

    public void togglePinTime() {
        if (!isOpen()) return;

        int time = this.imageFile.getTime();
        if (isPinned(time)) this.pinTimes.remove(time);
        else this.pinTimes.add(time);
    }

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

    public void clear(boolean removeImageFilesDirectory) {
        if (removeImageFilesDirectory) {
            this.imageFilesDirectory = null;
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

    private boolean loadImageFiles(LoadingImageListener progress) throws InterruptedException {
        progress.onLoadingStart();

        // Dateien laden
        List<Path> paths;
        try (DirectoryStream<Path> directory = Files.newDirectoryStream(this.imageFilesDirectory)) {
            paths = StreamSupport.stream(directory.spliterator(), true).toList();
        } catch (IOException e) {
            progress.onLoadingComplete(0);
            return false;
        }

        progress.onScanningStart(paths.size(), 0, 0);
        Set<ImageFile> imageFiles = new TreeSet<>(ImageFile::compareTo);
        int tempMaxTime = 0;
        int tempMaxLevel = 0;

        // Dateien im Verzeichnis durchlaufen
        for (int number = 1; number <= paths.size(); number++) {
            if (Thread.currentThread().isInterrupted()) throw new InterruptedException();

            Path path = paths.get(number - 1);
            String fileNamePattern = getImageFileNamePattern();

            // Prüfe, ob reguläre Datei und Name auf Muster passt
            if (Files.isRegularFile(path) && path.getFileName().toString().matches(fileNamePattern)) {
                int time = Integer.parseInt(getTimeStr(path.getFileName().toString()));
                int level = Integer.parseInt(getLevelStr(path.getFileName().toString()));

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

    public String getImageFileNamePattern() {
        return "(?i)" +                            // Case-insensitive Matching
                this.config.getTimeSep() + "\\d{1,5}" +   // Zeitkomponente (1–5 Ziffern)
                this.config.getLevelSep() + "\\d{1,3}" +  // Levelkomponente (1–3 Ziffern)
                "\\." + StringUtils.formatArray(this.config.getImageFileType().getExtensions(), "|", '(', ')') + "$"; // Dateiende
    }

    private String getTimeStr(String fileName) {
        int indexStart = fileName.indexOf(this.config.getTimeSep()) + this.config.getTimeSep().length();
        int indexEnd = fileName.lastIndexOf(this.config.getLevelSep());
        return fileName.substring(indexStart, indexEnd);
    }

    private String getLevelStr(String fileName) {
        int indexStart = fileName.indexOf(this.config.getLevelSep()) + this.config.getLevelSep().length();
        int indexEnd = fileName.lastIndexOf(".");
        return fileName.substring(indexStart, indexEnd);
    }

    private String getExtension(String fileName) {
        int indexStart = fileName.lastIndexOf(".");
        return fileName.substring(indexStart + 1);
    }

    // Sucht in der Matrix eine beliebige existierende Datei,
    // um deren Namensschema als Vorlage zu verwenden.
    private ImageFile findExistingImageFileReference() {
        for (int time = 0; time <= this.maxTime; time++) {
            for (int level = 0; level <= this.maxLevel; level++) {
                ImageFile imageFile = getImageFile(time, level);
                if (imageFile != null && imageFile.exists()) return imageFile;
            }
        }
        throw new IllegalStateException("No existing image file found."); // Kann eigentlich nicht eintreten
    }

    private Path getMissingFile(int time, int level, ImageFile imageFileNameReference) {
        int timeStrLength = getTimeStr(imageFileNameReference.getName()).length();
        int levelStrLength = getLevelStr(imageFileNameReference.getName()).length();
        String extension = getExtension(imageFileNameReference.getName());

        // Dynamische Bestandteile erzeugen
        String timeStr = (this.config.getTimeSep() + "%0" + timeStrLength + "d").formatted(time);
        String levelStr = (this.config.getLevelSep() + "%0" + levelStrLength + "d").formatted(level);
        return imageFileNameReference.getPath().getParent().resolve(timeStr + levelStr + "." + extension);
    }

    private int createMatrix(Set<ImageFile> imageFiles) {
        int tSize = this.maxTime + 1;
        int lSize = this.maxLevel + 1;
        this.matrix = new ImageFile[tSize][lSize];

        StringBuilder duplicatedImagesReport = new StringBuilder();
        int imagesCount = 0;
        int duplicatedImagesCount = 0;

        // Bilddateien in Matrix einfügen
        for (ImageFile imageFile : imageFiles) {
            int time = imageFile.getTime();
            int level = imageFile.getLevel();

            // Prüfe, ob diese Position schon belegt ist (Duplikat)
            if (getImageFile(time, level) == null) {
                setImageFile(time, level, imageFile);
                imagesCount++;
            } else {
                duplicatedImagesReport.append("- Filename: '").append(imageFile.getName()).append("' (time=").append(time).append(", level=").append(level).append(")").append(StringUtils.NEXT_LINE);
                duplicatedImagesCount++;
            }
        }

        if (duplicatedImagesCount > 0) {
            String tempText = (duplicatedImagesCount > 1 ? "Images are" : "Image is");
            String headerText = duplicatedImagesCount + " " + tempText + " duplicated:" + StringUtils.NEXT_LINE;
            logger.warning(headerText + duplicatedImagesReport);
        }

        int expectedImagesCount = tSize * lSize;
        if (imagesCount < expectedImagesCount) {
            checkMissingFiles();
        }
        return imagesCount;
    }

    public void checkMissingFiles() {
        if (!isOpen()) return;

        StringBuilder missingImagesReport = new StringBuilder();
        int missingImagesCount = 0;

        // Versuche, ein existierendes Referenzbild zu finden
        ImageFile imageFileReference = findExistingImageFileReference();

        // Durchlaufe die ganze Matrix
        for (int time = 0; time <= this.maxTime; time++) {
            for (int level = 0; level <= this.maxLevel; level++) {
                ImageFile imageFile = getImageFile(time, level);
                if (imageFile == null || !imageFile.exists()) {
                    if (imageFile == null) {
                        imageFile = new ImageFile(getMissingFile(time, level, imageFileReference), time, level);
                        setImageFile(time, level, imageFile);
                    }
                    missingImagesReport.append("- Filename: '").append(imageFile.getName()).append("' (time=").append(time).append(", level=").append(level).append(")").append(StringUtils.NEXT_LINE);
                    missingImagesCount++;
                }
            }
        }

        if (missingImagesCount > this.missingImagesCount) {
            String tempText = (missingImagesCount > 1 ? "Images are" : "Image is");
            String headerText = missingImagesCount + " " + tempText + " missing:" + StringUtils.NEXT_LINE;
            logger.warning(headerText + missingImagesReport);
        } else if (missingImagesCount < this.missingImagesCount) {
            // Es wurden einige Bilder wieder hergestellt
            int imageFiles = (this.maxTime + 1) * (this.maxLevel + 1) - missingImagesCount;
            logger.info("Some Missing Images were restored." + StringUtils.NEXT_LINE + "Loaded Images: " + imageFiles);
        }
        this.missingImagesCount = missingImagesCount;
    }

    public String getMissingImages() {
        StringBuilder sb = new StringBuilder();
        if (!isOpen()) return sb.toString();

        int totalMissing = 0;
        for (int time = 0; time <= this.maxTime; time++) {
            List<Integer> missingLevels = new ArrayList<>();

            for (int level = 0; level <= this.maxLevel; level++) {
                ImageFile imageFile = getImageFile(time, level);
                if (imageFile == null || !imageFile.exists()) {
                    missingLevels.add(level);
                    totalMissing++;
                }
            }

            if (!missingLevels.isEmpty()) {
                String timeStr = StringUtils.wrapBold("--- Time: " + time + " ---");
                sb.append(timeStr).append(StringUtils.NEXT_LINE);
                sb.append("Missing levels: ").append(missingLevels).append(StringUtils.NEXT_LINE);

                sb.append("Expected images:").append(StringUtils.NEXT_LINE);
                for (int level : missingLevels) {
                    ImageFile imageFile = getImageFile(time, level);
                    String name = imageFile != null ? imageFile.getName() : "???";
                    sb.append(" - '").append(name).append("' (time=").append(time).append(", level=").append(level).append(")").append(StringUtils.NEXT_LINE);
                }
                sb.append(StringUtils.NEXT_LINE);
            }
        }

        if (totalMissing == 0) {
            sb.append("No missing images.").append(StringUtils.NEXT_LINE);
        } else {
            String tempText = (totalMissing > 1 ? "images are" : "image is");
            String headerText = StringUtils.applyColor(StringUtils.wrapBold(totalMissing + " " + tempText + " missing:"), COLOR_RED);
            sb.insert(0, headerText + StringUtils.NEXT_LINE + StringUtils.NEXT_LINE);
        }

        return sb.toString();
    }
}
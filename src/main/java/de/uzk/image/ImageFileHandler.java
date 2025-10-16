package de.uzk.image;

import de.uzk.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static de.uzk.Main.logger;
import static de.uzk.gui.GuiUtils.COLOR_RED;

public class ImageFileHandler {
    // Bild Eigenschaften
    private File imageFilesDirectory;
    private ImageFileNameExtension imageFileNameExtension;
    private String imageFileNameTimeSep;
    private String imageFileNameLevelSep;
    private boolean imageMirrorX;
    private boolean imageMirrorY;
    private int imageRotation;

    // Bild Bewegungen Eigenschaften
    private double shiftTimeUnit;
    private double shiftLevelUnit;

    // Weitere Eigenschaften
    private ImageFile[][] matrix;
    private ImageFile imageFile;
    private int missingImagesCount;
    private int maxTime;
    private int maxLevel;
    private int pinTime;

    public ImageFileHandler() {
    }

    public File getImageFilesDirectory() {
        return this.imageFilesDirectory;
    }

    public void setImageFilesDirectory(String directoryPath, LoadingImageListener progress) {
        if (directoryPath != null && !directoryPath.isBlank()) {
            File file = new File(directoryPath);
            if (file.isDirectory()) {
                File oldDirectory = this.imageFilesDirectory;
                this.imageFilesDirectory = file;

                // Setze das Verzeichnis zurück, wenn das übergebene Verzeichnis keine Image-Files hat
                if (!loadImageFiles(progress)) {
                    this.imageFilesDirectory = oldDirectory;
                }
            }
        }
    }

    public void removeImageFilesDirectory() {
        this.imageFilesDirectory = null;
    }

    public String getImageFilesDirectoryPath() {
        return hasImageFilesDirectory() ? this.imageFilesDirectory.getAbsolutePath() : "";
    }

    public boolean hasImageFilesDirectory() {
        return this.imageFilesDirectory != null;
    }

    public ImageFileNameExtension getImageFileNameExtension() {
        return this.imageFileNameExtension;
    }

    public void setImageFileNameExtension(String extension) {
        ImageFileNameExtension temp = ImageFileNameExtension.fromExtension(extension);
        this.imageFileNameExtension = temp != null ? temp : ImageFileNameExtension.getDefault();
    }

    public String getImageFileNameTimeSep() {
        return this.imageFileNameTimeSep;
    }

    public void setImageFileNameTimeSep(String imageFileNameTimeSep) {
        this.imageFileNameTimeSep = imageFileNameTimeSep;
    }

    public String getImageFileNameLevelSep() {
        return this.imageFileNameLevelSep;
    }

    public void setImageFileNameLevelSep(String imageFileNameLevelSep) {
        this.imageFileNameLevelSep = imageFileNameLevelSep;
    }

    public boolean isImageMirrorX() {
        return this.imageMirrorX;
    }

    public void setImageMirrorX(boolean imageMirrorX) {
        this.imageMirrorX = imageMirrorX;
    }

    public boolean isImageMirrorY() {
        return this.imageMirrorY;
    }

    public void setImageMirrorY(boolean imageMirrorY) {
        this.imageMirrorY = imageMirrorY;
    }

    public int getImageRotation() {
        return this.imageRotation;
    }

    public void setImageRotation(int imageRotation) {
        this.imageRotation = imageRotation;
    }

    public double getShiftTimeUnit() {
        return this.shiftTimeUnit;
    }

    public void setShiftTimeUnit(double shiftTimeUnit) {
        this.shiftTimeUnit = shiftTimeUnit;
    }

    public double getShiftLevelUnit() {
        return this.shiftLevelUnit;
    }

    public void setShiftLevelUnit(double shiftLevelUnit) {
        this.shiftLevelUnit = shiftLevelUnit;
    }

    private ImageFile getImageFile(int time, int level) {
        return this.matrix[time][level];
    }

    private void setImageFile(int time, int level, ImageFile imageFile) {
        this.matrix[time][level] = imageFile;
    }

    public ImageFile getImageFile() {
        return this.imageFile;
    }

    public boolean isEmpty() {
        return this.matrix == null || this.imageFile == null;
    }

    public int getMissingImagesCount() {
        return this.missingImagesCount;
    }

    public int getTime() {
        return isEmpty() ? -1 : this.imageFile.getTime();
    }

    public void setTime(int time) {
        if (!isEmpty() && checkTime(time)) {
            ImageFile loadedImageFile = getImageFile(time, this.imageFile.getLevel());
            if (loadedImageFile != null) this.imageFile = loadedImageFile;
        }
    }

    public int getLevel() {
        return isEmpty() ? -1 : this.imageFile.getLevel();
    }

    public void setLevel(int level) {
        if (!isEmpty() && checkLevel(level)) {
            ImageFile loadedImageFile = getImageFile(this.imageFile.getTime(), level);
            if (loadedImageFile != null) this.imageFile = loadedImageFile;
        }
    }

    private boolean checkTime(int time) {
        if (time >= 0 && time <= this.maxTime) return true;
        logger.error("Invalid time: " + time);
        return false;
    }

    private boolean checkLevel(int level) {
        if (level >= 0 && level <= this.maxLevel) return true;
        logger.error("Invalid level: " + level);
        return false;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public int getMaxTime() {
        return this.maxTime;
    }

    public int getPinTime() {
        return this.pinTime;
    }

    public boolean hasPinTime() {
        return this.pinTime != -1;
    }

    public void togglePinTime() {
        if (!isEmpty()) {
            if (this.pinTime == this.imageFile.getTime()) this.pinTime = -1;
            else this.pinTime = this.imageFile.getTime();
        }
    }

    public void toFirst(Axis axis) {
        if (!isEmpty() && axis != null) {
            this.imageFile = axis == Axis.TIME ?
                    getImageFile(0, this.imageFile.getLevel()) :
                    getImageFile(this.imageFile.getTime(), 0);
        }
    }

    public void toLast(Axis axis) {
        if (!isEmpty() && axis != null) {
            this.imageFile = axis == Axis.TIME ?
                    getImageFile(this.maxTime, this.imageFile.getLevel()) :
                    getImageFile(this.imageFile.getTime(), this.maxLevel);
        }
    }

    public void prev(Axis axis) {
        if (!isEmpty() && axis != null) {
            if (axis == Axis.TIME) {
                int prevTime = Math.max(0, this.imageFile.getTime() - 1);
                this.imageFile = getImageFile(prevTime, this.imageFile.getLevel());
            } else {
                int prevLevel = Math.max(0, this.imageFile.getLevel() - 1);
                this.imageFile = getImageFile(this.imageFile.getTime(), prevLevel);
            }
        }
    }

    public void next(Axis axis) {
        if (!isEmpty() && axis != null) {
            if (axis == Axis.TIME) {
                int nextTime = Math.min(this.maxTime, this.imageFile.getTime() + 1);
                this.imageFile = getImageFile(nextTime, this.imageFile.getLevel());
            } else {
                int nextLevel = Math.min(this.maxLevel, this.imageFile.getLevel() + 1);
                this.imageFile = getImageFile(this.imageFile.getTime(), nextLevel);
            }
        }
    }

    public void clear() {
        this.matrix = null;
        this.imageFile = null;
        this.missingImagesCount = 0;
        this.maxLevel = 0;
        this.maxTime = 0;
        this.pinTime = -1;
    }

    private boolean loadImageFiles(LoadingImageListener progress) {
        progress.onLoadingStart();
        File[] files = imageFilesDirectory == null ? new File[0] : imageFilesDirectory.listFiles();

        int count = files == null ? 0 : files.length;
        progress.onScanningStart(count);

        int tempMaxTime = 0;
        int tempMaxLevel = 0;
        Set<ImageFile> imageFiles = new TreeSet<>(ImageFile::compareTo);

        // Dateien im Verzeichnis durchlaufen
        for (int scan = 0; scan < count; scan++) {
            if (Thread.currentThread().isInterrupted()) {
                logger.info("Loading process was interrupted.");
                return false;
            }

            File file = files[scan];
            String fileNamePattern = getFileNamePattern();

            // Prüfen, ob reguläre Datei und Name auf Muster passt
            if (file.isFile() && file.getName().matches(fileNamePattern)) {
                int time = Integer.parseInt(getTimeStr(file.getName()));
                int level = Integer.parseInt(getLevelStr(file.getName()));

                // Maximalwerte bestimmen (Matrixgröße)
                tempMaxTime = Math.max(tempMaxTime, time);
                tempMaxLevel = Math.max(tempMaxLevel, level);

                imageFiles.add(new ImageFile(file, time, level));
            }

            // Fortschritt aktualisieren
            progress.onScanningUpdate(file, scan + 1, imageFiles.size(), files.length);
        }

        progress.onScanningComplete();

        // Wenn keine Bilder gefunden wurden → Abbruch
        if (imageFiles.isEmpty()) {
            progress.onLoadingComplete(0);
            return false;
        }

        // Matrix vorbereiten
        clear();
        this.maxTime = tempMaxTime;
        this.maxLevel = tempMaxLevel;

        // Erstelle die Matrix und setze imageFile auf (time=0, level=0)
        int imagesCount = createMatrix(imageFiles);
        this.imageFile = getImageFile(0, 0);

        progress.onLoadingComplete(imagesCount);
        return true;
    }

    public String getFileNamePattern() {
        return "(?i)" +                            // Case-insensitive Matching
                this.imageFileNameTimeSep + "\\d{1,5}" +   // Zeitkomponente (1–5 Ziffern)
                this.imageFileNameLevelSep + "\\d{1,3}" +  // Levelkomponente (1–3 Ziffern)
                "\\." + StringUtils.formatArray(this.imageFileNameExtension.getExtensions(), "|", '(', ')') +
                "$"; // Dateiende
    }

    private String getTimeStr(String fileName) {
        int indexStart = fileName.indexOf(this.imageFileNameTimeSep) + this.imageFileNameTimeSep.length();
        int indexEnd = fileName.lastIndexOf(this.imageFileNameLevelSep);
        return fileName.substring(indexStart, indexEnd);
    }

    private String getLevelStr(String fileName) {
        int indexStart = fileName.indexOf(this.imageFileNameLevelSep) + this.imageFileNameLevelSep.length();
        int indexEnd = fileName.lastIndexOf(".");
        return fileName.substring(indexStart, indexEnd);
    }

    private String getExtension(String fileName) {
        int indexStart = fileName.lastIndexOf(".");
        return fileName.substring(indexStart + 1);
    }

    private int createMatrix(Set<ImageFile> imageFiles) {
        int tSize = this.maxTime + 1;
        int lSize = this.maxLevel + 1;
        this.matrix = new ImageFile[tSize][lSize];

        // StringBuilder für kompakte Gesamtausgabe
        StringBuilder duplicatedImagesReport = new StringBuilder();
        int imagesCount = 0;
        int duplicatedImagesCount = 0;

        // Bilddateien in Matrix einfügen
        for (ImageFile imageFile : imageFiles) {
            int time = imageFile.getTime();
            int level = imageFile.getLevel();

            // Prüfen, ob diese Position schon belegt ist (Duplikat)
            if (getImageFile(time, level) == null) {
                setImageFile(time, level, imageFile);
                imagesCount++;
            } else {
                duplicatedImagesReport.append("- Filename: '").append(imageFile.getName()).append("' (time=")
                        .append(time).append(", level=").append(level).append(")").append(StringUtils.NEXT_LINE);
                duplicatedImagesCount++;
            }
        }

        if (duplicatedImagesCount > 0) {
            String headerText = duplicatedImagesCount + " " + (duplicatedImagesCount > 1 ? "images are" : "image is") + " duplicated:" + StringUtils.NEXT_LINE;
            logger.warning(headerText + duplicatedImagesReport);
        }

        int expectedImagesCount = tSize * lSize;
        if (imagesCount < expectedImagesCount) {
            checkMissingFiles();
        }
        return imagesCount;
    }

    public void checkMissingFiles() {
        // StringBuilder für kompakte Gesamtausgabe
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
                    missingImagesReport.append("- Filename: '").append(imageFile.getName()).append("' (time=")
                            .append(time).append(", level=").append(level).append(")").append(StringUtils.NEXT_LINE);
                    missingImagesCount++;
                }
            }
        }

        if (missingImagesCount > this.missingImagesCount) {
            String tempText = (missingImagesCount > 1 ? "images are" : "image is");
            String headerText = missingImagesCount + " " + tempText + " missing:" + StringUtils.NEXT_LINE;
            logger.warning(headerText + missingImagesReport);
        } else if (missingImagesCount < this.missingImagesCount) {
            // Es wurden einige Bilder wieder hergestellt
            int imageFiles = (this.maxTime + 1) * (this.maxLevel + 1) - missingImagesCount;
            logger.info("Some missing images were restored." + StringUtils.NEXT_LINE + "Loaded images: " + imageFiles);
        }
        this.missingImagesCount = missingImagesCount;
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

    private File getMissingFile(int time, int level, ImageFile imageFileNameReference) {
        int timeStrLength = getTimeStr(imageFileNameReference.getName()).length();
        int levelStrLength = getLevelStr(imageFileNameReference.getName()).length();
        String extension = getExtension(imageFileNameReference.getName());

        // Dynamische Bestandteile erzeugen
        String timeStr = (imageFileNameTimeSep + "%0" + timeStrLength + "d").formatted(time);
        String levelStr = (imageFileNameLevelSep + "%0" + levelStrLength + "d").formatted(level);
        return new File(imageFileNameReference.getFile().getParent(), timeStr + levelStr + "." + extension);
    }

    public String getMissingImages() {
        StringBuilder sb = new StringBuilder();
        int totalMissing = 0;

        for (int time = 0; time <= this.maxTime; time++) {
            List<Integer> missingLevels = new ArrayList<>();

            for (int level = 0; level <= this.maxLevel; level++) {
                ImageFile imageFile = getImageFile(time, level);
                if (!imageFile.exists()) {
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
                    sb.append(" - '").append(getImageFile(time, level).getName()).append("' (time=")
                            .append(time).append(", level=").append(level).append(")").append(StringUtils.NEXT_LINE);
                }
                sb.append(StringUtils.NEXT_LINE);
            }
        }

        if (totalMissing == 0) {
            sb.append("No missing Images.").append(StringUtils.NEXT_LINE);
        } else {
            String tempText = (totalMissing > 1 ? "images are" : "image is");
            String headerText = StringUtils.wrapBold(StringUtils.wrapColor(totalMissing + " " + tempText + " missing:", COLOR_RED));
            sb.insert(0, headerText + StringUtils.NEXT_LINE + StringUtils.NEXT_LINE);
        }

        return sb.toString();
    }
}

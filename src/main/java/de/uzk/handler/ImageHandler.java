package de.uzk.handler;

import de.uzk.utils.SystemConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static de.uzk.handler.ImageFileConstants.DEFAULT_PIN_TIME;
import static de.uzk.Main.logger;

public class ImageHandler {
    private ImageFile[][] imageFilesMatrix;
    private ImageDetails imageDetails;
    private File imageFolder;
    private ImageFile currentImage;
    private int maxTime;
    private int maxLevel;
    private int level;
    private int time;
    private double timeUnit;
    private double levelUnit;
    private int expectedImagesCount;
    private int missingImagesCount;
    private int pinTime;

    public ImageHandler() {
        setDefaultPinTime();
    }

    // getter / setter

    public File getImageFolder() {
        return this.imageFolder;
    }

    public void setImageFolder(File folderDir) {
        this.imageFolder = folderDir;
    }

    public String getImageDir() {
        return hasImageFolder() ? this.imageFolder.getAbsolutePath() : "";
    }

    public boolean hasImageFolder() {
        return this.imageFolder != null;
    }

    public ImageDetails getImageDetails() {
        return this.imageDetails;
    }

    protected void setImageDetails(ImageDetails imageDetails) {
        this.imageDetails = imageDetails;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public int getMaxTime() {
        return this.maxTime;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int newLevel, boolean allowOtherTime) {
        if (checkLevel(newLevel)) {
            ImageFile loadImageFile = getImageFile(this.time, newLevel);
            if (loadImageFile == null) {
                if (!allowOtherTime) return;

                // search next level
                int factor = newLevel - this.level;
                if (factor > 0) searchNextLevel(newLevel + 1, this.time);
                else if (factor < 0) searchPrevLevel(newLevel - 1, this.time);
            } else {
                this.currentImage = loadImageFile;
                updateLevel();
            }
        }
    }

    public int getTime() {
        return this.time;
    }

    public void setTime(int newTime, boolean allowOtherTime) {
        if (checkTime(newTime)) {
            ImageFile loadImageFile = getImageFile(newTime, this.level);
            if (loadImageFile == null) {
                if (!allowOtherTime) return;

                // search next time
                int factor = newTime - this.time;
                if (factor < 0) searchNextTime(newTime + 1, this.level);
                else if (factor > 0) searchPrevTime(newTime - 1, this.level);
            } else {
                this.currentImage = loadImageFile;
                updateTime();
            }
        }
    }

    public int getPinTime() {
        return pinTime;
    }

    public void togglePinTime() {
        if (this.pinTime == this.time) setDefaultPinTime();
        else this.pinTime = this.time;
    }

    public boolean hasPinTime() {
        return this.pinTime != DEFAULT_PIN_TIME;
    }

    public void setDefaultPinTime() {
        this.pinTime = DEFAULT_PIN_TIME;
    }

    public double getTimeUnit() {
        return this.timeUnit;
    }

    public void setTimeUnit(double timeUnit) {
        this.timeUnit = timeUnit;
    }

    public double getLevelUnit() {
        return this.levelUnit;
    }

    public void setLevelUnit(double levelUnit) {
        this.levelUnit = levelUnit;
    }

    public int getMissingImagesCount() {
        return missingImagesCount;
    }

    public ImageFile getCurrentImage() {
        return this.currentImage;
    }

    // methods
    public void clear() {
        this.imageFolder = null;
        this.clearTemp();
    }

    private void clearTemp() {
        this.currentImage = null;
        this.level = 0;
        this.time = 0;
        this.maxLevel = 0;
        this.maxTime = 0;
        this.expectedImagesCount = 0;
        this.missingImagesCount = 0;

        // clear files
        this.imageFilesMatrix = null;
    }

    public void loadImageFiles(LoadingImageListener progress) {
        File[] files = imageFolder.listFiles();
        logger.info("Loading Image-Files from '" + getImageDir() + "'...");

        // imageFiles
        int filesCount = files != null ? files.length : 0;
        progress.onScanningStart(filesCount);

        final List<ImageFile> imageFiles = new ArrayList<>();
        String imageFilePattern = ImageFile.getImageNamePattern(this.imageDetails);

        for (int i = 0; i < filesCount; i++) {
            if (Thread.currentThread().isInterrupted()) {
                logger.info("Loading process was interrupted.");
                return;
            }

            String fileName = getScannedImageFile(imageFiles, imageFilePattern, files[i]);
            progress.onScanningUpdate(fileName, i + 1, imageFiles.size(), files.length);
        }

        progress.onScanningComplete();
        createImageMatrix(imageFiles);
        progress.onLoadingComplete();
    }

    private String getScannedImageFile(List<ImageFile> imageFiles, String imageFilePattern, File file) {
        if (file != null) {
            String fileName = file.getName();
            if (file.isFile() && fileName.matches(imageFilePattern)) {
                imageFiles.add(new ImageFile(this.imageDetails, file, fileName));
            }
            return fileName;
        }
        return null;
    }

    private void createImageMatrix(List<ImageFile> imageFiles) {
        if (!imageFiles.isEmpty()) {
            clearTemp();
            for (ImageFile file : imageFiles) {
                if (file.getTime() > this.maxTime) this.maxTime = file.getTime();
                if (file.getLevel() > this.maxLevel) this.maxLevel = file.getLevel();
            }

            // create imageFilesMatrix
            this.imageFilesMatrix = new ImageFile[this.maxTime + 1][this.maxLevel + 1];
            Map<ImageFile, List<ImageFile>> duplicatedImages = new TreeMap<>(ImageFile::compareTo);

            // count
            int imagesCount = addImageFileToMatrix(imageFiles, duplicatedImages);
            this.expectedImagesCount = (this.maxTime + 1) * (this.maxLevel + 1);
            this.missingImagesCount = imageFiles.size() - imagesCount - duplicatedImages.size();

            // show loaded images
            showLoadedImages(imagesCount, this.expectedImagesCount, duplicatedImages.size(), this.missingImagesCount);

            // do checks
            checkDuplicateImages(duplicatedImages);
            checkMissingImages();
        } else {
            showLoadedImages(0, 0, 0, 0);
        }
    }

    private int addImageFileToMatrix(List<ImageFile> imageFiles, Map<ImageFile, List<ImageFile>> duplicatedImages) {
        int imageCount = 0;
        for (ImageFile imageFile : imageFiles) {
            ImageFile tempImage = this.imageFilesMatrix[imageFile.getTime()][imageFile.getLevel()];
            if (tempImage == null) {
                // normal initialization
                this.imageFilesMatrix[imageFile.getTime()][imageFile.getLevel()] = imageFile;
                imageCount++;
            } else {
                // add to duplicates
                if (duplicatedImages.containsKey(imageFile)) {
                    duplicatedImages.get(tempImage).add(imageFile);
                } else {
                    List<ImageFile> duplicates = new ArrayList<>();
                    duplicates.add(imageFile);
                    duplicatedImages.put(tempImage, duplicates);
                }
            }
        }
        return imageCount;
    }

    private void showLoadedImages(int imageCount, int expectedImagesCount, int duplicatedImagesCount, int missingImagesCount) {
        if (imageCount == 0 && expectedImagesCount == 0) {
            logger.warning("No images found. Loading failed!");
        } else {
            boolean noErrorsOccurred = duplicatedImagesCount == 0 && missingImagesCount == 0;
            logger.info("Loaded Image-Files: " + imageCount + (noErrorsOccurred ? "" : "/" + expectedImagesCount) + " (" +
                    (this.maxTime + 1) + "x" + (this.maxLevel + 1) + ")");
        }
    }

    private void checkDuplicateImages(Map<ImageFile, List<ImageFile>> duplicatedImagesMap) {
        int duplicatedImagesCount = duplicatedImagesMap.size();
        if (duplicatedImagesCount > 0) {
            StringBuilder duplicatedImages = new StringBuilder();

            duplicatedImagesMap.keySet().forEach(imageFile -> {
                duplicatedImages.append(getImageFileMsg(imageFile.getTime(), imageFile.getLevel(),
                        "duplicated")).append(SystemConstants.NEXT_LINE);
                duplicatedImages.append("\t> ").append(imageFile).append(SystemConstants.NEXT_LINE);

                List<ImageFile> duplicates = duplicatedImagesMap.get(imageFile);
                for (ImageFile duplicate : duplicates) {
                    duplicatedImages.append("\t> ").append(duplicate).append(SystemConstants.NEXT_LINE);
                }
            });

            showImagesResult(duplicatedImages, duplicatedImagesCount, "duplicated");
        }
    }

    private void checkMissingImages() {
        StringBuilder missingImages = new StringBuilder();
        for (int t = 0; t <= this.maxTime; t++) {
            for (int l = 0; l <= this.maxLevel; l++) {
                if (getImageFile(t, l) == null) {
                    missingImages.append(getImageFileMsg(t, l, "missing")).append(SystemConstants.NEXT_LINE);
                    this.missingImagesCount++;
                }
            }
        }
        showImagesResult(missingImages, this.missingImagesCount, "missing");
    }

    public void checkLostImages() {
        StringBuilder lostImages = new StringBuilder();
        int lostImagesCount = 0;

        for (int t = 0; t <= this.maxTime; t++) {
            for (int l = 0; l <= this.maxLevel; l++) {
                if (getImageFile(t, l) != null && !getImageFile(t, l).getFile().exists()) {
                    this.imageFilesMatrix[t][l] = null;
                    lostImagesCount++;
                    lostImages.append(getImageFileMsg(t, l, "lost")).append(SystemConstants.NEXT_LINE);
                }
            }
        }

        if (lostImagesCount > 0) {
            showImagesResult(lostImages, lostImagesCount, "lost");
        }
    }

    private void showImagesResult(StringBuilder imagesBuilder, int imagesCount, String imagesStateMsg) {
        if (!imagesBuilder.isEmpty()) {
            String result = "Some Image-Files are " + imagesStateMsg + '.' + SystemConstants.NEXT_LINE;
            result += "--------------- Information  ---------------" + SystemConstants.NEXT_LINE;
            result += imagesBuilder.toString();
            result += "Image-Files: " + imagesCount + SystemConstants.NEXT_LINE;
            result += "--------------------------------------------";

            logger.warning(result);
        }
    }

    public boolean isEmpty() {
        return this.imageFilesMatrix == null;
    }

    // sets the current imageFile to the first in the list
    public void toFirst() {
        if (!isEmpty()) {
            for (int t = 0; t <= this.maxTime; t++) {
                searchNextLevel(0, t);
                if (this.currentImage != null) {
                    updateTime();
                    return;
                }
            }
        }
    }

    // sets the current imageFile to the last in the list
    public void toLast() {
        if (!isEmpty()) {
            for (int t = this.maxTime; t >= 0; t--) {
                searchPrevLevel(this.maxLevel, t);
                if (this.currentImage != null) {
                    updateTime();
                    return;
                }
            }
        }
    }

    // sets the current imageFile to the first imageFile in the current level/time (can be chosen by layer)
    public void toFirst(ImageLayer layer) {
        if (!isEmpty() && layer != null) {
            if (layer == ImageLayer.TIME) searchNextTime(0, this.level);
            else searchNextLevel(0, this.time);
        }
    }

    // sets the current imageFile to the last imageFile in the current level/time (can be chosen by layer)
    public void toLast(ImageLayer layer) {
        if (!isEmpty() && layer != null) {
            if (layer == ImageLayer.TIME) searchPrevTime(this.maxTime, this.level);
            else searchPrevLevel(this.maxLevel, this.time);
        }
    }

    public void next(ImageLayer layer) {
        if (!isEmpty() && layer != null) {
            if (layer == ImageLayer.TIME) searchNextTime(this.time + 1, this.level);
            else searchNextLevel(this.level + 1, this.time);
        }
    }

    public void prev(ImageLayer layer) {
        if (!isEmpty() && layer != null) {
            if (layer == ImageLayer.TIME) searchPrevTime(this.time - 1, this.level);
            else searchPrevLevel(this.level - 1, this.time);
        }
    }

    private void searchNextTime(int startTime, int searchLevel) {
        for (int t = startTime; t <= this.maxTime; t++) {
            if (getImageFile(t, searchLevel) != null) {
                this.currentImage = getImageFile(t, searchLevel);
                updateTime();
                return;
            }
        }
    }

    private void searchPrevTime(int lastTime, int searchLevel) {
        for (int l = lastTime; l >= 0; l--) {
            if (getImageFile(l, searchLevel) != null) {
                this.currentImage = getImageFile(l, searchLevel);
                updateTime();
                return;
            }
        }
    }

    private void searchNextLevel(int startLevel, int searchTime) {
        for (int l = startLevel; l <= this.maxLevel; l++) {
            if (getImageFile(searchTime, l) != null) {
                this.currentImage = getImageFile(searchTime, l);
                updateLevel();
                return;
            }
        }
    }

    private void searchPrevLevel(int lastLevel, int searchTime) {
        for (int l = lastLevel; l >= 0; l--) {
            if (getImageFile(searchTime, l) != null) {
                this.currentImage = getImageFile(searchTime, l);
                updateLevel();
                return;
            }
        }
    }

    private void updateTime() {
        this.time = this.currentImage.getTime();
    }

    private void updateLevel() {
        this.level = this.currentImage.getLevel();
    }

    private ImageFile getImageFile(int searchTime, int searchLevel) {
        return this.imageFilesMatrix[searchTime][searchLevel];
    }

    private boolean checkLevel(int checkLevel) {
        if (checkLevel >= 0 && checkLevel <= this.maxLevel) return true;

        logger.error("Invalid level: " + checkLevel);
        return false;
    }

    private boolean checkTime(int checkTime) {
        if (checkTime >= 0 && checkTime <= this.maxTime) return true;

        logger.error("Invalid time: " + checkTime);
        return false;
    }

    public List<Integer> getMissingLevels(int searchTime) {
        List<Integer> missingValues = new ArrayList<>();
        if (checkTime(searchTime)) {
            for (int l = 0; l <= this.maxLevel; l++) {
                if (getImageFile(searchTime, l) == null) missingValues.add(l);
            }
        }
        return missingValues;
    }

    public List<Integer> getMissingTimes(int searchLevel) {
        List<Integer> missingValues = new ArrayList<>();
        if (checkLevel(searchLevel)) {
            for (int t = 0; t <= this.maxTime; t++) {
                if (getImageFile(t, searchLevel) == null) missingValues.add(t);
            }
        }
        return missingValues;
    }

    private String getImageFileMsg(int missingTime, int missingLevel, String imageStateMsg) {
        return "The Image-File '" + getImageName(String.valueOf(missingTime), String.valueOf(missingLevel)) + "' is " + imageStateMsg + ".";
    }

    private StringBuilder getImageName(String imageTime, String imageLevel) {
        return new StringBuilder(this.imageDetails.getSepTime()).append(imageTime).
                append(this.imageDetails.getSepLevel()).append(imageLevel).append('.').
                append(this.imageDetails.getImageType().getType());
    }

    // key values are the time values
    public String getAllMissingImages() {
        StringBuilder missingImagesBuilder = new StringBuilder();

        if (this.missingImagesCount > 0) {
            missingImagesBuilder.append("Missing Images: ").append(this.missingImagesCount).append(" / ").append(this.expectedImagesCount).
                    append(SystemConstants.NEXT_LINE.repeat(3));
        }

        for (int t = 0; t <= this.maxTime; t++) {
            List<Integer> missingLevels = getMissingLevels(t);

            if (!missingLevels.isEmpty()) {
                missingImagesBuilder.append("========== Time: ").append(t).append(" ==========").append(SystemConstants.NEXT_LINE);
                if (missingLevels.size() == this.maxLevel + 1) {
                    missingImagesBuilder.append("Levels: ALL!");
                } else {
                    missingImagesBuilder.append("Levels: ").append(missingLevels);
                }
                missingImagesBuilder.append(SystemConstants.NEXT_LINE).
                        append("Image Names: '").append(getImageName(String.valueOf(t), "(LEVEL)")).append("'").
                        append(SystemConstants.NEXT_LINE.repeat(2));
            }
        }

        return missingImagesBuilder.toString();
    }
}

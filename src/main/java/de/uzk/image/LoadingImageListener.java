package de.uzk.image;

import java.nio.file.Path;

public interface LoadingImageListener {
    void onLoadingStart();

    void onScanningStart(int filesCount, int currentFileNumber, int imagesCount);

    void onScanningUpdate(int filesCount, int currentFileNumber, Path path, int imagesCount) throws InterruptedException;

    void onScanningComplete(int filesCount, int currentFileNumber, int imagesCount);

    void onLoadingComplete(int imagesCount);
}
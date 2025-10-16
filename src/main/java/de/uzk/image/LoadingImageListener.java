package de.uzk.image;

import java.io.File;

public interface LoadingImageListener {
    void onLoadingStart();

    void onScanningStart(int filesCount, int currentFileNumber, int imagesCount);

    void onScanningUpdate(int filesCount, int currentFileNumber, File currentFile, int imagesCount) throws InterruptedException;

    void onScanningComplete(int filesCount, int currentFileNumber, int imagesCount);

    void onLoadingComplete(int imagesCount);
}
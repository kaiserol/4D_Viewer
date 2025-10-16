package de.uzk.image;

import java.io.File;

public interface LoadingImageListener {
    void onLoadingStart();

    void onScanningStart(int files);

    void onScanningUpdate(File file, int currentFile, int imageFiles, int files);

    void onScanningComplete();

    void onLoadingComplete(int imageFiles);
}
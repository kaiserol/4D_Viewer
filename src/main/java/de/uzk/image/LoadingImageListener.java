package de.uzk.image;

public interface LoadingImageListener {
    void onLoadingStart();

    void onScanningStart(int maxScans);

    void onScanningUpdate(String fileName, int currentScan, int downloadedImages, int maxScans);

    void onScanningComplete();

    void onLoadingComplete();
}
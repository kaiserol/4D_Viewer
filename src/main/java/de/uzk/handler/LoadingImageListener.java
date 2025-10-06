package de.uzk.handler;

public interface LoadingImageListener {
    void onScanningStart(int maxScans);

    void onScanningUpdate(String fileName, int currentScan, int downloadedImages, int maxScans);

    void onScanningComplete();

    void onLoadingComplete();
}
package de.uzk.gui.dialogs;

import de.uzk.gui.Gui;
import de.uzk.image.LoadingImageListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Objects;

import static de.uzk.Main.imageFileHandler;
import static de.uzk.Main.logger;
import static de.uzk.config.LanguageHandler.getWord;

// TODO: Überarbeite Klasse
public class DialogImageLoad implements LoadingImageListener {
    private JDialog loadingDialog;
    private JLabel scanningFileLabel;
    private JLabel uploadsLabel;
    private JProgressBar progressBar;
    // Variables using for download thread
    private Thread loadingThread;
    private boolean allowInterruptingDownload;

    public boolean openImageFilesDirectory(JFrame frame, String directoryPath) {
        this.loadingDialog = new JDialog(frame, true);
        this.loadingDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.loadingDialog.setResizable(false);
        this.loadingDialog.setTitle(getWord("loading.readingIn"));
        this.loadingDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Gui.exitApp(loadingDialog, () -> finishLoading());
            }
        });

        // progressPanel
        JPanel progressPanel = new JPanel(new BorderLayout(0, 10));
        progressPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

        final String htmlStartBold = "<html><b>";
        final String htmlEndBold = "</b></html>";

        // titlesPanel
        JPanel labelPanelWest = new JPanel(new BorderLayout(0, 5));
        labelPanelWest.add(new JLabel(htmlStartBold + getWord("file.directory") + ":" + htmlEndBold), BorderLayout.NORTH);
        labelPanelWest.add(new JLabel(htmlStartBold + getWord("file.file") + ":" + htmlEndBold), BorderLayout.SOUTH);

        // labelPanelEast
        JPanel labelPanelEast = new JPanel(new BorderLayout(0, 5));
        labelPanelEast.add(new JLabel(imageFileHandler.getImageFilesDirectoryPath()), BorderLayout.NORTH);

        this.scanningFileLabel = new JLabel("...");
        labelPanelEast.add(this.scanningFileLabel, BorderLayout.SOUTH);

        // labelPanel
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        labelPanel.add(labelPanelWest);
        labelPanel.add(labelPanelEast);
        progressPanel.add(labelPanel, BorderLayout.NORTH);

        // progressBar
        this.progressBar = new JProgressBar();
        this.progressBar.setStringPainted(false);
        this.progressBar.setIndeterminate(true);
        progressPanel.add(this.progressBar, BorderLayout.CENTER);

        // uploadsPanel
        JPanel uploadsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        uploadsPanel.add(new JLabel(htmlStartBold + getWord("loading.uploads") + ":" + htmlEndBold));
        this.uploadsLabel = new JLabel("0");
        uploadsPanel.add(this.uploadsLabel);
        progressPanel.add(uploadsPanel, BorderLayout.SOUTH);
        this.loadingDialog.add(progressPanel);
        this.loadingDialog.pack();
        this.loadingDialog.setLocationRelativeTo(this.loadingDialog.getOwner());

        // here stops the method until the dialog gets closed
        startLoading(directoryPath);
        this.loadingDialog.setVisible(true);

        return Objects.equals(directoryPath, imageFileHandler.getImageFilesDirectoryPath());
    }

    private void startLoading(String directoryPath) {
        if (this.loadingThread != null) return;
        this.loadingThread = new Thread(() -> {
            imageFileHandler.setImageFilesDirectory(directoryPath, DialogImageLoad.this);
            closeLoadingDialog();
        });

        this.allowInterruptingDownload = true;
        this.loadingThread.start();
    }

    private void finishLoading() {
        if (this.loadingThread == null) return;

        if (this.loadingThread.isAlive()) {
            this.loadingThread.interrupt();
            try {
                this.loadingThread.join();
            } catch (InterruptedException e) {
                logger.logException(e);
                Thread.currentThread().interrupt();
            }
            this.loadingDialog.setTitle(getWord("loading.interrupted"));
            this.uploadsLabel.setText("-1");
        }
        closeLoadingDialog();
    }

    private void closeLoadingDialog() {
        this.loadingThread = null;
        if (!Thread.currentThread().isInterrupted()) {
            this.loadingDialog.dispose();
        }
    }

    @Override
    public void onLoadingStart() {
        logger.info("Loading images from '" + imageFileHandler.getImageFilesDirectoryPath() + "' ...");
    }

    @Override
    public void onScanningStart(int files) {
        SwingUtilities.invokeLater(() -> {
            this.progressBar.setStringPainted(true);
            this.progressBar.setIndeterminate(false);
            this.progressBar.setValue(0);
            this.progressBar.setMaximum(files);
            this.progressBar.setString("0 / " + files + " (0%)");

            // update dialog
            this.loadingDialog.setTitle(getWord("loading.scanning"));
            this.loadingDialog.pack();
        });
    }

    @Override
    public void onScanningUpdate(File file, int currentFile, int imageFiles, int files) {
        SwingUtilities.invokeLater(() -> {
            this.scanningFileLabel.setText(file.getName());
            int progress = (int) (100.0 * currentFile / files);
            this.progressBar.setValue(currentFile);
            this.progressBar.setString(currentFile + " / " + files + " (" + progress + "%)");
            this.uploadsLabel.setText(String.valueOf(imageFiles));
            this.loadingDialog.pack();
        });
    }

    @Override
    public void onScanningComplete() {
        SwingUtilities.invokeLater(() -> {
            this.allowInterruptingDownload = false;
            this.progressBar.setString("100%");
        });
    }

    @Override
    public void onLoadingComplete(int imageFiles) {
        SwingUtilities.invokeLater(() -> {
            this.loadingDialog.setTitle(getWord("loading.finished"));
            this.loadingDialog.dispose();
        });

        if (imageFiles <= 0) logger.info("No images found.");
        else logger.info("Loaded images: " + imageFiles);
    }
}

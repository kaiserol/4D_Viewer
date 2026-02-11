package de.uzk.gui.dialogs;

import de.uzk.gui.UIEnvironment;
import de.uzk.image.ImageFileType;
import de.uzk.image.LoadingImageListener;
import de.uzk.image.LoadingResult;
import de.uzk.utils.ComponentUtils;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.uzk.Main.logger;
import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

public class DialogLoadingImages implements LoadingImageListener {
    // Für einen schönen Ladeeffekt SLEEP_TIME_NANOS > 0 setzen (1 Millisekunde = 1_000_000 Nanos)
    private static final int SLEEP_TIME_NANOS = 0;
    // Dialoge
    private final JDialog dialog;
    // Gui Elemente
    private JProgressBar progressBar;
    private JTextField textFieldFileName;
    private JLabel labelImagesCount;
    // Thread
    private Thread thread;
    private LoadingResult result;

    public DialogLoadingImages(Window parentWindow) {
        dialog = ComponentUtils.createDialog(parentWindow, this::closeThread);
    }

    public LoadingResult load(Path imagesDirectory, ImageFileType imageFileType) {
        if (imagesDirectory == null || !Files.exists(imagesDirectory)) return LoadingResult.DIRECTORY_DOES_NOT_EXIST;

        String title = getWord("dialog.loadingImages");
        if (imageFileType != null) {
            // Wenn wir ein bestehendes Projekt öffnen, wird der imageFileType erst später aus der Config ausgelesen.
            // Um ihn jetzt schon anzeigen zu können, müssten wir die Config vorher schon einlesen.

            title += " (" + imageFileType.getDescription() + ")";
        }
        dialog.setTitle(title);
        dialog.getContentPane().removeAll();
        dialog.setLayout(new BorderLayout());

        // Inhalt hinzufügen
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBorder(UIEnvironment.BORDER_EMPTY_DEFAULT);
        contentPanel.add(createProgressBarPanel(), BorderLayout.CENTER);
        contentPanel.add(createFileNamesPanel(imagesDirectory), BorderLayout.SOUTH);

        dialog.add(contentPanel, BorderLayout.CENTER);

        // Thread starten
        thread = null;
        result = null;
        startThread(imagesDirectory, imageFileType);

        // Dialog anzeigen
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.setVisible(true);

        // Das übergebene LoadingResult wird zurückgegeben, wenn beim Laden der Bilder nichts schiefläuft (es können
        // theoretisch Exceptions auftreten, wodurch LoadingResult null gleichen könnte)
        return result != null ? result : LoadingResult.LOADING_INTERRUPTED;
    }

    // ========================================
    // Komponenten-Erzeugung
    // ========================================
    private JPanel createProgressBarPanel() {
        JPanel progressBarPanel = new JPanel(UIEnvironment.getDefaultBorderLayout());

        // Fortschrittsbalken hinzufügen
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(0, 20));
        progressBarPanel.add(progressBar, BorderLayout.SOUTH);

        // Anzahl gefundener Bilder hinzufügen
        JPanel imagesFoundPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        imagesFoundPanel.add(new JLabel(getWord("dialog.loadingImages.foundImages") + ":"));
        imagesFoundPanel.add(labelImagesCount = new JLabel("0"));
        progressBarPanel.add(imagesFoundPanel, BorderLayout.CENTER);

        return progressBarPanel;
    }

    private JPanel createFileNamesPanel(Path imagesDirectory) {
        JPanel fileNamesPanel = new JPanel(new GridBagLayout());

        // Layout Manager
        GridBagConstraints gbc = ComponentUtils.createGridBagConstraints();

        // Dateiname hinzufügen
        textFieldFileName = createTextField();
        textFieldFileName.setFocusable(false);
        ComponentUtils.addLabeledRow(fileNamesPanel, gbc, getWord("name.file"), textFieldFileName, 0);

        // Verzeichnisname hinzufügen
        JTextField textFieldDirectoryName = createTextField();
        textFieldDirectoryName.setText(imagesDirectory.toAbsolutePath().toString());
        ComponentUtils.addLabeledRow(fileNamesPanel, gbc, getWord("name.directory"), textFieldDirectoryName, 5);

        return fileNamesPanel;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setEditable(false);
        return textField;
    }

    // ========================================
    // Thread Methoden
    // ========================================
    private void startThread(Path imagesDirectory, ImageFileType imageFileType) {
        if (thread != null) return;
        thread = new Thread(() -> {
            result = workspace.loadImagesDirectory(imagesDirectory, imageFileType, DialogLoadingImages.this);
            SwingUtilities.invokeLater(dialog::dispose);
        });
        thread.start();
    }

    private void closeThread() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                logger.error("Failed to join the thread. (Process: Loading images ...)");
            }
        }
    }

    // ========================================
    // LoadingImageListener Methoden
    // ========================================
    @Override
    public void onLoadingStart() {
        logger.info("Loading images from the directory '%s' ...".formatted(workspace.getImagesDirectory().toAbsolutePath()));
    }

    @Override
    public void onScanningStart(int filesCount, int currentFileNumber, int imagesCount) {
        SwingUtilities.invokeLater(() -> {
            updateProgress(filesCount, currentFileNumber, imagesCount);
            progressBar.setIndeterminate(false);
            progressBar.setMaximum(filesCount);
        });
    }

    @Override
    public void onScanningUpdate(int filesCount, int currentFileNumber, Path path, int imagesCount) throws InterruptedException {
        // Thread anhalten
        Thread.sleep(0, SLEEP_TIME_NANOS);

        SwingUtilities.invokeLater(() -> {
            updateProgress(filesCount, currentFileNumber, imagesCount);
            textFieldFileName.setText(path.getFileName().toString());
        });
    }

    @Override
    public void onScanningComplete(int filesCount, int currentFileNumber, int imagesCount) {
        SwingUtilities.invokeLater(() -> {
            updateProgress(filesCount, currentFileNumber, imagesCount);
            progressBar.setIndeterminate(true);
        });
    }

    @Override
    public void onLoadingComplete(int imageFiles) {
        if (imageFiles == 0) {
            logger.info("Loaded Images: " + imageFiles);
            return;
        }

        int maxTime = workspace.getMaxTime();
        int maxLevel = workspace.getMaxLevel();
        String loadedImages = "%d (%dx%d)".formatted(imageFiles, maxTime + 1, maxLevel + 1);
        logger.info("Loaded Images: " + loadedImages);
    }

    private void updateProgress(int filesCount, int currentFileNumber, int imagesCount) {
        progressBar.setValue(currentFileNumber);
        progressBar.setString(currentFileNumber + " / " + filesCount);
        labelImagesCount.setText(String.valueOf(imagesCount));
    }
}

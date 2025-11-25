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
    // Dialoge
    private final JDialog dialog;

    // Gui Elemente
    private JProgressBar progressBar;
    private JTextField textFieldFileName;
    private JLabel labelImagesCount;

    // Thread
    private Thread thread;
    private LoadingResult result;

    // Für einen schönen Ladeeffekt SLEEP_TIME_NANOS > 0 setzen (1 Millisekunde = 1_000_000 Nanos)
    private static final int SLEEP_TIME_NANOS = 0;

    public DialogLoadingImages(Window parentWindow) {
        this.dialog = ComponentUtils.createDialog(parentWindow, this::closeThread);
    }


    public LoadingResult load(Path imagesDirectory, ImageFileType imageFileType) {
        if (imagesDirectory == null || !Files.exists(imagesDirectory)) return LoadingResult.DIRECTORY_DOES_NOT_EXIST;


        String title = getWord("dialog.loadingImages");
        if (imageFileType != null) {
            // Wenn wir ein bestehendes Projekt öffnen, wird der imageFileType erst später aus der Config ausgelesen.
            // Um ihn jetzt schon anzeigen zu können, müssten wir die Config vorher schon einlesen.

            title += " (" + imageFileType.getDescription() + ")";
        }
        this.dialog.setTitle(title);
        this.dialog.getContentPane().removeAll();
        this.dialog.setLayout(new BorderLayout());

        // Inhalt hinzufügen
        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBorder(UIEnvironment.BORDER_EMPTY_DEFAULT);
        contentPanel.add(createProgressBarPanel(), BorderLayout.CENTER);
        contentPanel.add(createFileNamesPanel(imagesDirectory), BorderLayout.SOUTH);

        this.dialog.add(contentPanel, BorderLayout.CENTER);

        // Thread starten
        this.thread = null;
        this.result = null;
        startThread(imagesDirectory, imageFileType);

        // Dialog anzeigen
        this.dialog.pack();
        this.dialog.setResizable(false);
        this.dialog.setLocationRelativeTo(this.dialog.getOwner());
        this.dialog.setVisible(true);

        // Das übergebene LoadingResult wird zurückgegeben, wenn beim Laden der Bilder nichts schiefläuft (es können
        // theoretisch Exceptions auftreten, wodurch LoadingResult null gleichen könnte)
        return this.result != null ? this.result : LoadingResult.LOADING_INTERRUPTED;
    }

    // ========================================
    // Komponenten-Erzeugung
    // ========================================
    private JPanel createProgressBarPanel() {
        JPanel progressBarPanel = new JPanel(new BorderLayout(0, 10));

        // Fortschrittsbalken hinzufügen
        this.progressBar = new JProgressBar();
        this.progressBar.setStringPainted(true);
        this.progressBar.setIndeterminate(true);
        this.progressBar.setPreferredSize(new Dimension(0, 20));
        progressBarPanel.add(this.progressBar, BorderLayout.SOUTH);

        // Anzahl gefundener Bilder hinzufügen
        JPanel imagesFoundPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        imagesFoundPanel.add(new JLabel(getWord("dialog.loadingImages.foundImages") + ":"));
        imagesFoundPanel.add(this.labelImagesCount = new JLabel("0"));
        progressBarPanel.add(imagesFoundPanel, BorderLayout.CENTER);

        return progressBarPanel;
    }

    private JPanel createFileNamesPanel(Path imagesDirectory) {
        JPanel fileNamesPanel = new JPanel(new GridBagLayout());

        // Layout Manager
        GridBagConstraints gbc = ComponentUtils.createGridBagConstraints();

        // Dateiname hinzufügen
        this.textFieldFileName = createTextField();
        this.textFieldFileName.setFocusable(false);
        ComponentUtils.addLabeledRow(fileNamesPanel, gbc, getWord("name.file"), this.textFieldFileName, 0);

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
        if (this.thread != null) return;
        this.thread = new Thread(() -> {
            this.result = workspace.loadImagesDirectory(imagesDirectory, imageFileType, DialogLoadingImages.this);
            SwingUtilities.invokeLater(this.dialog::dispose);
        });
        this.thread.start();
    }

    private void closeThread() {
        if (this.thread != null && this.thread.isAlive()) {
            this.thread.interrupt();
            try {
                this.thread.join();
            } catch (InterruptedException e) {
                logger.error("Failed to join the thread. (Process: Loading images ...)");
            }
        }
        this.dialog.dispose();
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
            this.progressBar.setIndeterminate(false);
            this.progressBar.setMaximum(filesCount);
        });
    }

    @Override
    public void onScanningUpdate(int filesCount, int currentFileNumber, Path path, int imagesCount) throws InterruptedException {
        // Thread anhalten
        Thread.sleep(0, SLEEP_TIME_NANOS);

        SwingUtilities.invokeLater(() -> {
            updateProgress(filesCount, currentFileNumber, imagesCount);
            this.textFieldFileName.setText(path.getFileName().toString());
        });
    }

    @Override
    public void onScanningComplete(int filesCount, int currentFileNumber, int imagesCount) {
        SwingUtilities.invokeLater(() -> {
            updateProgress(filesCount, currentFileNumber, imagesCount);
            this.progressBar.setIndeterminate(true);
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
        this.progressBar.setValue(currentFileNumber);
        this.progressBar.setString(currentFileNumber + " / " + filesCount);
        this.labelImagesCount.setText(String.valueOf(imagesCount));
    }
}

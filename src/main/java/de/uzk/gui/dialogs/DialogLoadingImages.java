package de.uzk.gui.dialogs;

import de.uzk.gui.GuiUtils;
import de.uzk.image.ImageFileType;
import de.uzk.image.LoadingImageListener;
import de.uzk.image.LoadingResult;
import de.uzk.utils.ComponentUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.uzk.Main.logger;
import static de.uzk.Main.workspace;
import static de.uzk.config.LanguageHandler.getWord;

public class DialogLoadingImages implements LoadingImageListener {
    // GUI-Elemente
    private final JDialog dialog;
    private JProgressBar progressBar;
    private JTextField textFieldFileName, textFieldDirectoryName;
    private JLabel labelImagesCount;

    // Thread
    private Thread thread;
    private LoadingResult result;

    // Für einen schönen Ladeeffekt SLEEP_TIME_NANOS > 0 setzen (1 Millisekunde = 1_000_000 Nanos)
    private static final int SLEEP_TIME_NANOS = 0;

    public DialogLoadingImages(JFrame frame) {
        this.dialog = new JDialog(frame, true);
        this.dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeThread();
            }
        });

        // ESC schließt Dialog
        this.dialog.getRootPane().registerKeyboardAction(e -> closeThread(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    public LoadingResult show(Path imagesDirectory, ImageFileType imageFileType) {
        if (imagesDirectory == null || !Files.exists(imagesDirectory)) return LoadingResult.DIRECTORY_DOES_NOT_EXIST;
        // TODO: Warum rausgenommen (für mich)
//        this.dialog.setTitle(getWord("dialog.imageLoading") + " (" + imageFileType.getDescription() + ")");
        this.dialog.setTitle(getWord("dialog.loadingImages"));
        this.dialog.getContentPane().removeAll();
        this.dialog.setLayout(new BorderLayout(0, 10));

        // Inhalt hinzufügen
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBorder(GuiUtils.BORDER_PADDING_LARGE);
        panel.add(createProgressPanel(), BorderLayout.CENTER);
        panel.add(createFileDirectoryPanel(), BorderLayout.SOUTH);
        this.dialog.add(panel);

        // Wenn eine gültige "Datei" übergeben wird, wird ins Elternverzeichnis navigiert,
        // ansonsten wird "imagesDirectory" beibehalten
        Path changedImagesDirectory = Files.isRegularFile(imagesDirectory) ? imagesDirectory.getParent() : imagesDirectory;
        this.textFieldDirectoryName.setText(changedImagesDirectory.toAbsolutePath().toString());

        // Thread starten
        this.thread = null;
        this.result = null;
        startThread(changedImagesDirectory, imageFileType);

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
    private JPanel createProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));

        // Fortschrittsbalken hinzufügen
        this.progressBar = new JProgressBar();
        this.progressBar.setStringPainted(true);
        this.progressBar.setIndeterminate(true);
        this.progressBar.setPreferredSize(new Dimension(0, 20));
        panel.add(this.progressBar, BorderLayout.SOUTH);

        // Anzahl gefundener Bilder hinzufügen
        JPanel imagesFoundPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        imagesFoundPanel.add(new JLabel(getWord("dialog.loadingImages.foundImages") + ":"));
        imagesFoundPanel.add(this.labelImagesCount = new JLabel("0"));
        panel.add(imagesFoundPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFileDirectoryPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        // Layout Manager
        GridBagConstraints gbc = ComponentUtils.createGridBagConstraints();

        // Dateiname hinzufügen
        this.textFieldFileName = createTextField();
        this.textFieldFileName.setFocusable(false);
        ComponentUtils.addLabeledRow(panel, gbc, getWord("name.file"), this.textFieldFileName, 0);

        // Verzeichnisname hinzufügen
        this.textFieldDirectoryName = createTextField();
        ComponentUtils.addLabeledRow(panel, gbc, getWord("name.directory"), this.textFieldDirectoryName, 5);

        return panel;
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
            this.result = workspace.openImagesDirectory(imagesDirectory, imageFileType, DialogLoadingImages.this);
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
                logger.error("Failed to join loading images thread");
            }
        }
        this.dialog.dispose();
    }

    // ========================================
    // LoadingImageListener Methoden
    // ========================================
    @Override
    public void onLoadingStart() {
        logger.info(String.format("Loading images from the directory '%s'", workspace.getImagesDirectory().toAbsolutePath()));
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
        if (imageFiles == 0) logger.info("Loaded images: " + imageFiles);

        int maxTime = workspace.getMaxTime();
        int maxLevel = workspace.getMaxLevel();
        String loadedImages = String.format("%d (%dx%d)", imageFiles, maxTime + 1, maxLevel + 1);
        logger.info("Loaded images: " + loadedImages);
    }

    private void updateProgress(int filesCount, int currentFileNumber, int imagesCount) {
        this.progressBar.setValue(currentFileNumber);
        this.progressBar.setString(currentFileNumber + " / " + filesCount);
        this.labelImagesCount.setText(String.valueOf(imagesCount));
    }
}

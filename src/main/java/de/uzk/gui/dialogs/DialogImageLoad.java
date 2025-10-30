package de.uzk.gui.dialogs;

import de.uzk.image.ImageFileType;
import de.uzk.image.LoadingImageListener;
import de.uzk.image.LoadingResult;

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

public class DialogImageLoad implements LoadingImageListener {
    // Für einen schönen Ladeeffekt SLEEP_TIME_NANOS > 0 setzen
    // (1 Millisekunde = 1_000_000 Nanos)
    private static final int SLEEP_TIME_NANOS = 0;
    private final JDialog dialog;
    private JTextField textFieldFileName;
    private JTextField textFieldDirectoryName;
    private JProgressBar progressBar;
    private JLabel labelImageFilesCount;

    // Thread
    private Thread thread;
    private LoadingResult result;

    public DialogImageLoad(JFrame frame) {
        this.dialog = new JDialog(frame, true);
        this.dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.dialog.setResizable(false);
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

    public LoadingResult show(Path directory, ImageFileType imageFileType) {
        if (directory == null) return LoadingResult.DIRECTORY_NOT_EXISTING;
        this.dialog.getContentPane().removeAll();
        this.dialog.setLayout(new BorderLayout());
        this.dialog.setTitle(getWord("dialog.imageLoading") + " (" + imageFileType.getDescription() + ")");

        // Inhalt hinzufügen
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        panel.add(createProgressPanel(), BorderLayout.CENTER);
        panel.add(createFileDirectoryPanel(), BorderLayout.SOUTH);
        this.dialog.add(panel);

        // Wenn eine gültige "Datei" übergeben wird, wird ins Elternverzeichnis navigiert,
        // ansonsten wird "directory" beibehalten
        Path changedDirectory = Files.isRegularFile(directory) ? directory.getParent() : directory;
        this.textFieldDirectoryName.setText(changedDirectory.toAbsolutePath().toString());

        // Thread starten
        this.thread = null;
        this.result = null;
        startThread(changedDirectory, imageFileType);

        // Dialog anzeigen
        this.dialog.pack();
        this.dialog.setLocationRelativeTo(this.dialog.getOwner());
        this.dialog.setVisible(true);

        // Das übergebene LoadingResult wird zurückgegeben, wenn beim Laden der Bilder nichts schiefläuft (es können
        // theoretisch Exceptions auftreten, wodurch LoadingResult null gleichen könnte)
        return this.result != null ? this.result : LoadingResult.INTERRUPTED;
    }

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
        imagesFoundPanel.add(new JLabel(getWord("dialog.imageLoading.foundImages") + ":"));
        imagesFoundPanel.add(this.labelImageFilesCount = new JLabel("0"));
        panel.add(imagesFoundPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFileDirectoryPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        // Layout Manager
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;

        // Dateiname anzeigen
        gbc.gridwidth = 1;
        this.textFieldFileName = addLabelTextFieldRow(panel, gbc, 1, getWord("file.fileName") + ":");
        this.textFieldFileName.setFocusable(false);

        // Verzeichnisname anzeigen
        gbc.insets.top = 5;
        this.textFieldDirectoryName = addLabelTextFieldRow(panel, gbc, 2, getWord("file.directoryName") + ":");

        return panel;
    }

    // ==========================================================
    // Überschreibungen des LoadingImageListener Interface
    // ==========================================================
    @Override
    public void onLoadingStart() {
        logger.info("Loading Images from '" + workspace.getImageFilesDirectory() + "' ...");
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
        logger.info("Loaded Images: " + imageFiles);
    }

    // ==========================================================
    // Hilfsfunktionen
    // ==========================================================
    private JTextField addLabelTextFieldRow(JPanel panel, GridBagConstraints gbc, int row, String labelText) {
        // Label hinzufügen
        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.insets.right = 10;
        panel.add(new JLabel(labelText), gbc);

        // Textfeld hinzufügen
        gbc.weightx = 1;
        gbc.gridx = 1;
        gbc.insets.right = 0;
        JTextField textField = new JTextField();
        textField.setEditable(false);
        panel.add(textField, gbc);

        return textField;
    }

    private void updateProgress(int filesCount, int currentFileNumber, int imagesCount) {
        this.progressBar.setValue(currentFileNumber);
        this.progressBar.setString(currentFileNumber + " / " + filesCount);
        this.labelImageFilesCount.setText(String.valueOf(imagesCount));
    }

    // ==========================================================
    // Thread Methoden
    // ==========================================================
    private void startThread(Path directory, ImageFileType imageFileType) {
        if (this.thread != null) return;
        this.thread = new Thread(() -> {
            this.result = workspace.openDirectory(directory, imageFileType, DialogImageLoad.this);
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
                logger.error("Interrupted 'loading images' while waiting for the thread to finish.");
            }
        }
        dialog.dispose();
    }
}

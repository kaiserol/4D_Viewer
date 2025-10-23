package de.uzk.gui.dialogs;

import de.uzk.image.ImageFileNameExtension;
import de.uzk.image.LoadingImageListener;
import de.uzk.image.LoadingResult;
import de.uzk.image.Workspace;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
        init();

        // ESC schließt Dialog
        this.dialog.getRootPane().registerKeyboardAction(e -> closeThread(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void init() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBorder(new EmptyBorder(20, 10, 10, 10));

        // Inhalt hinzufügen
        panel.add(createProgressPanel(), BorderLayout.CENTER);
        panel.add(createFileDirectoryPanel(), BorderLayout.SOUTH);
        this.dialog.add(panel);
    }

    private JPanel createProgressPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));

        // Fortschrittsbalken anzeigen
        this.progressBar = new JProgressBar();
        this.progressBar.setStringPainted(true);
        this.progressBar.setIndeterminate(true);
        this.progressBar.setPreferredSize(new Dimension(0, 20));
        panel.add(this.progressBar, BorderLayout.SOUTH);

        // Anzahl geladener Bilder anzeigen
        JPanel panelNorth = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panelNorth.add(new JLabel(getWord("dialog.imageLoading.foundImages") + ":"));

        this.labelImageFilesCount = new JLabel("0");
        panelNorth.add(this.labelImageFilesCount);
        panel.add(panelNorth, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFileDirectoryPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 0);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1;

        // Dateiname, Verzeichnisname anzeigen
        gbc.gridwidth = 1;
        this.textFieldFileName = addLabelTextFieldRow(panel, gbc, 1, getWord("file.fileName") + ":");
        this.textFieldFileName.setFocusable(false);
        gbc.insets.top = 5;
        this.textFieldDirectoryName = addLabelTextFieldRow(panel, gbc, 2, getWord("file.directoryName") + ":");

        return panel;
    }

    private JTextField addLabelTextFieldRow(JPanel panel, GridBagConstraints gbc, int row, String labelText) {
        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.insets.right = 10;
        panel.add(new JLabel(labelText), gbc);

        gbc.weightx = 1;
        gbc.gridx = 1;
        gbc.insets.right = 0;
        JTextField textField = new JTextField();
        textField.setEditable(false);
        panel.add(textField, gbc);

        return textField;
    }

    public LoadingResult loadImages(Path directoryPath, ImageFileNameExtension extension) {
        this.dialog.setTitle(getWord("dialog.imageLoading.title") + " (" + extension.getDescription() + ")");
        // TODO: Das wird einen Nebeneffekt haben: wenn man einen Ordner angibt, der nicht existiert,
        // dann wird der Überordner als Verzeichnisname. Mit isFile deshalb prüfen
        Path directory = Files.isDirectory(directoryPath) ? directoryPath : directoryPath.getParent();
        this.textFieldDirectoryName.setText(directory.toAbsolutePath().toString());

        // Fenster packen
        this.dialog.pack();
        this.dialog.setLocationRelativeTo(this.dialog.getOwner());

        // Thread starten
        this.thread = null;
        this.result = null;
        startThread(directoryPath, extension);

        // Dialog anzeigen
        this.dialog.setVisible(true);

        return this.result != null ? this.result : LoadingResult.INTERRUPTED;
    }

    private void startThread(Path directoryPath, ImageFileNameExtension extension) {
        if (this.thread != null) return;
        this.thread = new Thread(() -> {
            this.result = workspace.open(directoryPath, extension, DialogImageLoad.this);
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
                logger.logException(e);
            }
        }
        dialog.dispose();
    }

    // ------------------------- LoadingImageListener Funktionen -------------------------

    private void updateProgress(int filesCount, int currentFileNumber, int imagesCount) {
        this.progressBar.setValue(currentFileNumber);
        this.progressBar.setString(currentFileNumber + " / " + filesCount);
        this.labelImageFilesCount.setText(String.valueOf(imagesCount));
    }

    @Override
    public void onLoadingStart() {
        logger.info("Loading Images...");
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
    public void onScanningUpdate(int filesCount, int currentFileNumber, Path currentFile, int imagesCount) throws InterruptedException {
        // Thread anhalten
        Thread.sleep(0, SLEEP_TIME_NANOS);

        SwingUtilities.invokeLater(() -> {
            updateProgress(filesCount, currentFileNumber, imagesCount);
            this.progressBar.setStringPainted(true);
            this.progressBar.setBorderPainted(true);
            this.textFieldFileName.setText(currentFile.getFileName().toString());
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

    @Override
    public void onFinished(LoadingResult result) {
        this.result = result;
    }
}

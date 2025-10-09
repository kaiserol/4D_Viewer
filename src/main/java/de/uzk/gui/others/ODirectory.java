package de.uzk.gui.others;

import de.uzk.actions.ActionType;
import de.uzk.actions.ActionTypeListener;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.gui.InteractiveContainer;
import de.uzk.image.ImageType;
import de.uzk.image.LoadingImageListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Objects;

import static de.uzk.Main.*;
import static de.uzk.config.LanguageHandler.getWord;
import static de.uzk.image.ImageFileConstants.IMAGE_TYPES;

public class ODirectory extends InteractiveContainer<JPanel> implements LoadingImageListener, ActionTypeListener {
    private JTextField pathField;
    private JComboBox<ImageType> fileTypeCB;
    private JDialog loadingDialog;
    private JLabel scanningFileLabel;
    private JLabel uploadsLabel;
    private JProgressBar progressBar;
    // Variables using for download thread
    private Thread loadingThread;
    private boolean allowInterruptingDownload;

    public ODirectory(Gui gui) {
        super(new JPanel(), gui);
        gui.addActionTypeListener(this);
        init();
    }

    private void init() {
        container.setLayout(new GridBagLayout());

        // GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // dirField
        pathField = new JTextField(20);
        pathField.setEditable(false);
        pathField.putClientProperty("JTextField.placeholderText", getWord("file.path"));
        container.add(pathField, gbc);

        // gbc
        gbc.weightx = 0;
        gbc.gridx++;
        gbc.insets = new Insets(0, 10, 0, 10);

        // fileTypeCB
        fileTypeCB = new JComboBox<>(IMAGE_TYPES);
        fileTypeCB.setSelectedItem(imageHandler.getImageDetails().getImageType());
        fileTypeCB.addItemListener(this::changeFileType);
        fileTypeCB.setRenderer(new ImageTypeComboBoxRenderer());
        container.add(fileTypeCB, gbc);

        // gbc
        gbc.gridx++;
        gbc.insets = new Insets(0, 0, 0, 0);

        // dirButton
        JButton pathButton = new JButton(getWord("file.chooseDirText"));
        pathButton.addActionListener(a -> openDirectoryDialog(gui.getFrame()));
        container.add(pathButton, gbc);
    }

    private void changeFileType(ItemEvent e) {
        // using equals comparison to avoid running the block twice
        if (Objects.equals(fileTypeCB.getSelectedItem(), e.getItem()) && GuiUtils.isEnabled(fileTypeCB)) {
            fileTypeCB.setPopupVisible(false);
            ImageType oldImageType = imageHandler.getImageDetails().getImageType();
            imageHandler.getImageDetails().setImageType((ImageType) fileTypeCB.getSelectedItem());

            changeImages(imageHandler.getImageFolder(), false, () -> {
                if (imageHandler.hasImageFolder()) {
                    // change values to the old values
                    imageHandler.getImageDetails().setImageType(oldImageType);
                    GuiUtils.updateSecretly(fileTypeCB, () -> fileTypeCB.setSelectedItem(oldImageType));
                }
            });
        }
    }

    // TODO: Verbessere den Filter, sodass nur Ordner gesehen werden können, die Bilder enthalten...
    private void openDirectoryDialog(Frame parent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.putClientProperty("FileChooser.readOnly", Boolean.TRUE);

        // Dialogtitel dynamisch setzen
        String dialogTitle = String.format("%s (%s)",
                getWord("file.chooseDirTitle"),
                imageHandler.getImageDetails().getImageType().getTypeDescription());
        fileChooser.setDialogTitle(dialogTitle);

        // FileFilter einstellen
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                } else {
                    return true;
                }
            }

            @Override
            public String getDescription() {
                return imageHandler.getImageDetails().getImageType().toString();
            }
        };
        fileChooser.setFileFilter(filter);

        if (imageHandler.hasImageFolder()) {
            fileChooser.setSelectedFile(imageHandler.getImageFolder());
        } else {
            // set to user dir if existing
            String userDir = System.getProperty("user.dir");
            if (userDir != null) fileChooser.setCurrentDirectory(new File(userDir));
        }

        int state = fileChooser.showOpenDialog(parent);
        if (state == JFileChooser.APPROVE_OPTION) {
            if (!fileChooser.getSelectedFile().exists()) {
                JOptionPane.showMessageDialog(parent, getWord("file.chosenDirNotExisting"),
                        getWord("optionPane.title.error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            File oldFolder = imageHandler.getImageFolder();
            // newFolder
            File tempFile = fileChooser.getSelectedFile();
            File newFolder = tempFile.isDirectory() ? tempFile : tempFile.getParentFile();
            if (newFolder.equals(oldFolder)) return;

            imageHandler.setImageFolder(newFolder);
            changeImages(oldFolder, false, null);
        }
    }

    @Override
    public void toggleOn() {
        pathField.setText(imageHandler.getImageDir());
    }

    @Override
    public void toggleOff() {
        pathField.setText(null);
    }

    // update functions
    private void changeImages(File oldFolder, boolean startingGui, Runnable runIfPathRemained) {
        if (imageHandler.hasImageFolder()) {
            changeImagesHelper(gui.getFrame(), oldFolder, startingGui, runIfPathRemained);
        }
    }

    private void changeImagesHelper(JFrame frame, File oldFolder, boolean startingGui, Runnable runIfPathRemained) {
        // load images gui with images
        int loadedImages = openLoadingDialog(frame, startingGui);
        if (loadedImages > 0) {
            gui.toggleOn();
        } else {
            if (!startingGui && loadedImages == 0) {
                JOptionPane.showMessageDialog(gui.getFrame(), getWord("file.chosenDirNoneFiles") + " " +
                                imageHandler.getImageDetails().getImageType().getTypeDescription() + ".",
                        getWord("optionPane.title.error"), JOptionPane.ERROR_MESSAGE);
            }

            if (runIfPathRemained != null) {
                runIfPathRemained.run();
            }
            imageHandler.setImageFolder(oldFolder);
        }
    }

    private int openLoadingDialog(JFrame frame, boolean startingGui) {
        this.loadingDialog = new JDialog(frame, true);
        this.loadingDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.loadingDialog.setResizable(false);
        this.loadingDialog.setTitle(getWord("loading.readingIn"));
        this.loadingDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (startingGui) {
                    closeApp(loadingDialog, () -> finishLoading());
                } else if (ODirectory.this.allowInterruptingDownload) {
                    finishLoading();
                }
            }
        });

        // progressPanel
        JPanel progressPanel = new JPanel(new BorderLayout(0, 10));
        progressPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

        final String htmlStartBold = "<html><b>";
        final String htmlEndBold = "</b></html>";

        // titlesPanel
        JPanel labelPanelWest = new JPanel(new BorderLayout(0, 5));
        labelPanelWest.add(new JLabel(htmlStartBold + getWord("file.dir") + ":" + htmlEndBold), BorderLayout.NORTH);
        labelPanelWest.add(new JLabel(htmlStartBold + getWord("file") + ":" + htmlEndBold), BorderLayout.SOUTH);

        // labelPanelEast
        JPanel labelPanelEast = new JPanel(new BorderLayout(0, 5));
        labelPanelEast.add(new JLabel(imageHandler.getImageDir()), BorderLayout.NORTH);

        this.scanningFileLabel = new JLabel("...");
        GuiUtils.updateFontSize(this.scanningFileLabel, 0, Font.PLAIN);
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
        this.loadingDialog.setLocationRelativeTo(frame);

        startLoading();

        // here stops the method until the dialog gets closed
        this.loadingDialog.setVisible(true);

        // returns the result
        return Integer.parseInt(this.uploadsLabel.getText());
    }

    private void startLoading() {
        if (this.loadingThread != null) return;
        this.loadingThread = new Thread(() -> {
            imageHandler.loadImageFiles(ODirectory.this);
            markerHandler.resetImageCount(imageHandler.getMaxTime() + 1);
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
    public void onScanningStart(int maxScans) {
        SwingUtilities.invokeLater(() -> {
            this.progressBar.setStringPainted(true);
            this.progressBar.setIndeterminate(false);
            this.progressBar.setValue(0);
            this.progressBar.setMaximum(maxScans);
            this.progressBar.setString("0 / " + maxScans + " (0%)");

            // update dialog
            this.loadingDialog.setTitle(getWord("loading.scanning"));
            this.loadingDialog.pack();
        });
    }

    @Override
    public void onScanningUpdate(String fileName, int currentScan, int downloadedImages, int maxScans) {
        SwingUtilities.invokeLater(() -> {
            this.scanningFileLabel.setText(fileName);
            int progress = (int) (100.0 * currentScan / maxScans);
            this.progressBar.setValue(currentScan);
            this.progressBar.setString(currentScan + " / " + maxScans + " (" + progress + "%)");
            this.uploadsLabel.setText(String.valueOf(downloadedImages));
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
    public void onLoadingComplete() {
        SwingUtilities.invokeLater(() -> {
            this.loadingDialog.setTitle(getWord("loading.finished"));
            this.loadingDialog.dispose();
        });
    }

    @Override
    public void handleAction(ActionType actionType) {
        if (actionType == ActionType.LOAD_IMAGES) {
            if (imageHandler.hasImageFolder()) {
                changeImages(null, true, gui::toggleOff);
            } else gui.toggleOff();
        }
    }

    @Override
    public void updateUI() {
        GuiUtils.updateFontSize(pathField, 1, Font.BOLD);
    }
}

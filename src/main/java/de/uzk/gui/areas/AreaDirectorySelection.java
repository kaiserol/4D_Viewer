package de.uzk.gui.areas;

import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.gui.Icons;
import de.uzk.image.Axis;
import de.uzk.image.ImageFileType;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static de.uzk.Main.*;
import static de.uzk.config.LanguageHandler.getWord;
import static de.uzk.utils.PathManager.USER_WORKING_DIRECTORY;

public class AreaDirectorySelection extends AreaContainerInteractive<JPanel> {
    // GUI-Elemente
    private JTextField txtFieldDirectory;
    private JButton clearImagesButton;

    // Button-Margin
    private final Insets buttonMargin = new Insets(5, 5, 5, 5);

    public AreaDirectorySelection(Gui gui) {
        super(new JPanel(), gui);
        init();
    }

    private void init() {
        this.container.setLayout(new BorderLayout(10, 0));

        // "Bilder entfernen"-Button hinzufügen
        this.clearImagesButton = new JButton(Icons.ICON_DELETE);
        this.clearImagesButton.addActionListener(a -> clearImages());
        this.clearImagesButton.setMargin(buttonMargin);
        GuiUtils.setToolTipText(this.clearImagesButton, getWord("tooltip.clearImages"));

        // ButtonBar hinzufügen (Verändert das Aussehen der Buttons)
        JToolBar buttonBar = new JToolBar();
        buttonBar.setFloatable(false);
        buttonBar.setRollover(true);
        buttonBar.add(this.clearImagesButton);
        this.container.add(buttonBar, BorderLayout.WEST);

        // "Verzeichnis Pfad"-Textfeld hinzufügen
        this.txtFieldDirectory = new JTextField();
        this.txtFieldDirectory.setEditable(false);
        this.txtFieldDirectory.putClientProperty("JTextField.placeholderText", getWord("placeholder.imageDirectory"));
        this.container.add(this.txtFieldDirectory, BorderLayout.CENTER);

        // "Verzeichnis Auswählen"-Button hinzufügen
        JButton btnChooseDirectory = new JButton(getWord("button.chooseDirectory"));
        btnChooseDirectory.addActionListener(a -> openFileChooser());
        this.container.add(btnChooseDirectory, BorderLayout.EAST);

        // Fokus setzen, nachdem die UI aufgebaut ist
        SwingUtilities.invokeLater(btnChooseDirectory::requestFocusInWindow);
    }

    private void clearImages() {
        int option = JOptionPane.showConfirmDialog(gui.getContainer(),
                getWord("optionPane.directory.clear"),
                getWord("optionPane.title.confirm"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (option == JOptionPane.YES_OPTION) {
            logger.info("Clear images...");
            gui.toggleOff();
        } else this.container.requestFocusInWindow();
    }

    // ========================================
    // Dateiauswahl Methoden
    // ========================================
    private void openFileChooser() {
        JFileChooser fileChooser = getFileChooser();

        // Startverzeichnis wählen
        if (history.isEmpty()) {
            Path startDirectory = USER_WORKING_DIRECTORY;
            if (Files.isDirectory(startDirectory)) fileChooser.setCurrentDirectory(startDirectory.toFile());
        } else {
            fileChooser.setSelectedFile(history.getLast().toFile());
        }

        // Dialog öffnen
        int option = fileChooser.showOpenDialog(gui.getContainer());
        if (option == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile == null) return;

            // Image-Files laden
            ImageFileType imageFileType = getSelectedImageFileType((FileNameExtensionFilter) fileChooser.getFileFilter());
            gui.loadImageFiles(Path.of(selectedFile.getAbsolutePath()), imageFileType, false);
        }
    }

    private JFileChooser getFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.putClientProperty("FileChooser.readOnly", Boolean.TRUE);
        resetButtonsMarginRecursively(fileChooser);

        // Dialogtitel setzen
        setFileChooserTitel(fileChooser);

        // Filter setzen
        setFileChooserFilter(fileChooser);

        // Inhalt hinzufügen (Zeit-Trenner, Ebenen-Trenner)
        addFileChooserContent(fileChooser);
        return fileChooser;
    }

    private void resetButtonsMarginRecursively(Component comp) {
        if (comp instanceof AbstractButton button) {
            // Nur Buttons ohne Text (also IconButtons) zurücksetzen
            if (button.getText() == null || button.getText().isEmpty()) button.setMargin(buttonMargin);
        }

        if (comp instanceof Container innerContainer) {
            for (Component child : GuiUtils.getComponents(innerContainer)) {
                resetButtonsMarginRecursively(child);
            }
        }
    }

    private void setFileChooserTitel(JFileChooser fileChooser) {
        String dialogTitle = String.format("%s", getWord("button.chooseDirectory"));
        fileChooser.setDialogTitle(dialogTitle);

        // Dialogtitel dynamisch setzen, wenn der Filter geändert wird
        fileChooser.addPropertyChangeListener(evt -> {
            if (evt.getNewValue() instanceof FileNameExtensionFilter filter) {
                ImageFileType imageFileType = getSelectedImageFileType(filter);
                String newTitle = String.format("%s (%s)", getWord("button.chooseDirectory"), imageFileType.getDescription());
                fileChooser.setDialogTitle(newTitle);
            }
        });
    }

    private void setFileChooserFilter(JFileChooser fileChooser) {
        fileChooser.setAcceptAllFileFilterUsed(false);

        for (ImageFileType type : ImageFileType.sortedValues()) {
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    type.getFullDescription(),
                    type.getExtensions()
            );
            fileChooser.addChoosableFileFilter(filter);

            // Standardfilter auswählen
            if (type == workspace.getConfig().getImageFileType()) {
                fileChooser.setFileFilter(filter);
            }
        }
    }

    private void addFileChooserContent(JFileChooser fileChooser) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 10, 0, 0),
                BorderFactory.createLineBorder(GuiUtils.getBorderColor())
        ));

        // contentPanel
        JPanel contentPanel = new JPanel();
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        contentPanel.setBackground(GuiUtils.getBackgroundColor());

        // contentPanel hinzufügen
        panel.add(contentPanel, BorderLayout.CENTER);
        fileChooser.setAccessory(panel);
    }

    private ImageFileType getSelectedImageFileType(FileNameExtensionFilter filter) {
        if (filter == null) return ImageFileType.getDefault();

        String firstExtension = filter.getExtensions()[0];
        return ImageFileType.fromExtension(firstExtension);
    }

    // ========================================
    // Observer Methoden
    // ========================================
    @Override
    public void toggleOn() {
        this.clearImagesButton.setEnabled(true);
        updateDirectoryText();
    }

    @Override
    public void toggleOff() {
        this.clearImagesButton.setEnabled(false);
        updateDirectoryText();
    }

    @Override
    public void update(Axis axis) {
        updateDirectoryText();
    }

    // ========================================
    // Hilfsmethoden
    // ========================================
    private void updateDirectoryText() {
        if (workspace.isOpen()) {
            Path path = workspace.getImageFilesDirectory().resolve(workspace.getImageFile().getFileName());
            this.txtFieldDirectory.setText(path.toString());
        } else {
            this.txtFieldDirectory.setText(null);
        }
        this.txtFieldDirectory.setCaretPosition(0);
    }
}

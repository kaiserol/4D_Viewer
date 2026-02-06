package de.uzk.gui;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

import static de.uzk.config.LanguageHandler.getWord;

public class ScreenshotDirectorySelector extends JPanel {
    private Path currentDirectory;
    private final JFileChooser fileChooser;
    private final java.util.List<Consumer<Path>> handlers = new ArrayList<>();

    public ScreenshotDirectorySelector(Path screenshotDirectory) {
        this.fileChooser = new JFileChooser();
        this.fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        this.fileChooser.setCurrentDirectory(screenshotDirectory.toFile());
        this.currentDirectory = screenshotDirectory;
        init();
    }

    private void init() {
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(Integer.MAX_VALUE, getPreferredSize().height));
        JTextField textField = new JTextField();
        textField.setEditable(false);
        textField.setText(currentDirectory.toString());
        add(textField, BorderLayout.CENTER);

        JButton openButton = new JButton(getWord("dialog.settings.screenshotDirectory.change"));
        openButton.addActionListener(e -> openDialog());
        add(openButton, BorderLayout.EAST);
    }

    public void addChangeListener(Consumer<Path> handler) {
        handlers.add(handler);
    }

    public Path getScreenshotDirectory() {
        return currentDirectory;
    }

    private void openDialog() {
       this.fileChooser.showOpenDialog(null);
       if(fileChooser.getSelectedFile() != null && !Objects.equals(currentDirectory, fileChooser.getSelectedFile().toPath())) {
           currentDirectory = fileChooser.getSelectedFile().toPath();
           handlers.forEach(handler -> handler.accept(currentDirectory));
       }
    }

}

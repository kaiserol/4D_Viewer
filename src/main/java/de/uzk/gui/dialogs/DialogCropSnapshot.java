package de.uzk.gui.dialogs;

import de.uzk.gui.SnapshotCropper;
import de.uzk.utils.ColorUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;

import static de.uzk.config.LanguageHandler.getWord;

public class DialogCropSnapshot extends JDialog {
    private final SnapshotCropper snapshotCropper;
    private boolean confirmed = false;

    public DialogCropSnapshot(BufferedImage image) {
        super(null, getWord("dialog.snapshot"), Dialog.ModalityType.DOCUMENT_MODAL);
        snapshotCropper = new SnapshotCropper(image);
        init();
    }

    private void init() {
        confirmed = false;
        getContentPane().setLayout(new BorderLayout(0, 0));
        getContentPane().add(snapshotCropper, BorderLayout.CENTER);
        getContentPane().add(createButtonsPanel(), BorderLayout.EAST);
        setMaximumSize(snapshotCropper.getPreferredSize());
        setLocationRelativeTo(null);
        setResizable(false);
        pack();
    }

    public Optional<BufferedImage> getCroppedImage() {
        if(!confirmed) return Optional.empty();
        return Optional.of(snapshotCropper.getCroppedImage());
    }


    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

        JButton okButton = new JButton(getWord("dialog.snapshot.save"));
        okButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        okButton.setBackground(ColorUtils.COLOR_BLUE);

        JButton cancelButton = new JButton(getWord("dialog.snapshot.cancel"));
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        cancelButton.setBackground(ColorUtils.COLOR_RED);

        okButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, okButton.getPreferredSize().height));
        cancelButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, cancelButton.getPreferredSize().height));


        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        return buttonsPanel;
    }
}

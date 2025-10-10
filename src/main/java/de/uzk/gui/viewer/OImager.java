package de.uzk.gui.viewer;

import de.uzk.actions.ActionType;
import de.uzk.gui.AreaContainerInteractive;
import de.uzk.gui.Gui;
import de.uzk.gui.GuiUtils;
import de.uzk.gui.Icons;
import de.uzk.image.ImageLayer;
import de.uzk.markers.MarkerMapping;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import static de.uzk.Main.*;
import static de.uzk.config.LanguageHandler.getWord;

public class OImager extends AreaContainerInteractive<JPanel> {
    private BufferedImage originalImage;
    private BufferedImage currentImage;

    public OImager(Gui gui) {
        super(null, gui);
        this.setContainer(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintImage(g);
            }
        });
    }

    private void paintImage(Graphics g) {
        Graphics2D g2d = GuiUtils.createHighQualityGraphics2D(g);

        if (this.currentImage != null) {
            double scaleFactor = GuiUtils.getImageScaleFactor(this.container, this.currentImage);

            int width = (int) (this.currentImage.getWidth() * scaleFactor);
            int height = (int) (this.currentImage.getHeight() * scaleFactor);
            int x = (this.container.getWidth() - width) / 2;
            int y = (this.container.getHeight() - height) / 2;

            g2d.drawImage(currentImage, x, y, width, height, null);

            List<MarkerMapping> marker = markerHandler.getMarkers(imageHandler.getTime());
            for(MarkerMapping m : marker) {
                m.getMarker().draw(g2d, new Rectangle(x, y, width, height), scaleFactor);
            }
        } else {
            String noImagesText = imageHandler.isEmpty() ? getWord("viewer.labels.noImages") : getWord("viewer.labels.couldNotLoadImage");
            FontMetrics metrics = g2d.getFontMetrics(g.getFont());
            int textWidth = metrics.stringWidth(noImagesText);
            int textHeight = metrics.getHeight();
            int x = (this.container.getWidth() - textWidth) / 2;
            int y = (this.container.getHeight() + textHeight) / 2;
            g2d.drawString(noImagesText, x, y);
        }
    }

    @Override
    public void handleAction(ActionType actionType) {
        if (actionType == ActionType.EDIT_IMAGE) {
            editImage();
        } else if (actionType == ActionType.TAKE_SCREENSHOT) {
            takeScreenshot();
        }
    }

    @Override
    public void toggleOn() {
        updateCurrentImage();
    }

    @Override
    public void toggleOff() {
        updateCurrentImage();
    }

    @Override
    public void update(ImageLayer layer) {
        updateCurrentImage();
    }

    @Override
    public void appGainedFocus() {
        checkImages();
    }

    private void updateCurrentImage() {
        BufferedImage tempImage = null;
        if (imageHandler.getCurrentImage() != null) {
            File imageFile = imageHandler.getCurrentImage().getFile();
            tempImage = Icons.loadImage(imageFile);
        }
        this.currentImage = this.originalImage = tempImage;
        editImage();
    }

    private void editImage() {
        if (this.originalImage != null) {
            this.currentImage = GuiUtils.getEditedImage(this.originalImage, imageHandler.getImageDetails(), false);
        }
        this.container.repaint();
    }

    private void takeScreenshot() {
        String savePath = config.saveScreenshot(this.originalImage);
        if (savePath != null) gui.handleAction(ActionType.UPDATE_SCREENSHOT_COUNTER);
    }

    private void checkImages() {
        if (imageHandler.isEmpty()) return;
        if (imageHandler.getCurrentImage() != null) {
            // reopen image to look if image was deleted
            updateCurrentImage();
        }
        imageHandler.checkLostImages();
    }
}

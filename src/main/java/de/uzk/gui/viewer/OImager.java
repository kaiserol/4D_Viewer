package de.uzk.gui.viewer;

import de.uzk.actions.ActionType;
import de.uzk.actions.ActionTypeListener;
import de.uzk.gui.*;
import de.uzk.image.ImageLayer;
import de.uzk.gui.GuiUtils;
import de.uzk.gui.Icons;
import de.uzk.markers.Marker;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static de.uzk.Main.*;
import static de.uzk.config.LanguageHandler.getWord;

public class OImager extends InteractiveContainer<JPanel> implements ActionTypeListener, WindowFocusListener {
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
        gui.addActionTypeListener(this);
        gui.addWindowFocusListener(this);

    }

    private void paintImage(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        if (this.currentImage != null) {
            double scaleFactor = GuiUtils.getImageScaleFactor(this.container, this.currentImage);

            int width = (int)(this.currentImage.getWidth() * scaleFactor);
            int height = (int)(this.currentImage.getHeight() * scaleFactor);
            int x = (this.container.getWidth() - width) / 2;
            int y = (this.container.getHeight() - height) / 2;

            g2d.drawImage(currentImage, x, y, width, height, null);

            Marker marker = markerHandler.getMarker(imageHandler.getTime());
            if(marker != null) {
                marker.draw(g2d, new Rectangle(x, y, width, height), scaleFactor);
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

    @Override
    public void handleAction(ActionType actionType) {
        if (actionType == ActionType.EDIT_IMAGE) {
            editImage();
        } else if (actionType == ActionType.TAKE_SCREENSHOT) {
            takeScreenshot();
        }
    }

    private void takeScreenshot() {
        String savePath = config.saveScreenshot(this.originalImage);
        if (savePath != null) {
            JOptionPane.showMessageDialog(null,
                    "<html>" + getWord("optionPane.screenshotTakenMsg") + "<br>" +
                            getWord("file.path") + ": " + savePath+ "</html>",
                    getWord("optionPane.titles.info"), JOptionPane.INFORMATION_MESSAGE);
            gui.handleAction(ActionType.UPDATE_SCREENSHOT_COUNTER);
        }
    }

    @Override
    public void gainedWindowFocus() {
        checkImages();
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

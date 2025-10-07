package de.uzk.gui.viewer;

import de.uzk.gui.Gui;
import de.uzk.gui.InteractiveContainer;
import de.uzk.image.ImageFile;
import de.uzk.image.ImageLayer;
import de.uzk.logger.OLogInfo;
import de.uzk.gui.GuiUtils;
import de.uzk.gui.IconUtils;
import de.uzk.utils.StringUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.Objects;

import static de.uzk.Main.*;

public class OInfo extends InteractiveContainer<JDialog> {

    public OInfo(Gui gui) {
        super(new JDialog(gui.getFrame(), "Information", true), gui);
        this.container.setLayout(new BorderLayout());
        this.container.setResizable(false);

        // init
        init();
        this.container.pack();
        validateWindowSize();

        this.container.setLocationRelativeTo(gui.getFrame());
        this.container.setVisible(true);
    }

    private void validateWindowSize() {
        int minWidth = 300;
        int minHeight = 250;
        int maxWidth = 500;
        int maxHeight = 300;
        this.container.setMinimumSize(new Dimension(minWidth, minHeight));
        this.container.setMaximumSize(new Dimension(maxWidth, maxHeight));

        int width = this.container.getWidth();
        int height = this.container.getHeight();

        int newHeight = height > maxHeight ? maxHeight : Math.max(height, minHeight);
        int scrollBarWidth = UIManager.getInt("ScrollBar.width");
        int curWidth = width + (newHeight == maxHeight ? scrollBarWidth : 0);
        int newWidth = curWidth > maxWidth ? maxWidth : Math.max(curWidth, minWidth);

        this.container.setPreferredSize(new Dimension(newWidth, newHeight));
        this.container.pack();
    }

    private void init() {
        // infoPanel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        // tabs
        JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.add("Logs", getLogs());

        if (!imageHandler.isEmpty()) {
            if (imageHandler.getMissingImagesCount() > 0) {
                String missingImages = StringUtils.javaToHTML(imageHandler.getAllMissingImages());
                tabs.add("Missing Images", getEditorPane(missingImages));
            }
            infoPanel.add(getInfoPanel(), BorderLayout.SOUTH);
        }
        infoPanel.add(tabs, BorderLayout.CENTER);
        this.container.add(infoPanel);
    }

    private JComponent getEditorPane(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JEditorPane editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        editorPane.setMargin(new Insets(5, 5, 5, 5));

        Font font = new Font("Consolas", Font.PLAIN, config.getFontSize() - 1);
        editorPane.setFont(font);
        String htmlContent = "<html><head><style>" +
                "body { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; }" +
                "pre { font-family: " + font.getFamily() + "; font-size: " + font.getSize() + "pt; }" +
                "</style></head><body>"
                + text + "</body></html>";
        editorPane.setText(htmlContent);

        JScrollPane scrollPane = new JScrollPane(editorPane);
        panel.add(scrollPane);

        return panel;
    }

    private JComponent getLogs() {
        StringBuilder htmlBuilder = new StringBuilder();
        for (OLogInfo info : logger.getLogs()) {
            String newInfo = StringUtils.javaToHTML(StringUtils.toHTML(info));
            htmlBuilder.append(newInfo);
        }
        return getEditorPane(htmlBuilder.toString());
    }

    private JPanel getInfoPanel() {
        JPanel infoBar = new JPanel();
        infoBar.setLayout(new BorderLayout());
        infoBar.setBorder(new CompoundBorder(new EmptyBorder(10, 0, 0, 0),
                new CompoundBorder(new MatteBorder(new Insets(2, 0, 0, 0), GuiUtils.getBorderColor()),
                        new EmptyBorder(10, 0, 0, 0))));
        infoBar.add(getInfoBar(ImageLayer.TIME), BorderLayout.NORTH);
        infoBar.add(getInfoBar(ImageLayer.LEVEL), BorderLayout.SOUTH);

        return infoBar;
    }

    private JToolBar getInfoBar(ImageLayer layer) {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setLayout(new GridBagLayout());
        toolBar.setBorder(new EmptyBorder(0, 0, 0, 0));

        // GridBagConstraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = -1;
        gbc.gridy = 0;
        gbc.weighty = 1;

        // images
        String[] images = getPrevCurrentNext(layer);
        boolean isTime = (layer == ImageLayer.TIME);
        String text = isTime ? "Image" : "Level";

        // gbc
        updateGPC(gbc, 1);

        JLabel prevImageLabel = new JLabel(images[0]);
        GuiUtils.updateFontSize(prevImageLabel, -2, Font.BOLD);
        toolBar.add(prevImageLabel, gbc);

        // gbc
        updateGPC(gbc, 0);

        JButton prevButton = new JButton(IconUtils.PREV_IMAGE_ICON);
        prevButton.setToolTipText("Previous " + text);
        toolBar.add(prevButton, gbc);

        // gbc
        updateGPC(gbc, 1);

        JLabel currentImageLabel = new JLabel(images[1]);
        GuiUtils.updateFontSize(currentImageLabel, -2, Font.BOLD);
        toolBar.add(currentImageLabel, gbc);

        // gbc
        updateGPC(gbc, 0);

        JButton nextButton = new JButton(IconUtils.NEXT_IMAGE_ICON);
        nextButton.setToolTipText("Next " + text);
        toolBar.add(nextButton, gbc);

        // gbc
        updateGPC(gbc, 1);

        JLabel nextImageLabel = new JLabel(images[2]);
        GuiUtils.updateFontSize(nextImageLabel, -2, Font.BOLD);
        toolBar.add(nextImageLabel, gbc);

        return toolBar;
    }

    private void updateGPC(GridBagConstraints gbc, int value) {
        gbc.gridx++;
        gbc.weightx = value;
    }

    private String[] getPrevCurrentNext(ImageLayer layer) {
        logger.close();
        String current = colorizeString(imageHandler.getCurrentImage(), layer);
        int time = imageHandler.getTime();
        int level = imageHandler.getLevel();

        imageHandler.prev(layer);
        String prev = colorizeString(imageHandler.getCurrentImage(), layer);
        if (imageHandler.getTime() != time || imageHandler.getLevel() != level) {
            imageHandler.next(layer);
        }

        imageHandler.next(layer);
        String next = colorizeString(imageHandler.getCurrentImage(), layer);
        if (imageHandler.getTime() != time || imageHandler.getLevel() != level) {
            imageHandler.prev(layer);
        }

        logger.open();
        String emptyString = "/";
        return new String[]{Objects.equals(prev, current) ? emptyString : prev, current,
                Objects.equals(current, next) ? emptyString : next};
    }

    private String colorizeString(ImageFile imageFile, ImageLayer layer) {
        String imageName = imageFile.getFileName();
        int indexFirstSep = imageName.indexOf((layer == ImageLayer.TIME) ?
                imageHandler.getImageDetails().getSepTime() :
                imageHandler.getImageDetails().getSepLevel());

        int indexSecSep = imageName.indexOf((layer == ImageLayer.TIME) ?
                imageHandler.getImageDetails().getSepLevel() :
                '.' + imageFile.getFileExtension());

        String color = (layer == ImageLayer.TIME) ? "#007aff" : "#ff5656";
        String text = imageName.substring(0, indexFirstSep + 1) +
                "<font color=" + color + ">" + imageName.substring(indexFirstSep + 1, indexSecSep) + "</font>" +
                imageName.substring(indexSecSep);

        return "<html>" + text + "</html>";
    }
}

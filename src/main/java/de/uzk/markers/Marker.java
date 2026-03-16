package de.uzk.markers;

import com.fasterxml.jackson.annotation.*;
import de.uzk.markers.interactions.MarkerModificator;
import de.uzk.utils.ColorUtils;
import de.uzk.utils.GraphicsUtils;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes(value = {@JsonSubTypes.Type(value = ShapeMarker.class), @JsonSubTypes.Type(value = ArrowMarker.class)})
public abstract class Marker {
    public static final int LINE_WIDTH = 5;
    // Zeitraum, in dem der Marker sichtbar sein soll
    protected int timeStart;
    protected int timeEnd;
    // Ebenen, auf denen der Marker sichtbar sein soll
    protected int levelStart;
    protected int levelEnd;
    // Aussehen
    protected Color color;
    protected String label;

    public int getTimeStart() {
        return timeStart;
    }

    @JsonSetter("from")
    public void setTimeStart(int timeStart) {
        this.timeStart = timeStart;
    }

    public int getTimeEnd() {
        return timeEnd;
    }

    @JsonSetter("to")
    public void setTimeEnd(int timeEnd) {
        this.timeEnd = timeEnd;
    }

    public int getLevelStart() {
        return levelStart;
    }

    public void setLevelStart(int timeStart) {
        this.timeStart = timeStart;
    }

    public int getLevelEnd() {
        return levelEnd;
    }

    public void setLevelEnd(int levelEnd) {
        this.levelEnd = levelEnd;
    }

    public boolean shouldRender(int t, int y) {
        return timeStart <= t && timeEnd >= t && levelStart <= y && levelEnd >= y;
    }

    @JsonIgnore
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @JsonGetter("color")
    private String getHexColor() {
        return ColorUtils.colorToHex(color);
    }

    @JsonSetter("color")
    private void setHexColor(String color) {
        setColor(Color.decode(color));
    }

    public String getLabel() {
        return label;
    }

    @JsonSetter("label")
    public void setLabel(String label) {
        this.label = label;
    }


    public abstract void draw(Graphics2D g2d);

    public void drawDragPoints(Graphics2D g2d) {
        g2d = (Graphics2D) g2d.create();
        g2d.setColor(Color.WHITE);

        Point2D[] scalePoints = getScalePoints();
        for (Point2D point : scalePoints) {
            Shape c = new Ellipse2D.Double(point.getX() - (float) LINE_WIDTH, point.getY() - (float) LINE_WIDTH, 2.0 * LINE_WIDTH, 2.0 * LINE_WIDTH);
            g2d.fill(c);
        }
    }

    protected void drawName(Graphics2D g2d) {
        g2d = (Graphics2D) g2d.create();
        Shape labelArea = getLabelArea(g2d);
        g2d.fill(labelArea);
        boolean lightColor = ColorUtils.calculatePerceivedBrightness(color) > 0.5;
        g2d.setColor(lightColor ? Color.BLACK : Color.WHITE);

        FontMetrics metrics = GraphicsUtils.updateMetrics(g2d);
        g2d.drawString(label, labelArea.getBounds().x, labelArea.getBounds().y + metrics.getAscent());
    }

    /**
     * @return Die Punkte, die dem Nutzer zur Bearbeitung hervorgehoben werden sollen.
     *
     */
    @JsonIgnore
    public abstract Point2D[] getScalePoints();

    public abstract Shape getLabelArea(Graphics g2d);

    public abstract Marker copy();

    @JsonIgnore
    public abstract MarkerModificator getSuitableModificator();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Marker that = (Marker) o;
        return timeStart == that.timeStart && timeEnd == that.timeEnd && color.equals(that.color) && label.equals(that.label);
    }
}

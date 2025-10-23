package de.uzk.markers;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import de.uzk.utils.NumberUtils;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.util.Arrays;

public class Marker {
    private static final int LINE_WIDTH = 5;

    private int x;
    private int y;
    private int width;
    private int height;
    private MarkerShape shape;
    @JsonAdapter(ColorAdapter.class)
    private Color color;
    private String label;

    public Marker() {
        this(0,0,0,0, MarkerShape.RECTANGLE, Color.RED, "Marker");
    }

    public Marker(Marker other) {
        this(
                other.x,
                other.y,
                other.width,
                other.height,
                other.shape,
                other.color,
                other.label
        );
    }

    public Marker(int x, int y, int width, int height, MarkerShape shape, Color color, String label) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.shape = shape;
        this.color = color;
        this.label = label;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public MarkerShape getShape() {
        return shape;
    }

    public void setShape(MarkerShape shape) {
        this.shape = shape;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public  void draw(Graphics2D to, Rectangle imageArea, double scaleFactor) {
        Rectangle actualBounds = this.getActualBounds(imageArea, scaleFactor);
        Shape finalShape = switch(this.shape) {
            case RECTANGLE -> actualBounds;
            case ELLIPSE -> new Ellipse2D.Float(actualBounds.x, actualBounds.y, actualBounds.width, actualBounds.height);

        };



        Color prevColor = to.getColor();
        Stroke prevStroke = to.getStroke();

        to.setColor(this.color);
        to.setStroke(new BasicStroke(LINE_WIDTH * (float)scaleFactor));
        to.draw(finalShape);
        this.drawName(to, actualBounds.x, actualBounds.y);

        to.setColor(prevColor);
        to.setStroke(prevStroke);
    }

    private void drawName(Graphics2D to, int x, int y) {
        FontMetrics metrics = to.getFontMetrics();
        int width = metrics.stringWidth(this.label);
        int height = metrics.getHeight();
        to.fillRect(x,y - metrics.getAscent()  ,width,height);

        double brightness = NumberUtils.calculatePerceivedBrightness(this.color);
        if (brightness > 186) {
           to.setColor(Color.BLACK);
        } else {
            to.setColor(Color.WHITE);
        }

        to.drawString(this.label, x, y);
    }


    private Rectangle getActualBounds(Rectangle imageBounds, double scale) {
        int x = imageBounds.x + (int)(this.x * scale);
        int y = imageBounds.y + (int)(this.y * scale);

        int width = (int)(this.width * scale);
        int height = (int)(this.height * scale);

        return new Rectangle(x, y, width,height);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    private static class ColorAdapter extends TypeAdapter<Color> {

        @Override
        public void write(JsonWriter jsonWriter, Color color) throws IOException {
            float[] components = color.getRGBComponents(null /* Array wird von der Methode erstellt*/);
            StringBuilder builder = new StringBuilder("#");
            for (float component : components) {
                int c = (int)(component*255);
                if(c != 0) {
                    builder.append(Integer.toHexString(c));
                } else {
                    // toHexString würde 0 einstellig darstellen
                    builder.append("00");
                }
            }
            jsonWriter.value(builder.toString());
        }

        @Override
        public Color read(JsonReader jsonReader) throws IOException {
            String color = jsonReader.nextString();
            if(color.charAt(0) == '#') {
                int[] components = new int[3];
                for(int i = 1; i < 7; i+=2) {
                    components[i / 2] =  Integer.parseInt(color.substring(i,i+2), 16);
                }
                return new Color(components[0], components[1], components[2]);
            }
            return null;
        }
    }
}

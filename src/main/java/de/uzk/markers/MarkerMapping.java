package de.uzk.markers;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.uzk.utils.NumberUtils;

public class MarkerMapping {
    private Marker marker;
    private int from;
    private int to;

    @JsonCreator
    public MarkerMapping(
            @JsonProperty("marker") Marker marker, @JsonProperty("from") int from, @JsonProperty("to") int to) {
        this.marker = marker;
        this.from = from;
        this.to = to;
    }

    public MarkerMapping(Marker marker, int on) {
        this(marker, on, on);
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public void setRange(int from, int to) {
        this.setFrom(from);
        this.setTo(to);
    }

    public boolean shouldRender(int imageTime) {
        return NumberUtils.valueInRange(imageTime, this.from, this.to);
    }

    public Marker getMarker() {
        return this.marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MarkerMapping other) {
            return this.marker.equals(other.marker) && this.from == other.from && this.to == other.to;
        }
        return false;
    }
}

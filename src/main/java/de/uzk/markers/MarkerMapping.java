package de.uzk.markers;


public class MarkerMapping {
    private Marker marker;
    private int from;
    private int to;


    public MarkerMapping(Marker marker, int from, int to) {
        this.marker = marker;
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
        return imageTime >= this.from && imageTime <= this.to;
    }

    public Marker getMarker() {
        return this.marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}

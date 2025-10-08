package de.uzk.markers;

import java.util.HashMap;

import static de.uzk.Main.imageHandler;

public class MarkerHandler {
    private Marker[] markers;

    public MarkerHandler() {
        this.markers = new Marker[0];
    }

    public boolean hasMarker(int time) {
        return time < this.markers.length && this.markers[time] != null;
    }

    public Marker getMarker(int time) {
        if(!this.hasMarker(time)) {
            return null;
        }
        return this.markers[time];
    }

    public void addMarker(Marker marker) {
        int currentTime = imageHandler.getTime();
        this.markers[currentTime] = marker;
    }

    public void resetImageCount(int count) {
        this.markers = new Marker[count];
    }
}

package de.uzk.markers;


import java.util.ArrayList;
import java.util.List;

public class MarkerHandler {
    private final List<MarkerMapping> markers;

    public MarkerHandler() {
        this.markers = new ArrayList<>();
    }


    public List<MarkerMapping> getMarkers(int time) {
        return this.markers.stream().filter(m -> m.shouldRender(time)).toList();
    }

    public List<MarkerMapping> getMarkers() {
        return this.markers;
    }


    public void addMarker(Marker marker, int image) {
        this.addMarker(marker, image, image);
    }

    public void addMarker(Marker marker, int from, int to) {

        this.markers.add(new MarkerMapping(marker, from, to));
    }

}

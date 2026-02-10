package de.uzk.edit.markers;

import de.uzk.markers.Marker;

public class RenameMarkerEdit extends MarkerEdit {
    public RenameMarkerEdit(Marker marker, String newName) {
       super(marker, markerWithName(marker, newName));
    }

    private static Marker markerWithName(Marker marker, String newName) {
        Marker copy = marker.copy();
        copy.setLabel(newName);
        return copy;
    }
}

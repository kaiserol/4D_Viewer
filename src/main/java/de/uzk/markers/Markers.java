package de.uzk.markers;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.uzk.io.PathManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Globales (per-Workspace) Objekt, das alle Marker für alle Bilder im Projekt speichert.
 * Außerdem zur Zentralisierung der Markerserialisierung verwendet.
 *
 */
public class Markers {
    // Markierungen
    //TODO Bei Deserialisierung einzelne Ungültige Marker überspringen statt alle zu löschen.
    @JsonProperty("markers")
    private final List<Marker> markers;

    public Markers() {
        this.markers = new ArrayList<>();
    }

    public static Markers load() {
        Path filePath = PathManager.resolveProjectPath(PathManager.MARKERS_FILE_NAME);

        Object object = PathManager.load(filePath, Markers.class);
        if (object instanceof Markers markers) {
            markers.markers.removeIf(Objects::isNull);
            return markers;
        } else return new Markers();
    }

    public List<Marker> getAllMarkers() {
        return this.markers;
    }

    public void addMarker(Marker marker) {
        this.markers.add(marker);
    }

    public void save() {
        Path filePath = PathManager.resolveProjectPath(PathManager.MARKERS_FILE_NAME);
        PathManager.save(filePath, this);
    }

    public void remove(Marker marker) {
        this.markers.remove(marker);
    }

    public void replace(Marker oldMarker, Marker newMarker) {
        this.markers.remove(oldMarker);
        this.markers.add(newMarker);
    }

    /**
     * @return Alle Marker, die zum gegebenen Zeitpunkt zu sehen sind.
     *
     */
    public List<Marker> getMarkersForImage(int time) {
        return this.markers.stream().filter(m -> m.shouldRender(time)).toList();
    }

}

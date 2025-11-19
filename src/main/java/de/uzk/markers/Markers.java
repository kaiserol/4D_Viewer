package de.uzk.markers;

import com.fasterxml.jackson.annotation.JsonGetter;
import de.uzk.io.PathManager;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Markers {
    @JsonGetter("markers")
    public List<Marker> getAllMarkers() {
        return this.markers;
    }

    public void addMarker(Marker marker) {
        this.markers.add(marker);
    }

    // Markierungen
    @JsonDeserialize(contentUsing = NullInvalidMarkers.class)
    private final List<Marker> markers;

    private Markers(Marker[] markers) {
        this.markers = List.of(markers);
    }

    public Markers() {
        this.markers = new ArrayList<>();
    }

    public void save() {
        Path filePath = PathManager.resolveProjectPath(PathManager.MARKERS_FILE_NAME);
        PathManager.save(filePath, this);
    }

    public static Markers load() {
        Path filePath = PathManager.resolveProjectPath(PathManager.MARKERS_FILE_NAME);

        Object object = PathManager.load(filePath, Markers.class);
        if (object instanceof Markers markers) {
            markers.markers.removeIf(Objects::isNull);
            return markers;
        } else return new Markers();
    }

    public void remove(Marker marker) {

    }

    public List<Marker> getMarkersForImage(int image) {
        return this.markers.stream().filter(m -> m.shouldRender(image)).toList();
    }

    // Helferklasse, die ungültige Marker zu nulls macht, die herausgefiltert werden können
    private static class NullInvalidMarkers extends StdDeserializer<Marker> {
        public NullInvalidMarkers() {
            super(Marker.class);
        }

        @Override
        public Marker deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            try {
                return p.readValueAs(Marker.class);
            } catch (Exception e) {
                return null;
            }
        }
    }
}

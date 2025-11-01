package de.uzk.markers;

import com.fasterxml.jackson.annotation.JsonGetter;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static de.uzk.utils.AppPath.*;

public class Markers {
    @JsonGetter("markers")
    public List<MarkerMapping> getAllMarkers() {
        return this.markers;
    }

    public void addMarker(Marker marker, int image) {
        this.markers.add(new MarkerMapping(marker, image, image));
    }

    // Markierungen
    @JsonDeserialize(contentUsing = NullInvalidMarkers.class)
    private final List<MarkerMapping> markers;

    private Markers(MarkerMapping[] markers) {
        this.markers = List.of(markers);
    }

    public Markers() {
        this.markers = new ArrayList<>();
    }

    public void save() {
        Path jsonPath = getAppProjectPath(Path.of(MARKERS_FILE_NAME));
        saveJson(jsonPath, this);
    }

    public static Markers load() {
        Path jsonPath = getAppProjectPath(Path.of(MARKERS_FILE_NAME));

        Object obj = loadJson(jsonPath, Markers.class);
        if (obj instanceof Markers markers) {
            markers.markers.removeIf(m -> m == null || m.getMarker() == null);
            return markers;
        } else return new Markers();
    }

    //Helferklasse, die ungültige Marker zu nulls macht, die herausgefiltert werden können
    private static class NullInvalidMarkers extends StdDeserializer<MarkerMapping> {
        public NullInvalidMarkers() {
            super(MarkerMapping.class);
        }

        @Override
        public MarkerMapping deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            try {
                return p.readValueAs(MarkerMapping.class);
            } catch (Exception e) {
                return null;
            }
        }
    }
}

package de.uzk.markers;

import com.fasterxml.jackson.annotation.JsonGetter;
import de.uzk.utils.AppPath;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static de.uzk.Main.logger;

public class Markers {

    private static final Path MARKERS_FILE_NAME = Path.of("markers.json");

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

    public static Markers load(Path directoryName) {
        Path location = AppPath.VIEWER_HOME_DIRECTORY.resolve(directoryName).resolve(MARKERS_FILE_NAME);
        try {
            Markers markers = new ObjectMapper().readValue(location, Markers.class);
            markers.markers.removeIf(m -> m == null || m.getMarker() == null);
            return markers;
        } catch (JacksonException e) {
            logger.logException(e);
            return new Markers();
        }
    }

    public void save(Path directoryName) {
        Path location = AppPath.VIEWER_HOME_DIRECTORY.resolve(directoryName).resolve(MARKERS_FILE_NAME);
        try {
            if (!Files.exists(location)) {
                Files.createDirectories(location.getParent());
            }
            new ObjectMapper().writeValue(location, this);
        } catch (JacksonException | IOException e) {
            logger.logException(e);
        }
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

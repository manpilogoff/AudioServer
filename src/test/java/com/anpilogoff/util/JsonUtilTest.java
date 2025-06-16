package com.anpilogoff.util;

import com.anpilogoff.dao.Album;
import com.anpilogoff.dao.Track;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilTest {

    @Test
    void parseJson_validString_returnsJsonNode() {
        String json = "{\"id\":\"track1\",\"title\":\"Test Track\"}";
        JsonNode node = JsonUtil.parseJson(json);
        assertEquals("track1", node.get("id").asText());
        assertEquals("Test Track", node.get("title").asText());
    }

    @Test
    void extractTracks_arrayJson_returnsTracks() {
        String tracksJson = "[{\"id\":\"t1\",\"title\":\"T1\",\"duration\":111},{\"id\":\"t2\",\"title\":\"T2\",\"duration\":222}]";
        Album album = Album.builder().id("a1").title("Album1").build();

        JsonNode array = JsonUtil.parseJson(tracksJson);
        List<Track> tracks = JsonUtil.extractTracks(array, album);

        assertEquals(2, tracks.size());
        assertEquals("t1", tracks.get(0).getId());
        assertEquals(album, tracks.get(0).getAlbum());
        assertEquals(222, tracks.get(1).getDuration());
    }

    @Test
    void extractTrackUrl_validJson_returnsUrl() {
        String input = "{\"url\":\"https:\\/\\/test.com\\/file.flac\"}";
        String url = JsonUtil.extractTrackUrl(input);
        assertEquals("https://test.com/file.flac", url);
    }
}

package com.anpilogoff.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class JsonUtil {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static JsonNode parseJson(String str) {
        try {return MAPPER.readTree(str);}
        catch (JsonProcessingException e) { return null;}
    }

    public static List<String> extractTrackIds(JsonNode tracksString) {
        List<String> ids = new ArrayList<>();
        JsonNode tracksJson = tracksString.get("tracks").path("items");
        for (JsonNode track : tracksJson) {
            ids.add(track.asText());
        }
        return ids;
    }

    public static List<String> extractAlbumsIds(JsonNode albums) {
        List<String> ids = new ArrayList<>();
        JsonNode tracksJson = albums.get("albums").path("items");
        for (JsonNode track : tracksJson) {
            ids.add(track.asText());
        }
        return ids;
    }

}


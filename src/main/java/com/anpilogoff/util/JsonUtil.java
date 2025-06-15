package com.anpilogoff.util;

import com.anpilogoff.dao.Album;
import com.anpilogoff.dao.Track;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;

public class JsonUtil {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static JsonNode parseJson(String str) {
        try {
            return MAPPER.readTree(str);
        }
        catch (JsonProcessingException e) {
            throw new CompletionException(new RuntimeException("JSONfailed: " + e.getMessage()));}
    }

    public static List<Track> extractTracks(JsonNode tracksJsonArray, Album album) {
        List<Track> tracks = new ArrayList<>();
        if (tracksJsonArray.isArray()) {
            for (JsonNode trackNode : tracksJsonArray) {
                String trackId = trackNode.path("id").asText(null);
                String trackTitle = trackNode.path("title").asText("Untitled");
                int duration = trackNode.path("duration").asInt(0);
                Track track = Track.builder()
                        .id(trackId)
                        .title(trackTitle)
                        .album(album)
                        .duration(duration)
                        .s3_exists(false)
                        .build();

                tracks.add(track);
                System.out.println("         [+] Трек: " + trackTitle + " (" + duration + " сек)");
            }
        } else {
            System.out.println("         [!] У альбома нет треков.");
        }
        return tracks;
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


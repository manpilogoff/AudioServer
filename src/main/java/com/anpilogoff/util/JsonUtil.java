package com.anpilogoff.util;

import com.anpilogoff.database.entity.Album;
import com.anpilogoff.database.entity.Track;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class JsonUtil {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static JsonNode parseJson(String str) {
        try {
            return MAPPER.readTree(str);
        } catch (JsonProcessingException e) {
            throw new CompletionException(new RuntimeException("JSONfailed: " + e.getMessage()));
        }
    }

    public static List<Track> extractTracks(JsonNode tracksJsonArray, Album album) {
        List<Track> tracks = new ArrayList<>();
        if (tracksJsonArray.isArray()) {

            for (JsonNode trackNode : tracksJsonArray) {
                Track track = Track.builder()
                        .id(trackNode.path("id").asText(null))
                        .title(trackNode.path("title").asText("Untitled"))
                        .album(album)
                        .duration(trackNode.path("duration").asInt(0))
                        .s3Exists(false)
                        .build();

                tracks.add(track);
                log.info(("             [+] track extracted: ".concat( track.getId())));
            }
        } else {
            log.info("         [!] У альбома нет треков.\n");
            return null;
        }
        return tracks;
    }

    public static String extractTrackUrl(String textJson) {
        return parseJson(textJson).path("url").asText().replace("\\","");
    }


    public static String extractSearch(String textJson)  {
        JsonNode root = null;
        String tracksString = null;

        try {
            root = MAPPER.readTree(textJson);

            List<Map<String, Object>> filteredTracks = StreamSupport.stream(
                            root.path("tracks").path("items").spliterator(), false)
                    .map(track -> {
                        Map<String, Object> trackMap = new LinkedHashMap<>();
                        trackMap.put("id", track.path("id").asText());
                        trackMap.put("title", track.path("title").asText());

                        Map<String, Object> artist = new LinkedHashMap<>();
                        artist.put("id", track.path("performer").path("id").asText());
                        artist.put("name", track.path("performer").path("name").asText());
                        trackMap.put("artist", artist);

                        Map<String, Object> album = new LinkedHashMap<>();
                        album.put("title", track.path("album").path("title").asText());
                        album.put("qobuz_id", track.path("album").path("qobuz_id").asText());
                        album.put("tracks_count", track.path("album").path("tracks_count").asInt());
                        album.put("genre_id", track.path("album").path("genre").path("id").asText());
                        trackMap.put("album", album);

                        return trackMap;
                    }).collect(Collectors.toList());

            tracksString = MAPPER.writeValueAsString(filteredTracks);

        } catch (JsonProcessingException e) {
            log.error("Json parsing failed {}", e.getMessage(),e);
            return null;
        }
        return tracksString;
    }


    public static String toJson(Object obj) throws JsonProcessingException {
       return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

}


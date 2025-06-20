package com.anpilogoff.util;

import com.anpilogoff.database.entity.Album;
import com.anpilogoff.database.entity.Track;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;

public class JsonUtil {
    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);
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
            log.info(("         [<Album>] : ".concat( album.getTitle())));

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
            //кинуть исключение
        }
        return tracks;
    }

    public static String extractTrackUrl(String textJson) {
        return parseJson(textJson).path("url").asText().replace("\\","");
    }



}


package com.anpilogoff.service;

import com.anpilogoff.database.entity.Artist;
import com.anpilogoff.database.entity.Album;
import com.anpilogoff.util.ConfigUtil;
import com.anpilogoff.util.HttpUtil;
import com.anpilogoff.util.JsonUtil;
import com.anpilogoff.util.MD5Util;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import java.net.http.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class QobuzFetcher {
    private final HttpClient CLIENT = HttpClient.newHttpClient();
    private final String API_BASE_URL;
    private final String APP_ID;
    private final String AUTH_TOKEN;
    private final String SECRET;

    public QobuzFetcher() {
        log.info("Reading configuration from properties file...");
        Properties prop = ConfigUtil.loadConfig("env.properties");

        this.API_BASE_URL = prop.getProperty("API_BASE_URL");
        this.APP_ID = prop.getProperty("APP_ID");
        this.AUTH_TOKEN = prop.getProperty("AUTH_TOKEN");
        this.SECRET = prop.getProperty("SECRET");
    }

    /**
     * Async Artist data obtaining (inner Albums and Tracks).
     * @param artistId - artist id in "Qobuz" service
     * @return CompletableFuture<Artist> - Artist - Albums inside - Tracks inside
     */
    public CompletableFuture<Artist> fetchArtistWithDetailsAsync(String artistId) {

        // Artist data + albums data api URL
        String url = String.format("%s/artist/get?artist_id=%s&extra=albums&app_id=%s&user_auth_token=%s",
                API_BASE_URL, artistId, APP_ID, AUTH_TOKEN);
        log.info("Асинхронный запрос к Qobuz API: artistId=".concat( artistId));

        // Async fetch artist and albums data
        return sendAsync(url).thenCompose(artistJson -> {
            String name = artistJson.path("name").asText(null);

            if (name == null) {
                log.info("Error. Couldn't retrieve artist name.");
                throw new CompletionException(new IllegalStateException("Error artist name retrievement"));
            }

            Artist artist = Artist.builder().id(artistId).name(name).build();

            // Album data processing
            JsonNode albumsArrayJson = artistJson.path("albums").path("items");
            return fetchAlbumsAndTracksForArtist(albumsArrayJson, artist, API_BASE_URL, APP_ID, AUTH_TOKEN);
        });
    }


    // Each ID album async fetch -> object create -> tracks data extracting -> tracks object creating
    public CompletableFuture<Artist> fetchAlbumsAndTracksForArtist(
            JsonNode albumsArray, Artist artist, String apiBaseUrl, String appId, String authToken) {

        //"promise" list of obtained albums
        List<CompletableFuture<Album>> albumFutures = new ArrayList<>();

        for (JsonNode albumNode : albumsArray) {
            String albumId = albumNode.path("id").asText();
            albumFutures.add(fetchAlbumWithTracksAsync(albumId, artist, apiBaseUrl, appId, authToken));
        }
        // Wait till all async operations ends.
        return CompletableFuture.allOf(albumFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<Album> albums = new ArrayList<>();
                    // Get completed results of async requests
                    for (CompletableFuture<Album> future : albumFutures) {
                        try {
                            Album album = future.join();
                            albums.add(album);
                        } catch (CompletionException ex) {
                            throw new CompletionException("Error during album download", ex.getCause());
                        }
                    }
                    artist.setAlbums(albums);
                    System.out.println(artist);
                    log.info("Artist object completely built: ".concat(String.valueOf(artist.getAlbums().size())));
                    return artist;
                });
    }

    // Async fetching each album tracks info and building nested tracks objects -> return
    public CompletableFuture<Album> fetchAlbumWithTracksAsync(
            String albumId, Artist artist, String baseUrl, String appId, String authToken) {

        String albumUrl = String.format("%s/album/get?album_id=%s&app_id=%s&user_auth_token=%s",
                baseUrl, albumId, appId, authToken);

        // Fetch album + album tracks data...
        return sendAsync(albumUrl).thenApply(albumJson -> {
            String albumTitle = albumJson.path("title").asText();
            JsonNode tracksArray = albumJson.path("tracks").path("items");
            String genreId = albumJson.path("genre").path("id").asText(null);
            String coverUrl = albumJson.path("image").path("large").asText(null);

            Album album = Album.builder()
                    .id(albumId)
                    .title(albumTitle)
                    .genreId(genreId)
                    .cover_url(coverUrl)
                    .artist(artist)
                    .tracks(new ArrayList<>())
                    .build();
            // Set fully completed tracks list to album object
            album.setTracks(JsonUtil.extractTracks(tracksArray, album));
            artist.setGenreId(genreId);
            return album;
        });
    }

    // Method which obtain short-live audio stream url
    public String searchTracks(String query, int limit, String token)  {
        String url = String.format("%s/catalog/search?app_id=172934108&query=%s&limit=%s&type=tracks&user_auth_token=%s",
                API_BASE_URL, query, limit, token);

        HttpRequest request = HttpUtil.checkSearchResponseValidWithAuthToken(url);

        try {
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            log.error("Error while CLIENT.s{}", e.getMessage(), e);
        }
        return null;
    }

    public String getFileUrl(int trackId, int formatId) {
        // Part of raw request signature
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String signatureRaw = String.format("trackgetFileUrlformat_id%sintentstreamtrack_id%s%s%s",
                formatId ,trackId, timestamp , SECRET);

        String audioUrl;
        try {
            // Must be calculated to each "getFileUrl" request
            String signature = MD5Util.calculateMD5(signatureRaw);

            String url = String.format("%s/track/getFileUrl?app_id=798273057&track_id=%s&trackId&format_id=%s&intent=stream" +
                    "&request_ts=%s&request_sig=%s", API_BASE_URL, trackId, formatId, timestamp, signature);

         //   String url = String.format("%s/track/getFileUrl?app_id=798273057&track_id=%s&trackId&format_id=%s&intent=stream" +
              //      "&request_ts=%s&request_sig=%s", API_BASE_URL, trackId, formatId, timestamp, signature);


            HttpRequest request = HttpUtil.buildGetRequest(url);
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            audioUrl = JsonUtil.extractTrackUrl(response.body());
        } catch (Exception e) {
            log.error("Exception in getFileUrl(): {}", e.getMessage(), e);
            return null;
        }
        return audioUrl;
    }

    // Async request sending -> response json string parsing with "JsonUtils"...
    public CompletableFuture<JsonNode> sendAsync(String url) {
        return CLIENT.sendAsync(HttpUtil.buildGetRequest(url), HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(JsonUtil::parseJson);
    }
}

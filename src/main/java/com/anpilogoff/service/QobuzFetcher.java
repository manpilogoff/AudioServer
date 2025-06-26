package com.anpilogoff.service;

import com.anpilogoff.database.entity.Artist;
import com.anpilogoff.database.entity.Album;
import com.anpilogoff.util.ConfigUtil;
import com.anpilogoff.util.HttpUtil;
import com.anpilogoff.util.JsonUtil;
import com.anpilogoff.util.MD5Util;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class QobuzFetcher {
    private static final Logger log = LoggerFactory.getLogger(QobuzFetcher.class);
    public static HttpClient CLIENT = HttpClient.newHttpClient();
    public  final String API_BASE_URL;
    public  final String APP_ID;
    public  final String AUTH_TOKEN;
    public final String SECRET;

    public QobuzFetcher() {
        log.info("Reading configuration from properties file...");
        Properties prop;
        prop = ConfigUtil.loadConfig("env.properties");

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

        // Artist data + albums data api url
        String url = String.format("%s/artist/get?artist_id=%s&extra=albums&app_id=%s&user_auth_token=%s",
                API_BASE_URL, artistId, APP_ID, AUTH_TOKEN);
        log.info("Асинхронный запрос к Qobuz API: artistId=".concat( artistId));

        // Async fetch artist and albums data
        return sendAsync(url).thenCompose(artistJson -> {
            System.out.println(artistJson);
            String name = artistJson.path("name").asText(null);
           // String genreId = artistJson.path("genre").path("id").asText();

            if (name == null) {
                log.info("Error. Couldn't retrieve artist name.");
                throw new CompletionException(new IllegalStateException("Error artist name retrievement"));
            }

            Artist artist = Artist.builder().id(artistId).name(name).build();

            // Обработка альбомов
            JsonNode albumsArrayJson = artistJson.path("albums").path("items");
            return fetchAlbumsAndTracksForArtist(albumsArrayJson, artist, API_BASE_URL, APP_ID, AUTH_TOKEN);
        });
    }


    //each ID album async fetch -> object create -> tracks data extracting -> tracks object creating
    public CompletableFuture<Artist> fetchAlbumsAndTracksForArtist(
            JsonNode albumsArray, Artist artist, String apiBaseUrl, String appId, String authToken) {

        //"promise" list of obtained albums
        List<CompletableFuture<Album>> albumFutures = new ArrayList<>();

        for (JsonNode albumNode : albumsArray) {
            String albumId = albumNode.path("id").asText();
            albumFutures.add(fetchAlbumWithTracksAsync(albumId, artist, apiBaseUrl, appId, authToken));
        }
        //waiting untill all async operations ends.
        return CompletableFuture.allOf(albumFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<Album> albums = new ArrayList<>();
                    //get completed results of async requests
                    for (CompletableFuture<Album> future : albumFutures) {
                        try {
                            Album album = future.join();
                            albums.add(album);
                        } catch (CompletionException ex) {
                            throw new CompletionException("Ошибка при загрузке альбома", ex.getCause());
                        }
                    }
                    artist.setAlbums(albums);

                    log.info("Artist object completely built: ".concat(String.valueOf(artist.getAlbums().size())));
                    return artist;
                });
    }

    //async fetching each album tracks info and building nested tracks objects -> return
    public CompletableFuture<Album> fetchAlbumWithTracksAsync(
            String albumId, Artist artist, String baseUrl, String appId, String authToken) {

        String albumUrl = String.format("%s/album/get?album_id=%s&app_id=%s&user_auth_token=%s",
                baseUrl, albumId, appId, authToken);

        //fetch album + album tracks data...
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
            //set fully completed tracks list to album object
            album.setTracks(JsonUtil.extractTracks(tracksArray, album));
            artist.setGenreId(genreId);
            return album;
        });
    }

    //method which obtain short-live audio stream url
    public String getFileUrl(int trackId, int formatId) throws Exception {
        //part of raw request signature
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String signatureRaw = String.format("trackgetFileUrlformat_id%sintentstreamtrack_id%s%s%s",
                formatId ,trackId, timestamp , SECRET);
        //we connect it to every "getFileUrl" request
        String signature = MD5Util.calculateMD5(signatureRaw);

        String url = String.format("%s/track/getFileUrl?app_id=798273057&track_id=%s&trackId&format_id=%s&intent=stream"+
                        "&request_ts=%s&request_sig=%s", API_BASE_URL, trackId, formatId, timestamp, signature);

        HttpRequest request = HttpUtil.buildGetRequest(url);

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return JsonUtil.extractTrackUrl(response.body());
    }

    public String searchTracks(String query, int limit) throws Exception {
        String url = String.format("%s/catalog/search?app_id=172934108&query=%s&limit=%s&type=tracks&user_auth_token=%s",
                API_BASE_URL, query, limit, AUTH_TOKEN);

        HttpRequest request = HttpUtil.buildGetRequest(url);

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    // Async request sending -> response json string parsing with "JsonUtils"...
    public CompletableFuture<JsonNode> sendAsync(String url) {
        return CLIENT.sendAsync(HttpUtil.buildGetRequest(url), HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(JsonUtil::parseJson);
    }

    public void downloadFile(String url, String fileName) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        InputStream inputStream = CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream()).body();

        try (OutputStream outputStream = Files.newOutputStream(Paths.get(fileName))) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);

            }
            outputStream.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
        // Files.write(Path.of(fileName), fileBytes, StandardOpenOption.CREATE);
    }
}

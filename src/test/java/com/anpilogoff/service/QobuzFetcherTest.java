package com.anpilogoff.service;

import com.anpilogoff.dao.Artist;
import com.anpilogoff.dao.Album;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QobuzFetcherTest {

    @Test
    void fetchArtistWithDetailsAsync_withMockedSendAsync() throws Exception {
        // 1. Мокаем QobuzFetcher
        QobuzFetcher fetcher = Mockito.spy(new QobuzFetcher());

        // 2. Мокаем sendAsync, чтобы вернуть фиктивный JsonNode для артиста
        ObjectMapper mapper = new ObjectMapper();
        String jsonStr = "{\"name\":\"Mock Artist\",\"genre\":{\"id\":\"genre1\"},\"albums\":{\"items\":[{\"id\":\"album1\"}]}}";
        JsonNode artistJson = mapper.readTree(jsonStr);

        doReturn(CompletableFuture.completedFuture(artistJson))
                .when(fetcher)
                .sendAsync(anyString());

        // 3. Мокаем fetchAlbumWithTracksAsync, чтобы вернуть фиктивный альбом (если нужно)
        Album album = Album.builder().id("album1").title("Album1").build();
        doReturn(CompletableFuture.completedFuture(album))
                .when(fetcher)
                .fetchAlbumWithTracksAsync(anyString(), any(), anyString(), anyString(), anyString());

        // 4. Вызываем метод и проверяем результат
        CompletableFuture<Artist> future = fetcher.fetchArtistWithDetailsAsync("anyId");
        Artist artist = future.join();
        assertEquals("Mock Artist", artist.getName());
        assertEquals(1, artist.getAlbums().size());
        assertEquals("album1", artist.getAlbums().get(0).getId());
    }

    @Test
    void fetchAlbumWithTracksAsync_returnsAlbumWithTracks() throws Exception {
        QobuzFetcher fetcher = Mockito.spy(new QobuzFetcher());

        // Мокаем sendAsync: возвращаем альбом с двумя треками
        ObjectMapper mapper = new ObjectMapper();
        String albumJsonStr = """
        {
          "title": "Album Title",
          "tracks": { "items": [
            { "id": "track1", "title": "Track One", "duration": 123 },
            { "id": "track2", "title": "Track Two", "duration": 234 }
          ]}
     }
    """;
        JsonNode albumJson = mapper.readTree(albumJsonStr);
        doReturn(CompletableFuture.completedFuture(albumJson))
                .when(fetcher)
                .sendAsync(anyString());

        // Входной артист
        Artist artist = Artist.builder().id("artist1").name("Art1").build();

        CompletableFuture<Album> future = fetcher.fetchAlbumWithTracksAsync("album-5", artist, "mock", "id", "token");
        Album album = future.join();

        assertEquals("album-5", album.getId());
        assertEquals("Album Title", album.getTitle());
        assertEquals(2, album.getTracks().size());
        assertEquals("Track One", album.getTracks().get(0).getTitle());
        assertEquals(123, album.getTracks().get(0).getDuration());
        assertEquals(album, album.getTracks().get(0).getAlbum());
    }
}

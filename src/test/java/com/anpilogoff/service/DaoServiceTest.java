package com.anpilogoff.service;

import com.anpilogoff.database.dto.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;

import com.anpilogoff.database.entity.Artist;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DaoServiceTest {
    private EntityManagerFactory emf;
    private EntityManager em;
    private DaoService daoService;
    private final String ARTIST_ID = "4560370";

    @BeforeAll
    void setup() {
        Artist artist = new QobuzFetcher().fetchArtistWithDetailsAsync(ARTIST_ID).join();
        emf = Persistence.createEntityManagerFactory("testPU");
        em = emf.createEntityManager();
        daoService = new DaoService(emf);

        assertTrue(daoService.saveArtist(artist));
    }

    @AfterAll
    void tearDown() {
        if(em != null) em.close();
        if (emf != null) emf.close();
    }

    @Test
    void testUpdateTrackS3Status() {
        assertTrue(daoService.updateTrackS3Status("243271641"));
    }

    @Test
    void testGetTrack() {
        TrackWithArtistAndAlbumDto track = daoService.getTrack("243271641");

        assertNotNull(track);
        assertFalse(track.albumId().isEmpty());
        assertFalse(track.artistId().isEmpty());
    }


    @Test
    void testGetTracksByArtistId() {
        List<TrackWithArtistAndAlbumDto> tracks = daoService.getArtistTracks(ARTIST_ID);
        assertTrue(!tracks.isEmpty());
    }

    @Test
    void testGetArtistWithAlbums() {
        ArtistWithAlbumsDto artistDto = daoService.getArtistWithAlbums(ARTIST_ID);

        assertNotNull(artistDto);
        assertEquals(ARTIST_ID, artistDto.id());
        assertFalse(artistDto.albums().isEmpty());
    }

    @Test
    void testGetAlbumWithTracks() {
        AlbumWithTracksDto albumDto = daoService.getAlbumWithTracks("z0xk5b97qqfla");

        assertNotNull(albumDto);
        assertEquals("z0xk5b97qqfla", albumDto.id());
        assertFalse(albumDto.tracks().isEmpty());
    }
}


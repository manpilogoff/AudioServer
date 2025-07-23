package com.anpilogoff.service;

import com.anpilogoff.database.dto.*;
import com.anpilogoff.database.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
public class DaoService {
    private final EntityManagerFactory emf;

    public DaoService(EntityManagerFactory emf) { this.emf = emf; }

    public boolean saveArtist(Artist artist) {
        EntityTransaction tx = null;

        try (EntityManager em = emf.createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            em.persist(artist);
            tx.commit();
            em.close();

            return true;
        } catch (RuntimeException e) {
            log.error("Error during saveArtist() execution: {}", e.getMessage(), e);

            if(tx != null) {
                tx.rollback();
                tx.commit();
            }

            return false;
        }
    }

    public TrackWithArtistAndAlbumDto getTrack(String id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("""
                    SELECT new com.anpilogoff.database.dto.TrackWithArtistAndAlbumDto(
                    t.id, t.title, a.id, a.title, a.cover_url, ar.id, ar.name)
                    FROM Track t
                    JOIN t.album a
                    JOIN a.artist ar
                    WHERE t.id = :id""",TrackWithArtistAndAlbumDto.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (RuntimeException e) {
            log.error("Error during getTrack() execution: {}", e.getMessage(), e);
            return null;
        }
    }

    // the description speaks for itself
    public boolean updateTrackS3Status(String trackId) {
        EntityTransaction tx;

        try (EntityManager em = emf.createEntityManager()) {
            tx = em.getTransaction();
            tx.begin();
            Track track = em.find(Track.class, trackId);
            log.debug("{}    trackId!!!!!", track);
            log.debug("!!!!!TRACK   {}", track);
            track.setS3Exists(true);

            em.merge(track);
            tx.commit();

            return true;
        } catch (Exception e) {
            log.error("Error during S3 status updating: {}", e.getMessage(), e);
            return false;
        }
    }

    // Track with artist/album metadata fields (NOT NESTED!!)
    public List<TrackWithArtistAndAlbumDto> getArtistTracks(String artistId) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.createQuery("""
                     SELECT new com.anpilogoff.database.dto.TrackWithArtistAndAlbumDto(
                        t.id, t.title, a.id, a.title, a.cover_url, ar.id, ar.name)
                     FROM Track t
                     JOIN t.album a
                     JOIN a.artist ar
                     WHERE ar.id = :idParam""",TrackWithArtistAndAlbumDto.class)
                    .setParameter("idParam", artistId)
                    .getResultList();
        } catch (RuntimeException e) {
            log.error("Error during getArtistTracks() execution: {}", e.getMessage(), e);
            return null;
        }
    }

    // Artist with EMPTY albums nested inside JPQL Query;
    public ArtistWithAlbumsDto getArtistWithAlbums(String artistId) {
        try (EntityManager em = emf.createEntityManager()) {
            ArtistCompactDto artistData = em.createQuery("""
                    SELECT new com.anpilogoff.database.dto.ArtistCompactDto(ar.id, ar.name, ar.genreId)
                    FROM Artist ar
                    WHERE ar.id = :id""", ArtistCompactDto.class)
                    .setParameter("id", artistId)
                    .getSingleResult();

            List<AlbumCompactDto> albums = em.createQuery("""
                    SELECT new com.anpilogoff.database.dto.AlbumCompactDto(a.id, a.title, a.cover_url)
                    FROM Album a
                    WHERE a.artist.id = :id""", AlbumCompactDto.class)
                    .setParameter("id", artistId)
                    .getResultList();

            return new ArtistWithAlbumsDto(artistData.id(), artistData.name(), artistData.genreId(), albums);
        } catch (RuntimeException e) {
            log.error("Error during getArtistWithAlbums() execution: {}", e.getMessage(), e);
            return null;
        }
    }

    // Album with filled tracks list inside JPQL Query;
    public AlbumWithTracksDto getAlbumWithTracks(String albumId) {
        try (EntityManager em = emf.createEntityManager()) {

            // Album with compactArtist dto inside JPQL Query;
            AlbumWithTracksDto albumDto = em.createQuery("""
                SELECT new com.anpilogoff.database.dto.AlbumWithTracksDto(
                    a.id, a.title, a.genreId, a.cover_url,
                    new com.anpilogoff.database.dto.ArtistCompactDto(ar.id, ar.name, ar.genreId),
                    null)
                FROM Album a
                JOIN a.artist ar
                WHERE a.id = :id""", AlbumWithTracksDto.class)
                    .setParameter("id", albumId)
                    .getSingleResult();

            // Album tracks collect query
            List<TrackCompactDto> tracks = em.createQuery("""
                SELECT new com.anpilogoff.database.dto.TrackCompactDto(t.id, t.title)
                FROM Track t WHERE t.album.id = :id""", TrackCompactDto.class)
                    .setParameter("id", albumId)
                    .getResultList();

            // Album Dto with tracks filled
            return new AlbumWithTracksDto(
                    albumDto.id(), albumDto.title(), albumDto.genreId(),albumDto.coverUrl(), albumDto.artist(), tracks);
        } catch (RuntimeException e) {
            log.error("Error during getAlbumWithTracks() execution: {}", e.getMessage(), e);
            return null;
        }
    }

    public Track getTrackEntity(String trackId){
        Track track;

        try (EntityManager em = emf.createEntityManager()){
            track =  em.createQuery("SELECT t FROM Track t WHERE t.id = :id", Track.class)
                    .setParameter("id", trackId)
                    .getSingleResult();
        } catch (NoResultException e ){ return null; }

        return track;
    }
}

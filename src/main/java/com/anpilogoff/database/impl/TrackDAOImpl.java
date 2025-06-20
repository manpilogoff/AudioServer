package com.anpilogoff.database.impl;

import com.anpilogoff.Main;
import com.anpilogoff.database.dao.TrackDao;
import com.anpilogoff.database.dto.TrackDTO;
import com.anpilogoff.database.entity.Track;
import jakarta.persistence.EntityManager;

import java.util.List;

public class TrackDAOImpl implements TrackDao {

    @Override
    public Track getById(String id) {
        EntityManager em = Main.emf.createEntityManager();
        Track track = em.find(Track.class, id);
        em.close();

        return track;
    }

    @Override
    public List<Track> findByAlbumId(String albumId) {
        return Main.emf.createEntityManager()
                .createQuery("SELECT t FROM Track t WHERE t.album.id = :albumId", Track.class)
                .setParameter("albumId", albumId)
                .getResultList();
    }

    @Override
    public List<Track> findAllByArtistId(String artistId) {
        return Main.emf.createEntityManager()
                .createQuery("SELECT t FROM Track t WHERE t.album.artist.id = :artistId", Track.class)
                .setParameter("artistId", artistId)
                .getResultList();
    }

    @Override
    public TrackDTO getTrackDTOById(String trackId) {
        return Main.emf.createEntityManager()
                .createQuery(
                        "SELECT new com.anpilogoff.database.dto.TrackDTO(" +
                                "t.id, a.artist.id, a.id, t.title, a.title, a.artist.name) " +
                                "FROM Track t " +
                                "JOIN t.album a " +
                                "WHERE t.id = :trackId", TrackDTO.class)
                .setParameter("trackId", trackId)
                .getSingleResult();
    }

    @Override
    public List<TrackDTO> findTracksByAlbumId(String albumId) {
        return Main.emf.createEntityManager()
                .createQuery(
                        "SELECT new com.anpilogoff.database.dto.TrackDTO(" +
                                "t.id, a.artist.id, a.id, t.title, a.title, a.artist.name) " +
                                "FROM Track t " +
                                "JOIN t.album a " +
                                "WHERE a.id = :albumId", TrackDTO.class)
                .setParameter("albumId", albumId)
                .getResultList();
    }

    @Override
    public boolean updateTrackS3ExistStatus(String trackId) {
        EntityManager em = Main.emf.createEntityManager();
        em.getTransaction().begin();

        Track track = em.find(Track.class, trackId);
        track.setS3Exists(true);

        em.merge(track);
        em.getTransaction().commit();
        em.close();

        return true;
    }
}

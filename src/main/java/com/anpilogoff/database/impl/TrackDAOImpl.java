package com.anpilogoff.database.impl;

import com.anpilogoff.Main;
import com.anpilogoff.database.dao.TrackDao;
import com.anpilogoff.database.dto.TrackDTO;
import com.anpilogoff.database.entity.Track;

import java.util.List;

public class TrackDAOImpl implements TrackDao {

    @Override
    public Track getById(String id) {
        return Main.emf.createEntityManager()
                .createQuery("SELECT t FROM Track t WHERE t.id = :id", Track.class)
                .setParameter("id", id)
                .getSingleResult();
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
                        "SELECT new com.anpilogoff.dao.dto.TrackDTO(" +
                                "t.id, a.artist.id, a.id, t.title, a.title, a.artist.name) " +
                                "FROM Track t " +
                                "JOIN t.album a " +
                                "WHERE t.id = :trackId", TrackDTO.class)
                .setParameter("trackId", trackId)
                .getSingleResult();
    }

    @Override
    public List<TrackDTO> findTrackDTOsByAlbumId(String albumId) {
        return Main.emf.createEntityManager()
                .createQuery(
                        "SELECT new com.anpilogoff.dao.dto.TrackDTO(" +
                                "t.id, a.artist.id, a.id, t.title, a.title, a.artist.name) " +
                                "FROM Track t " +
                                "JOIN t.album a " +
                                "WHERE a.id = :albumId", TrackDTO.class)
                .setParameter("albumId", albumId)
                .getResultList();
    }
}

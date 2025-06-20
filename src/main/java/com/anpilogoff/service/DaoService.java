package com.anpilogoff.service;

import com.anpilogoff.database.dao.AlbumDao;
import com.anpilogoff.database.dao.ArtistDao;
import com.anpilogoff.database.dao.TrackDao;
import com.anpilogoff.database.dto.AlbumDTO;
import com.anpilogoff.database.dto.TrackDTO;
import com.anpilogoff.database.entity.Album;
import com.anpilogoff.database.entity.Artist;
import com.anpilogoff.database.entity.Track;
import com.anpilogoff.database.impl.AlbumDAOImpl;
import com.anpilogoff.database.impl.ArtistDAOImpl;
import com.anpilogoff.database.impl.TrackDAOImpl;

import java.util.List;

public class DaoService {
    private final ArtistDao artistDao;
    private final AlbumDao albumDao;
    private final TrackDao trackDao;

    public DaoService(ArtistDao artistDao, AlbumDao albumDao, TrackDao trackDao) {
        this.artistDao = artistDao;
        this.albumDao = albumDao;
        this.trackDao = trackDao;
    }



    public boolean saveArtist(Artist artist) { return artistDao.save(artist); }

    public boolean updateTrackS3ExistsStatus(String id) { return trackDao.updateTrackS3ExistStatus(id); }

    public Artist getArtist(String artistId) { return artistDao.getById(artistId); }
    public Album getAlbum(String albumId) { return albumDao.getById(albumId); }
    public Track getTrack(String trackId) { return trackDao.getById(trackId); }

    public List<AlbumDTO> getArtistAlbums(String artistId) { return albumDao.findAlbumsByArtistId(artistId); }

    public List<Track> getArtistTracks(String artistId) { return trackDao.findAllByArtistId(artistId); }

    public List<TrackDTO> getAlbumTracks(String albumId) { return trackDao.findTracksByAlbumId(albumId); }


}

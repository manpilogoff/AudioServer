package com.anpilogoff.dao;

import com.anpilogoff.dao.dto.TrackDTO;
import com.anpilogoff.dao.entity.Track;

import java.util.List;

public interface TrackDao {
    /**
     * Получить трек по его id
     */
    Track getById(String id);

    /**
     * Получить список треков по id альбома
     */
    List<Track> findByAlbumId(String albumId);

    /**
     * Получить все треки определённого артиста по artistId (JOIN)
     */
    List<Track> findAllByArtistId(String artistId);

    /**
     * Получить трек DTO по id (например, с именем альбома и артиста)
     */
    TrackDTO getTrackDTOById(String trackId);

    /**
     * Получить список треков DTO по id альбома
     */
    List<TrackDTO> findTrackDTOsByAlbumId(String albumId);
}

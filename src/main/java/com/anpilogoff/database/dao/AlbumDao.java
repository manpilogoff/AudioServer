package com.anpilogoff.database.dao;

import com.anpilogoff.database.dto.AlbumDTO;
import com.anpilogoff.database.entity.Album;

import java.util.List;

public interface AlbumDao {
    /**
     * Получить альбом по его id
     */
    Album getById(String albumId);

    /**
     * Получить все альбомы артиста по artistId
     */
    List<Album> findByArtistId(String artistId);

    /**
     * Получить AlbumDTO по id альбома
     */
    AlbumDTO getAlbumDTOById(String albumId);

    /**
     * Получить список AlbumDTO по artistId
     */
    List<AlbumDTO> findAlbumDTOsByArtistId(String artistId);
}

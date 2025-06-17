package com.anpilogoff.dao;

import com.anpilogoff.dao.dto.ArtistDTO;
import com.anpilogoff.dao.entity.Artist;

import java.util.List;

public interface ArtistDao {
        /**
         * Получить артиста по id
         */
        Artist getById(String artistId);

        /**
         * Получить список всех артистов
         */
        List<Artist> findAll();

        /**
         * Получить ArtistDTO по id
         */
        ArtistDTO getArtistDTOById(String artistId);
    }



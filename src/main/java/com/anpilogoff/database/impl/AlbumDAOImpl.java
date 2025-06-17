package com.anpilogoff.database.impl;

import com.anpilogoff.Main;
import com.anpilogoff.database.dao.AlbumDao;
import com.anpilogoff.database.dto.AlbumDTO;
import com.anpilogoff.database.entity.Album;
import java.util.List;

    public class AlbumDAOImpl implements AlbumDao {
        @Override
        public Album getById(String albumId) {
            return Main.emf.createEntityManager()
                    .createQuery("SELECT a FROM Album a WHERE a.id = :id", Album.class)
                    .setParameter("id", albumId)
                    .getSingleResult();
        }

        @Override
        public List<Album> findByArtistId(String artistId) {
            return Main.emf.createEntityManager()
                    .createQuery("SELECT a FROM Album a WHERE a.artist.id = :artistId", Album.class)
                    .setParameter("artistId", artistId)
                    .getResultList();
        }

        @Override
        public AlbumDTO getAlbumDTOById(String albumId) {
            return Main.emf.createEntityManager()
                    .createQuery(
                            "SELECT new com.anpilogoff.dao.dto.AlbumDTO(" +
                                    "a.id, a.title, a.cover_url, ar.id, ar.name) " +
                                    "FROM Album a " +
                                    "JOIN a.artist ar " +
                                    "WHERE a.id = :albumId", AlbumDTO.class)
                    .setParameter("albumId", albumId)
                    .getSingleResult();
        }

        @Override
        public List<AlbumDTO> findAlbumDTOsByArtistId(String artistId) {
            return Main.emf.createEntityManager()
                    .createQuery(
                            "SELECT new com.anpilogoff.dao.dto.AlbumDTO(" +
                                    "a.id, a.title, a.cover_url, ar.id, ar.name) " +
                                    "FROM Album a " +
                                    "JOIN a.artist ar " +
                                    "WHERE ar.id = :artistId", AlbumDTO.class)
                    .setParameter("artistId", artistId)
                    .getResultList();
        }
    }


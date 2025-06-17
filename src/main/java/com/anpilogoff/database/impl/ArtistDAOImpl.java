package com.anpilogoff.database.impl;


import com.anpilogoff.Main;
import com.anpilogoff.database.dao.ArtistDao;
import com.anpilogoff.database.dto.ArtistDTO;
import com.anpilogoff.database.entity.Artist;

import java.util.List;

public class ArtistDAOImpl implements ArtistDao {

    @Override
    public Artist getById(String artistId) {
        return Main.emf.createEntityManager()
                .createQuery("SELECT ar FROM Artist ar WHERE ar.id = :id", Artist.class)
                .setParameter("id", artistId)
                .getSingleResult();
    }

    @Override
    public List<Artist> findAll() {
        return Main.emf.createEntityManager()
                .createQuery("SELECT ar FROM Artist ar", Artist.class)
                .getResultList();
    }

    @Override
    public ArtistDTO getArtistDTOById(String artistId) {
        return Main.emf.createEntityManager()
                .createQuery(
                        "SELECT new com.anpilogoff.dao.dto.ArtistDTO(" +
                                "ar.id, ar.name, ar.genreId) " +
                                "FROM Artist ar " +
                                "WHERE ar.id = :artistId", ArtistDTO.class)
                .setParameter("artistId", artistId)
                .getSingleResult();
    }
}

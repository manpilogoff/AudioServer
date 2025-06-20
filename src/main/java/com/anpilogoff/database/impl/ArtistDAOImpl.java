package com.anpilogoff.database.impl;


import com.anpilogoff.Main;
import com.anpilogoff.database.dao.ArtistDao;
import com.anpilogoff.database.dto.ArtistDTO;
import com.anpilogoff.database.entity.Artist;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ArtistDAOImpl implements ArtistDao {

    @Override
    public Artist getById(String artistId) {
        EntityManager em = Main.emf.createEntityManager();
        em.getTransaction().begin();
        Artist artist = em.find(Artist.class, artistId);

        em.close();

        return artist;
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
                        "SELECT new com.anpilogoff.database.dto.ArtistDTO(" +
                                "ar.id, ar.name, ar.genreId) " +
                                "FROM Artist ar " +
                                "WHERE ar.id = :artistId", ArtistDTO.class)
                .setParameter("artistId", artistId)
                .getSingleResult();
    }

    @Override
    public boolean save(Artist artist) {
        EntityManager em = Main.emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(artist);
        em.getTransaction().commit();
        em.close();

        return true;
    }
}

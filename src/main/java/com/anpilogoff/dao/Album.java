package com.anpilogoff.dao;

import java.util.List;

import lombok.*;

@Builder
@Getter
@Setter
public class Album {
    private String id;
    private String title;
    // @JoinColumn(name = "artist_id", referencedColumnName = "id")
    private Artist artist;
    //@Transient
    //@OneToMany(mappedBy = "album", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Track> tracks;
}
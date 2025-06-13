package com.anpilogoff.dao;

import java.util.List;

import lombok.*;

@Builder
@AllArgsConstructor
@Getter
@Setter
public class Album {
    private String id;
    private String title;
    // will used by JPA
    // @ManyToOne, (@JoinColumn (name= "artist_id"), fetch = FetchType.LAZY)
    private Artist artist;
    // @Transient
    // @OneToMany ( mappedBy = "album"), fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Track> tracks;
}
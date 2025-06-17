package com.anpilogoff.dao;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Builder
@Setter
@Getter
@ToString
public class Artist {
    private String id;
    private String name;
    private String genre_id;
    // Will userd by JPA
    //@OneToMany(mappedBy = "artist", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Album> albums;
}

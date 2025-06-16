package com.anpilogoff.dao;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class Track {
    private String id;
    private String title;
    //@ManyToOne
    //@JoinColumn(name = "album_id", referencedColumnName = "id")
    private Album album;
    private int duration;
    // Is exists in s3 storage
    private boolean s3_exists;
}

package com.anpilogoff.dao;

import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
public class Track {
    private String id;
    private String title;
    // will user by JPA
    // ( @ManyToOne (mappedBy = "tracks", fetch = FetchType.EAGER, orphanRemoval = true)
    private Album album;
    private int duration;
    // Is exists in s3 storage
    private boolean s3_exists;
}

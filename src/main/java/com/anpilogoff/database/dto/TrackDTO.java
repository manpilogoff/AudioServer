package com.anpilogoff.database.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class TrackDTO {
    private String trackId;
    private String artistId;
    private String albumId;
    private String title;
    private String artistName;
    private String albumName;
    private String genreId;
}

package com.anpilogoff.database.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class AlbumDTO {
    private String albumId;
    private String title;
    private String coverUrl;
    private String artistId;
    private String artistName;
    private List<TrackDTO> tracks;
}
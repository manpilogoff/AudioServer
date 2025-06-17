package com.anpilogoff.database.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ArtistDTO {
    private String artistId;
    private String name;
    private int genreId;
    private List<AlbumDTO> albums;
}
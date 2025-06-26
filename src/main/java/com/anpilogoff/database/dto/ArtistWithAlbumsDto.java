package com.anpilogoff.database.dto;

import java.util.List;


public record ArtistWithAlbumsDto(String id, String name, String genreId, List<AlbumCompactDto> albums) {

}
package com.anpilogoff.database.dto;

import java.util.List;

public record AlbumWithTracksDto(
        String id,
        String title,
        String genreId,
        String coverUrl,
        ArtistCompactDto artist,
        List<TrackCompactDto> tracks
) {}
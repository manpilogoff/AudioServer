package com.anpilogoff.database.dto;

public record TrackWithArtistAndAlbumDto(
        String trackId,
        String title,
        String albumId,
        String albumTitle,
        String albumCoverUrl,
        String artistId,
        String artistName
) {}
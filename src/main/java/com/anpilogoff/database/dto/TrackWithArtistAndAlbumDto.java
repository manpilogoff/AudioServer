package com.anpilogoff.database.dto;

public record TrackWithArtistAndAlbumDto(
        String trackId,
        String trackTitle,
        String albumId,
        String albumTitle,
        String albumCoverUrl,
        String artistId,
        String artistName
) {}
package com.anpilogoff.database.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Builder
@Setter
@Getter
@ToString
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Artist {
    @Id
    private String id;
    private String name;
    private int genreId;

    @OneToMany(mappedBy = "artist", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Album> albums;
}

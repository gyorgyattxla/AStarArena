package com.astar.arena.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table( name = "sokoban_map" )
public class SokobanMap {

    @Id
    @GeneratedValue ( strategy = GenerationType.IDENTITY)
    private Long id;

    @Column ( name = "content", nullable = false )
    private String content;

    @ManyToOne ( fetch = FetchType.LAZY, optional = false )
    @JoinColumn ( name = "user_id", nullable = false )
    private User user;

    @Column ( name = "is_validated", nullable = false )
    private boolean isValidated;

}

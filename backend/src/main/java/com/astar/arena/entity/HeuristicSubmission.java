package com.astar.arena.entity;

import com.astar.arena.enums.StatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table( name = "heuristic_submission" )
public class HeuristicSubmission {

    @Id
    @GeneratedValue ( strategy = GenerationType.IDENTITY )
    private Long id;

    @Column ( name = "source_code", nullable = false, columnDefinition = "TEXT" )
    private String sourceCode;

    @ManyToOne ( fetch = FetchType.LAZY, optional = false )
    @JoinColumn ( name = "user_id", nullable = false )
    private User author;

    @Column ( name = "status" )
    private StatusEnum status;

    @Column ( name = "nodes_expanded" )
    private Long nodesExpanded;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

}

package com.astar.arena.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "evaluation_result")
public class EvaluationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private HeuristicSubmission submission;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "map_id", nullable = false)
    private SokobanMap map;

    @Column(name = "is_solved", nullable = false)
    private boolean isSolved;

    // How many states the A* algorithm had to check using this heuristic
    @Column(name = "nodes_expanded")
    private Long nodesExpanded;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    // If the user's code threw an exception (e.g., NullPointerException) or timed out
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
}
package com.astar.arena.engine;

public record AStarResult(
        boolean solved,
        long nodesExpanded,
        long executionTimeMs,
        String errorMessage
) {}
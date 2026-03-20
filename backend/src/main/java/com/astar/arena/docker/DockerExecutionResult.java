package com.astar.arena.docker;

public record DockerExecutionResult(
        boolean success,
        boolean solved,
        long nodesExpanded,
        long executionTimeMs,
        String errorMessage
) {}
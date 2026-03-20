package com.astar.arena.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HeuristicSubmissionRequest {
    @NotBlank(message = "Source code cannot be empty")
    private String sourceCode;
}
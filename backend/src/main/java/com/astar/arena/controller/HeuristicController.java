package com.astar.arena.controller;

import com.astar.arena.dto.request.HeuristicSubmissionRequest;
import com.astar.arena.entity.EvaluationResult;
import com.astar.arena.entity.HeuristicSubmission;
import com.astar.arena.entity.User;
import com.astar.arena.repository.EvaluationResultRepository;
import com.astar.arena.repository.HeuristicSubmissionRepository;
import com.astar.arena.repository.UserRepository;
import com.astar.arena.service.HeuristicSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/heuristics")
@RequiredArgsConstructor
public class HeuristicController {

    private final HeuristicSubmissionService submissionService;
    private final UserRepository userRepository;
    private final HeuristicSubmissionRepository submissionRepository;
    private final EvaluationResultRepository evaluationResultRepository;

    @PostMapping("/submit")
    public ResponseEntity<HeuristicSubmission> submitHeuristic(@Valid @RequestBody HeuristicSubmissionRequest request, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        HeuristicSubmission submission = submissionService.submitHeuristic(request.getSourceCode(), user);

        return ResponseEntity.accepted().body(submission);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<HeuristicSubmission>> getLeaderboard() {
        List<HeuristicSubmission> leaderboard = submissionService.getLeaderboard();
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/{submissionId}")
    public ResponseEntity<HeuristicSubmission> getSubmissionStatus(@PathVariable Long submissionId) {
        HeuristicSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found with ID: " + submissionId));
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/{submissionId}/results")
    public ResponseEntity<List<EvaluationResult>> getDetailedResults(@PathVariable Long submissionId) {
        HeuristicSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found with ID: " + submissionId));

        List<EvaluationResult> results = evaluationResultRepository.findBySubmission(submission);
        return ResponseEntity.ok(results);
    }
}
package com.astar.arena.service;

import com.astar.arena.entity.EvaluationResult;
import com.astar.arena.entity.HeuristicSubmission;
import com.astar.arena.entity.SokobanMap;
import com.astar.arena.enums.StatusEnum;
import com.astar.arena.repository.EvaluationResultRepository;
import com.astar.arena.repository.HeuristicSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationResultService {

    private final EvaluationResultRepository resultRepository;
    private final HeuristicSubmissionRepository submissionRepository;

    @Transactional
    public void recordResult(HeuristicSubmission submission, SokobanMap map, boolean isSolved,
                             Long nodesExpanded, Long executionTimeMs, String errorMessage) {

        EvaluationResult result = EvaluationResult.builder()
                .submission(submission)
                .map(map)
                .isSolved(isSolved)
                .nodesExpanded(nodesExpanded)
                .executionTimeMs(executionTimeMs)
                .errorMessage(errorMessage)
                .build();

        resultRepository.save(result);
    }

    @Transactional
    public void finalizeSubmissionScore(Long submissionId) {
        HeuristicSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        List<EvaluationResult> results = resultRepository.findBySubmission(submission);

        long totalNodes = 0;
        long totalTime = 0;
        boolean anyFailed = false;

        for (EvaluationResult res : results) {
            if (!res.isSolved()) {
                anyFailed = true;
                break;
            }
            totalNodes += (res.getNodesExpanded() != null) ? res.getNodesExpanded() : 0;
            totalTime += (res.getExecutionTimeMs() != null) ? res.getExecutionTimeMs() : 0;
        }

        if (anyFailed) {
            submission.setNodesExpanded(null);
            submission.setExecutionTimeMs(null);
        } else {
            submission.setNodesExpanded(totalNodes);
            submission.setExecutionTimeMs(totalTime);
        }

        submission.setStatus(StatusEnum.COMPLETED);
        submissionRepository.save(submission);
    }
}
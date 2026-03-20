package com.astar.arena.worker;

import com.astar.arena.config.RabbitConfig;
import com.astar.arena.docker.DockerExecutionResult;
import com.astar.arena.docker.DockerExecutionService;
import com.astar.arena.entity.HeuristicSubmission;
import com.astar.arena.entity.SokobanMap;
import com.astar.arena.enums.StatusEnum;
import com.astar.arena.repository.HeuristicSubmissionRepository;
import com.astar.arena.service.EvaluationResultService;
import com.astar.arena.service.SokobanMapService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JudgeWorker {

    private static final Logger log = LoggerFactory.getLogger(JudgeWorker.class);

    private final HeuristicSubmissionRepository submissionRepository;
    private final SokobanMapService sokobanMapService;
    private final DockerExecutionService dockerExecutionService;
    private final EvaluationResultService evaluationResultService;

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void processSubmission(Long submissionId) {
        log.info("===========================================================");
        log.info(">>> RABBITMQ WORKER WOKE UP FOR SUBMISSION ID: {} <<<", submissionId);
        log.info("===========================================================");

        try {
            HeuristicSubmission submission = submissionRepository.findById(submissionId)
                    .orElseThrow(() -> new IllegalArgumentException("Submission not found with ID: " + submissionId));

            log.info("1. Updating submission status to PROCESSING...");
            submission.setStatus(StatusEnum.PROCESSING);
            submissionRepository.save(submission);

            List<SokobanMap> playableMaps = sokobanMapService.getAllPlayableMaps();
            log.info("2. Found {} playable maps in the database.", playableMaps.size());

            if (playableMaps.isEmpty()) {
                log.warn("!!! CRITICAL: NO MAPS FOUND IN DATABASE. EVALUATION ABORTED. !!!");
            }

            for (SokobanMap map : playableMaps) {
                log.info("3. Starting Docker evaluation for Map ID: {}", map.getId());

                DockerExecutionResult result = dockerExecutionService.evaluateHeuristic(
                        submission.getSourceCode(),
                        map.getContent()
                );

                log.info("4. Docker Execution Finished! Success: {}, Solved: {}, Nodes: {}, Error: {}",
                        result.success(), result.solved(), result.nodesExpanded(), result.errorMessage());

                evaluationResultService.recordResult(
                        submission,
                        map,
                        result.solved(),
                        result.nodesExpanded(),
                        result.executionTimeMs(),
                        result.errorMessage()
                );
            }

            log.info("5. Finalizing submission score for ID: {}", submissionId);
            evaluationResultService.finalizeSubmissionScore(submission.getId());
            log.info(">>> WORKER SUCCESSFULLY COMPLETED SUBMISSION ID: {} <<<", submissionId);

        } catch (Exception e) {
            log.error("!!! WORKER CRASHED WHILE PROCESSING SUBMISSION ID: {} !!!", submissionId, e);
        }
    }
}
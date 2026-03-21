package com.astar.arena.service;

import com.astar.arena.config.RabbitConfig;
import com.astar.arena.entity.HeuristicSubmission;
import com.astar.arena.entity.User;
import com.astar.arena.enums.StatusEnum;
import com.astar.arena.repository.HeuristicSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HeuristicSubmissionService {

    private final HeuristicSubmissionRepository submissionRepository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public HeuristicSubmission submitHeuristic(String sourceCode, User user) {
        HeuristicSubmission submission = new HeuristicSubmission();
        submission.setSourceCode(sourceCode);
        submission.setAuthor(user);
        submission.setStatus(StatusEnum.PENDING);
        submission.setSubmittedAt(LocalDateTime.now());

        HeuristicSubmission savedSubmission = submissionRepository.save(submission);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_NAME, savedSubmission.getId());
            }
        });

        return savedSubmission;
    }

    public List<HeuristicSubmission> getLeaderboard() {
        return submissionRepository.findAll().stream()
                .filter(sub -> sub.getStatus() == StatusEnum.COMPLETED)
                .filter(sub -> sub.getNodesExpanded() != null && sub.getExecutionTimeMs() != null)
                .filter(sub -> sub.getNodesExpanded() > 0)
                .sorted(Comparator.comparingLong(HeuristicSubmission::getNodesExpanded)
                        .thenComparingLong(HeuristicSubmission::getExecutionTimeMs))
                .collect(Collectors.toList());
    }
}
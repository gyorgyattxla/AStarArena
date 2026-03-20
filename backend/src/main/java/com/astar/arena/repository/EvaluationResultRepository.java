package com.astar.arena.repository;

import com.astar.arena.entity.EvaluationResult;
import com.astar.arena.entity.HeuristicSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationResultRepository extends JpaRepository<EvaluationResult, Long> {

    List<EvaluationResult> findBySubmission(HeuristicSubmission submission);
}
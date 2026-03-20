package com.astar.arena.repository;

import com.astar.arena.entity.HeuristicSubmission;
import com.astar.arena.enums.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HeuristicSubmissionRepository extends JpaRepository<HeuristicSubmission, Long> {

    List<HeuristicSubmission> findByStatusOrderBySubmittedAtAsc(StatusEnum status);

    List<HeuristicSubmission> findByStatusOrderByNodesExpandedAsc(StatusEnum status);

}

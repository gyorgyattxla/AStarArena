package com.astar.arena.repository;

import com.astar.arena.entity.SokobanMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SokobanMapRepository extends JpaRepository<SokobanMap, Long> {

    List<SokobanMap> findByIsValidatedTrue();

}

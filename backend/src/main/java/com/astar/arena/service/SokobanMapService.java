package com.astar.arena.service;

import com.astar.arena.entity.SokobanMap;
import com.astar.arena.entity.User;
import com.astar.arena.helper.SokobanParser;
import com.astar.arena.repository.SokobanMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SokobanMapService {

    private final SokobanMapRepository sokobanMapRepository;

    @Transactional
    public SokobanMap uploadAndValidateMap(String content, User author) {
        SokobanParser.parse(content);

        SokobanMap newMap = SokobanMap.builder()
                .content(content)
                .user(author)
                .isValidated(true)
                .build();

        return sokobanMapRepository.save(newMap);
    }

    @Transactional(readOnly = true)
    public List<SokobanMap> getAllPlayableMaps() {
        return sokobanMapRepository.findByIsValidatedTrue();
    }

}

package com.astar.arena.controller;

import com.astar.arena.dto.request.MapUploadRequest;
import com.astar.arena.entity.SokobanMap;
import com.astar.arena.entity.User;
import com.astar.arena.repository.UserRepository;
import com.astar.arena.service.SokobanMapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/maps")
@RequiredArgsConstructor
public class SokobanMapController {

    private final SokobanMapService mapService;
    private final UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<SokobanMap> uploadMap(@Valid @RequestBody MapUploadRequest request, Principal principal) {
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        SokobanMap map = mapService.uploadAndValidateMap(request.getContent(), user);
        return ResponseEntity.ok(map);
    }

    @GetMapping
    public ResponseEntity<List<SokobanMap>> getAllPlayableMaps() {
        List<SokobanMap> maps = mapService.getAllPlayableMaps();
        return ResponseEntity.ok(maps);
    }
}
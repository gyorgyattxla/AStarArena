package com.astar.arena.engine;

import com.astar.arena.helper.SokobanState;

public interface SokobanHeuristic {
    int heur(SokobanState state);
}
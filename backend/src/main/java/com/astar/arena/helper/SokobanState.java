package com.astar.arena.helper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

@AllArgsConstructor
@Getter
@Setter
public class SokobanState {

    private final boolean[][] walls;
    private final Set<Point> targets;
    private final Point player;
    private final Set<Point> boxes;

    public boolean isWin() {
        return targets.containsAll(boxes);
    }

    public boolean isWall(int x, int y) {
        if (y < 0 || y >= walls.length || x < 0 || x >= walls[y].length) return true;
        return walls[y][x];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SokobanState that = (SokobanState) o;
        return Objects.equals(player, that.player) && Objects.equals(boxes, that.boxes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, boxes);
    }
}

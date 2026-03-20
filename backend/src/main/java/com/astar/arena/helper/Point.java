package com.astar.arena.helper;

public record Point(int x, int y) {

    public Point move(int dx, int dy) {
        return new Point(this.x + dx, this.y + dy);
    }

}

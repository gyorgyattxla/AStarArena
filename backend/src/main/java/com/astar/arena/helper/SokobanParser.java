package com.astar.arena.helper;

import java.util.HashSet;
import java.util.Set;

public class SokobanParser {

    public static SokobanState parse(String content) {
        // Handle both Windows and Unix line endings
        String[] lines = content.split("\\r?\\n");
        int height = lines.length;
        int width = 0;

        // Find the maximum width (Sokoban maps aren't always perfect rectangles)
        for (String line : lines) {
            width = Math.max(width, line.length());
        }

        boolean[][] walls = new boolean[height][width];
        Set<Point> targets = new HashSet<>();
        Set<Point> boxes = new HashSet<>();
        Point player = null;

        for (int y = 0; y < height; y++) {
            String line = lines[y];
            for (int x = 0; x < line.length(); x++) {
                char c = line.charAt(x);
                Point p = new Point(x, y);

                switch (c) {
                    case '#': // Wall
                        walls[y][x] = true;
                        break;
                    case '@': // Player
                        if (player != null) throw new IllegalArgumentException("Multiple players found.");
                        player = p;
                        break;
                    case '+': // Player on target
                        if (player != null) throw new IllegalArgumentException("Multiple players found.");
                        player = p;
                        targets.add(p);
                        break;
                    case '$': // Box
                        boxes.add(p);
                        break;
                    case '*': // Box on target
                        boxes.add(p);
                        targets.add(p);
                        break;
                    case '.': // Target
                        targets.add(p);
                        break;
                    case ' ': // Empty space
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid character '" + c + "' at coordinate (" + x + "," + y + ")");
                }
            }
        }

        // --- Core Game Logic Validation ---
        if (player == null) {
            throw new IllegalArgumentException("Map must contain exactly one player.");
        }
        if (boxes.isEmpty()) {
            throw new IllegalArgumentException("Map must contain at least one box.");
        }
        if (boxes.size() != targets.size()) {
            throw new IllegalArgumentException("Map must have an equal number of boxes and targets.");
        }

        return new SokobanState(walls, targets, player, boxes);
    }
}

package com.astar.arena.engine;

import com.astar.arena.helper.Point;
import com.astar.arena.helper.SokobanState;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

@Component
public class AStarEngine {

    private static final int[][] DIRECTIONS = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

    private static class Node implements Comparable<Node> {
        SokobanState state;
        int g;
        int f;

        public Node(SokobanState state, int g, int f) {
            this.state = state;
            this.g = g;
            this.f = f;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.f, other.f);
        }
    }

    public AStarResult solve(SokobanState initialState, SokobanHeuristic heuristic, long maxNodesAllowed) {
        long startTime = System.currentTimeMillis();
        long nodesExpanded = 0;

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        HashSet<SokobanState> closedSet = new HashSet<>();

        try {
            int startH = heuristic.heur(initialState);
            openSet.add(new Node(initialState, 0, startH));

            while (!openSet.isEmpty()) {
                Node current = openSet.poll();
                SokobanState currentState = current.state;

                if (currentState.isWin()) {
                    long endTime = System.currentTimeMillis();
                    return new AStarResult(true, nodesExpanded, endTime - startTime, null);
                }

                if (closedSet.contains(currentState)) continue;
                closedSet.add(currentState);
                nodesExpanded++;

                if (nodesExpanded > maxNodesAllowed) {
                    return new AStarResult(false, nodesExpanded, System.currentTimeMillis() - startTime, "Node limit exceeded. Heuristic is too inefficient.");
                }

                for (int[] dir : DIRECTIONS) {
                    int dx = dir[0];
                    int dy = dir[1];

                    Point newPlayerPos = currentState.getPlayer().move(dx, dy);

                    if (currentState.isWall(newPlayerPos.x(), newPlayerPos.y())) continue;

                    Set<Point> newBoxes = new HashSet<>(currentState.getBoxes());

                    if (newBoxes.contains(newPlayerPos)) {
                        Point newBoxPos = newPlayerPos.move(dx, dy);
                        if (currentState.isWall(newBoxPos.x(), newBoxPos.y()) || newBoxes.contains(newBoxPos)) {
                            continue;
                        }
                        newBoxes.remove(newPlayerPos);
                        newBoxes.add(newBoxPos);
                    }

                    SokobanState nextState = new SokobanState(
                            currentState.getWalls(),
                            currentState.getTargets(),
                            newPlayerPos,
                            newBoxes
                    );

                    if (!closedSet.contains(nextState)) {
                        int gNode = current.g + 1;
                        int hNode = heuristic.heur(nextState);
                        openSet.add(new Node(nextState, gNode, gNode + hNode));
                    }
                }
            }

            return new AStarResult(false, nodesExpanded, System.currentTimeMillis() - startTime, "Search exhausted without finding a solution.");

        } catch (Exception e) {
            return new AStarResult(false, nodesExpanded, System.currentTimeMillis() - startTime, "User heuristic threw an exception: " + e.getMessage());
        }
    }
}
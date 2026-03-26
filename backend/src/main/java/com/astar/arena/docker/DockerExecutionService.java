package com.astar.arena.docker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DockerExecutionService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String WORKSPACE_DIR = "/workspace";
    private static final String VOLUME_NAME = "sokoban_eval_workspace";

    public DockerExecutionResult evaluateHeuristic(String userCode, String mapContent) {
        String sessionId = UUID.randomUUID().toString();
        Path sessionDir = Paths.get(WORKSPACE_DIR, sessionId);
        String containerName = "eval-" + sessionId;

        try {
            Files.createDirectories(sessionDir);
            File sourceFile = new File(sessionDir.toFile(), "JudgeRunner.java");
            File stdoutFile = new File(sessionDir.toFile(), "stdout.txt");
            File stderrFile = new File(sessionDir.toFile(), "stderr.txt");

            String fullSource = buildRunnerSourceCode(userCode, mapContent);
            Files.writeString(sourceFile.toPath(), fullSource);

            ProcessBuilder compilePb = new ProcessBuilder(
                    "docker", "run", "--rm",
                    "-v", VOLUME_NAME + ":/workspace",
                    "-w", "/workspace/" + sessionId,
                    "eclipse-temurin:21-jdk-alpine",
                    "javac", "JudgeRunner.java"
            );
            Process compileProcess = compilePb.start();
            boolean compiled = compileProcess.waitFor(10, TimeUnit.SECONDS);

            if (!compiled || compileProcess.exitValue() != 0) {
                String compileError = new BufferedReader(new InputStreamReader(compileProcess.getErrorStream()))
                        .lines().collect(Collectors.joining("\n"));
                return new DockerExecutionResult(false, false, 0, 0, "Compilation failed: " + compileError);
            }

            ProcessBuilder runPb = new ProcessBuilder(
                    "docker", "run", "--rm",
                    "--name", containerName,
                    "--network", "none",
                    "--memory", "2g",
                    "--cpus", "2.0",
                    "--user", "nobody",
                    "-v", VOLUME_NAME + ":/workspace:ro",
                    "-w", "/workspace/" + sessionId,
                    "eclipse-temurin:21-jdk-alpine",
                    "java", "JudgeRunner"
            );

            runPb.redirectOutput(stdoutFile);
            runPb.redirectError(stderrFile);

            Process runProcess = runPb.start();

            boolean finished = runProcess.waitFor(60, TimeUnit.SECONDS);

            if (!finished) {
                runProcess.destroyForcibly();
                new ProcessBuilder("docker", "rm", "-f", containerName).start().waitFor();
                return new DockerExecutionResult(false, false, 0, 0, "Execution timed out (Infinite loop detected).");
            }

            String output = Files.exists(stdoutFile.toPath()) ? Files.readString(stdoutFile.toPath()) : "";
            String errorOutput = Files.exists(stderrFile.toPath()) ? Files.readString(stderrFile.toPath()) : "";

            if (runProcess.exitValue() != 0) {
                return new DockerExecutionResult(false, false, 0, 0, "Runtime Error / OOM. Error: " + errorOutput + " | Output: " + output);
            }

            int jsonStartIndex = output.lastIndexOf('{');
            if (jsonStartIndex >= 0) {
                String cleanJson = output.substring(jsonStartIndex);
                return objectMapper.readValue(cleanJson, DockerExecutionResult.class);
            } else {
                return new DockerExecutionResult(false, false, 0, 0, "Execution failed. Raw output: " + output);
            }

        } catch (Exception e) {
            return new DockerExecutionResult(false, false, 0, 0, "System Error: " + e.getMessage());
        } finally {
            try {
                new ProcessBuilder("docker", "rm", "-f", containerName).start().waitFor();
            } catch (Exception ignored) {}
            deleteDirectory(sessionDir.toFile());
        }
    }

    private String buildRunnerSourceCode(String userCode, String mapContent) {
        String template = """
            import java.util.*;
            
            public class JudgeRunner {
            
                public record Point(int x, int y) {
                    public Point move(int dx, int dy) {
                        return new Point(this.x + dx, this.y + dy);
                    }
                }
            
                public static class SokobanState {
                    private final boolean[][] walls;
                    private final Set<Point> targets;
                    private final Point player;
                    private final Set<Point> boxes;
                    private final int cachedHash;
            
                    public SokobanState(boolean[][] walls, Set<Point> targets, Point player, Set<Point> boxes) {
                        this.walls = walls;
                        this.targets = targets;
                        this.player = player;
                        this.boxes = Collections.unmodifiableSet(boxes);
                        this.cachedHash = Objects.hash(player, boxes);
                    }
            
                    public boolean isWin() { return targets.containsAll(boxes); }
                    public boolean isWall(int x, int y) {
                        if (y < 0 || y >= walls.length || x < 0 || x >= walls[y].length) return true;
                        return walls[y][x];
                    }
                    public Point getPlayer() { return player; }
                    public Set<Point> getBoxes() { return boxes; }
                    public boolean[][] getWalls() { return walls; }
                    public Set<Point> getTargets() { return targets; }
            
                    @Override
                    public boolean equals(Object o) {
                        if (this == o) return true;
                        if (o == null || getClass() != o.getClass()) return false;
                        SokobanState that = (SokobanState) o;
                        if (this.cachedHash != that.cachedHash) return false;
                        return Objects.equals(player, that.player) && Objects.equals(boxes, that.boxes);
                    }
            
                    @Override
                    public int hashCode() { return cachedHash; }
                }
            
                public interface SokobanHeuristic {
                    int heur(SokobanState state);
                }
            
                public static class UserHeuristic implements SokobanHeuristic {
                    %s
                }
            
                private static final int[][] DIRECTIONS = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
            
                private static class Node implements Comparable<Node> {
                    SokobanState state;
                    int g;
                    int f;
                    public Node(SokobanState state, int g, int f) {
                        this.state = state; this.g = g; this.f = f;
                    }
                    @Override
                    public int compareTo(Node other) { return Integer.compare(this.f, other.f); }
                }
            
                public static void main(String[] args) {
                    try {
                        String mapContent = %s;
                        boolean[][] initialWalls = parseWalls(mapContent);
                        Set<Point> initialTargets = parseTargets(mapContent);
                        Set<Point> initialBoxes = parseBoxes(mapContent);
                        Point rawPlayer = parsePlayer(mapContent);
            
                        // Instantiate canonical state
                        Point canonicalPlayer = getCanonicalPlayer(rawPlayer, initialBoxes, initialWalls);
                        SokobanState initialState = new SokobanState(initialWalls, initialTargets, canonicalPlayer, initialBoxes);
            
                        SokobanHeuristic heuristic = new UserHeuristic();
                        
                        long startTime = System.currentTimeMillis();
                        long nodesExpanded = 0;
                        long maxNodesAllowed = 2000000; // Easily handles macro-moves
            
                        PriorityQueue<Node> openSet = new PriorityQueue<>();
                        HashSet<SokobanState> closedSet = new HashSet<>();
                        
                        int startH = heuristic.heur(initialState);
                        openSet.add(new Node(initialState, 0, startH));
            
                        while (!openSet.isEmpty()) {
                            Node current = openSet.poll();
                            SokobanState currentState = current.state;
            
                            if (currentState.isWin()) {
                                long endTime = System.currentTimeMillis();
                                printResult(true, true, nodesExpanded, endTime - startTime, null);
                                return;
                            }
            
                            if (!closedSet.add(currentState)) continue;
                            nodesExpanded++;
            
                            if (nodesExpanded > maxNodesAllowed) {
                                printResult(true, false, nodesExpanded, System.currentTimeMillis() - startTime, "Node limit exceeded.");
                                return;
                            }
            
                            // MACRO-MOVE SEARCH: Find every square the player can currently walk to
                            HashSet<Point> reachable = getReachable(currentState.getPlayer(), currentState.getBoxes(), currentState.getWalls());
            
                            // For every reachable square, check if we can push an adjacent box
                            for (Point p : reachable) {
                                for (int[] dir : DIRECTIONS) {
                                    int dx = dir[0]; int dy = dir[1];
                                    Point boxPos = p.move(dx, dy);
            
                                    if (currentState.getBoxes().contains(boxPos)) {
                                        Point pushTo = boxPos.move(dx, dy);
            
                                        if (!currentState.isWall(pushTo.x(), pushTo.y()) && !currentState.getBoxes().contains(pushTo)) {
                                            
                                            // Engine-level corner deadlock detection
                                            if (!currentState.getTargets().contains(pushTo)) {
                                                boolean wallX = currentState.isWall(pushTo.x() - 1, pushTo.y()) || currentState.isWall(pushTo.x() + 1, pushTo.y());
                                                boolean wallY = currentState.isWall(pushTo.x(), pushTo.y() - 1) || currentState.isWall(pushTo.x(), pushTo.y() + 1);
                                                if (wallX && wallY) continue; 
                                            }
            
                                            Set<Point> newBoxes = new HashSet<>(currentState.getBoxes());
                                            newBoxes.remove(boxPos);
                                            newBoxes.add(pushTo);
                                            
                                            // Teleport player to the exact spot the box used to be, then canonicalize
                                            Point newPlayer = getCanonicalPlayer(boxPos, newBoxes, currentState.getWalls());
                                            SokobanState nextState = new SokobanState(currentState.getWalls(), currentState.getTargets(), newPlayer, newBoxes);
                                            
                                            if (!closedSet.contains(nextState)) {
                                                int gNode = current.g + 1; // 1 push cost, not 1 step cost
                                                int hNode = heuristic.heur(nextState);
                                                openSet.add(new Node(nextState, gNode, gNode + hNode));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        printResult(true, false, nodesExpanded, System.currentTimeMillis() - startTime, "Search exhausted.");
                    } catch (Exception e) {
                        printResult(false, false, 0, 0, e.getMessage());
                    }
                }
            
                // BFS to find all walking space
                private static HashSet<Point> getReachable(Point start, Set<Point> boxes, boolean[][] walls) {
                    Queue<Point> q = new LinkedList<>();
                    HashSet<Point> visited = new HashSet<>();
                    q.add(start);
                    visited.add(start);
                    while (!q.isEmpty()) {
                        Point curr = q.poll();
                        for (int[] dir : DIRECTIONS) {
                            Point next = curr.move(dir[0], dir[1]);
                            if (next.y() >= 0 && next.y() < walls.length && next.x() >= 0 && next.x() < walls[next.y()].length) {
                                if (!walls[next.y()][next.x()] && !boxes.contains(next) && visited.add(next)) {
                                    q.add(next);
                                }
                            }
                        }
                    }
                    return visited;
                }
            
                // Teleports player to the top-leftmost reachable square to normalize state hashing
                private static Point getCanonicalPlayer(Point start, Set<Point> boxes, boolean[][] walls) {
                    HashSet<Point> reachable = getReachable(start, boxes, walls);
                    Point canonical = start;
                    for (Point p : reachable) {
                        if (p.y() < canonical.y() || (p.y() == canonical.y() && p.x() < canonical.x())) {
                            canonical = p;
                        }
                    }
                    return canonical;
                }
            
                private static void printResult(boolean success, boolean solved, long nodes, long timeMs, String error) {
                    String errorStr = error == null ? "null" : "\\\"" + error.replace("\\\"", "\\\\\\\"") + "\\\"";
                    System.out.println("{\\\"success\\\":" + success + ", \\\"solved\\\":" + solved + ", \\\"nodesExpanded\\\":" + nodes + ", \\\"executionTimeMs\\\":" + timeMs + ", \\\"errorMessage\\\":" + errorStr + "}");
                }
            
                public static boolean[][] parseWalls(String content) {
                    String[] lines = content.split("\\\\r?\\\\n");
                    int height = lines.length;
                    int width = 0;
                    for (String line : lines) width = Math.max(width, line.length());
                    boolean[][] walls = new boolean[height][width];
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < lines[y].length(); x++) {
                            if (lines[y].charAt(x) == '#') walls[y][x] = true;
                        }
                    }
                    return walls;
                }
            
                public static Set<Point> parseTargets(String content) {
                    Set<Point> targets = new HashSet<>();
                    String[] lines = content.split("\\\\r?\\\\n");
                    for (int y = 0; y < lines.length; y++) {
                        for (int x = 0; x < lines[y].length(); x++) {
                            char c = lines[y].charAt(x);
                            if (c == '.' || c == '+' || c == '*') targets.add(new Point(x, y));
                        }
                    }
                    return targets;
                }
            
                public static Set<Point> parseBoxes(String content) {
                    Set<Point> boxes = new HashSet<>();
                    String[] lines = content.split("\\\\r?\\\\n");
                    for (int y = 0; y < lines.length; y++) {
                        for (int x = 0; x < lines[y].length(); x++) {
                            char c = lines[y].charAt(x);
                            if (c == '$' || c == '*') boxes.add(new Point(x, y));
                        }
                    }
                    return boxes;
                }
            
                public static Point parsePlayer(String content) {
                    String[] lines = content.split("\\\\r?\\\\n");
                    for (int y = 0; y < lines.length; y++) {
                        for (int x = 0; x < lines[y].length(); x++) {
                            char c = lines[y].charAt(x);
                            if (c == '@' || c == '+') return new Point(x, y);
                        }
                    }
                    return new Point(0, 0);
                }
            }
            """;

        String escapedMapContent = "\"" + mapContent.replace("\n", "\\n").replace("\r", "") + "\"";
        return String.format(template, userCode, escapedMapContent);
    }

    private void deleteDirectory(File directoryToBeDeleted) {
        if (!directoryToBeDeleted.exists()) return;
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}
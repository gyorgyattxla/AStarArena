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
                    "--memory", "512m",
                    "--cpus", "1.0",
                    "--user", "nobody",
                    "-v", VOLUME_NAME + ":/workspace:ro",
                    "-w", "/workspace/" + sessionId,
                    "eclipse-temurin:21-jdk-alpine",
                    "java", "JudgeRunner"
            );

            runPb.redirectOutput(stdoutFile);
            runPb.redirectError(stderrFile);

            Process runProcess = runPb.start();

            boolean finished = runProcess.waitFor(15, TimeUnit.SECONDS);

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
            
                    public SokobanState(boolean[][] walls, Set<Point> targets, Point player, Set<Point> boxes) {
                        this.walls = walls;
                        this.targets = targets;
                        this.player = player;
                        this.boxes = boxes;
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
                        return Objects.equals(player, that.player) && Objects.equals(boxes, that.boxes);
                    }
            
                    @Override
                    public int hashCode() { return Objects.hash(player, boxes); }
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
                        SokobanState initialState = parse(mapContent);
                        SokobanHeuristic heuristic = new UserHeuristic();
                        
                        long startTime = System.currentTimeMillis();
                        long nodesExpanded = 0;
                        long maxNodesAllowed = 500000;
            
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
            
                            if (closedSet.contains(currentState)) continue;
                            closedSet.add(currentState);
                            nodesExpanded++;
            
                            if (nodesExpanded > maxNodesAllowed) {
                                printResult(true, false, nodesExpanded, System.currentTimeMillis() - startTime, "Node limit exceeded.");
                                return;
                            }
            
                            for (int[] dir : DIRECTIONS) {
                                int dx = dir[0]; int dy = dir[1];
                                Point newPlayerPos = currentState.getPlayer().move(dx, dy);
            
                                if (currentState.isWall(newPlayerPos.x(), newPlayerPos.y())) continue;
                                Set<Point> newBoxes = new HashSet<>(currentState.getBoxes());
            
                                if (newBoxes.contains(newPlayerPos)) {
                                    Point newBoxPos = newPlayerPos.move(dx, dy);
                                    if (currentState.isWall(newBoxPos.x(), newBoxPos.y()) || newBoxes.contains(newBoxPos)) continue;
                                    newBoxes.remove(newPlayerPos);
                                    newBoxes.add(newBoxPos);
                                }
            
                                SokobanState nextState = new SokobanState(currentState.getWalls(), currentState.getTargets(), newPlayerPos, newBoxes);
                                if (!closedSet.contains(nextState)) {
                                    int gNode = current.g + 1;
                                    int hNode = heuristic.heur(nextState);
                                    openSet.add(new Node(nextState, gNode, gNode + hNode));
                                }
                            }
                        }
                        printResult(true, false, nodesExpanded, System.currentTimeMillis() - startTime, "Search exhausted.");
                    } catch (Exception e) {
                        printResult(false, false, 0, 0, e.getMessage());
                    }
                }
            
                private static void printResult(boolean success, boolean solved, long nodes, long timeMs, String error) {
                    String errorStr = error == null ? "null" : "\\\"" + error.replace("\\\"", "\\\\\\\"") + "\\\"";
                    System.out.println("{\\\"success\\\":" + success + ", \\\"solved\\\":" + solved + ", \\\"nodesExpanded\\\":" + nodes + ", \\\"executionTimeMs\\\":" + timeMs + ", \\\"errorMessage\\\":" + errorStr + "}");
                }
            
                public static SokobanState parse(String content) {
                    String[] lines = content.split("\\\\r?\\\\n");
                    int height = lines.length;
                    int width = 0;
                    for (String line : lines) width = Math.max(width, line.length());
            
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
                                case '#': walls[y][x] = true; break;
                                case '@': player = p; break;
                                case '+': player = p; targets.add(p); break;
                                case '$': boxes.add(p); break;
                                case '*': boxes.add(p); targets.add(p); break;
                                case '.': targets.add(p); break;
                                case ' ': break;
                            }
                        }
                    }
                    return new SokobanState(walls, targets, player, boxes);
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
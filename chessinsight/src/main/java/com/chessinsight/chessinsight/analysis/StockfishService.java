
package com.chessinsight.chessinsight.analysis;

import com.chessinsight.chessinsight.analysis.records.AnalysisResult;
import com.chessinsight.chessinsight.analysis.records.Candidate;
import com.chessinsight.chessinsight.analysis.records.CandidateSnapshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Service
public class StockfishService {

    private static final int MAX_DEPTH = 20;
    private static final int MULTI_PV = 3;
    private static final long TIMEOUT_SECONDS = 60;
    private static final String EOF = "__STOCKFISH_EOF__";

    private final String enginePath;

    public StockfishService(
        @Value("${stockfish.engine-path}") String enginePath) {
        this.enginePath = enginePath;
    }

    public synchronized AnalysisResult analyze(
        String fen, int depth)
        throws IOException, InterruptedException {

        return analyzeMany(List.of(fen), depth).get(0);
    }

    public synchronized List<AnalysisResult> analyzeMany(
        List<String> fens, int depth)
        throws IOException, InterruptedException {

        if (fens == null || fens.isEmpty()) {
            throw new IllegalArgumentException(
                "At least one FEN is required.");
        }

        if (fens.stream().anyMatch(
            fen -> fen == null || fen.isBlank())) {
            throw new IllegalArgumentException(
                "FEN must not be empty.");
        }

        if (depth < 1 || depth > MAX_DEPTH) {
            throw new IllegalArgumentException(
                "Depth must be between 1 and " + MAX_DEPTH + ".");
        }

        for (String fen : fens) {
            String[] fields = fen.trim().split("\\s+");

            if (fields.length < 2
                || !(fields[1].equals("w")
                || fields[1].equals("b"))) {
                throw new IllegalArgumentException(
                    "Invalid FEN side-to-move field.");
            }
        }

        Process process = new ProcessBuilder(enginePath)
            .redirectErrorStream(true)
            .start();

        BlockingQueue<String> output =
            new LinkedBlockingQueue<>();

        Thread readerThread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    process.getInputStream(),
                    StandardCharsets.UTF_8))) {

                String line;

                while ((line = reader.readLine()) != null) {
                    output.offer(line);
                }

            } catch (IOException ignored) {

            } finally {
                output.offer(EOF);
            }
        });

        readerThread.setDaemon(true);
        readerThread.start();

        try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(
                process.getOutputStream(),
                StandardCharsets.UTF_8))) {

            try {
                send(writer, "uci");
                await(output,"uciok", 15);

                send(writer,
                    "setoption name MultiPV value " + MULTI_PV);
                send(writer, "isready");
                await(output, "readyok", 15);

                List<AnalysisResult> results =
                    new ArrayList<>();

                for (String fen : fens) {
                    String normalizedFen = fen.trim();
                    boolean whiteToMove =
                        normalizedFen.split("\\s+")[1].equals("w");

                    send(writer,
                        "position fen " + normalizedFen);
                    send(writer, "go depth " + depth);

                    Map<Integer, CandidateSnapshot> candidates =
                        new HashMap<>();

                    String bestMove = null;

                    long deadline = System.nanoTime()
                        + TimeUnit.SECONDS.toNanos(
                        TIMEOUT_SECONDS);

                    while (true) {
                        long remaining =
                            deadline - System.nanoTime();

                        if (remaining <= 0) {
                            send(writer, "stop");
                            throw new IOException(
                                "Stockfish analysis timed out.");
                        }

                        String line = output.poll(
                            remaining, TimeUnit.NANOSECONDS);

                        if (line == null) {
                            send(writer, "stop");
                            throw new IOException(
                                "Stockfish analysis timed out.");
                        }

                        if (line.equals(EOF)) {
                            throw new IOException(
                                "Stockfish terminated unexpectedly.");
                        }

                        if (line.startsWith("info ")) {
                            CandidateSnapshot candidate =
                                parseInfo(line, whiteToMove);

                            if (candidate != null) {
                                CandidateSnapshot previous =
                                    candidates.get(candidate.rank());

                                if (previous == null
                                    || candidate.depth()
                                    >= previous.depth()) {
                                    candidates.put(
                                        candidate.rank(), candidate);
                                }
                            }
                        }

                        if (line.startsWith("bestmove ")) {
                            String[] tokens = line.split("\\s+");
                            bestMove = tokens.length > 1
                                ? tokens[1] : null;
                            break;
                        }
                    }

                    List<Candidate> rankedCandidates =
                        candidates.values().stream()
                            .sorted(Comparator.comparingInt(
                                CandidateSnapshot::rank))
                            .map(candidate -> new Candidate(
                                candidate.rank(),
                                candidate.depth(),
                                candidate.principalVariation().get(0),
                                candidate.principalVariation(),
                                candidate.centipawns(),
                                candidate.mateIn()))
                            .toList();

                    if (rankedCandidates.isEmpty()
                        && bestMove != null
                        && !bestMove.equals("(none)")) {
                        throw new IOException(
                            "Stockfish returned no candidate moves.");
                    }

                    results.add(new AnalysisResult(
                        normalizedFen,
                        depth,
                        bestMove,
                        rankedCandidates));
                }

                return results;

            } finally {
                try {
                    send(writer, "quit");
                } catch (IOException ignored) {

                }
            }

        } finally {
            if (process.isAlive()
                && !process.waitFor(2, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                process.waitFor(2, TimeUnit.SECONDS);
            }
        }
    }

    private CandidateSnapshot parseInfo(
        String line, boolean whiteToMove) {

        String[] tokens = line.split("\\s+");

        int depth = -1;
        int rank = 1;

        Integer score = null;
        Integer mate = null;
        String scoreType = null;

        List<String> pv = List.of();

        for (int i = 1; i < tokens.length; i++) {
            switch (tokens[i]) {
                case "depth" -> {
                    if (i + 1 < tokens.length) {
                        depth = Integer.parseInt(tokens[++i]);
                    }
                }

                case "multipv" -> {
                    if (i + 1 < tokens.length) {
                        rank = Integer.parseInt(tokens[++i]);
                    }
                }

                case "score" -> {
                    if (i + 2 < tokens.length) {
                        scoreType = tokens[++i];

                        int value =
                            Integer.parseInt(tokens[++i]);

                        if (!whiteToMove) {
                            value = -value;
                        }

                        if (scoreType.equals("cp")) {
                            score = value;
                        } else if (scoreType.equals("mate")) {
                            mate = value;
                        }
                    }
                }

                case "pv" -> {
                    List<String> moves = new ArrayList<>();

                    for (int j = i + 1;
                         j < tokens.length; j++) {
                        moves.add(tokens[j]);
                    }

                    pv = List.copyOf(moves);
                    i = tokens.length;
                }

                default -> {
                }
            }
        }

        if (depth < 1 || scoreType == null || pv.isEmpty()) {
            return null;
        }

        return new CandidateSnapshot(
            rank, depth, score, mate, pv);
    }

    private void send(
        BufferedWriter writer, String command)
        throws IOException {

        writer.write(command);
        writer.newLine();
        writer.flush();
    }

    private void await(
        BlockingQueue<String> output,
        String expected,
        long timeoutSeconds)
        throws IOException, InterruptedException {

        long deadline = System.nanoTime()
            + TimeUnit.SECONDS.toNanos(timeoutSeconds);

        while (true) {
            long remaining = deadline - System.nanoTime();

            if (remaining <= 0) {
                throw new IOException(
                    "Timed out waiting for Stockfish: " + expected);
            }

            String line = output.poll(
                remaining, TimeUnit.NANOSECONDS);

            if (line == null) {
                throw new IOException(
                    "Timed out waiting for Stockfish: " + expected);
            }

            if (line.equals(EOF)) {
                throw new IOException(
                    "Stockfish exited before sending " + expected);
            }

            if (line.equals(expected)) {
                return;
            }
        }
    }
}

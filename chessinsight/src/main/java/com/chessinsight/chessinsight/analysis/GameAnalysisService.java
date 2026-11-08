
package com.chessinsight.chessinsight.analysis;

import com.chessinsight.chessinsight.analysis.records.AnalysisResult;
import com.chessinsight.chessinsight.analysis.records.Candidate;
import com.chessinsight.chessinsight.analysis.records.GameAnalysisResponse;
import com.chessinsight.chessinsight.analysis.records.MoveAnalysis;
import com.chessinsight.chessinsight.analysis.records.PositionEvaluation;
import com.chessinsight.chessinsight.service.ChessGameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GameAnalysisService {

    private final ChessGameService chessGameService;
    private final StockfishService stockfishService;
    private final EvaluationLossService evaluationLossService;
    private final MoveClassificationService moveClassificationService;
    private final GameAccuracyService gameAccuracyService;

    @Transactional(readOnly = true)
    public List<String> getPositions(Long gameId) {
        return chessGameService.getPositionsById(gameId);
    }

    @Transactional(readOnly = true)
    public List<AnalysisResult> analyzePositions(
        Long gameId, int depth)
        throws IOException, InterruptedException {

        List<String> positions = getPositions(gameId);
        return stockfishService.analyzeMany(positions, depth);
    }

    private GameAnalysisData analyzeGameData(
        Long gameId, int depth)
        throws IOException, InterruptedException {

        List<String> sanMoves =
            chessGameService.getMovesById(gameId);

        List<String> uciMoves =
            chessGameService.getUciMovesById(gameId);

        List<String> positions =
            chessGameService.getPositionsById(gameId);

        if (sanMoves.size() != uciMoves.size()
            || positions.size() != sanMoves.size() + 1) {
            throw new IllegalStateException(
                "Move and position counts do not match."
            );
        }

        List<AnalysisResult> evaluations =
            stockfishService.analyzeMany(positions, depth);

        if (evaluations.size() != positions.size()) {
            throw new IllegalStateException(
                "Stockfish did not return an evaluation for every position."
            );
        }

        List<MoveAnalysis> results = new ArrayList<>();

        for (int i = 0; i < sanMoves.size(); i++) {
            AnalysisResult before = evaluations.get(i);
            AnalysisResult after = evaluations.get(i + 1);

            String[] fenParts = positions.get(i).trim().split("\\s+");

            if (fenParts.length < 2
                || (!fenParts[1].equals("w")
                && !fenParts[1].equals("b"))) {
                throw new IllegalStateException(
                    "A game position has an invalid active color."
                );
            }

            String mover = fenParts[1].equals("w")
                ? "WHITE"
                : "BLACK";

            Candidate bestBefore = getBestCandidate(before);
            Candidate bestAfter = getBestCandidate(after);

            String engineBestMove = bestBefore == null
                ? null
                : bestBefore.bestMove();

            Integer loss = null;
            String classification = "UNAVAILABLE";

            String mateClassification = classifyMateTransition(
                bestBefore, bestAfter, mover
            );

            boolean playedIsBestMove = uciMoves.get(i).equalsIgnoreCase(engineBestMove);

            if (mateClassification != null) {
                if ((mateClassification.equals("MATE_FOUND")
                    || mateClassification.equals("MATE_CLOSER"))
                    && !playedIsBestMove) {
                    try {
                        loss = evaluationLossService
                            .calculateCentipawnLoss(before, after);

                        classification =
                            moveClassificationService.classify(loss);
                    } catch (IllegalArgumentException ignored) {
                        classification = "UNAVAILABLE";
                    }
                } else if ((mateClassification.equals("MATE_FOUND")
                    || mateClassification.equals("MATE_CLOSER"))
                    && playedIsBestMove) {
                    classification = "BEST";
                } else {
                    classification = mateClassification;
                }
            } else {
                try {
                    loss = evaluationLossService
                        .calculateCentipawnLoss(before, after);

                    classification =
                        moveClassificationService.classify(loss);

                    if (uciMoves.get(i).equalsIgnoreCase(engineBestMove)) {
                        classification = "BEST";
                    }

                } catch (IllegalArgumentException ignored) {

                }
            }

            results.add(new MoveAnalysis(
                i + 1,
                sanMoves.get(i),
                mover,
                engineBestMove,
                loss,
                classification,
                toPositionEvaluation(after),
                getCandidateLines(after)
            ));
        }

        return new GameAnalysisData(
            toPositionEvaluation(evaluations.get(0)),
            results
        );
    }

    @Transactional(readOnly = true)
    public List<MoveAnalysis> analyzeGame(
        Long gameId,
        int depth
    ) throws IOException, InterruptedException {
        return analyzeGameData(gameId, depth).moves();
    }

    private Candidate getBestCandidate(AnalysisResult result) {
        if (result == null || result.candidates() == null) {
            return null;
        }

        return result.candidates().stream()
            .filter(candidate -> candidate != null
                && candidate.rank() == 1)
            .findFirst()
            .orElse(null);
    }

    private String classifyMateTransition(
        Candidate before,
        Candidate after,
        String mover) {

        Integer beforeMate = before == null ? null : before.mateIn();
        Integer afterMate = after == null ? null : after.mateIn();

        boolean beforeHasMate = beforeMate != null;
        boolean afterHasMate = afterMate != null;

        if (!beforeHasMate && !afterHasMate) {
            return null;
        }

        int moverSign = mover.equals("WHITE") ? 1 : -1;

        boolean beforeMateForMover = beforeHasMate
            && Integer.signum(beforeMate) == moverSign;

        boolean afterMateForMover = afterHasMate
            && Integer.signum(afterMate) == moverSign;

        boolean beforeMateAgainstMover = beforeHasMate
            && Integer.signum(beforeMate) == -moverSign;

        boolean afterMateAgainstMover = afterHasMate
            && Integer.signum(afterMate) == -moverSign;

        if (afterMateAgainstMover && !beforeMateAgainstMover) {
            return "MATE_BLUNDER";
        }

        if (afterMateForMover && !beforeMateForMover) {
            return "MATE_FOUND";
        }

        if (beforeMateForMover && !afterMateForMover) {
            return "MATE_LOST";
        }

        if (beforeMateAgainstMover && !afterMateAgainstMover) {
            return "MATE_AVOIDED";
        }

        if (beforeHasMate && afterHasMate
            && Integer.signum(beforeMate)
            == Integer.signum(afterMate)) {

            int beforeDistance = Math.abs(beforeMate);
            int afterDistance = Math.abs(afterMate);

            if (afterDistance < beforeDistance) {
                return "MATE_CLOSER";
            }

            if (afterDistance > beforeDistance) {
                return "MATE_FARTHER";
            }

            return "MATE_EVALUATION";
        }

        return "MATE_EVALUATION";
    }


    @Transactional(readOnly = true)
    public GameAnalysisResponse analyzeGameWithAccuracy(
        Long gameId,
        int depth
    ) throws IOException, InterruptedException {

        GameAnalysisData data = analyzeGameData(gameId, depth);
        List<MoveAnalysis> moves = data.moves();

        double whiteAccuracy =
            gameAccuracyService.calculateAccuracy(moves, "WHITE");

        double blackAccuracy =
            gameAccuracyService.calculateAccuracy(moves, "BLACK");

        return new GameAnalysisResponse(
            whiteAccuracy,
            blackAccuracy,
            data.initialPositionEvaluation(),
            moves
        );
    }


    private PositionEvaluation toPositionEvaluation(
        AnalysisResult result
    ) {
        Candidate candidate = getBestCandidate(result);

        if (candidate == null) {
            return null;
        }

        return new PositionEvaluation(
            candidate.centipawns(),
            candidate.mateIn()
        );
    }

    private record GameAnalysisData(
        PositionEvaluation initialPositionEvaluation,
        List<MoveAnalysis> moves
    ) {}

    private List<Candidate> getCandidateLines(AnalysisResult result) {
        if (result == null || result.candidates() == null) {
            return List.of();
        }

        return result.candidates().stream()
            .filter(Objects::nonNull)
            .sorted(java.util.Comparator.comparingInt(Candidate::rank))
            .limit(3)
            .toList();
    }

}



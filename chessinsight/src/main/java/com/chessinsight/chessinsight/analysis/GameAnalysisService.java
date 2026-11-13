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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GameAnalysisService {

    private static final int MATE_PENALTY = 100_000;

    private final ChessGameService chessGameService;
    private final StockfishService stockfishService;
    private final EvaluationLossService evaluationLossService;
    private final MoveClassificationService moveClassificationService;
    private final GameAccuracyService gameAccuracyService;

    private GameAnalysisData analyzeGameData(
        Long gameId,
        int depth
    ) throws IOException, InterruptedException {

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

            String activeColor = getActiveColor(positions.get(i));

            String mover = activeColor.equals("w")
                ? "WHITE"
                : "BLACK";

            Candidate bestBefore = getBestCandidate(before);

            String engineBestMove = bestBefore == null
                ? null
                : bestBefore.bestMove();

            Integer loss = null;
            String classification = "UNAVAILABLE";

            String mateClassification =
                classifyMateTransition(before, after, mover);

            try {
                loss = evaluationLossService
                    .calculateCentipawnLoss(before, after);
            } catch (IllegalArgumentException ignored) {

            }

            if (mateClassification != null) {
                classification = mateClassification;

                if (loss == null
                    && isSevereMateMistake(mateClassification)) {
                    loss = MATE_PENALTY;
                }

            } else if (loss != null) {
                classification =
                    moveClassificationService.classify(loss);
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
        AnalysisResult before,
        AnalysisResult after,
        String mover
    ) {
        MateEvaluation beforeMate = getMateEvaluation(before);
        MateEvaluation afterMate = getMateEvaluation(after);

        if (beforeMate == null && afterMate == null) {
            return null;
        }

        int moverSign = mover.equals("WHITE") ? 1 : -1;

        boolean beforeMateForMover =
            beforeMate != null
                && beforeMate.whiteSign() == moverSign;

        boolean afterMateForMover =
            afterMate != null
                && afterMate.whiteSign() == moverSign;

        boolean beforeMateAgainstMover =
            beforeMate != null
                && beforeMate.whiteSign() == -moverSign;

        boolean afterMateAgainstMover =
            afterMate != null
                && afterMate.whiteSign() == -moverSign;

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

        if (beforeMate != null
            && afterMate != null
            && beforeMate.whiteSign() == afterMate.whiteSign()) {

            if (afterMate.distance() < beforeMate.distance()) {
                return "MATE_CLOSER";
            }

            if (afterMate.distance() > beforeMate.distance()) {
                return "MATE_FARTHER";
            }

            return "MATE_EVALUATION";
        }

        return "MATE_EVALUATION";
    }

    private MateEvaluation getMateEvaluation(AnalysisResult result) {
        Candidate candidate = getBestCandidate(result);

        if (candidate == null || candidate.mateIn() == null) {
            return null;
        }

        int mate = candidate.mateIn();

        int whiteSign;

        if (mate == 0) {
            whiteSign = getActiveColor(result.fen()).equals("w")
                ? -1
                : 1;
        } else {
            whiteSign = Integer.signum(mate);
        }

        return new MateEvaluation(
            whiteSign,
            Math.abs(mate)
        );
    }

    private boolean isSevereMateMistake(String classification) {
        return classification.equals("MATE_LOST")
            || classification.equals("MATE_BLUNDER");
    }

    private String getActiveColor(String fen) {
        if (fen == null) {
            throw new IllegalArgumentException("FEN is required.");
        }

        String[] fenParts = fen.trim().split("\\s+");

        if (fenParts.length < 2
            || (!fenParts[1].equals("w")
            && !fenParts[1].equals("b"))) {
            throw new IllegalStateException(
                "A game position has an invalid active color."
            );
        }

        return fenParts[1];
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

    private List<Candidate> getCandidateLines(AnalysisResult result) {
        if (result == null || result.candidates() == null) {
            return List.of();
        }

        return result.candidates().stream()
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingInt(Candidate::rank))
            .limit(3)
            .toList();
    }

    private record MateEvaluation(
        int whiteSign,
        int distance
    ) {}

    private record GameAnalysisData(
        PositionEvaluation initialPositionEvaluation,
        List<MoveAnalysis> moves
    ) {}
}
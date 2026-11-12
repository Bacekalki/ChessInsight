
package com.chessinsight.chessinsight.analysis;

import com.chessinsight.chessinsight.analysis.records.MoveAnalysis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameAccuracyServiceTest {

    private GameAccuracyService service;

    @BeforeEach
    void setUp() {
        service = new GameAccuracyService();
    }

    @Test
    void shouldReturn100PercentWhenCentipawnLossIsZero() {
        List<MoveAnalysis> moves = List.of(
            move(1, "WHITE", 0, "BEST")
        );

        double accuracy = service.calculateAccuracy(moves, "WHITE");

        assertEquals(100.0, accuracy, 0.01);
    }

    @Test
    void shouldReturnLowerAccuracyForHigherCentipawnLoss() {
        List<MoveAnalysis> accurateMoves = List.of(
            move(1, "WHITE", 10, "EXCELLENT")
        );

        List<MoveAnalysis> inaccurateMoves = List.of(
            move(1, "WHITE", 100, "INACCURACY")
        );

        double accurateResult =
            service.calculateAccuracy(accurateMoves, "WHITE");
        double inaccurateResult =
            service.calculateAccuracy(inaccurateMoves, "WHITE");

        assertTrue(accurateResult > inaccurateResult);
        assertTrue(accurateResult < 100.0);
        assertTrue(inaccurateResult >= 0.0);
    }

    @Test
    void shouldCalculateAccuracySeparatelyForWhiteAndBlack() {
        List<MoveAnalysis> moves = List.of(
            move(1, "WHITE", 0, "BEST"),
            move(2, "BLACK", 100, "INACCURACY"),
            move(3, "WHITE", 50, "GOOD"),
            move(4, "BLACK", 200, "BLUNDER")
        );

        double whiteAccuracy =
            service.calculateAccuracy(moves, "WHITE");
        double blackAccuracy =
            service.calculateAccuracy(moves, "BLACK");

        assertEquals(80.33, whiteAccuracy, 0.01);
        assertEquals(25.16, blackAccuracy, 0.01);
        assertTrue(whiteAccuracy > blackAccuracy);
    }

    @Test
    void shouldReturnZeroWhenThereAreNoMoves() {
        assertEquals(0.0, service.calculateAccuracy(List.of(), "WHITE"));
    }

    @Test
    void shouldReturnZeroWhenMovesAreNull() {
        assertEquals(0.0, service.calculateAccuracy(null, "WHITE"));
    }

    @Test
    void shouldIgnoreMovesWithMissingCentipawnLoss() {
        List<MoveAnalysis> moves = List.of(
            move(1, "WHITE", null, "BEST"),
            move(2, "WHITE", 0, "BEST")
        );

        double accuracy = service.calculateAccuracy(moves, "WHITE");

        assertEquals(100.0, accuracy, 0.01);
    }

    @Test
    void shouldIgnoreNegativeCentipawnLoss() {
        List<MoveAnalysis> moves = List.of(
            move(1, "WHITE", -10, "GOOD"),
            move(2, "WHITE", 0, "BEST")
        );

        double accuracy = service.calculateAccuracy(moves, "WHITE");

        assertEquals(100.0, accuracy, 0.01);
    }

    @Test
    void shouldIgnoreMateClassifications() {
        List<MoveAnalysis> moves = List.of(
            move(1, "WHITE", 150, "MATE_FOUND"),
            move(2, "WHITE", 0, "BEST")
        );

        double accuracy = service.calculateAccuracy(moves, "WHITE");

        assertEquals(61.16, accuracy, 0.01);
    }

    private MoveAnalysis move(
        int ply,
        String mover,
        Integer centipawnLoss,
        String classification
    ) {
        return new MoveAnalysis(
            ply,
            "e2e4",
            mover,
            "e2e4",
            centipawnLoss,
            classification,
            null,
            null
        );
    }
}

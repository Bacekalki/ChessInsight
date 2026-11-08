package com.chessinsight.chessinsight.analysis;

import com.chessinsight.chessinsight.analysis.records.AnalysisResult;
import com.chessinsight.chessinsight.analysis.records.Candidate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluationLossServiceTest {

    private final EvaluationLossService service =
        new EvaluationLossService();

    private AnalysisResult result(String fen, int cp) {
        Candidate best = new Candidate(
            1, 10, "e2e4", List.of("e2e4"), cp, null
        );

        return new AnalysisResult(fen, 10, "e2e4", List.of(best));
    }

    private AnalysisResult mateResult(String fen, int mateIn) {
        Candidate best = new Candidate(
            1, 10, "e2e4", List.of("e2e4"), null, mateIn
        );

        return new AnalysisResult(fen, 10, "e2e4", List.of(best));
    }

    private AnalysisResult resultWithoutCandidates(String fen) {
        return new AnalysisResult(fen, 10, "e2e4", List.of());
    }

    @Test
    void shouldCalculateWhiteMoveLoss() {
        AnalysisResult before = result(
            "start w KQkq - 0 1", 50
        );
        AnalysisResult after = result(
            "start b KQkq - 0 1", 10
        );

        assertEquals(
            40,
            service.calculateCentipawnLoss(before, after)
        );
    }

    @Test
    void shouldCalculateBlackMoveLoss() {
        AnalysisResult before = result(
            "start b KQkq - 0 1", 50
        );
        AnalysisResult after = result(
            "start w KQkq - 0 1", 80
        );

        assertEquals(
            30,
            service.calculateCentipawnLoss(before, after)
        );
    }

    @Test
    void shouldReturnZeroWhenWhiteImproves() {
        AnalysisResult before = result(
            "start w KQkq - 0 1", 20
        );
        AnalysisResult after = result(
            "start b KQkq - 0 1", 60
        );

        assertEquals(
            0,
            service.calculateCentipawnLoss(before, after)
        );
    }

    @Test
    void shouldReturnZeroWhenBlackImproves() {
        AnalysisResult before = result(
            "start b KQkq - 0 1", 80
        );
        AnalysisResult after = result(
            "start w KQkq - 0 1", 30
        );

        assertEquals(
            0,
            service.calculateCentipawnLoss(before, after)
        );
    }


    @Test
    void shouldDetectLossOfForcedMate() {
        AnalysisResult before = mateResult(
            "start w KQkq - 0 1", 3
        );
        AnalysisResult after = mateResult(
            "start b KQkq - 0 1", -2
        );

        assertTrue(
            service.calculateCentipawnLoss(before, after) > 100_000
        );
    }

    @Test
    void shouldReturnZeroWhenForcedMateGetsCloser() {
        AnalysisResult before = mateResult(
            "start w KQkq - 0 1", 3
        );
        AnalysisResult after = mateResult(
            "start b KQkq - 0 1", 2
        );

        assertEquals(
            0,
            service.calculateCentipawnLoss(before, after)
        );
    }

    @Test
    void shouldRejectInvalidActiveColor() {
        AnalysisResult before = result(
            "start x KQkq - 0 1", 50
        );
        AnalysisResult after = result(
            "start b KQkq - 0 1", 10
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> service.calculateCentipawnLoss(before, after)
        );
    }

    @Test
    void shouldRejectMissingCandidates() {
        AnalysisResult before = resultWithoutCandidates(
            "start w KQkq - 0 1"
        );
        AnalysisResult after = result(
            "start b KQkq - 0 1", 10
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> service.calculateCentipawnLoss(before, after)
        );
    }

    @Test
    void shouldRejectMissingBestCandidate() {
        Candidate second = new Candidate(
            2, 10, "d2d4", List.of("d2d4"), 30, null
        );

        AnalysisResult before = new AnalysisResult(
            "start w KQkq - 0 1", 10, "d2d4", List.of(second)
        );
        AnalysisResult after = result(
            "start b KQkq - 0 1", 10
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> service.calculateCentipawnLoss(before, after)
        );
    }

}
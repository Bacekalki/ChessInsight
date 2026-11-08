package com.chessinsight.chessinsight.analysis;

import com.chessinsight.chessinsight.analysis.records.AnalysisResult;
import com.chessinsight.chessinsight.analysis.records.Candidate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StockfishServiceTest {

    @Autowired
    private StockfishService stockfishService;

    private static final String START_FEN =
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private static final String AFTER_E4_FEN =
        "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1";

    @Test
    void shouldAnalyzeSinglePosition() throws Exception {
        AnalysisResult result =
            stockfishService.analyze(START_FEN, 10);

        assertNotNull(result);
        assertEquals(START_FEN, result.fen());
        assertEquals(10, result.depth());
        assertNotNull(result.bestMove());
        assertFalse(result.bestMove().isBlank());
        assertNotNull(result.candidates());
        assertFalse(result.candidates().isEmpty());

        System.out.println("Single-position result:");
        System.out.println(result);
    }

    @Test
    void shouldAnalyzeMultiplePositionsUsingOneRequest()
        throws Exception {

        List<AnalysisResult> results =
            stockfishService.analyzeMany(
                List.of(START_FEN, AFTER_E4_FEN), 10);

        assertEquals(2, results.size());

        assertEquals(START_FEN, results.get(0).fen());
        assertEquals(AFTER_E4_FEN, results.get(1).fen());

        for (AnalysisResult result : results) {
            assertNotNull(result.bestMove());
            assertFalse(result.bestMove().isBlank());
            assertEquals(10, result.depth());
            assertNotNull(result.candidates());
            assertFalse(result.candidates().isEmpty());

            System.out.println(result);
        }
    }

    @Test
    void shouldReturnUpToThreeCandidates() throws Exception {
        AnalysisResult result =
            stockfishService.analyze(START_FEN, 10);

        assertTrue(result.candidates().size() <= 3);
        assertFalse(result.candidates().isEmpty());

        List<Integer> ranks = result.candidates().stream()
            .map(Candidate::rank)
            .toList();

        assertEquals(
            ranks.stream().sorted().toList(),
            ranks);
    }

    @Test
    void shouldRejectInvalidDepth() {
        assertThrows(
            IllegalArgumentException.class,
            () -> stockfishService.analyze(START_FEN, 0));

        assertThrows(
            IllegalArgumentException.class,
            () -> stockfishService.analyze(START_FEN, 21));
    }

    @Test
    void shouldRejectBlankFen() {
        assertThrows(
            IllegalArgumentException.class,
            () -> stockfishService.analyze(" ", 10));
    }

    @Test
    void shouldRejectEmptyFenList() {
        assertThrows(
            IllegalArgumentException.class,
            () -> stockfishService.analyzeMany(List.of(), 10));
    }

    @Test
    void shouldRejectBlankFenInList() {
        assertThrows(
            IllegalArgumentException.class,
            () -> stockfishService.analyzeMany(
                List.of(START_FEN, " "), 10));
    }
}

package com.chessinsight.chessinsight.analysis;

import com.chessinsight.chessinsight.analysis.records.AnalysisResult;
import com.chessinsight.chessinsight.analysis.records.Candidate;
import com.chessinsight.chessinsight.analysis.records.GameAnalysisResponse;
import com.chessinsight.chessinsight.analysis.records.MoveAnalysis;
import com.chessinsight.chessinsight.service.ChessGameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameAnalysisServiceTest {

    @Mock
    private ChessGameService chessGameService;

    @Mock
    private StockfishService stockfishService;

    @Mock
    private EvaluationLossService evaluationLossService;

    @Mock
    private MoveClassificationService moveClassificationService;

    @Mock
    private GameAccuracyService gameAccuracyService;

    @InjectMocks
    private GameAnalysisService gameAnalysisService;

    @Test
    void topEngineMoveFindsMateGetsBest() throws Exception {
        String start = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        String afterMate = "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3";
        List<String> sanMoves = List.of("e4");
        List<String> uciMoves = List.of("e2e4");
        List<String> positions = List.of(start, afterMate);

        Candidate bestBefore = new Candidate(1, 20, "e2e4", List.of("e2e4"), 20, null);
        Candidate bestAfter = new Candidate(1, 20, "d1h5", List.of("d1h5"), null, 1);

        AnalysisResult before = new AnalysisResult(start, 20, "e2e4", List.of(bestBefore));
        AnalysisResult after = new AnalysisResult(afterMate, 20, "d1h5", List.of(bestAfter));

        when(chessGameService.getMovesById(1L)).thenReturn(sanMoves);
        when(chessGameService.getUciMovesById(1L)).thenReturn(uciMoves);
        when(chessGameService.getPositionsById(1L)).thenReturn(positions);
        when(stockfishService.analyzeMany(positions, 12)).thenReturn(List.of(before, after));

        List<MoveAnalysis> result = gameAnalysisService.analyzeGame(1L, 12);
        assertEquals("BEST", result.get(0).classification());
    }

    @Test
    void nonBestMoveFindingMateDoesNotGetBest() throws Exception {
        String start = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        String afterMate = "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3";
        List<String> sanMoves = List.of("a3");
        List<String> uciMoves = List.of("a2a3");
        List<String> positions = List.of(start, afterMate);

        Candidate bestBefore = new Candidate(1, 20, "e2e4", List.of("e2e4"), 20, null);
        Candidate bestAfter = new Candidate(1, 20, "d1h5", List.of("d1h5"), null, 1);

        AnalysisResult before = new AnalysisResult(start, 20, "e2e4", List.of(bestBefore));
        AnalysisResult after = new AnalysisResult(afterMate, 20, "d1h5", List.of(bestAfter));

        when(chessGameService.getMovesById(1L)).thenReturn(sanMoves);
        when(chessGameService.getUciMovesById(1L)).thenReturn(uciMoves);
        when(chessGameService.getPositionsById(1L)).thenReturn(positions);
        when(stockfishService.analyzeMany(positions, 12)).thenReturn(List.of(before, after));
        when(evaluationLossService.calculateCentipawnLoss(before, after)).thenReturn(150);
        when(moveClassificationService.classify(150)).thenReturn("MISTAKE");

        List<MoveAnalysis> result = gameAnalysisService.analyzeGame(1L, 12);
        assertEquals("MISTAKE", result.get(0).classification());
    }

    @Test
    void nonBestMoveShorteningMateDoesNotGetBest() throws Exception {
        String beforeMate = "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2";
        String afterMateCloser = "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3";
        List<String> sanMoves = List.of("h4");
        List<String> uciMoves = List.of("h2h4");
        List<String> positions = List.of(beforeMate, afterMateCloser);

        Candidate bestBefore = new Candidate(1, 20, "d1h5", List.of("d1h5"), null, 3);
        Candidate bestAfter = new Candidate(1, 20, "d1h5", List.of("d1h5"), null, 1);

        AnalysisResult before = new AnalysisResult(beforeMate, 20, "d1h5", List.of(bestBefore));
        AnalysisResult after = new AnalysisResult(afterMateCloser, 20, "d1h5", List.of(bestAfter));

        when(chessGameService.getMovesById(1L)).thenReturn(sanMoves);
        when(chessGameService.getUciMovesById(1L)).thenReturn(uciMoves);
        when(chessGameService.getPositionsById(1L)).thenReturn(positions);
        when(stockfishService.analyzeMany(positions, 12)).thenReturn(List.of(before, after));
        when(evaluationLossService.calculateCentipawnLoss(before, after)).thenReturn(100);
        when(moveClassificationService.classify(100)).thenReturn("INACCURACY");

        List<MoveAnalysis> result = gameAnalysisService.analyzeGame(1L, 12);
        assertEquals("INACCURACY", result.get(0).classification());
    }

    @Test
    void losingMateRemainsMateLost() throws Exception {
        String beforeMate = "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3";
        String after = "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP1PP/RNBQKBNR b KQkq - 0 3";
        List<String> sanMoves = List.of("g3");
        List<String> uciMoves = List.of("g2g3");
        List<String> positions = List.of(beforeMate, after);

        Candidate bestBefore = new Candidate(1, 20, "g2g3", List.of("g2g3"), null, 1);
        Candidate bestAfter = new Candidate(
            1, 20, "g1f3", List.of("g1f3"), null, null
        );

        AnalysisResult before = new AnalysisResult(beforeMate, 20, "g2g3", List.of(bestBefore));
        AnalysisResult afterRes = new AnalysisResult(after, 20, "g1f3", List.of(bestAfter));

        when(chessGameService.getMovesById(1L)).thenReturn(sanMoves);
        when(chessGameService.getUciMovesById(1L)).thenReturn(uciMoves);
        when(chessGameService.getPositionsById(1L)).thenReturn(positions);
        when(stockfishService.analyzeMany(positions, 12)).thenReturn(List.of(before, afterRes));

        List<MoveAnalysis> result = gameAnalysisService.analyzeGame(1L, 12);
        assertEquals("MATE_LOST", result.get(0).classification());
    }

    @Test
    void mateAgainstPlayerIsBlunder() throws Exception {
        String start = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        String afterBlunder = "rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3";
        List<String> sanMoves = List.of("f3");
        List<String> uciMoves = List.of("f2f3");
        List<String> positions = List.of(start, afterBlunder);

        Candidate bestBefore = new Candidate(1, 20, "e2e4", List.of("e2e4"), 20, null);
        Candidate bestAfter = new Candidate(1, 20, "d1h5", List.of("d1h5"), null, -1);

        AnalysisResult before = new AnalysisResult(start, 20, "e2e4", List.of(bestBefore));
        AnalysisResult afterRes = new AnalysisResult(afterBlunder, 20, "d1h5", List.of(bestAfter));

        when(chessGameService.getMovesById(1L)).thenReturn(sanMoves);
        when(chessGameService.getUciMovesById(1L)).thenReturn(uciMoves);
        when(chessGameService.getPositionsById(1L)).thenReturn(positions);
        when(stockfishService.analyzeMany(positions, 12)).thenReturn(List.of(before, afterRes));

        List<MoveAnalysis> result = gameAnalysisService.analyzeGame(1L, 12);
        assertEquals("MATE_BLUNDER", result.get(0).classification());
    }
}

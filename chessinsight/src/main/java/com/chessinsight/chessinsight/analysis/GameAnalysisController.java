package com.chessinsight.chessinsight.analysis;

import com.chessinsight.chessinsight.analysis.records.GameAnalysisResponse;
import com.chessinsight.chessinsight.analysis.records.MoveAnalysis;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/analysis/games")
@RequiredArgsConstructor
public class GameAnalysisController {

    private final GameAnalysisService gameAnalysisService;


    @GetMapping("/{gameId}")
    public GameAnalysisResponse analyzeGame(
        @PathVariable Long gameId,
        @RequestParam(defaultValue = "12") int depth) {

        if (gameId <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Game ID must be positive."
            );
        }

        if (depth < 1 || depth > 20) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Depth must be between 1 and 20."
            );
        }

        try {
            return gameAnalysisService.analyzeGameWithAccuracy(gameId, depth);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Game analysis was interrupted.",
                e
            );
        } catch (IOException e) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Stockfish analysis failed.",
                e
            );
        }
    }

}

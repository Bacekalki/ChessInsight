package com.chessinsight.chessinsight.analysis;


import com.chessinsight.chessinsight.analysis.records.AnalysisRequest;
import com.chessinsight.chessinsight.analysis.records.AnalysisResult;
import com.chessinsight.chessinsight.analysis.records.ErrorResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final StockfishService stockfishService;
    private final FenValidator fenValidator;

    public AnalysisController(
        StockfishService stockfishService,
        FenValidator fenValidator) {
        this.stockfishService = stockfishService;
        this.fenValidator = fenValidator;
    }

    @PostMapping
    public ResponseEntity<?> analyze(
        @Valid @RequestBody AnalysisRequest request) {

        int depth = request.depth() == null ? 12 : request.depth();

        try {
            fenValidator.validate(request.fen());

            AnalysisResult result =
                stockfishService.analyze(request.fen(), depth);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));

        } catch (IOException e) {
            return ResponseEntity.status(
                    HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(
                    "Stockfish analysis failed. Check the engine configuration."));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            return ResponseEntity.status(
                    HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(
                    "Stockfish analysis was interrupted."));
        }
    }
}


package com.chessinsight.chessinsight.controller;

import com.chessinsight.chessinsight.chess.chesscom.ChessComImportService;
import com.chessinsight.chessinsight.chess.lichess.LichessImportService;
import com.chessinsight.chessinsight.chess.pgn.PgnImportReport;
import com.chessinsight.chessinsight.chess.pgn.PgnImportService;
import com.chessinsight.chessinsight.dto.ChessGameDto;
import com.chessinsight.chessinsight.service.ChessGameService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.web.PagedModel;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final PgnImportService importService;
    private final ChessGameService gameService;
    private final ChessComImportService chessComImportService;
    private final LichessImportService lichessImportService;

    @PostMapping(
        value = "/import",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>> importGames(
        @RequestParam("file") MultipartFile file) throws Exception {

        if (file.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "The uploaded file is empty."
            );
        }

        Path tempFile = Files.createTempFile("chessinsight-", ".pgn");

        try {
            file.transferTo(tempFile);

            PgnImportReport report = importService.importGames(tempFile.toFile());

            if (report.imported() == 0
                && report.malformed() == report.totalProcessed()
                && report.malformed() > 0) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The uploaded file contains no valid PGN games."
                );
            }

            int imported = report.imported();
            int skipped = report.skipped();
            int totalProcessed = report.totalProcessed();


            return ResponseEntity.ok(Map.of(
                "message", "Games imported successfully",
                "importedGames", imported,
                "skippedGames", skipped,
                "totalProcessedGames", totalProcessed
            ));
        } finally {
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException e) {
                tempFile.toFile().deleteOnExit();
            }
        }
    }

    @GetMapping
    public PagedModel<ChessGameDto> getGames(
        @RequestParam String username,
        @RequestParam(required = false) String opening,
        @RequestParam(required = false) String result,
        @RequestParam(required = false) String timeControl,
        @RequestParam(required = false) String platform,
        @RequestParam(required = false) String color,
        @RequestParam(required = false) String from,
        @RequestParam(required = false) String to,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

        if (page < 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Page must be zero or greater."
            );
        }

        if (size < 1) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Size must be greater than zero."
            );
        }

        Pageable pageable = PageRequest.of(page, size);

        return new PagedModel<>(
            gameService.searchGames(
                username,
                opening,
                result,
                timeControl,
                platform,
                color,
                from,
                to,
                pageable
            )
        );
    }

    @PostMapping("/import/chesscom")
    public PgnImportReport importChessComGames(
        @RequestParam String username, @RequestParam(defaultValue = "50") @Min(1) @Max(2000) int numberOfGames) throws Exception {
        return chessComImportService.importPlayerGames(username, numberOfGames);
    }

    @PostMapping("/import/lichess")
    public PgnImportReport importFromLichess(
        @RequestParam String username, @RequestParam(defaultValue = "50") @Min(1) @Max(2000) int numberOfGames) throws Exception {
        return lichessImportService.importGames(username, numberOfGames);
    }

    @GetMapping("/{id}/pgn")
    public ResponseEntity<String> downloadPgn(@PathVariable Long id) {
        String pgn = gameService.getPgnById(id);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/x-chess-pgn"))
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                ContentDisposition.attachment()
                    .filename("chessinsight-game-" + id + ".pgn",
                        StandardCharsets.UTF_8)
                    .build()
                    .toString()
            )
            .body(pgn);
    }

    @GetMapping("/{id}")
    public ChessGameDto getGameById(@PathVariable Long id) {
        return gameService.getGameById(id);
    }

    @GetMapping("/{id}/moves")
    public List<String> getGameMoves(@PathVariable Long id) {
        return gameService.getMovesById(id);
    }

    @GetMapping("/{id}/positions")
    public List<String> getPositionsById(@PathVariable Long id) {
        return gameService.getPositionsById(id);
    }
}

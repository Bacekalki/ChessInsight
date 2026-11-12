package com.chessinsight.chessinsight.chess.lichess;

import com.chessinsight.chessinsight.chess.pgn.PgnImportReport;
import com.chessinsight.chessinsight.chess.pgn.PgnImportService;
import com.chessinsight.chessinsight.game.GamePlatform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;

@Service
@RequiredArgsConstructor
public class LichessImportService {

    private final LichessApiClient lichessApiClient;
    private final PgnImportService pgnImportService;

    public PgnImportReport importGames(String username, int numberOfGames) throws Exception {
        String pgn = lichessApiClient.downloadPgn(username);

        if (pgn == null || pgn.isBlank()) {
            return new PgnImportReport(0, 0, 0, 0);
        }

        File tempFile = File.createTempFile("lichess-games-", ".pgn");

        try {
            Files.writeString(tempFile.toPath(), pgn);
            return pgnImportService.importGames(tempFile, GamePlatform.LICHESS, numberOfGames, 0);
        } finally {
            Files.deleteIfExists(tempFile.toPath());
        }
    }
}
package com.chessinsight.chessinsight.chess.chesscom;

import com.chessinsight.chessinsight.chess.pgn.PgnImportReport;
import com.chessinsight.chessinsight.chess.pgn.PgnImportService;
import com.chessinsight.chessinsight.game.GamePlatform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class ChessComImportService {

    private final ChessComApiClient apiClient;
    private final PgnImportService importService;

    public PgnImportReport importPlayerGames(String username, int numberOfGames)
        throws Exception {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                "Chess.com username must not be blank"
            );
        }

        ChessComArchivesResponse response =
            apiClient.getArchives(username.trim());

        if (response == null || response.archives() == null) {
            throw new IllegalStateException(
                "Could not retrieve Chess.com archives"
            );
        }

        int imported = 0;
        int skipped = 0;
        int totalProcessed = 0;
        int malformed = 0;

        for (String archiveUrl : response.archives()) {
            String pgn = apiClient.downloadPgn(archiveUrl);

            if (pgn == null || pgn.isBlank()) {
                continue;
            }

            Path tempFile = Files.createTempFile(
                "chessinsight-chesscom-", ".pgn"
            );

            try {
                Files.writeString(
                    tempFile,
                    pgn,
                    StandardCharsets.UTF_8
                );

                PgnImportReport report =
                    importService.importGames(tempFile.toFile(), GamePlatform.CHESS_COM, numberOfGames);

                imported += report.imported();
                skipped += report.skipped();
                malformed += report.malformed();
                totalProcessed += report.totalProcessed();

                if (totalProcessed > numberOfGames) {
                    break;
                }

            } finally {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    tempFile.toFile().deleteOnExit();
                }
            }
        }

        return new PgnImportReport(
            imported,
            skipped,
            malformed,
            totalProcessed
        );

    }
}

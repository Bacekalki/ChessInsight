package com.chessinsight.chessinsight.chess;

import com.chessinsight.chessinsight.chess.pgn.PgnImportReport;
import com.chessinsight.chessinsight.chess.pgn.PgnImportService;
import com.chessinsight.chessinsight.game.ChessGame;
import com.chessinsight.chessinsight.game.ChessGameRepository;
import com.chessinsight.chessinsight.game.GamePlatform;
import com.chessinsight.chessinsight.game.TimeControlType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PgnImportServiceTest {

    @Mock
    private ChessGameRepository repository;

    @Mock
    private OpeningClassifier classifier;

    private TimeControlClassifier timeControlClassifier;
    private PgnImportService service;

    @TempDir()
    Path tempDir;

    private Path duplicateGameFile;
    private Path unknownTimeControlFile;
    private Path invalidDateFile;

    @BeforeEach
    void setUp() throws Exception {
        timeControlClassifier = new TimeControlClassifier();

        service = new PgnImportService(
            repository,
            classifier,
            timeControlClassifier
        );

        duplicateGameFile = createPgn(
            "duplicate-game.pgn",
            "2026.10.22",
            "[TimeControl \"300\"]"
        );

        unknownTimeControlFile = createPgn(
            "unknown-time-control.pgn",
            "2026.10.22",
            ""
        );

        invalidDateFile = createPgn(
            "invalid-date.pgn",
            "????.??.??",
            "[TimeControl \"300\"]"
        );
    }

    private Path createPgn(
        String filename,
        String date,
        String extraHeader
    ) throws Exception {
        String pgn = """
                [Event "Live Chess"]
                [Site "Chess.com"]
                [Date "%s"]
                [Round "-"]
                [White "Bacekalki"]
                [Black "Opponent"]
                [Result "1-0"]
                %s

                1. e4 e5 1-0
                """.formatted(date, extraHeader);

        Path file = tempDir.resolve(filename);
        Files.writeString(file, pgn);

        return file;
    }

    private void allowNewGames() {
        when(repository.existsByPgnHash(anyString()))
            .thenReturn(false);
    }

    private ChessGame captureSavedGame() {
        ArgumentCaptor<ChessGame> captor =
            ArgumentCaptor.forClass(ChessGame.class);

        verify(repository).save(captor.capture());

        return captor.getValue();
    }

    @Test
    void shouldSkipDuplicateGame() throws Exception {
        when(repository.existsByPgnHash(anyString()))
            .thenReturn(true);

        PgnImportReport report =
            service.importGames(duplicateGameFile.toFile());

        assertEquals(0, report.imported());
        assertEquals(1, report.skipped());
        assertEquals(1, report.totalProcessed());

        verify(repository, never()).save(any(ChessGame.class));
    }

    @Test
    void shouldImportGameWithUnknownTimeControl() throws Exception {
        allowNewGames();

        PgnImportReport report =
            service.importGames(unknownTimeControlFile.toFile());

        assertEquals(1, report.imported());
        assertEquals(0, report.skipped());
        assertEquals(1, report.totalProcessed());

        ChessGame savedGame = captureSavedGame();

        assertEquals(
            TimeControlType.UNKNOWN,
            savedGame.getTimeControl()
        );
    }

    @Test
    void shouldImportGameWithInvalidDateAsNull() throws Exception {
        allowNewGames();

        PgnImportReport report =
            service.importGames(invalidDateFile.toFile());

        assertEquals(1, report.imported());
        assertEquals(0, report.skipped());
        assertEquals(1, report.totalProcessed());

        ChessGame savedGame = captureSavedGame();

        assertNull(savedGame.getPlayedAt());
    }

    @Test
    void shouldImportGameWithSpecifiedPlatform() throws Exception {
        allowNewGames();

        PgnImportReport report =
            service.importGames(unknownTimeControlFile.toFile(), GamePlatform.CHESS_COM);

        assertEquals(1, report.imported());
        assertEquals(0, report.skipped());
        assertEquals(1, report.totalProcessed());

        ChessGame savedGame = captureSavedGame();
        assertEquals(GamePlatform.CHESS_COM, savedGame.getPlatform());
    }

    @Test
    void shouldImportGameWithManualPlatformByDefault() throws Exception {
        allowNewGames();

        PgnImportReport report =
            service.importGames(unknownTimeControlFile.toFile());

        assertEquals(1, report.imported());
        assertEquals(0, report.skipped());
        assertEquals(1, report.totalProcessed());

        ChessGame savedGame = captureSavedGame();
        assertEquals(GamePlatform.MANUAL, savedGame.getPlatform());
    }
}

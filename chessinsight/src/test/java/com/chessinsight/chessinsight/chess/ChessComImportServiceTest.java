package com.chessinsight.chessinsight.chess;

import com.chessinsight.chessinsight.chess.chesscom.ChessComApiClient;
import com.chessinsight.chessinsight.chess.chesscom.ChessComArchivesResponse;
import com.chessinsight.chessinsight.chess.chesscom.ChessComImportService;
import com.chessinsight.chessinsight.chess.pgn.PgnImportReport;
import com.chessinsight.chessinsight.chess.pgn.PgnImportService;
import com.chessinsight.chessinsight.game.GamePlatform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChessComImportServiceTest {

    @Mock
    private ChessComApiClient chessComApiClient;

    @Mock
    private PgnImportService pgnImportService;

    @InjectMocks
    private ChessComImportService service;


    @Test
    void shouldReturnEmptyReportWhenNoArchivesExist() throws Exception {
        when(chessComApiClient.getArchives("testuser"))
            .thenReturn(new ChessComArchivesResponse(List.of()));

        PgnImportReport report =
            service.importPlayerGames("testuser");

        assertEquals(0, report.imported());
        assertEquals(0, report.skipped());
        assertEquals(0, report.totalProcessed());

        verify(chessComApiClient).getArchives("testuser");
        verify(pgnImportService, never()).importGames(any());
    }

    @Test
    void shouldDownloadPgnAndImportGames() throws Exception {
        String archiveUrl =
            "https://api.chess.com/pub/player/testuser/games/2026/10";

        String pgn = """
                [Event "Live Chess"]
                [White "testuser"]
                [Black "opponent"]
                [Result "1-0"]

                1. e4 e5 1-0
                """;

        when(chessComApiClient.getArchives("testuser"))
            .thenReturn(new ChessComArchivesResponse(
                List.of(archiveUrl)
            ));

        when(chessComApiClient.downloadPgn(archiveUrl))
            .thenReturn(pgn);

        PgnImportReport expectedReport =
            new PgnImportReport(2, 1, 0, 3);

        when(pgnImportService.importGames(any(), eq(GamePlatform.CHESS_COM)))
            .thenReturn(expectedReport);

        PgnImportReport report =
            service.importPlayerGames("testuser");

        assertEquals(2, report.imported());
        assertEquals(1, report.skipped());
        assertEquals(3, report.totalProcessed());

        verify(chessComApiClient).getArchives("testuser");
        verify(chessComApiClient).downloadPgn(archiveUrl);
        verify(pgnImportService).importGames(any(), eq(GamePlatform.CHESS_COM));
    }

    @Test
    void shouldPropagateExceptionWhenImportFails() throws Exception {
        String archiveUrl =
            "https://api.chess.com/pub/player/testuser/games/2026/10";

        String pgn = """
                [Event "Live Chess"]
                [White "testuser"]
                [Black "opponent"]
                [Result "1-0"]

                1. e4 e5 1-0
                """;

        when(chessComApiClient.getArchives("testuser"))
            .thenReturn(new ChessComArchivesResponse(
                List.of(archiveUrl)
            ));

        when(chessComApiClient.downloadPgn(archiveUrl))
            .thenReturn(pgn);

        when(pgnImportService.importGames(any(), eq(GamePlatform.CHESS_COM)))
            .thenThrow(new RuntimeException("Import failed"));

        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> service.importPlayerGames("testuser")
        );

        assertEquals("Import failed", exception.getMessage());
    }
}
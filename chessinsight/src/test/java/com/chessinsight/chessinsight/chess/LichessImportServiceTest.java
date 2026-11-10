package com.chessinsight.chessinsight.chess;

import com.chessinsight.chessinsight.chess.lichess.LichessApiClient;
import com.chessinsight.chessinsight.chess.lichess.LichessImportService;
import com.chessinsight.chessinsight.chess.pgn.PgnImportReport;
import com.chessinsight.chessinsight.chess.pgn.PgnImportService;
import com.chessinsight.chessinsight.game.GamePlatform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LichessImportServiceTest {

    @Mock
    private LichessApiClient lichessApiClient;

    @Mock
    private PgnImportService pgnImportService;

    @InjectMocks
    private LichessImportService service;


    @Test
    void shouldReturnEmptyReportWhenPgnIsEmpty() throws Exception {
        when(lichessApiClient.downloadPgn("testuser"))
            .thenReturn("");

        PgnImportReport report = service.importGames("testuser");

        assertEquals(0, report.imported());
        assertEquals(0, report.skipped());
        assertEquals(0, report.totalProcessed());

        verify(pgnImportService, never()).importGames(any());
    }

    @Test
    void shouldDownloadPgnAndImportGames() throws Exception {
        String pgn = """
                [Event "Rated Blitz game"]
                [Site "https://lichess.org/example"]
                [Date "2026.10.25"]
                [Round "-"]
                [White "testuser"]
                [Black "opponent"]
                [Result "1-0"]

                1. e4 e5 1-0
                """;

        when(lichessApiClient.downloadPgn("testuser"))
            .thenReturn(pgn);

        PgnImportReport expectedReport =
            new PgnImportReport(2, 1, 0,3);

        when(pgnImportService.importGames(any(), eq(GamePlatform.LICHESS)))
            .thenReturn(expectedReport);

        PgnImportReport report = service.importGames("testuser");

        assertEquals(2, report.imported());
        assertEquals(1, report.skipped());
        assertEquals(3, report.totalProcessed());

        verify(lichessApiClient).downloadPgn("testuser");
        verify(pgnImportService).importGames(any(),  eq(GamePlatform.LICHESS));
    }

    @Test
    void shouldDeleteTemporaryFileWhenImportFails() throws Exception {
        String pgn = """
            [Event "Rated Blitz game"]
            [White "testuser"]
            [Black "opponent"]
            [Result "1-0"]

            1. e4 e5 1-0
            """;

        when(lichessApiClient.downloadPgn("testuser"))
            .thenReturn(pgn);

        when(pgnImportService.importGames(any(), eq(GamePlatform.LICHESS)))
            .thenAnswer(invocation -> {
                Path tempFile = ((java.io.File) invocation.getArgument(0))
                    .toPath();

                assertTrue(Files.exists(tempFile));

                throw new RuntimeException("Import failed");
            });

        RuntimeException exception = org.junit.jupiter.api.Assertions
            .assertThrows(
                RuntimeException.class,
                () -> service.importGames("testuser")
            );

        assertEquals("Import failed", exception.getMessage());
    }
}
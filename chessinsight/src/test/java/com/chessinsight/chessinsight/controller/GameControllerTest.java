package com.chessinsight.chessinsight.controller;

import com.chessinsight.chessinsight.chess.pgn.PgnImportService;
import com.chessinsight.chessinsight.dto.ChessGameDto;
import com.chessinsight.chessinsight.service.ChessGameService;
import com.chessinsight.chessinsight.chess.chesscom.ChessComImportService;
import com.chessinsight.chessinsight.chess.lichess.LichessImportService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PgnImportService pgnImportService;

    @MockitoBean
    private ChessGameService chessGameService;

    @MockitoBean
    private ChessComImportService chessComImportService;

    @MockitoBean
    private LichessImportService lichessImportService;

    @Test
    void shouldReturnBadRequestWhenUsernameIsMissing() throws Exception {
        mockMvc.perform(get("/api/games"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message")
                .value("Missing required parameter: username"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnBadRequestWhenPageSizeIsNotAnInteger() throws Exception {
        mockMvc.perform(get("/api/games")
                .param("username", "bacekalki")
                .param("size", "abc"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message")
                .value("Invalid value for parameter: size"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnBadRequestWhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/games")
                .param("username", "bacekalki")
                .param("page", "-1"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message")
                .value("Page must be zero or greater."));
    }

    @Test
    void shouldReturnBadRequestWhenPageSizeIsZero() throws Exception {
        mockMvc.perform(get("/api/games")
                .param("username", "bacekalki")
                .param("size", "0"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message")
                .value("Size must be greater than zero."));
    }

    @Test
    void shouldPassPlatformFilterToService() throws Exception {
        when(chessGameService.searchGames(
            eq("bacekalki"),
            isNull(),
            isNull(),
            isNull(),
            eq("CHESS_COM"),
            isNull(),
            isNull(),
            isNull(),
            any()
        )).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/games")
                .param("username", "bacekalki")
                .param("platform", "CHESS_COM"))
            .andExpect(status().isOk());

        verify(chessGameService).searchGames(
            eq("bacekalki"),
            isNull(),
            isNull(),
            isNull(),
            eq("CHESS_COM"),
            isNull(),
            isNull(),
            isNull(),
            any()
        );
    }

    @Test
    void shouldReturnBadRequestWhenUploadedPgnIsEmpty() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.pgn",
            "application/x-chess-pgn",
            new byte[0]
        );

        mockMvc.perform(multipart("/api/games/import")
                .file(file))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message")
                .value("The uploaded file is empty."))
            .andExpect(jsonPath("$.timestamp").exists());
    }
}

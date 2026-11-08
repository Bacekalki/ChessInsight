package com.chessinsight.chessinsight.analytics;

import com.chessinsight.chessinsight.analytics.dto.ChessAnalyticsDto;
import com.chessinsight.chessinsight.analytics.dto.ColorPerformanceDto;
import com.chessinsight.chessinsight.analytics.dto.OpeningAnalyticsDto;
import com.chessinsight.chessinsight.analytics.dto.OpponentStrengthDto;
import com.chessinsight.chessinsight.analytics.dto.RatingHistoryDto;
import com.chessinsight.chessinsight.analytics.dto.TimeControlAnalyticsDto;
import com.chessinsight.chessinsight.game.GamePlatform;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChessAnalyticsController.class)
class ChessAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChessAnalyticsService analyticsService;

    @Test
    void shouldReturnSummaryForUsername() throws Exception {
        when(analyticsService.getSummary("bacekalki"))
            .thenReturn(new ChessAnalyticsDto(
                "bacekalki", 10, 6, 2, 2, 60.0
            ));

        mockMvc.perform(get("/api/analytics/summary")
                .param("username", "bacekalki"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("bacekalki"))
            .andExpect(jsonPath("$.totalGames").value(10))
            .andExpect(jsonPath("$.wins").value(6))
            .andExpect(jsonPath("$.losses").value(2))
            .andExpect(jsonPath("$.draws").value(2))
            .andExpect(jsonPath("$.winRate").value(60.0));

        verify(analyticsService).getSummary("bacekalki");
    }

    @Test
    void shouldReturnOpeningStatistics() throws Exception {
        when(analyticsService.getOpeningStats("bacekalki", null, null, 5, "winrate", "desc"))
            .thenReturn(java.util.List.of(
                new OpeningAnalyticsDto(
                    "French Defense", 5, 3, 1, 1, 60.0
                ),
                new OpeningAnalyticsDto(
                    "Sicilian Defense", 4, 2, 1, 1, 50.0
                )));

        mockMvc.perform(get("/api/analytics/openings")
                .param("username", "bacekalki")
                .param("sortBy", "winrate"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].openingName")
                .value("French Defense"))
            .andExpect(jsonPath("$[0].totalGames").value(5))
            .andExpect(jsonPath("$[0].wins").value(3))
            .andExpect(jsonPath("$[0].winRate").value(60.0))
            .andExpect(jsonPath("$[1].openingName")
                .value("Sicilian Defense"))
            .andExpect(jsonPath("$[1].winRate").value(50.0));

        verify(analyticsService)
            .getOpeningStats("bacekalki", null, null, 5, "winrate", "desc");
    }

    @Test
    void shouldReturnBadRequestWhenUsernameIsMissing()
        throws Exception {
        mockMvc.perform(get("/api/analytics/summary"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(analyticsService);
    }

    @Test
    void shouldReturnOpponentStrengthAnalytics() throws Exception {
        List<OpponentStrengthDto> analytics = List.of(
            new OpponentStrengthDto(
                "LOWER_RATED", 10, 7, 2, 1, 70.0
            ),
            new OpponentStrengthDto(
                "HIGHER_RATED", 5, 1, 3, 1, 20.0
            )
        );

        when(analyticsService.getOpponentStrength("bacekalki"))
            .thenReturn(analytics);

        mockMvc.perform(get("/api/analytics/opponent-strength")
                .param("username", "bacekalki"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].category").value("LOWER_RATED"))
            .andExpect(jsonPath("$[0].totalGames").value(10))
            .andExpect(jsonPath("$[0].wins").value(7))
            .andExpect(jsonPath("$[0].losses").value(2))
            .andExpect(jsonPath("$[0].draws").value(1))
            .andExpect(jsonPath("$[0].winRate").value(70.0))
            .andExpect(jsonPath("$[1].category").value("HIGHER_RATED"))
            .andExpect(jsonPath("$[1].winRate").value(20.0));

        verify(analyticsService).getOpponentStrength("bacekalki");
    }

    @Test
    void shouldReturnColorPerformanceAnalytics() throws Exception {
        List<ColorPerformanceDto> analytics = List.of(
            new ColorPerformanceDto("WHITE", 20, 12, 6, 2, 60.0),
            new ColorPerformanceDto("BLACK", 15, 7, 6, 2, 46.7)
        );

        when(analyticsService.getColorPerformance("bacekalki"))
            .thenReturn(analytics);

        mockMvc.perform(get("/api/analytics/color")
                .param("username", "bacekalki"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].color").value("WHITE"))
            .andExpect(jsonPath("$[0].totalGames").value(20))
            .andExpect(jsonPath("$[0].wins").value(12))
            .andExpect(jsonPath("$[0].losses").value(6))
            .andExpect(jsonPath("$[0].draws").value(2))
            .andExpect(jsonPath("$[0].winRate").value(60.0))
            .andExpect(jsonPath("$[1].color").value("BLACK"))
            .andExpect(jsonPath("$[1].totalGames").value(15))
            .andExpect(jsonPath("$[1].winRate").value(46.7));

        verify(analyticsService).getColorPerformance("bacekalki");
    }

    @Test
    void shouldReturnRatingHistory() throws Exception {
        List<RatingHistoryDto> history = List.of(
            new RatingHistoryDto(LocalDate.of(2026, 8, 10), 1200),
            new RatingHistoryDto(LocalDate.of(2026, 8, 12), 1220)
        );

        when(analyticsService.getRatingHistory("bacekalki", "RAPID", "CHESS_COM"))
            .thenReturn(history);

        mockMvc.perform(get(
                "/api/analytics/rating-history"
            )
                .param("username", "bacekalki")
                .param("timeControl", "RAPID")
                .param("platform", "CHESS_COM"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].playedAt").value("2026-08-10"))
            .andExpect(jsonPath("$[0].rating").value(1200))
            .andExpect(jsonPath("$[1].playedAt").value("2026-08-12"))
            .andExpect(jsonPath("$[1].rating").value(1220));

        verify(analyticsService).getRatingHistory("bacekalki", "RAPID", "CHESS_COM");
    }

    @Test
    void shouldReturnEmptyRatingHistory() throws Exception {
        when(analyticsService.getRatingHistory("bacekalki", "BLITZ", "CHESS_COM"))
            .thenReturn(List.of());

        mockMvc.perform(get("/api/analytics/rating-history")
                .param("username", "bacekalki")
                .param("timeControl", "BLITZ")
                .param("platform", "CHESS_COM"))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturnBadRequestWhenPlatformIsMissingForRatingHistory() throws Exception {
        mockMvc.perform(get("/api/analytics/rating-history")
                .param("username", "bacekalki")
                .param("timeControl", "RAPID"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message")
                .value("Missing required parameter: platform"));

        verifyNoInteractions(analyticsService);
    }

    @Test
    void shouldPassOpeningFiltersToService() throws Exception {
        when(analyticsService.getOpeningStats(
            "bacekalki", "WHITE", "BLITZ", 5, "winrate", "desc"
        )).thenReturn(List.of());

        mockMvc.perform(get("/api/analytics/openings")
                .param("username", "bacekalki")
                .param("color", "WHITE")
                .param("timeControl", "BLITZ")
                .param("sortBy", "winrate"))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));

        verify(analyticsService).getOpeningStats(
            "bacekalki", "WHITE", "BLITZ", 5, "winrate", "desc"
        );
    }

    @Test
    void shouldReturnTimeControlStats() throws Exception {
        when(analyticsService.getTimeControlStats("bacekalki"))
            .thenReturn(List.of(
                new TimeControlAnalyticsDto(
                    "BLITZ", 100L, 55L, 35L, 10L, 55.0
                )
            ));

        mockMvc.perform(get("/api/analytics/time-controls")
                .param("username", "bacekalki"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].timeControl").value("BLITZ"))
            .andExpect(jsonPath("$[0].totalGames").value(100))
            .andExpect(jsonPath("$[0].wins").value(55))
            .andExpect(jsonPath("$[0].winRate").value(55.0));

        verify(analyticsService).getTimeControlStats("bacekalki");
    }
}

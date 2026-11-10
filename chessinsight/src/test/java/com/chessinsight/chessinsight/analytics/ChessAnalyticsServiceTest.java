
package com.chessinsight.chessinsight.analytics;

import com.chessinsight.chessinsight.analytics.dto.ChessAnalyticsDto;
import com.chessinsight.chessinsight.analytics.dto.ColorPerformanceDto;
import com.chessinsight.chessinsight.analytics.dto.OpeningAnalyticsDto;
import com.chessinsight.chessinsight.analytics.dto.OpponentStrengthDto;
import com.chessinsight.chessinsight.analytics.dto.RatingHistoryDto;
import com.chessinsight.chessinsight.analytics.dto.TimeControlAnalyticsDto;
import com.chessinsight.chessinsight.analytics.projection.ChessAnalyticsProjection;
import com.chessinsight.chessinsight.analytics.projection.ColorPerformanceProjection;
import com.chessinsight.chessinsight.analytics.projection.OpponentStrengthProjection;
import com.chessinsight.chessinsight.analytics.projection.RatingHistoryProjection;
import com.chessinsight.chessinsight.analytics.projection.TimeControlAnalyticsProjection;
import com.chessinsight.chessinsight.game.ChessGameRepository;
import com.chessinsight.chessinsight.game.GamePlatform;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChessAnalyticsServiceTest {

    @Mock
    private ChessGameRepository repository;

    @InjectMocks
    private ChessAnalyticsService service;

    private static final String USERNAME = "bacekalki";


    @Test
    void shouldCalculateSummaryCorrectly() {
        ChessAnalyticsProjection projection =
            mock(ChessAnalyticsProjection.class);

        when(projection.getWins()).thenReturn(6L);
        when(projection.getLosses()).thenReturn(3L);
        when(projection.getDraws()).thenReturn(1L);

        when(repository.getAnalyticsForUsername(USERNAME))
            .thenReturn(projection);

        ChessAnalyticsDto result = service.getSummary(USERNAME);

        assertEquals(USERNAME, result.username());
        assertEquals(10L, result.totalGames());
        assertEquals(6L, result.wins());
        assertEquals(3L, result.losses());
        assertEquals(1L, result.draws());
        assertEquals(60.0, result.winRate());

        verify(repository).getAnalyticsForUsername(USERNAME);
    }

    @Test
    void shouldReturnZeroWinRateWhenThereAreNoGames() {
        ChessAnalyticsProjection projection =
            mock(ChessAnalyticsProjection.class);

        when(projection.getWins()).thenReturn(0L);
        when(projection.getLosses()).thenReturn(0L);
        when(projection.getDraws()).thenReturn(0L);

        when(repository.getAnalyticsForUsername(USERNAME))
            .thenReturn(projection);

        ChessAnalyticsDto result = service.getSummary(USERNAME);

        assertEquals(0L, result.totalGames());
        assertEquals(0.0, result.winRate());
    }

    @Test
    void shouldRoundWinRateToTwoDecimalPlaces() {
        ChessAnalyticsProjection projection =
            mock(ChessAnalyticsProjection.class);

        when(projection.getWins()).thenReturn(1L);
        when(projection.getLosses()).thenReturn(2L);
        when(projection.getDraws()).thenReturn(0L);

        when(repository.getAnalyticsForUsername(USERNAME))
            .thenReturn(projection);

        ChessAnalyticsDto result = service.getSummary(USERNAME);

        assertEquals(33.33, result.winRate());
    }

    @Test
    void shouldTrimUsernameBeforeQueryingRepository() {
        ChessAnalyticsProjection projection =
            mock(ChessAnalyticsProjection.class);

        when(projection.getWins()).thenReturn(1L);
        when(projection.getLosses()).thenReturn(0L);
        when(projection.getDraws()).thenReturn(0L);

        when(repository.getAnalyticsForUsername(USERNAME))
            .thenReturn(projection);

        ChessAnalyticsDto result =
            service.getSummary("  " + USERNAME + "  ");

        assertEquals(USERNAME, result.username());
        verify(repository).getAnalyticsForUsername(USERNAME);
    }

    @Test
    void shouldRejectBlankUsername() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getSummary("   ")
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldCalculateColorPerformanceCorrectly() {
        ColorPerformanceProjection white =
            mock(ColorPerformanceProjection.class);

        when(white.getColor()).thenReturn("WHITE");
        when(white.getTotalGames()).thenReturn(10L);
        when(white.getWins()).thenReturn(6L);
        when(white.getLosses()).thenReturn(3L);
        when(white.getDraws()).thenReturn(1L);

        ColorPerformanceProjection black =
            mock(ColorPerformanceProjection.class);

        when(black.getColor()).thenReturn("BLACK");
        when(black.getTotalGames()).thenReturn(8L);
        when(black.getWins()).thenReturn(2L);
        when(black.getLosses()).thenReturn(4L);
        when(black.getDraws()).thenReturn(2L);

        when(repository.getColorPerformance(USERNAME))
            .thenReturn(java.util.List.of(white, black));

        java.util.List<ColorPerformanceDto> result =
            service.getColorPerformance(USERNAME);

        assertEquals(2, result.size());

        assertEquals("WHITE", result.get(0).color());
        assertEquals(10L, result.get(0).totalGames());
        assertEquals(6L, result.get(0).wins());
        assertEquals(3L, result.get(0).losses());
        assertEquals(1L, result.get(0).draws());
        assertEquals(60.0, result.get(0).winRate());

        assertEquals("BLACK", result.get(1).color());
        assertEquals(8L, result.get(1).totalGames());
        assertEquals(2L, result.get(1).wins());
        assertEquals(4L, result.get(1).losses());
        assertEquals(2L, result.get(1).draws());
        assertEquals(25.0, result.get(1).winRate());

        verify(repository).getColorPerformance(USERNAME);
    }

    @Test
    void shouldReturnZeroWinRateForBothColorsWhenThereAreNoGames() {
        ColorPerformanceProjection white =
            mock(ColorPerformanceProjection.class);

        when(white.getColor()).thenReturn("WHITE");
        when(white.getTotalGames()).thenReturn(0L);
        when(white.getWins()).thenReturn(0L);
        when(white.getLosses()).thenReturn(0L);
        when(white.getDraws()).thenReturn(0L);

        ColorPerformanceProjection black =
            mock(ColorPerformanceProjection.class);

        when(black.getColor()).thenReturn("BLACK");
        when(black.getTotalGames()).thenReturn(0L);
        when(black.getWins()).thenReturn(0L);
        when(black.getLosses()).thenReturn(0L);
        when(black.getDraws()).thenReturn(0L);

        when(repository.getColorPerformance(USERNAME))
            .thenReturn(java.util.List.of(white, black));

        java.util.List<ColorPerformanceDto> result =
            service.getColorPerformance(USERNAME);

        assertEquals(2, result.size());
        assertEquals(0.0, result.get(0).winRate());
        assertEquals(0.0, result.get(1).winRate());
    }

    @Test
    void shouldTrimUsernameWhenGettingColorPerformance() {
        when(repository.getColorPerformance(USERNAME))
            .thenReturn(java.util.List.of());

        service.getColorPerformance("  " + USERNAME + "  ");

        verify(repository).getColorPerformance(USERNAME);
    }

    @Test
    void shouldRejectBlankUsernameForColorPerformance() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getColorPerformance("   ")
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldCalculateOpponentStrengthCorrectly() {
        OpponentStrengthProjection lower = mock(OpponentStrengthProjection.class);
        when(lower.getCategory()).thenReturn("LOWER_RATED");
        when(lower.getTotalGames()).thenReturn(10L);
        when(lower.getWins()).thenReturn(7L);
        when(lower.getLosses()).thenReturn(2L);
        when(lower.getDraws()).thenReturn(1L);

        OpponentStrengthProjection similar = mock(OpponentStrengthProjection.class);
        when(similar.getCategory()).thenReturn("SIMILAR_RATED");
        when(similar.getTotalGames()).thenReturn(5L);
        when(similar.getWins()).thenReturn(2L);
        when(similar.getLosses()).thenReturn(2L);
        when(similar.getDraws()).thenReturn(1L);

        when(repository.getOpponentStrength(USERNAME))
            .thenReturn(List.of(lower, similar));

        List<OpponentStrengthDto> result =
            service.getOpponentStrength(USERNAME);

        assertEquals(2, result.size());

        assertEquals("LOWER_RATED", result.get(0).category());
        assertEquals(10L, result.get(0).totalGames());
        assertEquals(7L, result.get(0).wins());
        assertEquals(2L, result.get(0).losses());
        assertEquals(1L, result.get(0).draws());
        assertEquals(70.0, result.get(0).winRate());

        assertEquals("SIMILAR_RATED", result.get(1).category());
        assertEquals(40.0, result.get(1).winRate());
    }

    @Test
    void shouldTrimUsernameWhenGettingOpponentStrength() {
        when(repository.getOpponentStrength(USERNAME))
            .thenReturn(List.of());

        service.getOpponentStrength(" " + USERNAME + " ");

        verify(repository).getOpponentStrength(USERNAME);
    }

    @Test
    void shouldRejectBlankUsernameForOpponentStrength() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getOpponentStrength(" ")
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldReturnRatingHistory() {
        String timeControl = "RAPID";

        List<RatingHistoryProjection> projections = List.of(
            createRatingHistoryProjection(
                LocalDate.of(2026, 8, 10), 1200
            ),
            createRatingHistoryProjection(
                LocalDate.of(2026, 8, 12), 1220
            )
        );

        when(repository.getRatingHistory(USERNAME, timeControl, "MANUAL"))
            .thenReturn(projections);

        List<RatingHistoryDto> result =
            service.getRatingHistory(USERNAME, timeControl, "MANUAL");

        assertEquals(2, result.size());
        assertEquals(LocalDate.of(2026, 8, 10), result.get(0).playedAt());
        assertEquals(1200, result.get(0).rating());
        assertEquals(LocalDate.of(2026, 8, 12), result.get(1).playedAt());
        assertEquals(1220, result.get(1).rating());

        verify(repository).getRatingHistory(USERNAME, timeControl, "MANUAL");
    }

    @Test
    void shouldRejectBlankUsernameForRatingHistory() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getRatingHistory(" ", "RAPID", "MANUAL")
        );
    }

    @Test
    void shouldRejectBlankTimeControlForRatingHistory() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getRatingHistory("bacekalki", " ", "MANUAL")
        );
    }

    @Test
    void shouldRejectInvalidTimeControlForRatingHistory() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getRatingHistory("bacekalki", "CLASSICAL", "MANUAL")
        );
    }

    @Test
    void shouldRejectUnknownTimeControlForRatingHistory() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getRatingHistory("bacekalki", "UNKNOWN", "MANUAL")
        );
    }

    @Test
    void shouldRejectBlankPlatformForRatingHistory() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getRatingHistory("bacekalki", "RAPID", " ")
        );
    }

    @Test
    void shouldRejectInvalidPlatformForRatingHistory() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getRatingHistory("bacekalki", "RAPID", "INVALID_PLATFORM")
        );
    }

    private RatingHistoryProjection createRatingHistoryProjection(
        LocalDate playedAt,
        Integer rating
    ) {
        RatingHistoryProjection projection =
            mock(RatingHistoryProjection.class);

        when(projection.getPlayedAt()).thenReturn(playedAt);
        when(projection.getRating()).thenReturn(rating);

        return projection;
    }

    @Test
    void shouldRejectInvalidColorForOpeningStats() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getOpeningStats(
                "bacekalki",
                "BLUE",
                null
            )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectInvalidTimeControlForOpeningStats() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getOpeningStats(
                "bacekalki",
                null,
                "DAILY"
            )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldNormalizeOpeningStatsFilters() {
        when(repository.getOpeningAnalytics(
            "bacekalki", "WHITE", "BLITZ", 1, "winrate", "desc"
        )).thenReturn(List.of());

        List<OpeningAnalyticsDto> result = service.getOpeningStats(
            " bacekalki ",
            "white",
            "blitz", 1, "winrate", "desc"
        );

        assertTrue(result.isEmpty());

        verify(repository).getOpeningAnalytics(
            "bacekalki", "WHITE", "BLITZ", 1, "winrate", "desc"
        );
    }

    @Test
    void shouldCalculateWinRateByTimeControl() {
        TimeControlAnalyticsProjection blitzProjection =
            mock(TimeControlAnalyticsProjection.class);

        when(blitzProjection.getTimeControl()).thenReturn("BLITZ");
        when(blitzProjection.getTotalGames()).thenReturn(100L);
        when(blitzProjection.getWins()).thenReturn(55L);
        when(blitzProjection.getLosses()).thenReturn(35L);
        when(blitzProjection.getDraws()).thenReturn(10L);

        when(repository.getTimeControlAnalytics("bacekalki"))
            .thenReturn(List.of(blitzProjection));

        List<TimeControlAnalyticsDto> result =
            service.getTimeControlStats("bacekalki");

        assertEquals(1, result.size());
        assertEquals("BLITZ", result.get(0).timeControl());
        assertEquals(100L, result.get(0).totalGames());
        assertEquals(55L, result.get(0).wins());
        assertEquals(55.0, result.get(0).winRate(), 0.001);

        verify(repository).getTimeControlAnalytics("bacekalki");

    }

    @Test
    void shouldRejectBlankUsernameForTimeControlStats() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.getTimeControlStats(" ")
        );

        verifyNoInteractions(repository);
    }
}

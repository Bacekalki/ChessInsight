
package com.chessinsight.chessinsight.game;

import com.chessinsight.chessinsight.analytics.projection.ChessAnalyticsProjection;
import com.chessinsight.chessinsight.analytics.projection.ColorPerformanceProjection;
import com.chessinsight.chessinsight.analytics.projection.OpeningAnalyticsProjection;
import com.chessinsight.chessinsight.analytics.projection.OpponentStrengthProjection;
import com.chessinsight.chessinsight.analytics.projection.RatingHistoryProjection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
class ChessGameRepositoryTest {

    private static final String USERNAME = "sql_test_user";

    @Autowired
    private ChessGameRepository repository;

    @Test
    void shouldCalculateWinsLossesAndDrawsCorrectly() {
        repository.saveAll(List.of(
            game("SQL_TEST_USER", "opponent1",
                GameResult.WHITE_WIN, "French Defense"),
            game("opponent2", "Sql_Test_User",
                GameResult.BLACK_WIN, "French Defense"),
            game("SQL_TEST_USER", "opponent3",
                GameResult.BLACK_WIN, "French Defense"),
            game("opponent4", "SQL_TEST_USER",
                GameResult.WHITE_WIN, "Sicilian Defense"),
            game("sql_test_user", "opponent5",
                GameResult.DRAW, "Sicilian Defense"),
            game("opponent6", "SQL_TEST_USER",
                GameResult.DRAW, "Sicilian Defense"),
            game("SQL_TEST_USER", "opponent7",
                GameResult.UNKNOWN, "Unknown Opening"),
            game("someone_else", "another_player",
                GameResult.WHITE_WIN, "Italian Game")
        ));

        repository.flush();

        ChessAnalyticsProjection result =
            repository.getAnalyticsForUsername(USERNAME);

        assertEquals(2L, result.getWins());
        assertEquals(2L, result.getLosses());
        assertEquals(2L, result.getDraws());
    }

    @Test
    void shouldCalculateOpeningStatisticsAndIgnoreInvalidOpenings() {
        repository.saveAll(List.of(
            game(USERNAME, "opponent1",
                GameResult.WHITE_WIN, "French Defense"),
            game("opponent2", USERNAME,
                GameResult.BLACK_WIN, "French Defense"),
            game(USERNAME, "opponent3",
                GameResult.BLACK_WIN, "French Defense"),

            game(USERNAME, "opponent4",
                GameResult.WHITE_WIN, "Sicilian Defense"),
            game("opponent5", USERNAME,
                GameResult.DRAW, "Sicilian Defense"),

            game(USERNAME, "opponent6",
                GameResult.WHITE_WIN, null),
            game(USERNAME, "opponent7",
                GameResult.WHITE_WIN, "   "),
            game(USERNAME, "opponent8",
                GameResult.UNKNOWN, "Unknown Opening")
        ));

        repository.flush();

        List<OpeningAnalyticsProjection> results =
            repository.getOpeningAnalytics(USERNAME, null, null, 0, "winrate", "desc");

        Map<String, OpeningAnalyticsProjection> openings =
            results.stream().collect(Collectors.toMap(
                OpeningAnalyticsProjection::getOpeningName,
                Function.identity()
            ));

        assertEquals(2, openings.size());

        OpeningAnalyticsProjection french =
            openings.get("French Defense");

        assertNotNull(french);
        assertEquals(3L, french.getTotalGames());
        assertEquals(2L, french.getWins());
        assertEquals(1L, french.getLosses());
        assertEquals(0L, french.getDraws());

        OpeningAnalyticsProjection sicilian =
            openings.get("Sicilian Defense");

        assertNotNull(sicilian);
        assertEquals(2L, sicilian.getTotalGames());
        assertEquals(1L, sicilian.getWins());
        assertEquals(0L, sicilian.getLosses());
        assertEquals(1L, sicilian.getDraws());
    }

    @Test
    void shouldReturnZeroesWhenUsernameHasNoGames() {
        ChessAnalyticsProjection result =
            repository.getAnalyticsForUsername(
                "username_without_games_987654"
            );

        assertNotNull(result);
        assertEquals(0L, result.getWins());
        assertEquals(0L, result.getLosses());
        assertEquals(0L, result.getDraws());
    }

    @Test
    void shouldReturnCorrectPerformanceForBothColors() {
        repository.save(game(
            "bacekalki", "opponent1",
            GameResult.WHITE_WIN, "Italian Game"
        ));

        repository.save(game(
            "opponent2", "bacekalki",
            GameResult.WHITE_WIN, "Sicilian Defense"
        ));

        repository.save(game(
            "bacekalki", "opponent3",
            GameResult.DRAW, "Ruy Lopez"
        ));

        repository.save(game(
            "opponent4", "bacekalki",
            GameResult.BLACK_WIN, "French Defense"
        ));

        repository.flush();

        List<ColorPerformanceProjection> result =
            repository.getColorPerformance("bacekalki");

        assertEquals(2, result.size());

        ColorPerformanceProjection white = result.stream()
            .filter(row -> row.getColor().equals("WHITE"))
            .findFirst()
            .orElseThrow();

        ColorPerformanceProjection black = result.stream()
            .filter(row -> row.getColor().equals("BLACK"))
            .findFirst()
            .orElseThrow();

        assertEquals(2, white.getTotalGames());
        assertEquals(1, white.getWins());
        assertEquals(0, white.getLosses());
        assertEquals(1, white.getDraws());

        assertEquals(2, black.getTotalGames());
        assertEquals(1, black.getWins());
        assertEquals(1, black.getLosses());
        assertEquals(0, black.getDraws());
    }

    @Test
    void shouldExcludeUnknownResultsFromColorPerformance() {
        repository.save(game(
            "bacekalki", "opponent1",
            GameResult.WHITE_WIN, "Italian Game"
        ));

        repository.save(game(
            "bacekalki", "opponent2",
            GameResult.UNKNOWN, "Unknown Opening"
        ));

        repository.save(game(
            "opponent3", "bacekalki",
            GameResult.UNKNOWN, "Unknown Opening"
        ));

        repository.flush();

        List<ColorPerformanceProjection> result =
            repository.getColorPerformance("bacekalki");

        ColorPerformanceProjection white = result.stream()
            .filter(row -> row.getColor().equals("WHITE"))
            .findFirst()
            .orElseThrow();

        ColorPerformanceProjection black = result.stream()
            .filter(row -> row.getColor().equals("BLACK"))
            .findFirst()
            .orElseThrow();

        assertEquals(1, white.getTotalGames());
        assertEquals(1, white.getWins());
        assertEquals(0, white.getLosses());
        assertEquals(0, white.getDraws());

        assertEquals(0, black.getTotalGames());
        assertEquals(0, black.getWins());
        assertEquals(0, black.getLosses());
        assertEquals(0, black.getDraws());
    }

    @Test
    void shouldReturnZeroCountsWhenUserHasNoGames() {
        List<ColorPerformanceProjection> result =
            repository.getColorPerformance("nonexistent-user");

        assertEquals(2, result.size());

        for (ColorPerformanceProjection row : result) {
            assertEquals(0, row.getTotalGames());
            assertEquals(0, row.getWins());
            assertEquals(0, row.getLosses());
            assertEquals(0, row.getDraws());
        }
    }

    @Test
    void shouldMatchUsernameIgnoringCase() {
        repository.save(game(
            "Bacekalki", "opponent1",
            GameResult.WHITE_WIN, "Italian Game"
        ));

        repository.flush();

        List<ColorPerformanceProjection> result =
            repository.getColorPerformance("bacekalki");

        ColorPerformanceProjection white = result.stream()
            .filter(row -> row.getColor().equals("WHITE"))
            .findFirst()
            .orElseThrow();

        assertEquals(1, white.getTotalGames());
        assertEquals(1, white.getWins());
    }

    @Test
    void shouldCalculateOpponentStrengthCorrectly() {
        repository.saveAll(List.of(
            new ChessGame(null, "bacekalki", "opponent1",
                1200, 1100, GameResult.WHITE_WIN,
                TimeControlType.RAPID, "Opening 1", null),

            new ChessGame(null, "bacekalki", "opponent2",
                1200, 1300, GameResult.BLACK_WIN,
                TimeControlType.RAPID, "Opening 2", null),

            new ChessGame(null, "opponent3", "bacekalki",
                1210, 1200, GameResult.DRAW,
                TimeControlType.RAPID, "Opening 3", null)
        ));

        List<OpponentStrengthProjection> result =
            repository.getOpponentStrength("bacekalki");

        assertEquals(3, result.size());

        OpponentStrengthProjection lower = result.stream()
            .filter(p -> p.getCategory().equals("LOWER_RATED"))
            .findFirst()
            .orElseThrow();

        assertEquals(1, lower.getTotalGames());
        assertEquals(1, lower.getWins());
        assertEquals(0, lower.getLosses());
        assertEquals(0, lower.getDraws());

        OpponentStrengthProjection higher = result.stream()
            .filter(p -> p.getCategory().equals("HIGHER_RATED"))
            .findFirst()
            .orElseThrow();

        assertEquals(1, higher.getTotalGames());
        assertEquals(0, higher.getWins());
        assertEquals(1, higher.getLosses());
        assertEquals(0, higher.getDraws());

        OpponentStrengthProjection similar = result.stream()
            .filter(p -> p.getCategory().equals("SIMILAR_RATED"))
            .findFirst()
            .orElseThrow();

        assertEquals(1, similar.getTotalGames());
        assertEquals(0, similar.getWins());
        assertEquals(0, similar.getLosses());
        assertEquals(1, similar.getDraws());
    }

    @Test
    void shouldExcludeGamesWithMissingRatingsOrUnknownResults() {
        repository.saveAll(List.of(
            new ChessGame(null, "bacekalki", "opponent1",
                1200, 1100, GameResult.WHITE_WIN,
                TimeControlType.RAPID, "Opening 1", null),

            new ChessGame(null, "bacekalki", "opponent2",
                null, 1300, GameResult.WHITE_WIN,
                TimeControlType.RAPID, "Opening 2", null),

            new ChessGame(null, "bacekalki", "opponent3",
                1200, null, GameResult.BLACK_WIN,
                TimeControlType.RAPID, "Opening 3", null),

            new ChessGame(null, "bacekalki", "opponent4",
                1200, 1250, GameResult.UNKNOWN,
                TimeControlType.RAPID, "Opening 4", null)
        ));

        List<OpponentStrengthProjection> result =
            repository.getOpponentStrength("bacekalki");

        assertEquals(1, result.size());

        OpponentStrengthProjection lower = result.getFirst();

        assertEquals("LOWER_RATED", lower.getCategory());
        assertEquals(1, lower.getTotalGames());
        assertEquals(1, lower.getWins());
        assertEquals(0, lower.getLosses());
        assertEquals(0, lower.getDraws());
    }

    @Test
    void shouldMatchUsernameCaseInsensitively() {
        repository.save(
            new ChessGame(null, "Bacekalki", "opponent1",
                1200, 1100, GameResult.WHITE_WIN,
                TimeControlType.RAPID, "Opening 1", null)
        );

        List<OpponentStrengthProjection> result =
            repository.getOpponentStrength("bacekalki");

        assertEquals(1, result.size());

        OpponentStrengthProjection lower = result.getFirst();

        assertEquals("LOWER_RATED", lower.getCategory());
        assertEquals(1, lower.getTotalGames());
        assertEquals(1, lower.getWins());
        assertEquals(0, lower.getLosses());
        assertEquals(0, lower.getDraws());
    }


    @Test
    void shouldReturnRatingHistoryInChronologicalOrder() {
        repository.saveAll(List.of(
            new ChessGame(
                null, "bacekalki", "opponent1",
                1200, 1100, GameResult.WHITE_WIN,
                TimeControlType.RAPID, "Opening 1", null
            ),
            new ChessGame(
                null, "opponent2", "bacekalki",
                1250, 1150, GameResult.BLACK_WIN,
                TimeControlType.RAPID, "Opening 2", null
            )
        ));

        List<ChessGame> games = repository.findAll();

        ChessGame first = games.stream()
            .filter(g -> g.getOpeningName().equals("Opening 1"))
            .findFirst()
            .orElseThrow();

        ChessGame second = games.stream()
            .filter(g -> g.getOpeningName().equals("Opening 2"))
            .findFirst()
            .orElseThrow();

        first.setPlayedAt(LocalDate.of(2026, 8, 10));
        second.setPlayedAt(LocalDate.of(2026, 8, 5));

        repository.saveAll(List.of(first, second));

        List<RatingHistoryProjection> result =
            repository.getRatingHistory("bacekalki", "RAPID", "MANUAL");

        assertEquals(2, result.size());

        assertEquals(
            LocalDate.of(2026, 8, 5),
            result.get(0).getPlayedAt()
        );
        assertEquals(1150, result.get(0).getRating());

        assertEquals(
            LocalDate.of(2026, 8, 10),
            result.get(1).getPlayedAt()
        );
        assertEquals(1200, result.get(1).getRating());
    }


    @Test
    void shouldFilterRatingHistoryByTimeControl() {
        repository.saveAll(List.of(
            new ChessGame(
                null, "bacekalki", "opponent1",
                1200, 1100, GameResult.WHITE_WIN,
                TimeControlType.RAPID, "Rapid game", null
            ),
            new ChessGame(
                null, "bacekalki", "opponent2",
                1250, 1150, GameResult.BLACK_WIN,
                TimeControlType.BLITZ, "Blitz game", null
            )
        ));

        List<ChessGame> games = repository.findAll();
        for (ChessGame game : games) {
            game.setPlayedAt(LocalDate.of(2026, 8, 10));
        }
        repository.saveAll(games);

        List<RatingHistoryProjection> result =
            repository.getRatingHistory("bacekalki", "RAPID", "MANUAL");

        assertEquals(1, result.size());
        assertEquals(1200, result.getFirst().getRating());
    }

    @Test
    void shouldMatchRatingHistoryUsernameCaseInsensitively() {
        ChessGame game = new ChessGame(
            null, "Bacekalki", "opponent1",
            1200, 1100, GameResult.WHITE_WIN,
            TimeControlType.RAPID, "Opening 1", null
        );
        game.setPlayedAt(LocalDate.of(2026, 8, 10));
        repository.save(game);

        List<RatingHistoryProjection> result =
            repository.getRatingHistory("bacekalki", "RAPID", "MANUAL");

        assertEquals(1, result.size());
        assertEquals(1200, result.getFirst().getRating());
    }

    @Test
    void shouldExcludeRatingHistoryEntriesWithMissingRatingsOrDates() {
        repository.saveAll(List.of(
            new ChessGame(
                null, "bacekalki", "opponent1",
                1200, 1100, GameResult.WHITE_WIN,
                TimeControlType.RAPID, "Valid game", null
            ),
            new ChessGame(
                null, "bacekalki", "opponent2",
                null, 1300, GameResult.WHITE_WIN,
                TimeControlType.RAPID, "Missing rating", null
            ),
            new ChessGame(
                null, "bacekalki", "opponent3",
                1250, 1200, GameResult.BLACK_WIN,
                TimeControlType.RAPID, "Missing date", null
            )
        ));

        List<ChessGame> games = repository.findAll();

        ChessGame valid = games.stream()
            .filter(g -> g.getOpeningName().equals("Valid game"))
            .findFirst()
            .orElseThrow();

        valid.setPlayedAt(LocalDate.of(2026, 8, 10));
        repository.save(valid);

        List<RatingHistoryProjection> result =
            repository.getRatingHistory("bacekalki", "RAPID", "MANUAL");

        assertEquals(1, result.size());
        assertEquals(LocalDate.of(2026, 8, 10), result.getFirst().getPlayedAt());
        assertEquals(1200, result.getFirst().getRating());
    }

    @Test
    void shouldFilterOpeningAnalyticsByColor() {
        ChessGame whiteWin = new ChessGame(
            null, "bacekalki", "opponent1",
            1500, 1450, GameResult.WHITE_WIN,
            TimeControlType.BLITZ, "Sicilian Defense", null
        );

        ChessGame blackWin = new ChessGame(null,
            "opponent2", "bacekalki",
            1500, 1450, GameResult.BLACK_WIN,
            TimeControlType.RAPID, "French Defense", null
        );

        repository.save(whiteWin);
        repository.save(blackWin);

        List<OpeningAnalyticsProjection> result =
            repository.getOpeningAnalytics("bacekalki", "WHITE", null, 0, "winrate", "desc");

        assertEquals(1, result.size());
        assertEquals("Sicilian Defense", result.get(0).getOpeningName());
        assertEquals(1L, result.get(0).getTotalGames());
    }

    @Test
    void shouldFilterOpeningAnalyticsByTimeControl() {
        ChessGame blitzGame = new ChessGame(
            null, "bacekalki", "opponent1",
            1500, 1450, GameResult.WHITE_WIN,
            TimeControlType.BLITZ, "Sicilian Defense", null
        );

        ChessGame rapidGame = new ChessGame(
            null, "bacekalki", "opponent2",
            1500, 1450, GameResult.WHITE_WIN,
            TimeControlType.RAPID, "French Defense", null
        );

        repository.save(blitzGame);
        repository.save(rapidGame);

        List<OpeningAnalyticsProjection> result =
            repository.getOpeningAnalytics("bacekalki", null, "BLITZ", 0, "winrate", "desc");

        assertEquals(1, result.size());
        assertEquals("Sicilian Defense", result.get(0).getOpeningName());
        assertEquals(1L, result.get(0).getTotalGames());
    }


    @Test
    void shouldFilterOpeningAnalyticsByMinimumGames() {
        ChessGame french1 = new ChessGame(
            null, "bacekalki", "opponent1",
            1500, 1500, GameResult.WHITE_WIN,
            TimeControlType.BLITZ, "French Defense", null);

        ChessGame french2 = new ChessGame(
            null, "bacekalki", "opponent2",
            1500, 1500, GameResult.DRAW,
            TimeControlType.BLITZ, "French Defense", null);

        ChessGame sicilian = new ChessGame(
            null, "bacekalki", "opponent3",
            1500, 1500, GameResult.WHITE_WIN,
            TimeControlType.RAPID, "Sicilian Defense", null);

        repository.saveAll(List.of(french1, french2, sicilian));
        repository.flush();

        List<OpeningAnalyticsProjection> results =
            repository.getOpeningAnalytics("bacekalki", null, null, 2, "winrate", "desc");

        assertEquals(1, results.size());
        assertEquals("French Defense", results.get(0).getOpeningName());
        assertEquals(2L, results.get(0).getTotalGames());
    }

    @Test
    void shouldSearchGamesByUsernameIgnoringCaseAndPaginate() {
        repository.saveAll(List.of(
            game("Bacekalki", "opponent1",
                GameResult.WHITE_WIN, "Italian Game"),
            game("opponent2", "bacekalki",
                GameResult.BLACK_WIN, "French Defense"),
            game("someone_else", "another_player",
                GameResult.DRAW, "Sicilian Defense")
        ));

        repository.flush();

        Page<ChessGame> result = repository.searchGames(
            "bacekalki",
            null,
            null,
            null,
            null,
            null,
            null,
            PageRequest.of(0, 1)
        );

        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertTrue(
            result.getContent().getFirst().getWhiteUsername()
                .equalsIgnoreCase("bacekalki")
                || result.getContent().getFirst().getBlackUsername()
                .equalsIgnoreCase("bacekalki")
        );
    }

    @Test
    void shouldSearchGamesByPartialOpeningIgnoringCase() {
        repository.saveAll(List.of(
            game("bacekalki", "opponent1",
                GameResult.WHITE_WIN, "Italian Game"),
            game("bacekalki", "opponent2",
                GameResult.BLACK_WIN, "French Defense"),
            game("bacekalki", "opponent3",
                GameResult.DRAW, "Sicilian Defense")
        ));

        repository.flush();

        Page<ChessGame> result = repository.searchGames(
            "bacekalki",
            "ITALIAN",
            null, null, null, null, null,
            PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals("Italian Game",
            result.getContent().get(0).getOpeningName());
    }

    @Test
    void shouldSearchGamesByResultFromUsersPerspective() {
        repository.saveAll(List.of(
            game("bacekalki", "opponent1",
                GameResult.WHITE_WIN, "Italian Game"),
            game("opponent2", "bacekalki",
                GameResult.BLACK_WIN, "French Defense"),
            game("bacekalki", "opponent3",
                GameResult.BLACK_WIN, "Sicilian Defense")
        ));

        repository.flush();

        Page<ChessGame> wins = repository.searchGames(
            "bacekalki", null, "WIN", null, null, null, null,
            PageRequest.of(0, 10)
        );

        Page<ChessGame> losses = repository.searchGames(
            "bacekalki", null, "LOSS", null, null, null, null,
            PageRequest.of(0, 10)
        );

        assertEquals(2, wins.getTotalElements());
        assertEquals(1, losses.getTotalElements());
    }

    @Test
    void shouldSearchGamesByTimeControl() {
        ChessGame blitz = game(
            "bacekalki", "opponent1",
            GameResult.WHITE_WIN, "Italian Game");
        blitz.setTimeControl(TimeControlType.BLITZ);

        ChessGame rapid = game(
            "bacekalki", "opponent2",
            GameResult.BLACK_WIN, "French Defense");
        rapid.setTimeControl(TimeControlType.RAPID);

        repository.saveAll(List.of(blitz, rapid));
        repository.flush();

        Page<ChessGame> result = repository.searchGames(
            "bacekalki", null, null,
            TimeControlType.BLITZ,
            null, null, null,
            PageRequest.of(0, 10)
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(TimeControlType.BLITZ,
            result.getContent().get(0).getTimeControl());
    }

    @Test
    void shouldSearchGamesByColor() {
        repository.saveAll(List.of(
            game("bacekalki", "opponent1",
                GameResult.WHITE_WIN, "Italian Game"),
            game("opponent2", "bacekalki",
                GameResult.BLACK_WIN, "French Defense"),
            game("bacekalki", "opponent3",
                GameResult.DRAW, "Sicilian Defense")
        ));

        repository.flush();

        Page<ChessGame> whiteGames = repository.searchGames(
            "bacekalki", null, null, null,
            "WHITE", null, null,
            PageRequest.of(0, 10)
        );

        Page<ChessGame> blackGames = repository.searchGames(
            "bacekalki", null, null, null,
            "BLACK", null, null,
            PageRequest.of(0, 10)
        );

        assertEquals(2, whiteGames.getTotalElements());
        assertEquals(1, blackGames.getTotalElements());

        assertTrue(whiteGames.getContent().stream()
            .allMatch(g -> g.getWhiteUsername()
                .equalsIgnoreCase("bacekalki")));

        assertTrue(blackGames.getContent().stream()
            .allMatch(g -> g.getBlackUsername()
                .equalsIgnoreCase("bacekalki")));
    }

    @Test
    void shouldSearchGamesWithinInclusiveDateRange() {
        ChessGame before = game(
            "bacekalki", "opponent1",
            GameResult.WHITE_WIN, "Italian Game");
        before.setPlayedAt(LocalDate.of(2026, 1, 9));

        ChessGame start = game(
            "bacekalki", "opponent2",
            GameResult.BLACK_WIN, "French Defense");
        start.setPlayedAt(LocalDate.of(2026, 1, 10));

        ChessGame end = game(
            "bacekalki", "opponent3",
            GameResult.DRAW, "Sicilian Defense");
        end.setPlayedAt(LocalDate.of(2026, 1, 20));

        ChessGame after = game(
            "bacekalki", "opponent4",
            GameResult.WHITE_WIN, "Caro-Kann Defense");
        after.setPlayedAt(LocalDate.of(2026, 1, 21));

        repository.saveAll(List.of(before, start, end, after));
        repository.flush();

        Page<ChessGame> result = repository.searchGames(
            "bacekalki", null, null, null, null,
            LocalDate.of(2026, 1, 10),
            LocalDate.of(2026, 1, 20),
            PageRequest.of(0, 10)
        );

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream()
            .allMatch(g -> !g.getPlayedAt()
                .isBefore(LocalDate.of(2026, 1, 10))
                && !g.getPlayedAt()
                .isAfter(LocalDate.of(2026, 1, 20))));
    }

    @Test
    void shouldSortGamesByPlayedAtDescending() {
        ChessGame older = game(
            "bacekalki", "opponent1",
            GameResult.WHITE_WIN, "Italian Game");
        older.setPlayedAt(LocalDate.of(2026, 1, 10));

        ChessGame newer = game(
            "bacekalki", "opponent2",
            GameResult.BLACK_WIN, "French Defense");
        newer.setPlayedAt(LocalDate.of(2026, 1, 20));

        repository.saveAll(List.of(older, newer));
        repository.flush();

        Page<ChessGame> result = repository.searchGames(
            "bacekalki", null, null, null, null, null, null,
            PageRequest.of(0, 10)
        );

        assertEquals(2, result.getTotalElements());
        assertEquals(LocalDate.of(2026, 1, 20),
            result.getContent().get(0).getPlayedAt());
        assertEquals(LocalDate.of(2026, 1, 10),
            result.getContent().get(1).getPlayedAt());
    }


    private ChessGame game(
        String whiteUsername,
        String blackUsername,
        GameResult result,
        String openingName
    ) {
        return new ChessGame(
            null,
            whiteUsername,
            blackUsername,
            null,
            null,
            result,
            null,
            openingName,
            null
        );
    }
}

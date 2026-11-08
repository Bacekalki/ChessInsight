package com.chessinsight.chessinsight.analytics;

import com.chessinsight.chessinsight.analytics.dto.ChessAnalyticsDto;
import com.chessinsight.chessinsight.analytics.dto.ColorPerformanceDto;
import com.chessinsight.chessinsight.analytics.dto.OpeningAnalyticsDto;
import com.chessinsight.chessinsight.analytics.dto.OpponentStrengthDto;
import com.chessinsight.chessinsight.analytics.dto.RatingHistoryDto;
import com.chessinsight.chessinsight.analytics.dto.TimeControlAnalyticsDto;
import com.chessinsight.chessinsight.analytics.projection.ChessAnalyticsProjection;
import com.chessinsight.chessinsight.analytics.projection.OpeningAnalyticsProjection;
import com.chessinsight.chessinsight.game.ChessGameRepository;
import com.chessinsight.chessinsight.game.GamePlatform;
import com.chessinsight.chessinsight.game.TimeControlType;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChessAnalyticsService {
    private final ChessGameRepository repository;

    @Transactional(readOnly = true)
    public ChessAnalyticsDto getSummary(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                "Username must not be empty"
            );
        }

        String normalizedUsername = username.trim();

        ChessAnalyticsProjection result =
            repository.getAnalyticsForUsername(normalizedUsername);

        long wins = result.getWins();
        long losses = result.getLosses();
        long draws = result.getDraws();

        long totalGames = wins + losses + draws;

        double winRate = totalGames == 0
            ? 0.0
            : Math.round((double) wins / totalGames * 10000) / 100.0;

        return new ChessAnalyticsDto(
            normalizedUsername,
            totalGames,
            wins,
            losses,
            draws,
            winRate
        );
    }

    @Transactional(readOnly = true)
    public List<OpeningAnalyticsDto> getOpeningStats(
        String username, String color, String timeControl) {
        return getOpeningStats(username, color, timeControl, 5);
    }

    @Transactional(readOnly = true)
    public List<OpeningAnalyticsDto> getOpeningStats(
        String username, String color, String timeControl, int minGames) {
        return getOpeningStats(
            username, color, timeControl, minGames, "games", "desc");
    }

    @Transactional(readOnly = true)
    public List<OpeningAnalyticsDto> getOpeningStats(
        String username,
        String color,
        String timeControl,
        int minGames,
        String sortBy,
        String direction) {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }

        if (minGames < 1) {
            throw new IllegalArgumentException("Minimum games must be at least 1");
        }

        String normalizedUsername = username.trim();

        String normalizedColor = null;
        if (color != null && !color.isBlank()) {
            normalizedColor = color.trim().toUpperCase();

            if (!normalizedColor.equals("WHITE")
                && !normalizedColor.equals("BLACK")) {
                throw new IllegalArgumentException("Color must be WHITE or BLACK");
            }
        }

        String normalizedTimeControl = null;
        if (timeControl != null && !timeControl.isBlank()) {
            normalizedTimeControl = timeControl.trim().toUpperCase();

            if (!normalizedTimeControl.equals("BULLET")
                && !normalizedTimeControl.equals("BLITZ")
                && !normalizedTimeControl.equals("RAPID")) {
                throw new IllegalArgumentException(
                    "Time control must be BULLET, BLITZ, or RAPID");
            }
        }

        String normalizedSortBy = sortBy == null || sortBy.isBlank()
            ? "games"
            : sortBy.trim();

        if (!normalizedSortBy.equalsIgnoreCase("games")
            && !normalizedSortBy.equalsIgnoreCase("winRate")) {
            throw new IllegalArgumentException(
                "Sort by must be games or winRate");
        }

        normalizedSortBy = normalizedSortBy.toLowerCase();

        String normalizedDirection = direction == null || direction.isBlank()
            ? "desc"
            : direction.trim().toLowerCase();

        if (!normalizedDirection.equals("asc")
            && !normalizedDirection.equals("desc")) {
            throw new IllegalArgumentException(
                "Direction must be asc or desc");
        }

        return repository.getOpeningAnalytics(
                normalizedUsername,
                normalizedColor,
                normalizedTimeControl,
                minGames,
                normalizedSortBy,
                normalizedDirection)
            .stream()
            .map(projection -> {
                double winRate = projection.getTotalGames() == 0
                    ? 0.0
                    : projection.getWins() * 100.0
                    / projection.getTotalGames();

                return new OpeningAnalyticsDto(
                    projection.getOpeningName(),
                    projection.getTotalGames(),
                    projection.getWins(),
                    projection.getLosses(),
                    projection.getDraws(),
                    winRate
                );
            })
            .toList();
    }

    public List<ColorPerformanceDto> getColorPerformance(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }

        return repository.getColorPerformance(username.trim())
            .stream()
            .map(row -> {
                long totalGames = row.getTotalGames();
                long wins = row.getWins();

                double winRate = totalGames == 0
                    ? 0.0
                    : Math.round((double) wins / totalGames * 10000) / 100.0;

                return new ColorPerformanceDto(
                    row.getColor(),
                    totalGames,
                    wins,
                    row.getLosses(),
                    row.getDraws(),
                    winRate
                );
            })
            .toList();
    }

    public List<OpponentStrengthDto> getOpponentStrength(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }

        return repository.getOpponentStrength(username.trim())
            .stream()
            .map(projection -> {
                long totalGames = projection.getTotalGames();

                double winRate = totalGames == 0
                    ? 0.0
                    : Math.round(
                    (double) projection.getWins() / totalGames * 1000
                ) / 10.0;

                return new OpponentStrengthDto(
                    projection.getCategory(),
                    totalGames,
                    projection.getWins(),
                    projection.getLosses(),
                    projection.getDraws(),
                    winRate
                );
            })
            .toList();
    }

    @Transactional(readOnly = true)
    public List<RatingHistoryDto> getRatingHistory(
        String username,
        String timeControl,
        String platform
    ) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }

        if (timeControl == null || timeControl.isBlank()) {
            throw new IllegalArgumentException("Time control must not be blank");
        }

        if (platform == null || platform.isBlank()) {
            throw new IllegalArgumentException("Platform must not be blank");
        }

        String normalizedUsername = username.trim();
        String normalizedTimeControl = timeControl.trim().toUpperCase();
        String normalizedPlatform = platform.trim().toUpperCase();

        TimeControlType control;

        try {
            control = TimeControlType.valueOf(normalizedTimeControl);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid time control. Use BULLET, BLITZ, or RAPID"
            );
        }

        if (control == TimeControlType.UNKNOWN) {
            throw new IllegalArgumentException(
                "Invalid time control. Use BULLET, BLITZ, or RAPID"
            );
        }

        GamePlatform gamePlatform;

        try {
            gamePlatform = GamePlatform.valueOf(normalizedPlatform);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Invalid platform. Use CHESS_COM, LICHESS, or MANUAL"
            );
        }

        return repository
            .getRatingHistory(
                normalizedUsername,
                control.name(),
                gamePlatform.name()
            )
            .stream()
            .map(entry -> new RatingHistoryDto(
                entry.getPlayedAt(),
                entry.getRating()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TimeControlAnalyticsDto> getTimeControlStats(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }

        String normalizedUsername = username.trim();

        return repository.getTimeControlAnalytics(normalizedUsername)
            .stream()
            .map(projection -> {
                double winRate = projection.getTotalGames() == 0
                    ? 0.0
                    : projection.getWins() * 100.0
                    / projection.getTotalGames();

                return new TimeControlAnalyticsDto(
                    projection.getTimeControl(),
                    projection.getTotalGames(),
                    projection.getWins(),
                    projection.getLosses(),
                    projection.getDraws(),
                    winRate
                );
            })
            .toList();
    }
}


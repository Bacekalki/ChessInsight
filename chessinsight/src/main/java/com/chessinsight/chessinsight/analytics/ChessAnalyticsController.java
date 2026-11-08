package com.chessinsight.chessinsight.analytics;

import com.chessinsight.chessinsight.analytics.dto.ChessAnalyticsDto;
import com.chessinsight.chessinsight.analytics.dto.ColorPerformanceDto;
import com.chessinsight.chessinsight.analytics.dto.OpeningAnalyticsDto;
import com.chessinsight.chessinsight.analytics.dto.OpponentStrengthDto;
import com.chessinsight.chessinsight.analytics.dto.RatingHistoryDto;
import com.chessinsight.chessinsight.analytics.dto.TimeControlAnalyticsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class ChessAnalyticsController {

    private final ChessAnalyticsService analyticsService;

    @GetMapping("/summary")
    public ChessAnalyticsDto getSummary(
        @RequestParam String username) {
        return analyticsService.getSummary(username);
    }

    @GetMapping("/openings")
    public List<OpeningAnalyticsDto> getOpeningStats(
        @RequestParam String username,
        @RequestParam(required = false) String color,
        @RequestParam(required = false) String timeControl,
        @RequestParam(defaultValue = "5") int minGames,
        @RequestParam(defaultValue = "games") String sortBy,
        @RequestParam(defaultValue = "desc") String direction) {

        return analyticsService.getOpeningStats(
            username,
            color,
            timeControl,
            minGames,
            sortBy,
            direction
        );
    }

    @GetMapping("/color")
    public List<ColorPerformanceDto> getColorPerformance(
        @RequestParam String username
    ) {
        return analyticsService.getColorPerformance(username);
    }

    @GetMapping("/opponent-strength")
    public List<OpponentStrengthDto> getOpponentStrength(
        @RequestParam String username) {
        return analyticsService.getOpponentStrength(username);
    }

    @GetMapping("/rating-history")
    public List<RatingHistoryDto> getRatingHistory(
        @RequestParam String username,
        @RequestParam String timeControl,
        @RequestParam String platform
    ) {
        return analyticsService.getRatingHistory(
            username,
            timeControl,
            platform
        );
    }

    @GetMapping("/time-controls")
    public List<TimeControlAnalyticsDto> getTimeControlStats(
        @RequestParam String username
    ) {
        return analyticsService.getTimeControlStats(username);
    }
}

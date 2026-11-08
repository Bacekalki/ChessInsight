package com.chessinsight.chessinsight.analytics.dto;

import java.time.LocalDate;

public record RatingHistoryDto(
    LocalDate playedAt,
    Integer rating
) {
}

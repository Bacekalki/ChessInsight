package com.chessinsight.chessinsight.analytics.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface RatingHistoryProjection {
    LocalDate getPlayedAt();

    Integer getRating();
}

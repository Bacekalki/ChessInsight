package com.chessinsight.chessinsight.dto;

import com.chessinsight.chessinsight.game.GamePlatform;
import com.chessinsight.chessinsight.game.GameResult;
import com.chessinsight.chessinsight.game.TimeControlType;

import java.time.LocalDate;

public record ChessGameDto(
    Long id,
    LocalDate playedAt,
    String whiteUsername,
    String blackUsername,
    Integer whiteRating,
    Integer blackRating,
    GameResult result,
    String timeControl,
    String openingName,
    GamePlatform platform
) {
    public ChessGameDto(
        Long id,
        LocalDate playedAt,
        String whiteUsername,
        String blackUsername,
        Integer whiteRating,
        Integer blackRating,
        GameResult result,
        String timeControl,
        String openingName
    ) {
        this(
            id,
            playedAt,
            whiteUsername,
            blackUsername,
            whiteRating,
            blackRating,
            result,
            timeControl,
            openingName,
            null
        );
    }
}

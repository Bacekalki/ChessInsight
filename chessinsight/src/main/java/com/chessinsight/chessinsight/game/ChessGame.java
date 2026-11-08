package com.chessinsight.chessinsight.game;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "chess_games")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChessGame {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    private LocalDate playedAt;

    private String whiteUsername;
    private String blackUsername;

    private Integer whiteRating;
    private Integer blackRating;

    @Enumerated(EnumType.STRING)
    private GameResult result;

    @Setter
    @Enumerated(EnumType.STRING)
    private TimeControlType timeControl;

    private String openingName;

    @Column(columnDefinition = "TEXT")
    private String pgn;

    @Setter
    @Column(unique = true, length = 64)
    private String pgnHash;

    @Enumerated(EnumType.STRING)
    private GamePlatform platform;

    public ChessGame(
        LocalDate playedAt,
        String whiteUsername,
        String blackUsername,
        Integer whiteRating,
        Integer blackRating,
        GameResult result,
        TimeControlType timeControl,
        String openingName,
        String pgn,
        GamePlatform gamePlatform
    ) {
        this.playedAt = playedAt;
        this.whiteUsername = whiteUsername;
        this.blackUsername = blackUsername;
        this.whiteRating = whiteRating;
        this.blackRating = blackRating;
        this.result = result;
        this.timeControl = timeControl;
        this.openingName = openingName;
        this.pgn = pgn;
        this.platform = gamePlatform;
    }

    public ChessGame(
        LocalDate playedAt,
        String whiteUsername,
        String blackUsername,
        Integer whiteRating,
        Integer blackRating,
        GameResult result,
        TimeControlType timeControl,
        String openingName,
        String pgn
    ) {
        this(
            playedAt,
            whiteUsername,
            blackUsername,
            whiteRating,
            blackRating,
            result,
            timeControl,
            openingName,
            pgn,
            GamePlatform.MANUAL
        );
    }

}
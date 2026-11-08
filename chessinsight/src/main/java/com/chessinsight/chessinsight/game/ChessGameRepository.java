package com.chessinsight.chessinsight.game;

import com.chessinsight.chessinsight.analytics.projection.ChessAnalyticsProjection;
import com.chessinsight.chessinsight.analytics.projection.ColorPerformanceProjection;
import com.chessinsight.chessinsight.analytics.projection.OpeningAnalyticsProjection;
import com.chessinsight.chessinsight.analytics.projection.OpponentStrengthProjection;
import com.chessinsight.chessinsight.analytics.projection.RatingHistoryProjection;
import com.chessinsight.chessinsight.analytics.projection.TimeControlAnalyticsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ChessGameRepository extends JpaRepository<ChessGame, Long> {
    @Query(value = """
        SELECT
            COALESCE(SUM(CASE
                WHEN (result = 'WHITE_WIN' AND LOWER(white_username) = LOWER(:username))\n              OR (result = 'BLACK_WIN' AND LOWER(black_username) = LOWER(:username))\n            THEN 1 ELSE 0\n        END), 0) AS wins,\n
            COALESCE(SUM(CASE
                WHEN (result = 'BLACK_WIN' AND LOWER(white_username) = LOWER(:username))\n              OR (result = 'WHITE_WIN' AND LOWER(black_username) = LOWER(:username))\n            THEN 1 ELSE 0\n        END), 0) AS losses,\n
            COALESCE(SUM(CASE
                WHEN result = 'DRAW'\n            THEN 1 ELSE 0\n        END), 0) AS draws\n
        FROM chess_games
        WHERE (
            LOWER(white_username) = LOWER(:username)
            OR LOWER(black_username) = LOWER(:username)
        )
        AND result IN ('WHITE_WIN', 'BLACK_WIN', 'DRAW')
        """, nativeQuery = true)
    ChessAnalyticsProjection getAnalyticsForUsername(@Param("username") String username);

    @Query(value = """
        SELECT
            opening_name AS "openingName",
            COUNT(*) AS "totalGames",
            SUM(CASE
                WHEN (result = 'WHITE_WIN'
                      AND LOWER(white_username) = LOWER(:username))
                  OR (result = 'BLACK_WIN'
                      AND LOWER(black_username) = LOWER(:username))
                THEN 1 ELSE 0
            END) AS wins,
            SUM(CASE
                WHEN (result = 'BLACK_WIN'
                      AND LOWER(white_username) = LOWER(:username))
                  OR (result = 'WHITE_WIN'
                      AND LOWER(black_username) = LOWER(:username))
                THEN 1 ELSE 0
            END) AS losses,
            SUM(CASE
                WHEN result = 'DRAW' THEN 1 ELSE 0
            END) AS draws
        FROM chess_games
        WHERE (
            LOWER(white_username) = LOWER(:username)
            OR LOWER(black_username) = LOWER(:username)
        )
        AND opening_name IS NOT NULL
        AND TRIM(opening_name) <> ''
        AND result IN ('WHITE_WIN', 'BLACK_WIN', 'DRAW')
        AND (
            :color IS NULL
            OR (:color = 'WHITE' AND LOWER(white_username) = LOWER(:username))
            OR (:color = 'BLACK' AND LOWER(black_username) = LOWER(:username))
        )
        AND (
            :timeControl IS NULL
            OR time_control = :timeControl
        )
        GROUP BY opening_name
        HAVING COUNT(*) >= :minGames
        ORDER BY  CASE
                         WHEN :sortBy = 'games' AND :direction = 'asc'
                         THEN COUNT(*)
                     END ASC,
                     CASE
                         WHEN :sortBy = 'games' AND :direction = 'desc'
                         THEN COUNT(*)
                     END DESC,
                     CASE
                         WHEN :sortBy = 'winrate' AND :direction = 'asc'
                         THEN SUM(CASE
                             WHEN (result = 'WHITE_WIN'
                                   AND LOWER(white_username) = LOWER(:username))
                               OR (result = 'BLACK_WIN'
                                   AND LOWER(black_username) = LOWER(:username))
                             THEN 1 ELSE 0
                         END) * 100.0 / COUNT(*)
                     END ASC,
                     CASE
                         WHEN :sortBy = 'winrate' AND :direction = 'desc'
                         THEN SUM(CASE
                             WHEN (result = 'WHITE_WIN'
                                   AND LOWER(white_username) = LOWER(:username))
                               OR (result = 'BLACK_WIN'
                                   AND LOWER(black_username) = LOWER(:username))
                             THEN 1 ELSE 0
                         END) * 100.0 / COUNT(*)
                     END DESC,
                     opening_name ASC
        """, nativeQuery = true)
    List<OpeningAnalyticsProjection> getOpeningAnalytics(
        @Param("username") String username,
        @Param("color") String color,
        @Param("timeControl") String timeControl,
        @Param("minGames") int minGames,
        @Param("sortBy") String sortBy,
        @Param("direction") String direction
    );

    boolean existsByPgnHash(String pgnHash);

    @Query(value = """
        SELECT
            'WHITE' AS color,
            COUNT(*) AS "totalGames",
            COALESCE(SUM(CASE WHEN result = 'WHITE_WIN' THEN 1 ELSE 0 END), 0) AS wins,
            COALESCE(SUM(CASE WHEN result = 'BLACK_WIN' THEN 1 ELSE 0 END), 0) AS losses,
            COALESCE(SUM(CASE WHEN result = 'DRAW' THEN 1 ELSE 0 END), 0) AS draws
        FROM chess_games
        WHERE LOWER(white_username) = LOWER(:username)
          AND result IN ('WHITE_WIN', 'BLACK_WIN', 'DRAW')
        
        UNION ALL
        
        SELECT
            'BLACK' AS color,
            COUNT(*) AS "totalGames",
            COALESCE(SUM(CASE WHEN result = 'BLACK_WIN' THEN 1 ELSE 0 END), 0) AS wins,
            COALESCE(SUM(CASE WHEN result = 'WHITE_WIN' THEN 1 ELSE 0 END), 0) AS losses,
            COALESCE(SUM(CASE WHEN result = 'DRAW' THEN 1 ELSE 0 END), 0) AS draws
        FROM chess_games
        WHERE LOWER(black_username) = LOWER(:username)
          AND result IN ('WHITE_WIN', 'BLACK_WIN', 'DRAW')
        """, nativeQuery = true)
    List<ColorPerformanceProjection> getColorPerformance(
        @Param("username") String username
    );


    @Query(value = """
        WITH user_games AS (
            SELECT
                CASE
                    WHEN LOWER(white_username) = LOWER(:username)
                        THEN white_rating
                    ELSE black_rating
                END AS user_rating,
        
                CASE
                    WHEN LOWER(white_username) = LOWER(:username)
                        THEN black_rating
                    ELSE white_rating
                END AS opponent_rating,
        
                CASE
                    WHEN LOWER(white_username) = LOWER(:username)
                         AND result = 'WHITE_WIN'
                        THEN 'WIN'
                    WHEN LOWER(white_username) = LOWER(:username)
                         AND result = 'BLACK_WIN'
                        THEN 'LOSS'
                    WHEN LOWER(black_username) = LOWER(:username)
                         AND result = 'BLACK_WIN'
                        THEN 'WIN'
                    WHEN LOWER(black_username) = LOWER(:username)
                         AND result = 'WHITE_WIN'
                        THEN 'LOSS'
                    WHEN result = 'DRAW'
                        THEN 'DRAW'
                END AS user_result
        
            FROM chess_games
            WHERE (
                LOWER(white_username) = LOWER(:username)
                OR LOWER(black_username) = LOWER(:username)
            )
            AND white_rating IS NOT NULL
            AND black_rating IS NOT NULL
            AND result IN ('WHITE_WIN', 'BLACK_WIN', 'DRAW')
        ),
        classified_games AS (
            SELECT
                CASE
                    WHEN opponent_rating < user_rating - 25
                        THEN 'LOWER_RATED'
                    WHEN opponent_rating > user_rating + 25
                        THEN 'HIGHER_RATED'
                    ELSE 'SIMILAR_RATED'
                END AS category,
                user_result
            FROM user_games
        )
        SELECT
            category AS "category",
            COUNT(*) AS "totalGames",
            COALESCE(SUM(
                CASE WHEN user_result = 'WIN' THEN 1 ELSE 0 END
            ), 0) AS wins,
            COALESCE(SUM(
                CASE WHEN user_result = 'LOSS' THEN 1 ELSE 0 END
            ), 0) AS losses,
            COALESCE(SUM(
                CASE WHEN user_result = 'DRAW' THEN 1 ELSE 0 END
            ), 0) AS draws
        FROM classified_games
        GROUP BY category
        """, nativeQuery = true)
    List<OpponentStrengthProjection> getOpponentStrength(
        @Param("username") String username
    );

    @Query(value = """
        SELECT
            played_at AS "playedAt",
            CASE
                WHEN LOWER(white_username) = LOWER(:username)
                    THEN white_rating
                ELSE black_rating
            END AS "rating"
        FROM chess_games
        WHERE (
            LOWER(white_username) = LOWER(:username)
            OR LOWER(black_username) = LOWER(:username)
        )
        AND time_control = :timeControl
        AND platform = :platform
        AND played_at IS NOT NULL
        AND (
            (LOWER(white_username) = LOWER(:username)
                AND white_rating IS NOT NULL)
            OR
            (LOWER(black_username) = LOWER(:username)
                AND black_rating IS NOT NULL)
        )
        ORDER BY played_at ASC, id ASC
        """, nativeQuery = true)
    List<RatingHistoryProjection> getRatingHistory(
        @Param("username") String username,
        @Param("timeControl") String timeControl,
        @Param("platform") String platform
    );


    @Query(value = """
        SELECT
            time_control AS "timeControl",
            COUNT(*) AS "totalGames",
        
            SUM(CASE
                WHEN (result = 'WHITE_WIN'
                      AND LOWER(white_username) = LOWER(:username))
                  OR (result = 'BLACK_WIN'
                      AND LOWER(black_username) = LOWER(:username))
                THEN 1 ELSE 0
            END) AS "wins",
        
            SUM(CASE
                WHEN (result = 'BLACK_WIN'
                      AND LOWER(white_username) = LOWER(:username))
                  OR (result = 'WHITE_WIN'
                      AND LOWER(black_username) = LOWER(:username))
                THEN 1 ELSE 0
            END) AS "losses",
        
            SUM(CASE
                WHEN result = 'DRAW'
                THEN 1 ELSE 0
            END) AS "draws"
        
        FROM chess_games
        WHERE (
            LOWER(white_username) = LOWER(:username)
            OR LOWER(black_username) = LOWER(:username)
        )
        AND result IN ('WHITE_WIN', 'BLACK_WIN', 'DRAW')
        AND time_control IN ('BULLET', 'BLITZ', 'RAPID')
        GROUP BY time_control
        ORDER BY time_control
        """, nativeQuery = true)
    List<TimeControlAnalyticsProjection> getTimeControlAnalytics(
        @Param("username") String username
    );

    @Query("""
        SELECT g
        FROM ChessGame g
        WHERE (
            :username IS NULL
            OR LOWER(g.whiteUsername) = LOWER(:username)
            OR LOWER(g.blackUsername) = LOWER(:username)
        )
        AND (
            CAST(:opening AS string) IS NULL
            OR LOWER(g.openingName) LIKE
               LOWER(CONCAT('%', CAST(:opening AS string), '%'))
        )
        AND (
            :result IS NULL
            OR (
                :result = 'DRAW'
                AND g.result = com.chessinsight.chessinsight.game.GameResult.DRAW
            )
            OR (
                :result = 'WIN'
                AND (
                    (
                        LOWER(g.whiteUsername) = LOWER(:username)
                        AND g.result = com.chessinsight.chessinsight.game.GameResult.WHITE_WIN
                    )
                    OR
                    (
                        LOWER(g.blackUsername) = LOWER(:username)
                        AND g.result = com.chessinsight.chessinsight.game.GameResult.BLACK_WIN
                    )
                )
            )
            OR (
                :result = 'LOSS'
                AND (
                    (
                        LOWER(g.whiteUsername) = LOWER(:username)
                        AND g.result = com.chessinsight.chessinsight.game.GameResult.BLACK_WIN
                    )
                    OR
                    (
                        LOWER(g.blackUsername) = LOWER(:username)
                        AND g.result = com.chessinsight.chessinsight.game.GameResult.WHITE_WIN
                    )
                )
            )
        )
        AND (
            :timeControl IS NULL
            OR g.timeControl = :timeControl
        )
        AND (
            :platform IS NULL
            OR g.platform = :platform
        )
        AND (
            :color IS NULL
            OR (
                :color = 'WHITE'
                AND LOWER(g.whiteUsername) = LOWER(:username)
            )
            OR (
                :color = 'BLACK'
                AND LOWER(g.blackUsername) = LOWER(:username)
            )
        )
            AND (
            CAST(:fromDate AS LocalDate) IS NULL
            OR g.playedAt >= :fromDate
            )
            AND (
            CAST(:toDate AS LocalDate) IS NULL
            OR g.playedAt <= :toDate
            )
        ORDER BY g.playedAt DESC
        """)
    Page<ChessGame> searchGames(
        @Param("username") String username,
        @Param("opening") String opening,
        @Param("result") String result,
        @Param("timeControl") TimeControlType timeControl,
        @Param("platform") GamePlatform platform,
        @Param("color") String color,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        Pageable pageable
    );

    default Page<ChessGame> searchGames(
        String username,
        String opening,
        String result,
        TimeControlType timeControl,
        String color,
        LocalDate fromDate,
        LocalDate toDate,
        Pageable pageable
    ) {
        return searchGames(
            username,
            opening,
            result,
            timeControl,
            null,
            color,
            fromDate,
            toDate,
            pageable
        );
    }
}

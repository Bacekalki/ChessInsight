package com.chessinsight.chessinsight.chess.pgn;

import com.chessinsight.chessinsight.chess.OpeningClassifier;
import com.chessinsight.chessinsight.chess.TimeControlClassifier;
import com.chessinsight.chessinsight.game.ChessGame;
import com.chessinsight.chessinsight.game.ChessGameRepository;
import com.chessinsight.chessinsight.game.GamePlatform;
import com.chessinsight.chessinsight.game.GameResult;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.pgn.PgnIterator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.bhlangonijr.chesslib.util.LargeFile;

import java.io.File;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PgnImportService {

    private final ChessGameRepository repository;
    private final OpeningClassifier classifier;
    private final TimeControlClassifier timeControlClassifier;

    private LocalDate parseDate(String date) {
        if (date == null || date.isBlank()
            || date.contains("?")) {
            return null;
        }

        try {
            return LocalDate.parse(
                date,
                java.time.format.DateTimeFormatter
                    .ofPattern("yyyy.MM.dd")
            );
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }

    private String extractTimeControl(String pgn) {
        java.util.regex.Matcher matcher = java.util.regex.Pattern
            .compile("(?m)^\\[TimeControl\\s+\"([^\"]+)\"\\]\\s*$")
            .matcher(pgn);

        return matcher.find() ? matcher.group(1) : null;
    }



    @Transactional(rollbackFor = Exception.class)
    public PgnImportReport importGames(File file) throws Exception {
        return importGames(file, GamePlatform.MANUAL, Integer.MAX_VALUE);
    }

    @Transactional(rollbackFor = Exception.class)
    public PgnImportReport importGames(
        File file,
        GamePlatform platform,
        int numberOfGames
    ) throws Exception {
        int imported = 0;
        int skipped = 0;
        int totalProcessed = 0;
        int malformed = 0;

        try (
            LargeFile largeFile = new LargeFile(file.getAbsolutePath());
            PgnIterator iterator = new PgnIterator(largeFile)
        ) {
            for (Game game : iterator) {
                if (totalProcessed > numberOfGames) {
                    break;
                }
                game.loadMoveText();

                if (game.getMoveText() == null) {
                    skipped++;
                    malformed++;
                    totalProcessed++;
                    continue;
                }

                String pgn = game.toPgn(false, false);
                String hash = PgnHashUtil.sha256(pgn);

                totalProcessed++;

                if (repository.existsByPgnHash(hash)) {
                    skipped++;
                    continue;
                }

                ChessGame chessGame = new ChessGame(
                    parseDate(game.getDate()),
                    game.getWhitePlayer().getName(),
                    game.getBlackPlayer().getName(),
                    game.getWhitePlayer().getElo(),
                    game.getBlackPlayer().getElo(),
                    mapResult(game.getResult().toString()),
                    timeControlClassifier.classify(
                        extractTimeControl(pgn)
                    ),
                    classifier.classify(
                        game.getHalfMoves().stream().toList()
                    ),
                    pgn,
                    platform
                );

                chessGame.setPgnHash(hash);
                repository.save(chessGame);
                imported++;
            }
        }

        return new PgnImportReport(
            imported,
            skipped,
            malformed,
            totalProcessed
        );
    }

    private GameResult mapResult(String result) {
        return switch (result.trim().toUpperCase()) {
            case "1-0", "WHITE_WON", "WHITE_WIN" ->
                GameResult.WHITE_WIN;
            case "0-1", "BLACK_WON", "BLACK_WIN" ->
                GameResult.BLACK_WIN;
            case "1/2-1/2", "DRAW" ->
                GameResult.DRAW;
            default -> GameResult.UNKNOWN;
        };
    }
}
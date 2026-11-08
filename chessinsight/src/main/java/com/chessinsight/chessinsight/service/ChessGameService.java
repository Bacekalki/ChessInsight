package com.chessinsight.chessinsight.service;

import com.chessinsight.chessinsight.dto.ChessGameDto;
import com.chessinsight.chessinsight.game.ChessGame;
import com.chessinsight.chessinsight.game.ChessGameRepository;
import com.chessinsight.chessinsight.game.GamePlatform;
import com.chessinsight.chessinsight.game.GameResult;
import com.chessinsight.chessinsight.game.TimeControlType;
import com.chessinsight.chessinsight.mapper.ChessGameMapper;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.game.Game;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.pgn.GameLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ChessGameService {

    private final ChessGameRepository repository;
    private final ChessGameMapper mapper;

    @Transactional(readOnly = true)
    public Page<ChessGameDto> getGames(Pageable pageable) {
        return repository.findAll(pageable)
            .map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public String getPgnById(Long id) {
        ChessGame game = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Game not found with id: " + id
            ));

        return game.getPgn();
    }

    private LocalDate parseDate(String value, String parameterName) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid " + parameterName + " date. Use YYYY-MM-DD."
            );
        }
    }

    @Transactional(readOnly = true)
    public Page<ChessGameDto> searchGames(
        String username,
        String opening,
        String result,
        String timeControl,
        String platform,
        String color,
        String from,
        String to,
        Pageable pageable) {

        if (username == null || username.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Username is required."
            );
        }

        String normalizedUsername = username.trim();

        String normalizedOpening = opening == null || opening.isBlank()
            ? null : opening.trim();

        String normalizedResult = null;

        if (result != null && !result.isBlank()) {
            normalizedResult = result.trim().toUpperCase(Locale.ROOT);

            if (!normalizedResult.equals("WIN")
                && !normalizedResult.equals("LOSS")
                && !normalizedResult.equals("DRAW")) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid result. Use WIN, LOSS, or DRAW."
                );
            }
        }

        TimeControlType normalizedTimeControl = null;

        if (timeControl != null && !timeControl.isBlank()) {
            try {
                normalizedTimeControl = TimeControlType.valueOf(
                    timeControl.trim().toUpperCase(Locale.ROOT)
                );
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid time control. Use BULLET, BLITZ, or RAPID."
                );
            }

            if (normalizedTimeControl == TimeControlType.UNKNOWN) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid time control. Use BULLET, BLITZ, or RAPID."
                );
            }
        }

        GamePlatform normalizedPlatform = null;

        if (platform != null && !platform.isBlank()) {
            try {
                normalizedPlatform = GamePlatform.valueOf(
                    platform.trim().toUpperCase(Locale.ROOT)
                );
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid platform. Use CHESS_COM, LICHESS, or MANUAL."
                );
            }
        }

        String normalizedColor = null;

        if (color != null && !color.isBlank()) {
            normalizedColor = color.trim().toUpperCase(Locale.ROOT);

            if (!normalizedColor.equals("WHITE")
                && !normalizedColor.equals("BLACK")) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid color. Use WHITE or BLACK."
                );
            }

        }

        LocalDate fromDate = parseDate(from, "from");
        LocalDate toDate = parseDate(to, "to");

        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "'from' date must not be after 'to' date."
            );
        }

        return repository.searchGames(
            normalizedUsername,
            normalizedOpening,
            normalizedResult,
            normalizedTimeControl,
            normalizedPlatform,
            normalizedColor,
            fromDate,
            toDate,
            pageable
        ).map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ChessGameDto> searchGames(
        String username,
        String opening,
        String result,
        String timeControl,
        String color,
        String from,
        String to,
        Pageable pageable) {
        return searchGames(
            username,
            opening,
            result,
            timeControl,
            null,
            color,
            from,
            to,
            pageable
        );
    }

    @Transactional(readOnly = true)
    public ChessGameDto getGameById(Long id) {
        ChessGame game = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Game not found with id: " + id
            ));

        return mapper.toDto(game);
    }

    @Transactional(readOnly = true)
    public List<String> getMovesById(Long id) {
        String pgn = getPgnById(id);

        try {
            Game game = GameLoader.loadNextGame(
                Arrays.asList(pgn.split("\\R")).iterator()
            );

            if (game == null) {
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not parse PGN for game id: " + id
                );
            }

            game.loadMoveText();

            return Arrays.asList(game.getHalfMoves().toSanArray());

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Could not parse PGN for game id: " + id,
                e
            );
        }
    }

    @Transactional(readOnly = true)
    public List<String> getUciMovesById(Long id) {
        String pgn = getPgnById(id);

        try {
            Game game = GameLoader.loadNextGame(
                Arrays.asList(pgn.split("\\R")).iterator()
            );

            if (game == null) {
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not parse PGN for game id: " + id
                );
            }

            game.loadMoveText();

            return game.getHalfMoves().stream()
                .map(Move::toString)
                .toList();

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Could not parse PGN for game id: " + id,
                e
            );
        }
    }

    @Transactional(readOnly = true)
    public List<String> getPositionsById(Long id) {
        String pgn = getPgnById(id);

        try {
            Game game = GameLoader.loadNextGame(
                Arrays.asList(pgn.split("\\R")).iterator()
            );

            if (game == null) {
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not parse PGN for game id: " + id
                );
            }

            game.loadMoveText();

            Board board = new Board();
            List<String> positions = new ArrayList<>();

            // Position 0: before the first move.
            positions.add(board.getFen());

            for (Move move : game.getHalfMoves()) {
                board.doMove(move);
                positions.add(board.getFen());
            }

            return positions;

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Could not reconstruct positions for game id: " + id,
                e
            );
        }
    }
}

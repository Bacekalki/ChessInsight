package com.chessinsight.chessinsight.service;

import com.chessinsight.chessinsight.game.ChessGame;
import com.chessinsight.chessinsight.game.ChessGameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ChessGameServiceTest {

    @Mock
    private ChessGameRepository chessGameRepository;

    @InjectMocks
    private ChessGameService chessGameService;

    @Test
    void shouldReturnMovesInUciNotation() {
        Long gameId = 1L;

        String pgn = """
            [Event "UCI test"]
            [Site "?"]
            [Date "2026.01.01"]
            [Round "1"]
            [White "Player1"]
            [Black "Player2"]
            [Result "1-0"]
            
            1. e4 e5 2. Nf3 Nc6 1-0
            """;

        when(chessGameRepository.findById(gameId))
            .thenReturn(Optional.of(new ChessGame(null, null, null, null, null, null, null, null, pgn)
            ));

        List<String> result = chessGameService.getUciMovesById(gameId);

        assertEquals(
            List.of("e2e4", "e7e5", "g1f3", "b8c6"),
            result
        );
    }
}

package com.chessinsight.chessinsight.analysis;

import com.github.bhlangonijr.chesslib.Board;
import org.springframework.stereotype.Component;

@Component
public class FenValidator {

    public void validate(String fen) {
        if (fen == null || fen.isBlank()) {
            throw new IllegalArgumentException(
                "FEN must not be blank.");
        }

        try {
            Board board = new Board();
            board.loadFromFen(fen.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Invalid FEN position.");
        }
    }
}
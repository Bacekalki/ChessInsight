package com.chessinsight.chessinsight.chess;


import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class OpeningClassifierTest {

    private static OpeningClassifier openingClassifier;

    @BeforeAll
    static void setUp() {
        openingClassifier = new OpeningClassifier();
        openingClassifier.loadOpenings();
    }

    @Test
    void shouldRecognizeKnownOpening() throws Exception {
        MoveList moveList = new MoveList();
        moveList.loadFromSan("1. e4 e5 2. Nf3 Nc6 3. Bc4");

        List<Move> moves = new ArrayList<>(moveList);

        String opening = openingClassifier.classify(moves);
        assertEquals("Italian Game", opening);
    }

    @Test
    void shouldReturnNullForGameWithoutRecognizedOpening() {
        String opening = openingClassifier.classify(List.of());

        assertNull(opening);
    }
}

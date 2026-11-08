package com.chessinsight.chessinsight.analysis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoveClassificationServiceTest {

    private final MoveClassificationService service =
        new MoveClassificationService();

    @Test
    void shouldClassifyBestMoves() {
        assertEquals("BEST", service.classify(0));
        assertEquals("BEST", service.classify(10));
    }

    @Test
    void shouldClassifyExcellentMoves() {
        assertEquals("EXCELLENT", service.classify(11));
        assertEquals("EXCELLENT", service.classify(25));
    }

    @Test
    void shouldClassifyGoodMoves() {
        assertEquals("GOOD", service.classify(26));
        assertEquals("GOOD", service.classify(50));
    }

    @Test
    void shouldClassifyInaccuracies() {
        assertEquals("INACCURACY", service.classify(51));
        assertEquals("INACCURACY", service.classify(100));
    }

    @Test
    void shouldClassifyMistakes() {
        assertEquals("MISTAKE", service.classify(101));
        assertEquals("MISTAKE", service.classify(200));
    }

    @Test
    void shouldClassifyBlunders() {
        assertEquals("BLUNDER", service.classify(201));
        assertEquals("BLUNDER", service.classify(500));
    }
}

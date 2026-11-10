package com.chessinsight.chessinsight.chess;

import com.chessinsight.chessinsight.game.TimeControlType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeControlClassifierTest {
    private static TimeControlClassifier classifier;

    @BeforeAll
    static void setUp() {
        classifier = new TimeControlClassifier();
    }

    @Test
    void shouldClassifyBullet() {
        assertEquals(TimeControlType.BULLET,
            classifier.classify("60+0"));
    }

    @Test
    void shouldClassifyBlitz() {
        assertEquals(TimeControlType.BLITZ,
            classifier.classify("180+2"));
    }

    @Test
    void shouldClassifyRapid() {
        assertEquals(TimeControlType.RAPID,
            classifier.classify("600+0"));
    }

    @Test
    void shouldClassifyBaseTimeWithoutIncrement() {
        assertEquals(TimeControlType.BLITZ,
            classifier.classify("300"));
    }

    @Test
    void shouldReturnUnknownForMissingOrMalformedValues() {
        assertEquals(TimeControlType.UNKNOWN, classifier.classify(null));
        assertEquals(TimeControlType.UNKNOWN, classifier.classify(""));
        assertEquals(TimeControlType.UNKNOWN, classifier.classify("?"));
        assertEquals(TimeControlType.UNKNOWN, classifier.classify("invalid"));
        assertEquals(TimeControlType.UNKNOWN, classifier.classify("-60+0"));
    }
}

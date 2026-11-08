package com.chessinsight.chessinsight.chess;

import com.chessinsight.chessinsight.game.TimeControlType;
import org.springframework.stereotype.Component;

@Component
public class TimeControlClassifier {

    public TimeControlType classify(String value) {
        if (value == null || value.isBlank() || value.equals("?")) {
            return TimeControlType.UNKNOWN;
        }

        try {
            String[] parts = value.split("\\+");

            if (parts.length > 2 || parts[0].isBlank()) {
                return TimeControlType.UNKNOWN;
            }

            double initialSeconds = Double.parseDouble(parts[0]);
            double incrementSeconds = parts.length == 2
                ? Double.parseDouble(parts[1])
                : 0;

            if (initialSeconds < 0 || incrementSeconds < 0) {
                return TimeControlType.UNKNOWN;
            }

            double estimatedSeconds =
                initialSeconds + 40 * incrementSeconds;

            if (estimatedSeconds < 180) {
                return TimeControlType.BULLET;
            }

            if (estimatedSeconds < 600) {
                return TimeControlType.BLITZ;
            }

            return TimeControlType.RAPID;

        } catch (NumberFormatException e) {
            return TimeControlType.UNKNOWN;
        }
    }
}

package com.chessinsight.chessinsight.analysis;

import com.chessinsight.chessinsight.analysis.records.MoveAnalysis;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameAccuracyService {

    private static final double ACCURACY_FACTOR = 0.01;

    public double calculateAccuracy(
        List<MoveAnalysis> moves,
        String playerColor
    ) {
        if (moves == null || moves.isEmpty()) {
            return 0.0;
        }

        double totalAccuracy = 0.0;
        int analyzedMoves = 0;

        for (MoveAnalysis move : moves) {
            if (!playerColor.equalsIgnoreCase(move.mover())) {
                continue;
            }

            Integer centipawnLoss = move.centipawnLoss();

            if (centipawnLoss == null) {
                continue;
            }

            int loss = Math.max(0, centipawnLoss);

            double moveAccuracy = 100.0 * Math.exp(
                -ACCURACY_FACTOR * loss
            );

            totalAccuracy += moveAccuracy;
            analyzedMoves++;
        }

        if (analyzedMoves == 0) {
            return 0.0;
        }

        return Math.round(
            totalAccuracy / analyzedMoves * 100.0
        ) / 100.0;
    }
}
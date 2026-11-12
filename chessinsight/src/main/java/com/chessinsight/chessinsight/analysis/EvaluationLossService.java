package com.chessinsight.chessinsight.analysis;

import com.chessinsight.chessinsight.analysis.records.AnalysisResult;
import com.chessinsight.chessinsight.analysis.records.Candidate;
import org.springframework.stereotype.Service;

@Service
public class EvaluationLossService {

    private static final int MATE_SCORE = 100_000;

    public int calculateCentipawnLoss(
        AnalysisResult before,
        AnalysisResult after
    ) {
        String activeColor = getActiveColor(before);

        int beforeScore = getWhitePerspectiveScore(before);
        int afterScore = getWhitePerspectiveScore(after);

        int loss = activeColor.equals("w")
            ? beforeScore - afterScore
            : afterScore - beforeScore;

        return Math.max(0, loss);
    }

    private String getActiveColor(AnalysisResult result) {
        if (result == null || result.fen() == null) {
            throw new IllegalArgumentException(
                "The analysis result and FEN are required."
            );
        }

        String[] fenParts = result.fen().trim().split("\\s+");

        if (fenParts.length < 2
            || (!fenParts[1].equals("w")
            && !fenParts[1].equals("b"))) {
            throw new IllegalArgumentException(
                "The FEN has an invalid active color."
            );
        }

        return fenParts[1];
    }

    private int getWhitePerspectiveScore(AnalysisResult result) {
        if (result == null
            || result.candidates() == null
            || result.candidates().isEmpty()) {
            throw new IllegalArgumentException(
                "Analysis candidates are missing."
            );
        }

        Candidate best = result.candidates().stream()
            .filter(candidate -> candidate != null
                && candidate.rank() == 1)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "The best candidate (rank 1) is missing."
            ));

        if (best.mateIn() != null) {
            int mate = best.mateIn();

            int whiteSign;

            if (mate == 0) {

                whiteSign = getActiveColor(result).equals("w")
                    ? -1
                    : 1;
            } else {

                whiteSign = Integer.signum(mate);
            }

            long distance = Math.min(
                Math.abs((long) mate),
                MATE_SCORE - 1L
            );

            int score = MATE_SCORE - (int) distance;

            return whiteSign * score;
        }

        if (best.centipawns() == null) {
            throw new IllegalArgumentException(
                "The best candidate has no evaluation score."
            );
        }


        return best.centipawns();
    }
}
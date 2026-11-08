package com.chessinsight.chessinsight.analysis;

import org.springframework.stereotype.Service;

@Service
public class MoveClassificationService {

    public String classify(int centipawnLoss) {
        if (centipawnLoss <= 10) {
            return "BEST";
        }

        if (centipawnLoss <= 25) {
            return "EXCELLENT";
        }

        if (centipawnLoss <= 50) {
            return "GOOD";
        }

        if (centipawnLoss <= 100) {
            return "INACCURACY";
        }

        if (centipawnLoss <= 200) {
            return "MISTAKE";
        }

        return "BLUNDER";
    }
}
package com.chessinsight.chessinsight.chess.pgn;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class PgnHashUtil {
    private PgnHashUtil() {

    }

    public static String sha256(String pgn) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(
                pgn.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                "SHA-256 is not available", e
            );
        }
    }
}


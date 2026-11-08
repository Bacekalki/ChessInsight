package com.chessinsight.chessinsight.chess.lichess;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LichessApiClient {

    private final RestClient restClient;

    public LichessApiClient(RestClient.Builder builder) {
        this.restClient = builder
            .baseUrl("https://lichess.org")
            .defaultHeader("Accept", "application/x-chess-pgn")
            .defaultHeader("User-Agent", "ChessInsight/1.0")
            .build();
    }

    public String downloadPgn(String username) {
        return restClient.get()
            .uri("/api/games/user/{username}", username)
            .retrieve()
            .body(String.class);
    }
}

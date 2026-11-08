package com.chessinsight.chessinsight.chess.chesscom;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ChessComApiClient {

    private final RestClient restClient;

    public ChessComApiClient(RestClient.Builder builder) {
        this.restClient = builder
            .defaultHeader("User-Agent", "ChessInsight/1.0")
            .build();
    }

    public ChessComArchivesResponse getArchives(String username) {
        return restClient.get()
            .uri(
                "https://api.chess.com/pub/player/{username}/games/archives",
                username
            )
            .retrieve()
            .body(ChessComArchivesResponse.class);
    }


    public String downloadPgn(String archiveUrl) {
        return restClient.get()
            .uri(archiveUrl + "/pgn")
            .retrieve()
            .body(String.class);
    }
}

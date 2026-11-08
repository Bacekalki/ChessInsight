package com.chessinsight.chessinsight.chess;

import com.chessinsight.chessinsight.chess.chesscom.ChessComApiClient;
import com.chessinsight.chessinsight.chess.chesscom.ChessComArchivesResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ChessComApiClientTest {

    @Test
    void shouldFetchAndParsePlayerArchives() {
        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server =
            MockRestServiceServer.bindTo(builder).build();

        ChessComApiClient client = new ChessComApiClient(builder);

        server.expect(requestTo(
                "https://api.chess.com/pub/player/bacekalki/games/archives"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("User-Agent", "ChessInsight/1.0"))
            .andRespond(withSuccess(
                """
                {
                  "archives": [
                    "https://api.chess.com/pub/player/bacekalki/games/2026/01",
                    "https://api.chess.com/pub/player/bacekalki/games/2026/02"
                  ]
                }
                """,
                MediaType.APPLICATION_JSON
            ));

        ChessComArchivesResponse response =
            client.getArchives("bacekalki");

        assertEquals(2, response.archives().size());
        assertEquals(
            "https://api.chess.com/pub/player/bacekalki/games/2026/01",
            response.archives().get(0)
        );
        assertEquals(
            "https://api.chess.com/pub/player/bacekalki/games/2026/02",
            response.archives().get(1)
        );

        server.verify();
    }

    @Test
    void shouldDownloadPgnFromArchive() {
        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server =
            MockRestServiceServer.bindTo(builder).build();

        ChessComApiClient client = new ChessComApiClient(builder);

        String archiveUrl =
            "https://api.chess.com/pub/player/bacekalki/games/2026/01";

        String pgn = """
            [Event "Live Chess"]
            [Site "Chess.com"]
            [Date "2026.01.10"]
            [White "bacekalki"]
            [Black "opponent"]
            [Result "1-0"]
            [TimeControl "600"]

            1. e4 e5 2. Nf3 Nc6 1-0
            """;

        server.expect(requestTo(archiveUrl + "/pgn"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("User-Agent", "ChessInsight/1.0"))
            .andRespond(withSuccess(pgn, MediaType.TEXT_PLAIN));

        String response = client.downloadPgn(archiveUrl);

        assertEquals(pgn, response);

        server.verify();
    }
}

package com.tic.app.gamesessionserviceapp.client;

import com.tic.app.gamesessionserviceapp.client.dto.GameEngineCreateResponse;
import com.tic.app.gamesessionserviceapp.client.dto.GameEngineGameResponse;
import com.tic.app.gamesessionserviceapp.client.dto.GameEngineMoveRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class GameEngineClient {

    private static final String BASE_URL = "http://game-engine-service-app";

    private final RestClient restClient;

    public GameEngineClient(@Qualifier("loadBalancedRestClientBuilder") RestClient.Builder builder) {
        this.restClient = builder.baseUrl(BASE_URL).build();
    }

    @Retryable(
            retryFor = {IllegalStateException.class, ResourceAccessException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 2000)
    )
    public GameEngineCreateResponse createGame() {
        return restClient
                .post()
                .uri("/games")
                .retrieve()
                .body(GameEngineCreateResponse.class);
    }

    @Retryable(
            retryFor = {IllegalStateException.class, ResourceAccessException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 2000)
    )
    public GameEngineGameResponse makeMove(String gameId, int position) {
        return restClient
                .post()
                .uri("/games/{gameId}/move", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new GameEngineMoveRequest(position))
                .retrieve()
                .body(GameEngineGameResponse.class);
    }

    @Retryable(
            retryFor = {IllegalStateException.class, ResourceAccessException.class},
            maxAttempts = 5,
            backoff = @Backoff(delay = 2000)
    )
    public GameEngineGameResponse getGame(String gameId) {
        return restClient
                .get()
                .uri("/games/{gameId}", gameId)
                .retrieve()
                .body(GameEngineGameResponse.class);
    }
}

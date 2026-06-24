package com.tic.app.gamesessionserviceapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tic.app.gamesessionserviceapp.client.GameEngineClient;
import com.tic.app.gamesessionserviceapp.client.dto.GameEngineCreateResponse;
import com.tic.app.gamesessionserviceapp.client.dto.GameEngineGameResponse;
import com.tic.app.gamesessionserviceapp.domain.GameStatus;
import com.tic.app.gamesessionserviceapp.domain.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.config.import=optional:",
                "eureka.client.enabled=false"
        }
)
class SessionControllerIT {

    @Autowired WebApplicationContext context;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean GameEngineClient gameEngineClient;

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void createSession_returns201WithSessionIdAndGameId() throws Exception {
        String gameId = UUID.randomUUID().toString();
        when(gameEngineClient.createGame()).thenReturn(new GameEngineCreateResponse(gameId, "ONGOING"));

        mockMvc.perform(post("/sessions"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").exists())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void fullGameFlow_simulatesUntilXWonAndPersistsMoveHistory() throws Exception {
        String gameId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        when(gameEngineClient.createGame()).thenReturn(new GameEngineCreateResponse(gameId, "ONGOING"));

        // Create session
        MvcResult createResult = mockMvc.perform(post("/sessions"))
                .andExpect(status().isCreated())
                .andReturn();
        String sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("sessionId").asText();

        // Initial board returned by getGame() at the start of simulate()
        when(gameEngineClient.getGame(gameId))
                .thenReturn(gameState(gameId, "         ", Player.X, GameStatus.ONGOING, now));

        // Five scripted move responses — the service picks positions randomly, mock ignores them
        GameEngineGameResponse m5 = gameState(gameId, "XXXOO    ", Player.X, GameStatus.X_WON, now);
        when(gameEngineClient.makeMove(eq(gameId), anyInt())).thenReturn(
                gameState(gameId, "X        ", Player.O, GameStatus.ONGOING, now),
                gameState(gameId, "X  O     ", Player.X, GameStatus.ONGOING, now),
                gameState(gameId, "XX O     ", Player.O, GameStatus.ONGOING, now),
                gameState(gameId, "XX OO    ", Player.X, GameStatus.ONGOING, now),
                m5
        );

        // POST /simulate — SseEmitter runs async; assert async started, then dispatch to collect body
        MvcResult asyncResult = mockMvc.perform(post("/sessions/{id}/simulate", sessionId))
                .andExpect(request().asyncStarted())
                .andReturn();

        String sseBody = mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        assertThat(sseBody).contains("event:move");
        assertThat(sseBody).contains("event:done");
        assertThat(sseBody.split("event:move", -1).length - 1).isEqualTo(5);

        // Verify the session was committed to COMPLETED with move history
        when(gameEngineClient.getGame(gameId)).thenReturn(m5);

        mockMvc.perform(get("/sessions/{id}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.gameStatus").value("X_WON"))
                .andExpect(jsonPath("$.moveHistory.length()").value(5));
    }

    @Test
    void getSession_returns404ForUnknownSessionId() throws Exception {
        mockMvc.perform(get("/sessions/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    private static GameEngineGameResponse gameState(
            String gameId, String board, Player player, GameStatus status, LocalDateTime createdAt) {
        return new GameEngineGameResponse(gameId, board, player, status, createdAt);
    }
}

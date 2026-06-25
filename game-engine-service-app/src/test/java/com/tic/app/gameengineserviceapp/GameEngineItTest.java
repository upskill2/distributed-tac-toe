package com.tic.app.gameengineserviceapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.config.import=optional:",
                "eureka.client.enabled=false"
        }
)
class GameEngineItTest {

    @Autowired WebApplicationContext context;
    @Autowired ObjectMapper objectMapper;

    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void createGame_returns201WithGameIdAndOngoingStatus() throws Exception {
        mockMvc.perform(post("/games"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").exists())
                .andExpect(jsonPath("$.status").value("ONGOING"));
    }

    @Test
    void makeMove_returns200WithUpdatedBoardAndNextPlayer() throws Exception {
        String gameId = createGameId();

        mockMvc.perform(post("/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"position\": 0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ONGOING"))
                .andExpect(jsonPath("$.currentPlayer").value("O"));
    }

    @Test
    void makeMove_returns400WhenCellAlreadyTaken() throws Exception {
        String gameId = createGameId();
        move(gameId, 4);

        mockMvc.perform(post("/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"position\": 4}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("already taken")));
    }

    @Test
    void makeMove_returns400WhenPositionOutOfRange() throws Exception {
        String gameId = createGameId();

        mockMvc.perform(post("/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"position\": 10}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void makeMove_returns400WhenGameIsAlreadyOver() throws Exception {
        String gameId = createGameId();
        // X wins top row
        for (int pos : new int[]{0, 3, 1, 4, 2}) {
            move(gameId, pos);
        }

        mockMvc.perform(post("/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"position\": 5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(containsString("already over")));
    }

    @Test
    void getGame_returns200WithCorrectState() throws Exception {
        String gameId = createGameId();

        mockMvc.perform(get("/games/{gameId}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.board").value("         "))
                .andExpect(jsonPath("$.status").value("ONGOING"));
    }

    @Test
    void getGame_returns404ForUnknownGameId() throws Exception {
        mockMvc.perform(get("/games/{gameId}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void fullGameFlow_xWinsTopRow() throws Exception {
        String gameId = createGameId();

        // X at 0, O at 3, X at 1, O at 4, then X at 2 wins [0,1,2]
        for (int pos : new int[]{0, 3, 1, 4}) {
            move(gameId, pos);
        }

        mockMvc.perform(post("/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"position\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("X_WON"))
                .andExpect(jsonPath("$.board").value("XXXOO    "));
    }

    @Test
    void fullGameFlow_oWinsLeftColumn() throws Exception {
        String gameId = createGameId();

        // X at 4, O at 0, X at 5, O at 3, X at 8, then O at 6 wins [0,3,6]
        for (int pos : new int[]{4, 0, 5, 3, 8}) {
            move(gameId, pos);
        }

        mockMvc.perform(post("/games/{gameId}/move", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"position\": 6}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("O_WON"));
    }

    private String createGameId() throws Exception {
        MvcResult result = mockMvc.perform(post("/games"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("gameId").asText();
    }

    private void move(String gameId, int position) throws Exception {
        mockMvc.perform(post("/games/{gameId}/move", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"position\": " + position + "}"))
                .andExpect(status().isOk());
    }
}

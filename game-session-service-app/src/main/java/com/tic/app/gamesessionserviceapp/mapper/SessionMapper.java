package com.tic.app.gamesessionserviceapp.mapper;

import com.tic.app.gamesessionserviceapp.client.dto.GameEngineGameResponse;
import com.tic.app.gamesessionserviceapp.domain.GameStatus;
import com.tic.app.gamesessionserviceapp.domain.Session;
import com.tic.app.gamesessionserviceapp.dto.CreateSessionResponse;
import com.tic.app.gamesessionserviceapp.dto.SessionResponse;
import com.tic.app.gamesessionserviceapp.dto.SimulateResponse;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SessionMapper {

    public CreateSessionResponse toCreateSessionResponse(Session session) {
        return CreateSessionResponse.builder()
                .sessionId(session.getId())
                .gameId(session.getGameId())
                .status(session.getStatus())
                .build();
    }

    public SimulateResponse toSimulateResponse(Session session, GameStatus finalStatus, List<String> moveHistory) {
        return SimulateResponse.builder()
                .sessionId(session.getId())
                .gameId(session.getGameId())
                .finalGameStatus(finalStatus)
                .moveHistory(moveHistory)
                .build();
    }

    public SessionResponse toSessionResponse(Session session, GameEngineGameResponse gameState) {
        return SessionResponse.builder()
                .sessionId(session.getId())
                .gameId(session.getGameId())
                .sessionStatus(session.getStatus())
                .gameStatus(gameState.status())
                .moveHistory(session.getMoveHistory())
                .createdAt(session.getCreatedAt())
                .build();
    }
}

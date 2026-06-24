package com.tic.app.gamesessionserviceapp.mapper;

import com.tic.app.gamesessionserviceapp.client.dto.GameEngineGameResponse;
import com.tic.app.gamesessionserviceapp.domain.Session;
import com.tic.app.gamesessionserviceapp.dto.CreateSessionResponse;
import com.tic.app.gamesessionserviceapp.dto.SessionResponse;

import org.springframework.stereotype.Component;

@Component
public class SessionMapper {

    public CreateSessionResponse toCreateSessionResponse(Session session) {
        return CreateSessionResponse.builder()
                .sessionId(session.getId())
                .gameId(session.getGameId())
                .status(session.getStatus())
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

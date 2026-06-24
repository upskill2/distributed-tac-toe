package com.tic.app.gamesessionserviceapp.service;

import com.tic.app.gamesessionserviceapp.client.GameEngineClient;
import com.tic.app.gamesessionserviceapp.client.dto.GameEngineCreateResponse;
import com.tic.app.gamesessionserviceapp.client.dto.GameEngineGameResponse;
import com.tic.app.gamesessionserviceapp.domain.GameStatus;
import com.tic.app.gamesessionserviceapp.domain.Player;
import com.tic.app.gamesessionserviceapp.domain.Session;
import com.tic.app.gamesessionserviceapp.domain.SessionStatus;
import com.tic.app.gamesessionserviceapp.dto.CreateSessionResponse;
import com.tic.app.gamesessionserviceapp.dto.SessionResponse;
import com.tic.app.gamesessionserviceapp.mapper.SessionMapper;
import com.tic.app.gamesessionserviceapp.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {

    @Mock private SessionRepository sessionRepository;
    @Mock private GameEngineClient gameEngineClient;
    @Mock private SessionMapper sessionMapper;
    @InjectMocks private SessionService sessionService;

    @Test
    void createSession_callsEngineAndPersistsSessionAndReturnsResponse() {
        String gameId = UUID.randomUUID().toString();
        UUID sessionId = UUID.randomUUID();
        Session savedSession = session(sessionId, gameId, SessionStatus.CREATED);
        CreateSessionResponse expected = CreateSessionResponse.builder()
                .sessionId(sessionId).gameId(gameId).status(SessionStatus.CREATED).build();

        when(gameEngineClient.createGame()).thenReturn(new GameEngineCreateResponse(gameId, "ONGOING"));
        when(sessionRepository.save(any(Session.class))).thenReturn(savedSession);
        when(sessionMapper.toCreateSessionResponse(savedSession)).thenReturn(expected);

        CreateSessionResponse result = sessionService.createSession();

        assertThat(result).isEqualTo(expected);
        verify(gameEngineClient).createGame();
        verify(sessionRepository).save(argThat(s ->
                s.getGameId().equals(gameId) && s.getStatus() == SessionStatus.CREATED
        ));
    }

    @Test
    void getSession_returnsSessionResponseWithGameState() {
        String gameId = UUID.randomUUID().toString();
        UUID sessionId = UUID.randomUUID();
        Session existing = session(sessionId, gameId, SessionStatus.COMPLETED);
        GameEngineGameResponse gameState = new GameEngineGameResponse(
                gameId, "XXXOO    ", Player.X, GameStatus.X_WON, LocalDateTime.now());
        SessionResponse expected = SessionResponse.builder()
                .sessionId(sessionId).gameId(gameId)
                .sessionStatus(SessionStatus.COMPLETED).gameStatus(GameStatus.X_WON)
                .moveHistory(List.of("X:0", "O:3", "X:1", "O:4", "X:2"))
                .createdAt(existing.getCreatedAt()).build();

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(existing));
        when(gameEngineClient.getGame(gameId)).thenReturn(gameState);
        when(sessionMapper.toSessionResponse(existing, gameState)).thenReturn(expected);

        SessionResponse result = sessionService.getSession(sessionId.toString());

        assertThat(result).isEqualTo(expected);
        verify(gameEngineClient).getGame(gameId);
    }

    @Test
    void getSession_throwsWhenSessionNotFound() {
        UUID sessionId = UUID.randomUUID();
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sessionService.getSession(sessionId.toString()))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining(sessionId.toString());
    }

    private static Session session(UUID id, String gameId, SessionStatus status) {
        return Session.builder().id(id).gameId(gameId).status(status).build();
    }
}

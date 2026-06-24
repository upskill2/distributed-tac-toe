package com.tic.app.gamesessionserviceapp.service;

import com.tic.app.gamesessionserviceapp.client.GameEngineClient;
import com.tic.app.gamesessionserviceapp.client.dto.GameEngineGameResponse;
import com.tic.app.gamesessionserviceapp.domain.GameStatus;
import com.tic.app.gamesessionserviceapp.domain.Player;
import com.tic.app.gamesessionserviceapp.domain.Session;
import com.tic.app.gamesessionserviceapp.domain.SessionStatus;
import com.tic.app.gamesessionserviceapp.dto.CreateSessionResponse;
import com.tic.app.gamesessionserviceapp.dto.SessionResponse;
import com.tic.app.gamesessionserviceapp.exception.SessionException;
import com.tic.app.gamesessionserviceapp.mapper.SessionMapper;
import com.tic.app.gamesessionserviceapp.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final GameEngineClient gameEngineClient;
    private final SessionMapper sessionMapper;

    private final Executor executor = Executors.newVirtualThreadPerTaskExecutor();

    public CreateSessionResponse createSession() {
        log.info("Creating new session - calling game engine");
        var engineResponse = gameEngineClient.createGame();
        log.info("Game engine responded: gameId={}", engineResponse.gameId());

        Session session = Session.builder()
                .gameId(engineResponse.gameId())
                .status(SessionStatus.CREATED)
                .build();
        session = sessionRepository.save(session);

        log.info("Session created: id={} gameId={}", session.getId(), session.getGameId());
        return sessionMapper.toCreateSessionResponse(session);
    }

    @Transactional
    public SseEmitter simulate(String sessionId) {
        Session session = sessionRepository.findById(UUID.fromString(sessionId))
                .orElseThrow(() -> new NoSuchElementException(String.format("Session not found: %s", sessionId)));

        if (session.getStatus() != SessionStatus.CREATED) {
            throw new SessionException(String.format("Session already %s: %s", session.getStatus(), sessionId));
        }

        session.setStatus(SessionStatus.SIMULATING);
        sessionRepository.saveAndFlush(session);

        String gameId = session.getGameId();
        SseEmitter emitter = new SseEmitter(60_000L);

        executor.execute(() -> {
            try {
                log.info("Starting simulation for sessionId={}", sessionId);
                GameEngineGameResponse gameState = gameEngineClient.getGame(gameId);
                List<String> moveHistory = new ArrayList<>();
                Random random = new Random();
                Player currentPlayer = Player.X;

                while (gameState.status() == GameStatus.ONGOING) {
                    List<Integer> available = getAvailablePositions(gameState.board());
                    if (available.isEmpty()) break;

                    int position = available.get(random.nextInt(available.size()));
                    log.debug("Player {} picks position {}", currentPlayer, position);
                    gameState = gameEngineClient.makeMove(gameId, position);
                    moveHistory.add(String.format("%s:%d", currentPlayer.name(), position));
                    currentPlayer = currentPlayer.next();

                    emitter.send(SseEmitter.event().name("move").data(gameState, MediaType.APPLICATION_JSON));
                }
                
                Session completed = sessionRepository.findById(UUID.fromString(sessionId))
                        .orElseThrow(() -> new NoSuchElementException(String.format("Session not found: %s", sessionId)));
                completed.getMoveHistory().addAll(moveHistory);
                completed.setStatus(SessionStatus.COMPLETED);
                sessionRepository.save(completed);

                log.info("Simulation done: sessionId={} gameId={} result={} totalMoves={}",
                        sessionId, gameId, gameState.status(), moveHistory.size());

                emitter.send(SseEmitter.event().name("done").data(""));
                emitter.complete();

            } catch (IOException e) {
                log.warn("SSE client disconnected during simulation for sessionId={}", sessionId);
            } catch (Exception e) {
                log.error("Simulation failed for sessionId={}", sessionId, e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    public SessionResponse getSession(String sessionId) {
        Session session = sessionRepository.findById(UUID.fromString(sessionId))
                .orElseThrow(() -> new NoSuchElementException(String.format("Session not found: %s", sessionId)));

        GameEngineGameResponse gameState = gameEngineClient.getGame(session.getGameId());
        return sessionMapper.toSessionResponse(session, gameState);
    }

    private List<Integer> getAvailablePositions(String board) {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < board.length(); i++) {
            if (board.charAt(i) == ' ') positions.add(i);
        }
        return positions;
    }
}

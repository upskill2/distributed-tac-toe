package com.tic.app.gamesessionserviceapp.service;

import com.tic.app.gamesessionserviceapp.client.GameEngineClient;
import com.tic.app.gamesessionserviceapp.client.dto.GameEngineCreateResponse;
import com.tic.app.gamesessionserviceapp.client.dto.GameEngineGameResponse;
import com.tic.app.gamesessionserviceapp.domain.Session;
import com.tic.app.gamesessionserviceapp.domain.SessionStatus;
import com.tic.app.gamesessionserviceapp.dto.CreateSessionResponse;
import com.tic.app.gamesessionserviceapp.dto.SessionResponse;
import com.tic.app.gamesessionserviceapp.dto.SimulateResponse;
import com.tic.app.gamesessionserviceapp.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final GameEngineClient gameEngineClient;

    public CreateSessionResponse createSession() {
        log.info("Creating new session - calling game engine");
        GameEngineCreateResponse engineResponse = gameEngineClient.createGame();
        log.info("Game engine responded: gameId={}", engineResponse.gameId());

        Session session = new Session();
        session.setGameId(engineResponse.gameId());
        session.setStatus(SessionStatus.CREATED);
        session = sessionRepository.save(session);

        log.info("Session created: id={} gameId={}", session.getId(), session.getGameId());
        return new CreateSessionResponse(session.getId(), session.getGameId(), session.getStatus().name());
    }

    public SimulateResponse simulate(String sessionId) {
        log.info("Starting simulation for sessionId={}", sessionId);
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Session not found: " + sessionId));

        session.setStatus(SessionStatus.SIMULATING);
        session = sessionRepository.save(session);

        String gameId = session.getGameId();
        GameEngineGameResponse gameState = gameEngineClient.getGame(gameId);
        List<String> moveHistory = new ArrayList<>();
        Random random = new Random();
        char currentPlayer = 'X';

        while ("ONGOING".equals(gameState.status())) {
            List<Integer> available = getAvailablePositions(gameState.board());
            if (available.isEmpty()) break;

            int position = available.get(random.nextInt(available.size()));
            log.debug("Player {} picks position {}", currentPlayer, position);
            gameState = gameEngineClient.makeMove(gameId, position);
            moveHistory.add(currentPlayer + ":" + position);
            currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
        }

        session.getMoveHistory().addAll(moveHistory);
        session.setStatus(SessionStatus.COMPLETED);
        sessionRepository.save(session);

        log.info("Simulation done: sessionId={} gameId={} result={} totalMoves={}", sessionId, gameId, gameState.status(), moveHistory.size());
        return new SimulateResponse(sessionId, gameId, gameState.status(), moveHistory);
    }

    public SessionResponse getSession(String sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("Session not found: " + sessionId));

        GameEngineGameResponse gameState = gameEngineClient.getGame(session.getGameId());

        return new SessionResponse(
                session.getId(),
                session.getGameId(),
                session.getStatus().name(),
                gameState.status(),
                session.getMoveHistory(),
                session.getCreatedAt()
        );
    }

    private List<Integer> getAvailablePositions(String board) {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < board.length(); i++) {
            if (board.charAt(i) == ' ') positions.add(i);
        }
        return positions;
    }
}

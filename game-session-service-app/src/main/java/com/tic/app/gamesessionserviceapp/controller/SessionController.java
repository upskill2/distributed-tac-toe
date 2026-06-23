package com.tic.app.gamesessionserviceapp.controller;

import com.tic.app.gamesessionserviceapp.dto.CreateSessionResponse;
import com.tic.app.gamesessionserviceapp.dto.SessionResponse;
import com.tic.app.gamesessionserviceapp.dto.SimulateResponse;
import com.tic.app.gamesessionserviceapp.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
@Validated
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateSessionResponse createSession() {
        log.info("POST /sessions - creating new session");
        CreateSessionResponse response = sessionService.createSession();
        log.info("Session created: id={} gameId={}", response.sessionId(), response.gameId());
        return response;
    }

    @PostMapping("/{sessionId}/simulate")
    public SimulateResponse simulate(@PathVariable String sessionId) {
        log.info("POST /sessions/{}/simulate - starting simulation", sessionId);
        SimulateResponse response = sessionService.simulate(sessionId);
        log.info("Simulation complete: sessionId={} result={} moves={}", sessionId, response.finalGameStatus(), response.moveHistory().size());
        return response;
    }

    @GetMapping("/{sessionId}")
    public SessionResponse getSession(@PathVariable String sessionId) {
        log.info("GET /sessions/{}", sessionId);
        return sessionService.getSession(sessionId);
    }
}

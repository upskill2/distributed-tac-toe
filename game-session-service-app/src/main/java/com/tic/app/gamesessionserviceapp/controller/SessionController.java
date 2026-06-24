package com.tic.app.gamesessionserviceapp.controller;

import com.tic.app.gamesessionserviceapp.dto.CreateSessionResponse;
import com.tic.app.gamesessionserviceapp.dto.SessionResponse;
import com.tic.app.gamesessionserviceapp.service.SessionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
        return sessionService.createSession();
    }

    @PostMapping("/{sessionId}/simulate")
    public SseEmitter simulate(@PathVariable @NonNull String sessionId) {
        return sessionService.simulate(sessionId);
    }

    @GetMapping("/{sessionId}")
    public SessionResponse getSession(@PathVariable @NonNull String sessionId) {
        return sessionService.getSession(sessionId);
    }
}

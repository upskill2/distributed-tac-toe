package com.tic.app.gameengineserviceapp.controller;

import com.tic.app.gameengineserviceapp.dto.CreateGameResponse;
import com.tic.app.gameengineserviceapp.dto.GameResponse;
import com.tic.app.gameengineserviceapp.dto.MoveRequest;
import com.tic.app.gameengineserviceapp.service.GameService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
@Validated
public class GameController {

    private final GameService gameService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateGameResponse createGame() {
        return gameService.createGame();
    }

    @PostMapping("/{gameId}/move")
    public GameResponse makeMove(
            @PathVariable @NonNull String gameId,
            @RequestBody @Valid MoveRequest request
    ) {
        return gameService.makeMove(gameId, request.position());
    }

    @GetMapping("/{gameId}")
    public GameResponse getGame(@PathVariable @NonNull String gameId) {
        return gameService.getGame(gameId);
    }
}

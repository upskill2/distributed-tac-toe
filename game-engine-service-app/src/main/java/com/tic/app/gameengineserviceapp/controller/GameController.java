package com.tic.app.gameengineserviceapp.controller;

import com.tic.app.gameengineserviceapp.dto.CreateGameResponse;
import com.tic.app.gameengineserviceapp.dto.GameResponse;
import com.tic.app.gameengineserviceapp.dto.MoveRequest;
import com.tic.app.gameengineserviceapp.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        log.info("POST /games - creating new game");
        CreateGameResponse response = gameService.createGame();
        log.info("Game created: id={}", response.gameId());
        return response;
    }

    @PostMapping("/{gameId}/move")
    public GameResponse makeMove(
            @PathVariable String gameId,
            @RequestBody @Valid MoveRequest request
    ) {
        log.info("POST /games/{}/move - position={}", gameId, request.position());
        GameResponse response = gameService.makeMove(gameId, request.position());
        log.info("Move applied: gameId={} status={} board=[{}]", gameId, response.status(), response.board());
        return response;
    }

    @GetMapping("/{gameId}")
    public GameResponse getGame(@PathVariable String gameId) {
        log.info("GET /games/{}", gameId);
        return gameService.getGame(gameId);
    }
}

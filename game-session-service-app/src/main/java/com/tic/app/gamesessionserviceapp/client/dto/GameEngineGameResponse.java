package com.tic.app.gamesessionserviceapp.client.dto;

import com.tic.app.gamesessionserviceapp.domain.GameStatus;
import com.tic.app.gamesessionserviceapp.domain.Player;

import java.time.LocalDateTime;

public record GameEngineGameResponse(
        String gameId,
        String board,
        Player currentPlayer,
        GameStatus status,
        LocalDateTime createdAt
) {
}

package com.tic.app.gamesessionserviceapp.client.dto;

import java.time.LocalDateTime;

public record GameEngineGameResponse(
        String gameId,
        String board,
        char currentPlayer,
        String status,
        LocalDateTime createdAt
) {
}

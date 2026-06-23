package com.tic.app.gameengineserviceapp.dto;

import java.time.LocalDateTime;

public record GameResponse(
        String gameId,
        String board,
        char currentPlayer,
        String status,
        LocalDateTime createdAt
) {
}

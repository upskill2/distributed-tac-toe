package com.tic.app.gameengineserviceapp.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.tic.app.gameengineserviceapp.domain.GameStatus;
import com.tic.app.gameengineserviceapp.domain.Player;

import lombok.Builder;

@Builder
public record GameResponse(
        UUID gameId,
        String board,
        Player currentPlayer,
        GameStatus status,
        LocalDateTime createdAt
) {
}

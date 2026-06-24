package com.tic.app.gamesessionserviceapp.dto;

import com.tic.app.gamesessionserviceapp.domain.GameStatus;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record SimulateResponse(
        UUID sessionId,
        String gameId,
        GameStatus finalGameStatus,
        List<String> moveHistory
) {
}

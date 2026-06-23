package com.tic.app.gamesessionserviceapp.dto;

import java.util.List;

public record SimulateResponse(
        String sessionId,
        String gameId,
        String finalGameStatus,
        List<String> moveHistory
) {
}

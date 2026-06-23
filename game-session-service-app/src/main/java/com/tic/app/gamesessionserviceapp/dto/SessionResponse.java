package com.tic.app.gamesessionserviceapp.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SessionResponse(
        String sessionId,
        String gameId,
        String sessionStatus,
        String gameStatus,
        List<String> moveHistory,
        LocalDateTime createdAt
) {
}

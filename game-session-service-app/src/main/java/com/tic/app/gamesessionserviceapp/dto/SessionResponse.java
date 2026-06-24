package com.tic.app.gamesessionserviceapp.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.tic.app.gamesessionserviceapp.domain.GameStatus;
import com.tic.app.gamesessionserviceapp.domain.SessionStatus;

@Builder
public record SessionResponse(
        UUID sessionId,
        String gameId,
        SessionStatus sessionStatus,
        GameStatus gameStatus,
        List<String> moveHistory,
        LocalDateTime createdAt
) {
}

package com.tic.app.gamesessionserviceapp.dto;

import java.util.UUID;

import com.tic.app.gamesessionserviceapp.domain.SessionStatus;

import lombok.Builder;

@Builder
public record CreateSessionResponse(UUID sessionId, String gameId, SessionStatus status) {

}

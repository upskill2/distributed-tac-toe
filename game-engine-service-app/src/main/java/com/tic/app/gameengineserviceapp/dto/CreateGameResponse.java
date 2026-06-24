package com.tic.app.gameengineserviceapp.dto;

import java.util.UUID;

import com.tic.app.gameengineserviceapp.domain.GameStatus;

import lombok.Builder;

@Builder
public record CreateGameResponse(UUID gameId, GameStatus status) {
}

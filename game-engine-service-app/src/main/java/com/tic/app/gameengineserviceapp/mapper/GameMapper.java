package com.tic.app.gameengineserviceapp.mapper;

import com.tic.app.gameengineserviceapp.domain.Game;
import com.tic.app.gameengineserviceapp.dto.CreateGameResponse;
import com.tic.app.gameengineserviceapp.dto.GameResponse;
import org.springframework.stereotype.Component;

@Component
public class GameMapper {

    public CreateGameResponse toCreateGameResponse(Game game) {
        return CreateGameResponse.builder()
                .gameId(game.getId())
                .status(game.getStatus())
                .build();
    }

    public GameResponse toGameResponse(Game game) {
        return GameResponse.builder()
                .gameId(game.getId())
                .board(game.getBoard())
                .currentPlayer(game.getCurrentPlayer())
                .status(game.getStatus())
                .createdAt(game.getCreatedAt())
                .build();
    }
}

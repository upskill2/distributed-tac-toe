package com.tic.app.gameengineserviceapp.service;

import com.tic.app.gameengineserviceapp.domain.Game;
import com.tic.app.gameengineserviceapp.domain.GameStatus;
import com.tic.app.gameengineserviceapp.domain.Player;
import com.tic.app.gameengineserviceapp.domain.TicTacToeBoard;
import com.tic.app.gameengineserviceapp.dto.CreateGameResponse;
import com.tic.app.gameengineserviceapp.dto.GameResponse;
import com.tic.app.gameengineserviceapp.exception.GameException;
import com.tic.app.gameengineserviceapp.mapper.GameMapper;
import com.tic.app.gameengineserviceapp.repository.GameRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final GameMapper gameMapper;

    public CreateGameResponse createGame() {
        log.info("Creating new game");
        Game game = Game.builder()
                .board(TicTacToeBoard.EMPTY_BOARD)
                .currentPlayer(Player.X)
                .status(GameStatus.ONGOING)
                .build();
        game = gameRepository.save(game);
        log.info("Game created: id={}", game.getId());
        return gameMapper.toCreateGameResponse(game);
    }

    @Transactional
    public GameResponse makeMove(String gameId, int position) {
        log.info("Making move: gameId={} position={}", gameId, position);
        Game game = gameRepository.findById(UUID.fromString(gameId))
                .orElseThrow(() -> new NoSuchElementException(String.format("Game not found: %s", gameId)));

        if (!GameStatus.ONGOING.equals(game.getStatus())) {
            throw new GameException(String.format("Game is already over with status: %s", game.getStatus()));
        }
        if (!TicTacToeBoard.isCellEmpty(game.getBoard(), position)) {
            throw new GameException(String.format("Cell at position %d is already taken", position));
        }

        String updatedBoard = TicTacToeBoard.applyMove(game.getBoard(), position, game.getCurrentPlayer());
        GameStatus newStatus = TicTacToeBoard.evaluate(updatedBoard);

        game.setBoard(updatedBoard);
        game.setStatus(newStatus);
        if (newStatus == GameStatus.ONGOING) {
            game.setCurrentPlayer(game.getCurrentPlayer().next());
        }

        game = gameRepository.save(game);
        log.info("Move result: gameId={} status={} board=[{}]", gameId, newStatus, updatedBoard);
        return gameMapper.toGameResponse(game);
    }

    public GameResponse getGame(String gameId) {
        Game game = gameRepository.findById(UUID.fromString(gameId))
                .orElseThrow(() -> new NoSuchElementException(String.format("Game not found: %s", gameId)));
        return gameMapper.toGameResponse(game);
    }
}

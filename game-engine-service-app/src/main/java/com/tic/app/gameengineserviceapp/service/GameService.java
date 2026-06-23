package com.tic.app.gameengineserviceapp.service;

import com.tic.app.gameengineserviceapp.domain.Game;
import com.tic.app.gameengineserviceapp.domain.GameStatus;
import com.tic.app.gameengineserviceapp.domain.TicTacToeBoard;
import com.tic.app.gameengineserviceapp.dto.CreateGameResponse;
import com.tic.app.gameengineserviceapp.dto.GameResponse;
import com.tic.app.gameengineserviceapp.exception.GameException;
import com.tic.app.gameengineserviceapp.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;

    public CreateGameResponse createGame() {
        log.info("Creating new game");
        Game game = new Game();
        game.setBoard(TicTacToeBoard.EMPTY_BOARD);
        game.setCurrentPlayer('X');
        game.setStatus(GameStatus.ONGOING);
        game = gameRepository.save(game);
        log.info("Game created: id={}", game.getId());
        return new CreateGameResponse(game.getId(), game.getStatus().name());
    }

    public GameResponse makeMove(String gameId, int position) {
        log.info("Making move: gameId={} position={}", gameId, position);
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NoSuchElementException("Game not found: " + gameId));

        if (game.getStatus() != GameStatus.ONGOING) {
            throw new GameException("Game is already over with status: " + game.getStatus());
        }
        if (!TicTacToeBoard.isCellEmpty(game.getBoard(), position)) {
            throw new GameException("Cell at position " + position + " is already taken");
        }

        String updatedBoard = TicTacToeBoard.applyMove(game.getBoard(), position, game.getCurrentPlayer());
        GameStatus newStatus = TicTacToeBoard.evaluate(updatedBoard);

        game.setBoard(updatedBoard);
        game.setStatus(newStatus);
        if (newStatus == GameStatus.ONGOING) {
            game.setCurrentPlayer(game.getCurrentPlayer() == 'X' ? 'O' : 'X');
        }

        game = gameRepository.save(game);
        log.info("Move result: gameId={} status={} board=[{}]", gameId, newStatus, updatedBoard);
        return toResponse(game);
    }

    public GameResponse getGame(String gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NoSuchElementException("Game not found: " + gameId));
        return toResponse(game);
    }

    private GameResponse toResponse(Game game) {
        return new GameResponse(
                game.getId(),
                game.getBoard(),
                game.getCurrentPlayer(),
                game.getStatus().name(),
                game.getCreatedAt()
        );
    }
}

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock private GameRepository gameRepository;
    @Mock private GameMapper gameMapper;
    @InjectMocks private GameService gameService;

    @Test
    void createGame_persistsNewGameAndReturnsResponse() {
        UUID gameId = UUID.randomUUID();
        Game saved = game(gameId, TicTacToeBoard.EMPTY_BOARD, Player.X, GameStatus.ONGOING);
        CreateGameResponse expected = CreateGameResponse.builder().gameId(gameId).status(GameStatus.ONGOING).build();

        when(gameRepository.save(any(Game.class))).thenReturn(saved);
        when(gameMapper.toCreateGameResponse(saved)).thenReturn(expected);

        CreateGameResponse result = gameService.createGame();

        assertThat(result).isEqualTo(expected);
        verify(gameRepository).save(argThat(g ->
                g.getBoard().equals(TicTacToeBoard.EMPTY_BOARD)
                && g.getCurrentPlayer() == Player.X
                && g.getStatus() == GameStatus.ONGOING
        ));
    }

    @Test
    void makeMove_appliesMoveAndReturnsUpdatedGame() {
        UUID gameId = UUID.randomUUID();
        Game existing = game(gameId, TicTacToeBoard.EMPTY_BOARD, Player.X, GameStatus.ONGOING);
        Game afterMove = game(gameId, "X        ", Player.O, GameStatus.ONGOING);
        GameResponse expected = GameResponse.builder()
                .gameId(gameId).board("X        ").currentPlayer(Player.O)
                .status(GameStatus.ONGOING).createdAt(existing.getCreatedAt()).build();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(existing));
        when(gameRepository.save(any(Game.class))).thenReturn(afterMove);
        when(gameMapper.toGameResponse(afterMove)).thenReturn(expected);

        GameResponse result = gameService.makeMove(gameId.toString(), 0);

        assertThat(result).isEqualTo(expected);
        verify(gameRepository).save(argThat(g -> g.getBoard().equals("X        ")));
    }

    @Test
    void makeMove_detectsXWonAfterWinningMove() {
        UUID gameId = UUID.randomUUID();
        // X has top-left two cells, needs position 2 to win
        Game existing = game(gameId, "XX       ", Player.X, GameStatus.ONGOING);
        Game afterWin  = game(gameId, "XXX      ", Player.X, GameStatus.X_WON);
        GameResponse expected = GameResponse.builder()
                .gameId(gameId).board("XXX      ").currentPlayer(Player.X)
                .status(GameStatus.X_WON).createdAt(existing.getCreatedAt()).build();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(existing));
        when(gameRepository.save(any(Game.class))).thenReturn(afterWin);
        when(gameMapper.toGameResponse(afterWin)).thenReturn(expected);

        GameResponse result = gameService.makeMove(gameId.toString(), 2);

        assertThat(result.status()).isEqualTo(GameStatus.X_WON);
    }

    @Test
    void makeMove_throwsWhenCellAlreadyTaken() {
        UUID gameId = UUID.randomUUID();
        Game existing = game(gameId, "X        ", Player.O, GameStatus.ONGOING);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> gameService.makeMove(gameId.toString(), 0))
                .isInstanceOf(GameException.class)
                .hasMessageContaining("already taken");
    }

    @Test
    void makeMove_throwsWhenGameIsAlreadyOver() {
        UUID gameId = UUID.randomUUID();
        Game finished = game(gameId, "XXXOO    ", Player.X, GameStatus.X_WON);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(finished));

        assertThatThrownBy(() -> gameService.makeMove(gameId.toString(), 5))
                .isInstanceOf(GameException.class)
                .hasMessageContaining("already over");
    }

    @Test
    void makeMove_throwsWhenGameNotFound() {
        UUID gameId = UUID.randomUUID();
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.makeMove(gameId.toString(), 0))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void getGame_returnsGameResponse() {
        UUID gameId = UUID.randomUUID();
        Game existing = game(gameId, "X        ", Player.O, GameStatus.ONGOING);
        GameResponse expected = GameResponse.builder()
                .gameId(gameId).board("X        ").currentPlayer(Player.O)
                .status(GameStatus.ONGOING).createdAt(existing.getCreatedAt()).build();

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(existing));
        when(gameMapper.toGameResponse(existing)).thenReturn(expected);

        assertThat(gameService.getGame(gameId.toString())).isEqualTo(expected);
    }

    @Test
    void getGame_throwsWhenNotFound() {
        UUID gameId = UUID.randomUUID();
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.getGame(gameId.toString()))
                .isInstanceOf(NoSuchElementException.class);
    }

    private static Game game(UUID id, String board, Player player, GameStatus status) {
        return Game.builder()
                .id(id).board(board).currentPlayer(player)
                .status(status).createdAt(LocalDateTime.now())
                .build();
    }
}

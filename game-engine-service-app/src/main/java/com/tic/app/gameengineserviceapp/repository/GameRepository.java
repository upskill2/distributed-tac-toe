package com.tic.app.gameengineserviceapp.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tic.app.gameengineserviceapp.domain.Game;

public interface GameRepository extends JpaRepository<Game, UUID> {
}

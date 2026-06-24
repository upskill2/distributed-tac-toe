package com.tic.app.gamesessionserviceapp.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tic.app.gamesessionserviceapp.domain.Session;

public interface SessionRepository extends JpaRepository<Session, UUID> {
}

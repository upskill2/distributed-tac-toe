package com.tic.app.gamesessionserviceapp.repository;

import com.tic.app.gamesessionserviceapp.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, String> {
}

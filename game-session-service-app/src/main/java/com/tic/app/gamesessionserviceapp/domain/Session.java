package com.tic.app.gamesessionserviceapp.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String gameId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "session_moves", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "move")
    @OrderColumn(name = "move_index")
    @Builder.Default
    private List<String> moveHistory = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}

package com.tic.app.gamesessionserviceapp.domain;

public enum Player {
    X, O;

    public Player next() {
        return this == X ? O : X;
    }
}

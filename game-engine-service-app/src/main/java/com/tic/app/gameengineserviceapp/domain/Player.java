package com.tic.app.gameengineserviceapp.domain;

public enum Player {
    X, O;

    public Player next() {
        return this == X ? O : X;
    }

    public char symbol() {
        return this == X ? 'X' : 'O';
    }
}

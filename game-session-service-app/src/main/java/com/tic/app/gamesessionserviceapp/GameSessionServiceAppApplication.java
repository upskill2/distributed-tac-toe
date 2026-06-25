package com.tic.app.gamesessionserviceapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class GameSessionServiceAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameSessionServiceAppApplication.class, args);
    }

}

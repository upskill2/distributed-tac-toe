package com.tic.app.gameengineserviceapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MoveRequest(
        @NotNull(message = "Position must not be null")
        @Min(value = 0, message = "Position must be between 0 and 8")
        @Max(value = 8, message = "Position must be between 0 and 8")
        int position
) {
}

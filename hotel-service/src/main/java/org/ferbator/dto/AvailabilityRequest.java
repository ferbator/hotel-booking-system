package org.ferbator.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AvailabilityRequest(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull String requestId
) {
}

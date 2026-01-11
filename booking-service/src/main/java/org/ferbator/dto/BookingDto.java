package org.ferbator.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class BookingDtos {
    public record CreateBookingRequest(
            Long roomId,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            boolean autoSelect,
            @NotNull String requestId
    ) {

    }
}

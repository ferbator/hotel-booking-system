package com.meeweel.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRoomDto(
        @NotNull Long hotelId,
        @NotBlank String number,
        boolean available
) {
}

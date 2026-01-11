package org.ferbator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRoomDto(
        @NotNull Long hotelId,
        @NotBlank String number,
        boolean available
) {
}

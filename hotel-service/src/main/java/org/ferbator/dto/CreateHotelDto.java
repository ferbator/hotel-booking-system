package org.ferbator.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateHotelDto(
        @NotBlank String name,
        @NotBlank String address
) {
}

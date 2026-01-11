package com.meeweel.hotel.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateHotelDto(
        @NotBlank String name,
        @NotBlank String address
) {
}

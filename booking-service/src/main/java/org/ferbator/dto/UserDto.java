package org.ferbator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.ferbator.entity.enums.Role;

public class UserDto {
    public record CreateUser(
            @NotBlank String username,
            @NotBlank String password,
            @NotNull Role role
    ) {

    }

    public record UpdateUser(
            @NotNull Long id,
            String password,
            Role role
    ) {
    }
}

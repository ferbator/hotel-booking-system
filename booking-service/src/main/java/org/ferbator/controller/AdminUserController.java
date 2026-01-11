package org.ferbator.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ferbator.dto.AuthDto;
import org.ferbator.dto.UserDto;
import org.ferbator.entity.User;
import org.ferbator.entity.enums.Role;
import org.ferbator.service.JwtService;
import org.ferbator.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    private final JwtService jwt;
    private final PasswordEncoder encoder;

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping
    public void delete(@RequestParam Long id) {
        userService.delete(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public User create(@Valid @RequestBody UserDto.CreateUser dto) {
        return userService.register(dto.username(), dto.password(), dto.role());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping
    public User update(@Valid @RequestBody UserDto.UpdateUser dto) {
        return userService.update(dto.id(), dto.password(), dto.role());
    }

    @PostMapping("/auth/register")
    public AuthDto.TokenResponse register(@Valid @RequestBody AuthDto.RegisterRequest req) {
        var u = userService.register(req.username(), req.password(), Role.USER);
        var token = jwt.issue(u.getUsername(), u.getRole());
        return new AuthDto.TokenResponse(token, "Bearer", 3600);
    }

    @PostMapping("/auth/login")
    public AuthDto.TokenResponse login(@Valid @RequestBody AuthDto.LoginRequest req) {
        var u = userService.byUsername(req.username()).orElseThrow(() -> new RuntimeException("Bad credentials"));
        if (!encoder.matches(req.password(), u.getPassword())) throw new RuntimeException("Bad credentials");
        var token = jwt.issue(u.getUsername(), u.getRole());
        return new AuthDto.TokenResponse(token, "Bearer", 3600);
    }
}

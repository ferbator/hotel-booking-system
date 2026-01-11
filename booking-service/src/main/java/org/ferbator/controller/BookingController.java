package org.ferbator.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ferbator.dto.BookingDto;
import org.ferbator.entity.Booking;
import org.ferbator.repository.UserRepository;
import org.ferbator.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;
    private final UserRepository userRepository;

    private String bearer(Jwt jwt) {
        return "Bearer " + jwt.getTokenValue();
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/booking")
    public Booking create(
            @Valid @RequestBody BookingDto.CreateBookingRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var user = userRepository.findByUsername(jwt.getSubject())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"
                ));

        return bookingService.createBooking(
                user.getId(),
                req,
                bearer(jwt)
        );
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/bookings")
    public List<Booking> my(@AuthenticationPrincipal Jwt jwt) {
        var user = userRepository.findByUsername(jwt.getSubject())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"
                ));

        return bookingService.getUserBookings(user.getId());
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/booking/{id}")
    public Booking one(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) throws AccessDeniedException {
        var user = userRepository.findByUsername(jwt.getSubject())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"
                ));

        return bookingService.getBooking(id, user.getId());
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/booking/{id}")
    public Map<String, String> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) throws AccessDeniedException {
        var user = userRepository.findByUsername(jwt.getSubject())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"
                ));

        bookingService.cancelBooking(id, user.getId(), bearer(jwt));
        return Map.of("status", "cancelled");
    }
}

package com.meeweel.booking.controller;

import com.meeweel.booking.client.HotelClient;
import com.meeweel.booking.dto.BookingDtos.CreateBookingRequest;
import com.meeweel.booking.model.Booking;
import com.meeweel.booking.repo.BookingRepository;
import com.meeweel.booking.repo.UserRepository;
import com.meeweel.booking.service.BookingServiceCore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BookingController {
    private final BookingServiceCore core;
    private final UserRepository users;
    private final BookingRepository bookings;
    private final HotelClient hotelClient;

    private String bearer(Jwt jwt) {
        return "Bearer " + jwt.getTokenValue();
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/booking")
    public Booking create(
            @Valid @RequestBody CreateBookingRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var user = users.findByUsername(jwt.getSubject()).orElseThrow();
        Long roomId = req.roomId();
        if (req.autoSelect()) {
            var rec = hotelClient.recommend(req.startDate(), req.endDate(), bearer(jwt));
            if (rec.isEmpty()) throw new IllegalArgumentException("No rooms available");
            roomId = rec.getFirst().id();
        }

        if (!req.startDate().isBefore(req.endDate())) throw new IllegalArgumentException("startDate must be before endDate");
        if (req.startDate().isBefore(java.time.LocalDate.now())) throw new IllegalArgumentException("startDate in the past");

        return core.createBooking(
                user.getId(),
                roomId,
                req.startDate(),
                req.endDate(),
                req.requestId(),
                bearer(jwt)
        );
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/bookings")
    public List<Booking> my(@AuthenticationPrincipal Jwt jwt) {
        var user = users.findByUsername(jwt.getSubject()).orElseThrow();
        return bookings.findByUserId(user.getId());
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/booking/{id}")
    public Booking one(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var user = users.findByUsername(jwt.getSubject()).orElseThrow();
        var b = bookings.findById(id).orElseThrow();
        if (!b.getUserId().equals(user.getId()))
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        return b;
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/booking/{id}")
    public Map<String, String> cancel(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        var user = users.findByUsername(jwt.getSubject()).orElseThrow();
        var b = bookings.findById(id).orElseThrow();
        if (!b.getUserId().equals(user.getId()))
            throw new org.springframework.security.access.AccessDeniedException("Forbidden");
        try {
            hotelClient.release(b.getRoomId(), b.getRequestId(), bearer(jwt)).join();
        } catch (Exception ignored) {
        }
        b.setStatus(com.meeweel.booking.model.BookingStatus.CANCELLED);
        bookings.save(b);
        return Map.of("status", "cancelled");
    }
}

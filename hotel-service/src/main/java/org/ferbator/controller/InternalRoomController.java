package com.meeweel.hotel.controller;

import com.meeweel.hotel.dto.AvailabilityRequest;
import com.meeweel.hotel.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/rooms")
@RequiredArgsConstructor
public class InternalRoomController {
    private final RoomService roomService;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/{id}/confirm-availability")
    public void confirmAvailability(@PathVariable Long id, @Valid @RequestBody AvailabilityRequest req) {
        roomService.hold(id, req.startDate(), req.endDate(), req.requestId());
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/{id}/confirm")
    public void confirm(@PathVariable Long id, @RequestParam String requestId) {
        roomService.confirm(id, requestId);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/{id}/release")
    public void release(@PathVariable Long id, @RequestParam String requestId) {
        roomService.release(id, requestId);
    }
}

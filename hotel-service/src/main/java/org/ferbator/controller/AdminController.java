package org.ferbator.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ferbator.dto.CreateHotelDto;
import org.ferbator.dto.CreateRoomDto;
import org.ferbator.entity.Hotel;
import org.ferbator.service.RoomService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminController {
    private final RoomService roomService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/hotels")
    public Hotel createHotel(@Valid @RequestBody CreateHotelDto dto) {
        return roomService.createHotel(dto.name(), dto.address());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/rooms")
    public Object createRoom(@Valid @RequestBody CreateRoomDto dto) {
        return roomService.createRoom(dto.hotelId(), dto.number(), dto.available());
    }
}

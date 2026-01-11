package org.ferbator.controller;

import lombok.RequiredArgsConstructor;
import org.ferbator.dto.RoomDto;
import org.ferbator.service.RoomService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomQueryController {
    private final RoomService roomService;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public List<RoomDto> free(@RequestParam java.time.LocalDate start, @RequestParam java.time.LocalDate end) {
        return roomService.free(start, end);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/recommend")
    public List<RoomDto> recommend(@RequestParam LocalDate start, @RequestParam LocalDate end) {
        return roomService.recommend(start, end);
    }
}

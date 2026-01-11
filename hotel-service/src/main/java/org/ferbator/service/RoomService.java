package com.meeweel.hotel.service;

import com.meeweel.hotel.dto.RoomDto;
import com.meeweel.hotel.entity.ReservationStatus;
import com.meeweel.hotel.entity.Room;
import com.meeweel.hotel.entity.RoomReservation;
import com.meeweel.hotel.repo.RoomRepository;
import com.meeweel.hotel.repo.RoomReservationRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository rooms;
    private final RoomReservationRepo reservations;

    public List<RoomDto> findAll() {
        return rooms.findAll().stream()
                .map(
                        r -> new RoomDto(
                                r.getId(),
                                r.getHotelId(),
                                r.getNumber(),
                                r.isAvailable(),
                                r.getTimesBooked()
                        )
                ).toList();
    }

    public List<RoomDto> free(LocalDate start, LocalDate end) {
        return rooms.findAll().stream()
                .filter(Room::isAvailable)
                .filter(r -> !reservations.existsOverlap(
                                r.getId(),
                                start,
                                end,
                                java.util.List.of(ReservationStatus.HELD, ReservationStatus.CONFIRMED)
                        )
                )
                .map(r -> new RoomDto(r.getId(), r.getHotelId(), r.getNumber(), r.isAvailable(), r.getTimesBooked()))
                .toList();
    }

    public List<RoomDto> recommend(
            LocalDate start,
            LocalDate end
    ) {
        return rooms.findAll().stream()
                .filter(Room::isAvailable)
                .filter(
                        r -> !reservations.existsOverlap(
                                r.getId(),
                                start,
                                end,
                                List.of(ReservationStatus.HELD, ReservationStatus.CONFIRMED)
                        )
                )
                .sorted(
                        Comparator
                                .comparingLong(Room::getTimesBooked)
                                .thenComparingLong(Room::getId)
                )
                .map(
                        r -> new RoomDto(
                                r.getId(),
                                r.getHotelId(),
                                r.getNumber(),
                                r.isAvailable(),
                                r.getTimesBooked()
                        )
                )
                .toList();
    }

    @Transactional
    public void hold(Long roomId, LocalDate start, LocalDate end, String requestId) {
        rooms.findByIdForUpdate(roomId).orElseThrow(() -> new EntityNotFoundException("Room not found"));

        var existing = reservations.findByRoomIdAndRequestId(roomId, requestId);
        if (existing.isPresent()) {
            var e = existing.get();
            if (e.getStatus() == ReservationStatus.HELD || e.getStatus() == ReservationStatus.CONFIRMED) return;
        }

        boolean conflict = reservations.existsOverlap(
                roomId, start, end, List.of(ReservationStatus.HELD, ReservationStatus.CONFIRMED)
        );
        if (conflict) {
            throw new IllegalStateException("Room busy for given dates");
        }

        reservations.save(RoomReservation.builder()
                .roomId(roomId).startDate(start).endDate(end)
                .requestId(requestId).status(ReservationStatus.HELD).build());
    }

    @Transactional
    public void confirm(
            Long roomId,
            String requestId
    ) {
        var res = reservations.findByRoomIdAndRequestId(roomId, requestId)
                .orElseThrow(() -> new EntityNotFoundException("Hold not found"));
        if (res.getStatus() == ReservationStatus.CONFIRMED) return;
        res.setStatus(ReservationStatus.CONFIRMED);
        var room = rooms.findById(roomId).orElseThrow();
        room.setTimesBooked(room.getTimesBooked() + 1);
    }

    @Transactional
    public void release(
            Long roomId,
            String requestId
    ) {
        var res = reservations.findByRoomIdAndRequestId(roomId, requestId);
        res.ifPresent(r -> {
            if (r.getStatus() != ReservationStatus.RELEASED) r.setStatus(ReservationStatus.RELEASED);
        });
    }

    @Transactional
    public Room createRoom(
            Long hotelId,
            String number,
            boolean available
    ) {
        return rooms.save(
                Room.builder()
                        .hotelId(hotelId)
                        .number(number)
                        .available(available)
                        .timesBooked(0)
                        .build()
        );
    }
}

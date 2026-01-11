package org.ferbator.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.ferbator.dto.RoomDto;
import org.ferbator.entity.Hotel;
import org.ferbator.entity.Reservation;
import org.ferbator.entity.Room;
import org.ferbator.entity.enums.ReservationStatus;
import org.ferbator.repository.HotelRepository;
import org.ferbator.repository.ReservationRepository;
import org.ferbator.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ReservationRepository reservationRepository;

    public List<RoomDto> findAll() {
        return roomRepository.findAll().stream()
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
        return roomRepository.findAll().stream()
                .filter(Room::isAvailable)
                .filter(r -> !reservationRepository.existsOverlap(
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
        return roomRepository.findAll().stream()
                .filter(Room::isAvailable)
                .filter(
                        r -> !reservationRepository.existsOverlap(
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
        roomRepository.findByIdForUpdate(roomId).orElseThrow(() -> new EntityNotFoundException("Room not found"));

        var existing = reservationRepository.findByRoomIdAndRequestId(roomId, requestId);
        if (existing.isPresent()) {
            var e = existing.get();
            if (e.getStatus() == ReservationStatus.HELD || e.getStatus() == ReservationStatus.CONFIRMED) return;
        }

        boolean conflict = reservationRepository.existsOverlap(
                roomId, start, end, List.of(ReservationStatus.HELD, ReservationStatus.CONFIRMED)
        );
        if (conflict) {
            throw new IllegalStateException("Room busy for given dates");
        }

        reservationRepository.save(Reservation.builder()
                .roomId(roomId).startDate(start).endDate(end)
                .requestId(requestId).status(ReservationStatus.HELD).build());
    }

    @Transactional
    public void confirm(
            Long roomId,
            String requestId
    ) {
        var res = reservationRepository.findByRoomIdAndRequestId(roomId, requestId)
                .orElseThrow(() -> new EntityNotFoundException("Hold not found"));
        if (res.getStatus() == ReservationStatus.CONFIRMED) return;
        res.setStatus(ReservationStatus.CONFIRMED);
        var room = roomRepository.findById(roomId).orElseThrow();
        room.setTimesBooked(room.getTimesBooked() + 1);
    }

    @Transactional
    public void release(
            Long roomId,
            String requestId
    ) {
        var res = reservationRepository.findByRoomIdAndRequestId(roomId, requestId);
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
        return roomRepository.save(
                Room.builder()
                        .hotelId(hotelId)
                        .number(number)
                        .available(available)
                        .timesBooked(0)
                        .build()
        );
    }

    @Transactional
    public Hotel createHotel(
            String name,
            String address
    ) {
        return hotelRepository.save(
                Hotel.builder()
                        .name(name)
                        .address(address)
                        .build()
        );
    }
}

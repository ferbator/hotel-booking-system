package org.ferbator.service;

import lombok.RequiredArgsConstructor;
import org.ferbator.client.HotelClient;
import org.ferbator.dto.BookingDto;
import org.ferbator.entity.Booking;
import org.ferbator.entity.enums.BookingStatus;
import org.ferbator.repository.BookingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final HotelClient hotelClient;

    @Transactional
    public Booking createBooking(
            Long userId,
            BookingDto.CreateBookingRequest req,
            String bearerToken
    ) {
        validateDates(req);

        Long roomId = resolveRoomId(req, bearerToken);

        Booking booking = Booking.builder()
                .userId(userId)
                .roomId(roomId)
                .startDate(req.startDate())
                .endDate(req.endDate())
                .status(BookingStatus.PENDING)
                .requestId(req.requestId())
                .build();

        bookingRepository.save(booking);

        try {
            hotelClient.hold(
                    roomId,
                    req.startDate(),
                    req.endDate(),
                    req.requestId(),
                    bearerToken
            ).join();

            booking.setStatus(BookingStatus.CONFIRMED);
            return bookingRepository.save(booking);

        } catch (Exception ex) {

            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);

            try {
                hotelClient.release(roomId, req.requestId(), bearerToken).join();
            } catch (Exception ignored) {
            }

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Room is not available"
            );
        }
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public Booking getBooking(Long bookingId, Long userId) throws AccessDeniedException {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Booking not found"
                ));

        if (!booking.getUserId().equals(userId)) {
            throw new AccessDeniedException("Forbidden");
        }

        return booking;
    }

    @Transactional
    public void cancelBooking(
            Long bookingId,
            Long userId,
            String bearerToken
    ) throws AccessDeniedException {
        Booking booking = getBooking(bookingId, userId);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return;
        }

        try {
            hotelClient.release(
                    booking.getRoomId(),
                    booking.getRequestId(),
                    bearerToken
            ).join();
        } catch (Exception ignored) {
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    private void validateDates(BookingDto.CreateBookingRequest req) {
        if (!req.startDate().isBefore(req.endDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "startDate must be before endDate"
            );
        }

        if (req.startDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "startDate in the past"
            );
        }
    }

    private Long resolveRoomId(
            BookingDto.CreateBookingRequest req,
            String bearerToken
    ) {
        if (!req.autoSelect()) {
            if (req.roomId() == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "roomId is required"
                );
            }
            return req.roomId();
        }

        var rooms = hotelClient.recommend(
                req.startDate(),
                req.endDate(),
                bearerToken
        );

        return rooms.stream()
                .findFirst()
                .map(HotelClient.RoomDto::id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "No available rooms"
                ));
    }
}

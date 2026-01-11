package com.meeweel.hotel.repo;

import com.meeweel.hotel.entity.ReservationStatus;
import com.meeweel.hotel.entity.RoomReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomReservationRepo extends JpaRepository<RoomReservation, Long> {

    Optional<RoomReservation> findByRoomIdAndRequestId(Long roomId, String requestId);

    @Query("select (count(r)>0) from RoomReservation r " +
            "where r.roomId = :roomId and r.status in :statuses " +
            "and not (r.endDate <= :start or r.startDate >= :end)")
    boolean existsOverlap(
            @Param("roomId") Long roomId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("statuses") List<ReservationStatus> statuses
    );
}

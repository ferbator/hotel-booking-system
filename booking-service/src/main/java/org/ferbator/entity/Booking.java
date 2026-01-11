package org.ferbator.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.ferbator.entities.enums.BookingStatus;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Data
public class Booking {
    @Id
    @GeneratedValue
    private Long id;


    private Long userId;
    private Long roomId;


    private LocalDate startDate;
    private LocalDate endDate;


    @Enumerated(EnumType.STRING)
    private BookingStatus status;


    @Column(unique = true)
    private String requestId;


    private Instant createdAt;
}

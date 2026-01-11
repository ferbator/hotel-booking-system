package org.ferbator.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ferbator.entity.enums.BookingStatus;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

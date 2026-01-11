package org.ferbator.entity;

import jakarta.persistence.*;
import lombok.*;
import org.ferbator.entity.enums.ReservationStatus;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = {@Index(name = "idx_room_request", columnList = "roomId,requestId", unique = true)})
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String requestId;
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
}

package com.hostfully.app.infra.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Arrays;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
public class BookingEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id")
    private String externalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private PropertyEntity property;

    @Column(name = "guest_name")
    private String guest;

    @Column(name = "number_guest")
    private Integer numberGuest;

    @Version
    @Column
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    private LocalDate startDate;
    private LocalDate endDate;

    public BookingEntity(
            String externalId,
            PropertyEntity property,
            String guest,
            Integer numberGuest,
            BookingStatus status,
            LocalDate startDate,
            LocalDate endDate) {
        this.externalId = externalId;
        this.property = property;
        this.guest = guest;
        this.numberGuest = numberGuest;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public enum BookingStatus {
        CONFIRMED,
        CANCELLED;

        public static BookingStatus fromString(String value) {
            if (value == null) return null;

            return Arrays.stream(BookingStatus.values())
                    .filter(e -> e.name().equalsIgnoreCase(value))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid SessionType: " + value));
        }
    }
}

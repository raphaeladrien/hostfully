package com.hostfully.app.infra.entity;

import jakarta.persistence.*;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PropertyEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id")
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column
    private PropertyEntityStatus status;

    @Version
    private Long version;

    private String description;
    private String alias;

    public PropertyEntity(final String externalId, final String description, final String alias) {
        this.externalId = externalId;
        this.description = description;
        this.alias = alias;
        this.status = PropertyEntityStatus.AVAILABLE;
    }

    public enum PropertyEntityStatus {
        AVAILABLE,
        BLOCKED,
        BOOKED;

        public static PropertyEntityStatus fromString(String value) {
            if (value == null) return null;

            return Arrays.stream(PropertyEntityStatus.values())
                    .filter(e -> e.name().equalsIgnoreCase(value))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid SessionType: " + value));
        }

        public boolean isBooked() {
            return this == BOOKED;
        }
    }
}

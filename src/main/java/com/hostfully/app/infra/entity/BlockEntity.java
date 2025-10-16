package com.hostfully.app.infra.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
public class BlockEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id")
    private String externalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id")
    private PropertyEntity property;

    private String reason;
    private LocalDate startDate;
    private LocalDate endDate;

    public BlockEntity(
            final String externalId,
            final PropertyEntity property,
            final String reason,
            final LocalDate startDate,
            final LocalDate endDate) {
        this.externalId = externalId;
        this.property = property;
        this.reason = reason;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}

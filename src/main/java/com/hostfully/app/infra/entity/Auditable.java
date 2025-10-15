package com.hostfully.app.infra.entity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class Auditable {

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    protected Instant createdAt;

    @CreatedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    protected Instant updatedAt;
}

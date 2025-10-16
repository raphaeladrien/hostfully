package com.hostfully.app.infra.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "idempotencies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Idempotency extends Auditable {

    @Id
    private UUID id;

    private String response;
}

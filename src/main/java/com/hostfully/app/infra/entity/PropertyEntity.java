package com.hostfully.app.infra.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
public class PropertyEntity extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id")
    private String externalId;

    private String description;
    private String alias;

    public PropertyEntity(final String externalId, final String description, final String alias) {
        this.externalId = externalId;
        this.description = description;
        this.alias = alias;
    }
}

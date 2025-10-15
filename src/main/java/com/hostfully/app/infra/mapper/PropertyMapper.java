package com.hostfully.app.infra.mapper;

import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.property.domain.Property;

public abstract class PropertyMapper {

    public static PropertyEntity toEntity(final Property property) {
        return new PropertyEntity(property.getId(), property.getDescription(), property.getAlias());
    }

    public static Property toDomain(final PropertyEntity propertyEntity) {
        return new Property(propertyEntity.getExternalId(), propertyEntity.getDescription(), propertyEntity.getAlias());
    }
}

package com.hostfully.app.infra.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.property.domain.Property;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PropertyMapperTest {

    @Test
    @DisplayName("should map property domain to entity")
    void shouldMapPropertyToEntity() {
        final String id = "12345-123456";
        final String description = "Nice apartment";
        final String alias = "apt-001";
        final Property property = new Property(id, description, alias);

        final PropertyEntity entity = PropertyMapper.toEntity(property);

        assertThat(entity).isNotNull();
        assertThat(entity.getExternalId()).isEqualTo(id);
        assertThat(entity.getDescription()).isEqualTo(description);
        assertThat(entity.getAlias()).isEqualTo(alias);
    }

    @Test
    @DisplayName("should map property entity to domain")
    void shouldMapEntityToDomain() {
        final String id = "12345-123456";
        final String description = "Nice apartment";
        final String alias = "apt-001";
        final PropertyEntity entity = new PropertyEntity(id, description, alias);

        final Property domain = PropertyMapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getDescription()).isEqualTo(description);
        assertThat(domain.getAlias()).isEqualTo(alias);
    }
}

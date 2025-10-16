package com.hostfully.app.infra.respository;

import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.infra.repository.PropertyRepository;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class PropertyRepositoryTest {

    @Autowired
    private PropertyRepository propertyRepository;

    private PropertyEntity property;
    private final String externalId = "qwerty-12345";

    @BeforeEach
    void setup() {
        property = propertyRepository.save(build());
    }

    @Test
    @DisplayName("returns matching property when external id parameter is supplied ")
    void returnsMatchingPropertyExternalId() {
        final Optional<PropertyEntity> result = propertyRepository.findByExternalId(externalId);

        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(result.isPresent()).isTrue();
            final PropertyEntity propertyEntity = result.get();
            softAssertions.assertThat(propertyEntity.getId()).isNotNull();
            softAssertions.assertThat(propertyEntity.getExternalId()).isEqualTo(externalId);
            softAssertions.assertThat(propertyEntity.getDescription()).isEqualTo(property.getDescription());
            softAssertions.assertThat(propertyEntity.getAlias()).isEqualTo(property.getAlias());
        });
    }

    private PropertyEntity build() {
        return new PropertyEntity(externalId, "a-super-description", "a-alias");
    }
}

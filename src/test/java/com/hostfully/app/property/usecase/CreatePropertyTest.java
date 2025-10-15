package com.hostfully.app.property.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.infra.repository.PropertyRepository;
import com.hostfully.app.property.domain.Property;
import com.hostfully.app.property.exception.PropertyCreationException;
import com.hostfully.app.property.usecase.CreateProperty.CreatePropertyCommand;
import com.hostfully.app.shared.util.NanoIdGenerator;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CreatePropertyTest {

    private final String idGenerated = "12345-123456";
    private final PropertyRepository propertyRepository = mock(PropertyRepository.class);
    private final NanoIdGenerator nanoIdGenerator = mock(NanoIdGenerator.class);

    private final CreateProperty subject = new CreateProperty(propertyRepository, nanoIdGenerator);

    @BeforeEach
    public void setup() {
        when(nanoIdGenerator.generateId()).thenReturn(idGenerated);
    }

    @Test
    @DisplayName("should create a property, when a command is provided")
    public void createPropertyTest() {
        final String description = "a-super-description";
        final String alias = "a-super-alias";

        final CreatePropertyCommand command = new CreatePropertyCommand(description, alias);

        when(propertyRepository.save(any())).thenReturn(new PropertyEntity(idGenerated, description, alias));

        final Property result = subject.execute(command);

        SoftAssertions.assertSoftly(assertion -> {
            assertion.assertThat(result).isNotNull();
            assertion.assertThat(result.getDescription()).isEqualTo(description);
            assertion.assertThat(result.getAlias()).isEqualTo(alias);
            assertion.assertThat(result.getId()).isEqualTo(idGenerated);
        });
    }

    @Test
    @DisplayName("throws PropertyCreationException, when an unexpected exception occurred")
    public void throwPropertyCreationException() {
        final String description = "a-super-description";
        final String alias = "a-super-alias";

        final CreatePropertyCommand command = new CreatePropertyCommand(description, alias);

        when(propertyRepository.save(any())).thenThrow(new RuntimeException("an exception"));

        Assertions.assertThrows(PropertyCreationException.class, () -> subject.execute(command));
    }
}

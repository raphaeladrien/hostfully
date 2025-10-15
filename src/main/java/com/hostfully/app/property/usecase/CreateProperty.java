package com.hostfully.app.property.usecase;

import com.hostfully.app.infra.mapper.PropertyMapper;
import com.hostfully.app.infra.repository.PropertyRepository;
import com.hostfully.app.property.domain.Property;
import com.hostfully.app.property.exception.PropertyCreationException;
import com.hostfully.app.shared.util.NanoIdGenerator;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CreateProperty {

    private static final Logger log = LoggerFactory.getLogger(CreateProperty.class);

    private final PropertyRepository propertyRepository;
    private final NanoIdGenerator nanoIdGenerator;

    public Property execute(final CreatePropertyCommand propertyCommand) {
        final Property property = buildDomain(propertyCommand);
        try {
            return PropertyMapper.toDomain(propertyRepository.save(PropertyMapper.toEntity(property)));
        } catch (Exception ex) {
            log.error("Failed to create property: {}", property, ex);
            throw new PropertyCreationException("Unexpected error while creating property", ex);
        }
    }

    private Property buildDomain(final CreatePropertyCommand propertyCommand) {
        return new Property(nanoIdGenerator.generateId(), propertyCommand.description, propertyCommand.alias);
    }

    public record CreatePropertyCommand(String description, String alias) {}
}

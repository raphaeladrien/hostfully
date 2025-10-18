package com.hostfully.app.runner;

import com.hostfully.app.infra.entity.PropertyEntity;
import com.hostfully.app.infra.repository.PropertyRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PropertyRunner implements CommandLineRunner {

    private final PropertyRepository propertyRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        final List<PropertyEntity> properties = List.of(
                new PropertyEntity("SunnyVilla01", "a cozy house", "Jewel of Portland"),
                new PropertyEntity("CozyNest123", "a cozy house", "Jewel of Montana"));

        propertyRepository.saveAll(properties);
    }
}

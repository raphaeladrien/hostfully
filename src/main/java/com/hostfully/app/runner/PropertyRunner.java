package com.hostfully.app.runner;

import com.hostfully.app.property.usecase.CreateProperty;
import com.hostfully.app.property.usecase.CreateProperty.CreatePropertyCommand;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PropertyRunner implements CommandLineRunner {

    private final CreateProperty createProperty;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        final List<CreatePropertyCommand> commands = List.of(
                new CreatePropertyCommand("a cozy house", "amazing"),
                new CreatePropertyCommand("amazing house", "oasis in portland/or"));

        commands.forEach(createProperty::execute);
    }
}

package com.archive.backend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * PostgreSQL for integration tests: a fresh container with the same major version as docker-compose.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>("postgres:13");
    }

    /** Pictures of screenshots go to a folder of the test, never to the storage of the developer. */
    @Bean
    DynamicPropertyRegistrar screenshotStorage() throws IOException {
        Path folder = Files.createTempDirectory("screenshots");
        return registry -> registry.add("screenshots.storage-path", folder::toString);
    }
}

package com.archive.backend;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * What integration tests share: one throwaway PostgreSQL per test JVM, with the same major version as docker-compose,
 * and folders of their own for uploaded files (docs/testing.md). Every test context of the JVM connects to it; the
 * profile {@code fixtures} to a database of its own there, so the fixture data set never reaches the tests of an
 * empty archive. A test leaves the database as it found it: rolled back, or reset after its class
 * ({@link ResetDatabaseAfterCommittingTests}). Testcontainers removes the container when the JVM ends.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:13");

    static {
        POSTGRES.start();
        try {
            POSTGRES.execInContainer("createdb", "-U", POSTGRES.getUsername(), "fixtures");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    /** The database of the context: {@code test.database} of the test settings. */
    @Bean
    DynamicPropertyRegistrar database(Environment environment) {
        String database = environment.getRequiredProperty("test.database");
        return registry -> {
            registry.add(
                    "spring.datasource.url",
                    () -> "jdbc:postgresql://" + POSTGRES.getHost() + ":" + POSTGRES.getFirstMappedPort() + "/"
                            + database);
            registry.add("spring.datasource.username", POSTGRES::getUsername);
            registry.add("spring.datasource.password", POSTGRES::getPassword);
        };
    }

    /** Uploaded files go to folders of the test, never to the storage of the developer. */
    @Bean
    DynamicPropertyRegistrar screenshotStorage() throws IOException {
        Path folder = Files.createTempDirectory("screenshots");
        Path releases = Files.createTempDirectory("release-archives");
        return registry -> {
            registry.add("screenshots.storage-path", folder::toString);
            registry.add("release-archives.storage-path", releases::toString);
        };
    }
}
